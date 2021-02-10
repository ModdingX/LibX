package io.github.noeppi_noeppi.libx.impl.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigImpl {

    public static final Gson GSON = net.minecraft.util.Util.make(() -> {
        GsonBuilder gsonbuilder = new GsonBuilder();
        gsonbuilder.disableHtmlEscaping();
        gsonbuilder.setLenient();
        gsonbuilder.setPrettyPrinting();
        return gsonbuilder.create();
    });

    public static final Gson INTERNAL = net.minecraft.util.Util.make(() -> {
        GsonBuilder gsonbuilder = new GsonBuilder();
        gsonbuilder.disableHtmlEscaping();
        return gsonbuilder.create();
    });

    private static final Map<ResourceLocation, ConfigImpl> configs = new HashMap<>();

    public static ConfigImpl getConfig(ResourceLocation id) {
        if (configs.containsKey(id)) {
            return configs.get(id);
        } else {
            throw new IllegalStateException("Config not registered: " + id);
        }
    }
    
    @Nullable
    public static ConfigImpl getConfigNullable(ResourceLocation id) {
        return configs.getOrDefault(id, null);
    }

    public final ResourceLocation id;
    public final Class<?> baseClass;
    public final Path path;
    public final Map<Field, ConfigKey> keys;
    public final boolean clientConfig;
    
    private boolean shadowed;
    private ConfigState savedState;
    private ConfigState defaultState;

    public ConfigImpl(ResourceLocation id, Class<?> baseClass, Path path, boolean clientConfig) {
        this.clientConfig = clientConfig;
        if (configs.containsKey(id)) {
            throw new IllegalStateException("Config registered twice: " + id + " (" + baseClass + ")");
        }
        configs.put(id, this);
        this.id = id;
        this.path = path;
        this.baseClass = baseClass;
        try {
            ImmutableMap.Builder<Field, ConfigKey> keys = ImmutableMap.builder();
            addAllFieldsToBuilder(baseClass, baseClass, keys);
            this.keys = keys.build();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to build config for class " + baseClass, e);
        }
        this.shadowed = false;
        this.savedState = null;
        this.defaultState = null;
    }

    public ConfigState stateFromValues() {
        try {
            ImmutableMap.Builder<ConfigKey, Object> values = ImmutableMap.builder();
            for (ConfigKey key : this.keys.values()) {
                Object value = key.field.get(null);
                if (value == null) {
                    throw new IllegalStateException("Null value in applied config. This is usually an error in the mod.");
                }
                values.put(key, value);
            }
            return new ConfigState(this, values.build());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read config state from current values.");
        }
    }

    public ConfigState readState(PacketBuffer buffer) {
        try {
            Set<ConfigKey> keysLeft = new HashSet<>(this.keys.values());
            ImmutableMap.Builder<ConfigKey, Object> values = ImmutableMap.builder();
            int size = buffer.readVarInt();
            for (int i = 0; i < size; i++) {
                Class<?> declaringClass = Class.forName(buffer.readString(0x7fff));
                Field field;
                try {
                    field = declaringClass.getDeclaredField(buffer.readString(0x7fff));
                } catch (NoSuchFieldException e) {
                    field = null;
                }
                ResourceLocation mapperId = buffer.readResourceLocation();
                String elementTypeStr = buffer.readString(0x7fff);
                Class<?> elementType = elementTypeStr.isEmpty() ? void.class : Class.forName(elementTypeStr);
                ConfigKey key = field == null ? null : this.keys.get(field);
                if (key == null) {
                    throw new IllegalStateException("Config between client and server mismatch. Server sent unknown or non-config field. Ignoring");
                } else if (!key.mapperId.equals(mapperId)) {
                    throw new IllegalStateException("Config incompatible. Don't know how to read object: Mapper unknown: Local mapper: " + key.mapperId + ", Remote Mapper: " + mapperId);
                } else if (!key.elementType.equals(elementType)) {
                    throw new IllegalStateException("Config incompatible. Don't know how to read object: Different element types.");
                }
                Object value = key.mapper.read(buffer, key.elementType);
                values.put(key, value);
                keysLeft.remove(key);
            }
            if (!keysLeft.isEmpty()) {
                LibX.logger.warn("Config " + this.id + ": There are additional fields on the client, not sent by the server. Using client values.");
            }
            return new ConfigState(this, values.build());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read config state.", e);
        }
    }

    public ConfigState readFromFileOrCreateBy(ConfigState state) throws IOException {
        if (!Files.isRegularFile(this.path)) {
            LibX.logger.info("Config '" + this.id + "' does not exist. Creating default.");
            state.writeToFile();
            return state;
        } else {
            return this.readFromFile();
        }
    }

    public ConfigState readFromFile() throws IOException {
        if (this.defaultState == null) {
            throw new IllegalStateException("Can't read config from file: Default state not set.");
        }
        if (!Files.isRegularFile(this.path) || !Files.isReadable(this.path)) {
            throw new IllegalStateException("Config '" + this.id + "' does not exist or is not readble.");
        }
        Reader reader = Files.newBufferedReader(this.path);
        JsonObject config = GSON.fromJson(reader, JsonObject.class);
        ImmutableMap.Builder<ConfigKey, Object> values = ImmutableMap.builder();
        AtomicBoolean needsCorrection = new AtomicBoolean(false);
        for (ConfigKey key : this.keys.values()) {
            JsonElement elem = getInObjectKeyPath(config, key, needsCorrection);
            if (elem != null && key.mapper.element().isAssignableFrom(elem.getClass())) {
                if (elem.isJsonNull()) {
                    LibX.logger.error("Null values are not allowed in the config. Using default.");
                    values.put(key, this.defaultState.getValue(key));
                    needsCorrection.set(true);
                } else {
                    //noinspection unchecked
                    Object value = ((ValueMapper<?, JsonElement>) key.mapper).fromJSON(elem, key.elementType);
                    values.put(key, value);
                }
            } else {
                values.put(key, this.defaultState.getValue(key));
                needsCorrection.set(true);
            }
        }
        reader.close();
        ConfigState state = new ConfigState(this, values.build());
        if (needsCorrection.get()) {
            LibX.logger.info("Correcting config '" + this.id + "'");
            state.writeToFile();
        }
        return state;
    }

    private static void addAllFieldsToBuilder(Class<?> baseClass, Class<?> currentClass, ImmutableMap.Builder<Field, ConfigKey> keys) throws ReflectiveOperationException {
        Set<String> names = new HashSet<>();
        for (Field field : currentClass.getDeclaredFields()) {
            ConfigKey key = ConfigKey.create(field, baseClass);
            if (key != null) {
                field.setAccessible(true);
                keys.put(field, key);
                if (names.contains(field.getName())) {
                    throw new IllegalStateException("Duplicate key in config definition: " + field.getName());
                } else {
                    names.add(field.getName());
                }
            }
        }
        for (Class<?> clazz : currentClass.getDeclaredClasses()) {
            if (names.contains(clazz.getSimpleName())) {
                throw new IllegalStateException("Duplicate key in config definition: " + clazz.getSimpleName());
            } else {
                names.add(clazz.getSimpleName());
            }
            addAllFieldsToBuilder(baseClass, clazz, keys);
        }
    }

    private static JsonElement getInObjectKeyPath(JsonObject root, ConfigKey key, @Nullable AtomicBoolean needsCorrection) {
        if (key.path.isEmpty()) {
            throw new IllegalStateException("Internal error in LibX config: Empty path for a config key: " + key.field.getName() + " @ " + key.field.getDeclaringClass());
        }
        JsonObject current = root;
        for (int i = 0; i < key.path.size() - 1; i++) {
            JsonElement elem = current.get(key.path.get(i));
            if (elem == null || !elem.isJsonObject()) {
                if (needsCorrection != null) {
                    needsCorrection.set(true);
                }
                return null;
            } else {
                 current = elem.getAsJsonObject();
            }
        }
        return current.get(key.path.get(key.path.size() - 1));
    }
    
    public void shadowBy(ConfigState state) {
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            LibX.logger.error("Config shadow was called on a dedicated server. This should not happen!");
        }
        if (!this.shadowed && this.savedState == null) {
            LibX.logger.warn("Capturing config state for '" + this.id + "' before shadowing. This should not happen. Was the config not loaded properly?");
            this.savedState = this.stateFromValues();
        }
        this.shadowed = true;
        state.apply();
    }
    
    public void restore() {
        if (this.shadowed && this.savedState != null) {
            this.savedState.apply();
        } else if (this.shadowed) {
            LibX.logger.warn("Could not restore config: No saved state");
        }
        this.shadowed = false;
    }
    
    public void saveState(ConfigState state) {
        this.savedState = state;
    }

    public void setDefaultState(ConfigState defaultState) {
        if (this.defaultState != null) {
            throw new IllegalStateException("Default state set twice.");
        }
        this.defaultState = defaultState;
    }

    public ConfigState cachedOrCurrent() {
        if (FMLEnvironment.dist != Dist.DEDICATED_SERVER) {
            LibX.logger.error("Config cached or current method was called on a physical client. This should not happen!");
        }
        if (this.savedState == null) {
            LibX.logger.warn("Capturing config state for '" + this.id + "' on server. This should not happen. Was the config not loaded properly?");
            this.savedState = this.stateFromValues();
        }
        return this.savedState;
    }
    
    public boolean isShadowed() {
        return this.shadowed;
    }
}
