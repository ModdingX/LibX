package io.github.noeppi_noeppi.libx.impl.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.event.ConfigLoadedEvent;
import io.github.noeppi_noeppi.libx.impl.config.correct.CorrectionInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigImpl {

    public static final Gson GSON = net.minecraft.Util.make(() -> {
        GsonBuilder gsonbuilder = new GsonBuilder();
        gsonbuilder.disableHtmlEscaping();
        gsonbuilder.setLenient();
        gsonbuilder.setPrettyPrinting();
        return gsonbuilder.create();
    });

    public static final Gson INTERNAL = net.minecraft.Util.make(() -> {
        GsonBuilder gsonbuilder = new GsonBuilder();
        gsonbuilder.disableHtmlEscaping();
        return gsonbuilder.create();
    });

    private static final Map<ResourceLocation, ConfigImpl> configs = Collections.synchronizedMap(new HashMap<>());

    @Nonnull
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

    public static Set<ConfigImpl> getAllConfigs() {
        return Set.copyOf(configs.values());
    }
    
    public final ResourceLocation id;
    public final Class<?> baseClass;
    public final Path path;
    public final Map<Field, ConfigKey> keys;
    public final Set<ConfigGroup> groups;
    public final boolean clientConfig;
    
    private boolean shadowed;
    private boolean shadowedLocal;
    private ConfigState savedState;
    private ConfigState defaultState;

    public ConfigImpl(ResourceLocation id, Class<?> baseClass, Path path, boolean clientConfig) {
        if (configs.containsKey(id)) {
            throw new IllegalStateException("Config registered twice: " + id + " (" + baseClass + ")");
        }
        configs.put(id, this);
        this.id = id;
        this.path = path;
        this.baseClass = baseClass;
        this.clientConfig = clientConfig;
        try {
            ImmutableMap.Builder<Field, ConfigKey> keys = ImmutableMap.builder();
            ImmutableSet.Builder<ConfigGroup> groups = ImmutableSet.builder();
            addAllFieldsToBuilder(id.getNamespace(), baseClass, baseClass, keys, groups);
            this.keys = keys.build();
            this.groups = groups.build();
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

    public ConfigState readState(FriendlyByteBuf buffer) {
        try {
            Set<ConfigKey> keysLeft = new HashSet<>(this.keys.values());
            ImmutableMap.Builder<ConfigKey, Object> values = ImmutableMap.builder();
            int size = buffer.readVarInt();
            for (int i = 0; i < size; i++) {
                Class<?> declaringClass = Class.forName(buffer.readUtf(0x7fff));
                Field field;
                try {
                    field = declaringClass.getDeclaredField(buffer.readUtf(0x7fff));
                } catch (NoSuchFieldException e) {
                    field = null;
                }
                ConfigKey key = field == null ? null : this.keys.get(field);
                if (key == null) {
                    throw new IllegalStateException("Config between client and server mismatch. Server sent unknown or non-config field. Ignoring");
                }
                Object value = key.mapper.fromNetwork(buffer);
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

    public ConfigState readFromFileOrCreateByDefault() throws IOException {
        if (this.defaultState == null) {
            throw new IllegalStateException("LibX config internal error: Default state not set.");
        }
        return this.readFromFileOrCreateBy(this.defaultState);
    }
    
    public ConfigState readFromFileOrCreateBy(ConfigState state) throws IOException {
        if (!Files.isRegularFile(this.path)) {
            LibX.logger.info("Config '" + this.id + "' does not exist. Creating default.");
            state.writeToFile(null, null);
            return state;
        } else {
            return this.readFromFile(null, null);
        }
    }
    
    public ConfigState readFromFile(@Nullable Path path, @Nullable ConfigState parent) throws IOException {
        Path filePath = path == null ? this.path : path;
        ConfigState parentConfig = parent == null ? this.defaultState : parent;
        if (this.defaultState == null) {
            // Even with other parent, a default state should always be available.
            throw new IllegalStateException("Can't read config from file: Default state not set.");
        }
        if (!Files.isRegularFile(filePath) || !Files.isReadable(filePath)) {
            if (parent == null) {
                if (path != null) {
                    throw new IllegalStateException("Config '" + this.id + "' at '" + path.toAbsolutePath().normalize() + "' does not exist or is not readable.");
                } else {
                    throw new IllegalStateException("Config '" + this.id + "' does not exist or is not readable.");
                }
            } else {
                // File not found, we just return parent
                return parent;
            }
        }
        Reader reader = Files.newBufferedReader(filePath);
        JsonObject config = GSON.fromJson(reader, JsonObject.class);
        ImmutableMap.Builder<ConfigKey, Object> values = ImmutableMap.builder();
        AtomicBoolean needsCorrection = new AtomicBoolean(false);
        Set<ConfigKey> keysToCorrect = parent == null ? null : new HashSet<>();
        for (ConfigKey key : this.keys.values()) {
            JsonElement elem = config == null ? null : getInObjectKeyPath(config, key, needsCorrection);
            if (elem != null) {
                if (keysToCorrect != null) keysToCorrect.add(key);
                try {
                    if (!key.mapper.element().isAssignableFrom(elem.getClass())) {
                        throw new IllegalStateException("Json element has invalid type for key '" + String.join(".", key.path) + "': Expected: " + key.mapper.element().getSimpleName() + " Got: " + elem.getClass().getSimpleName());
                    }
                    //noinspection unchecked
                    Object value = ((ValueMapper<?, JsonElement>) key.mapper).fromJson(elem);
                    if (value == null) throw new IllegalStateException("Config mapper reported null value.");
                    values.put(key, key.validate(value, "Invalid value in config file", needsCorrection));
                } catch (Exception e) {
                    LibX.logger.warn("Failed to read config value " + String.join(".", key.path) + ". Correcting. Error: " + e.getMessage());
                    CorrectionInstance<?, ?> correction = CorrectionInstance.create(parentConfig.getValue(key));
                    //noinspection unchecked
                    Object value = correction.correct(elem, (ValueMapper<Object, ?>) key.mapper, o -> o).orElse(parentConfig.getValue(key));
                    values.put(key, key.validate(value, "Invalid value in corrected config file", needsCorrection));
                    needsCorrection.set(true);
                }
            } else {
                values.put(key, parentConfig.getValue(key));
                if (parent == null) {
                    // No need for correction when there's a parent
                    // as in that case we're reading a partial state
                    //noinspection ConstantConditions
                    if (keysToCorrect != null) keysToCorrect.add(key);
                    needsCorrection.set(true);
                }
            }
        }
        reader.close();
        ConfigState state = new ConfigState(this, values.build());
        if (needsCorrection.get()) {
            if (path != null) {
                LibX.logger.info("Correcting config '" + this.id + "' at " + path.toAbsolutePath().normalize());
            } else {
                LibX.logger.info("Correcting config '" + this.id + "'");
            }
            state.writeToFile(path, keysToCorrect);
        }
        return state;
    }

    private static void addAllFieldsToBuilder(String modid, Class<?> baseClass, Class<?> currentClass, ImmutableMap.Builder<Field, ConfigKey> keys, ImmutableSet.Builder<ConfigGroup> groups) throws ReflectiveOperationException {
        Set<String> names = new HashSet<>();
        for (Field field : currentClass.getDeclaredFields()) {
            ConfigKey key = ConfigKey.create(modid, field, baseClass);
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
            ConfigGroup group = ConfigGroup.create(clazz, baseClass);
            if (group != null) {
                groups.add(group);
                if (names.contains(clazz.getSimpleName())) {
                    throw new IllegalStateException("Duplicate key in config definition: " + clazz.getSimpleName());
                } else {
                    names.add(clazz.getSimpleName());
                    addAllFieldsToBuilder(modid, baseClass, clazz, keys, groups);
                }
            }
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
    
    public  void shadowBy(ConfigState state) {
        this.shadowBy(state, false, null);
    }
    
    private void shadowBy(ConfigState state, boolean local, @Nullable Path loadPath) {
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            LibX.logger.error("Config shadow was called on a dedicated server. This should not happen!");
        }
        if (!this.shadowed && this.savedState == null) {
            LibX.logger.warn("Capturing config state for '" + this.id + "' before shadowing. This should not happen. Was the config not loaded properly?");
            this.savedState = this.stateFromValues();
        }
        this.shadowed = true;
        this.shadowedLocal = local;
        state.apply();
        ConfigLoadedEvent.LoadReason reason = local ? ConfigLoadedEvent.LoadReason.LOCAL_SHADOW : ConfigLoadedEvent.LoadReason.SHADOW;
        MinecraftForge.EVENT_BUS.post(new ConfigLoadedEvent(this.id, this.baseClass, reason, this.clientConfig, this.path, loadPath));
    }
    
    public void restore() {
        if (this.shadowed && this.savedState != null) {
            this.savedState.apply();
        } else if (this.shadowed) {
            LibX.logger.warn("Could not restore config: No saved state");
        }
        this.shadowed = false;
        this.shadowedLocal = false;
        MinecraftForge.EVENT_BUS.post(new ConfigLoadedEvent(this.id, this.baseClass, ConfigLoadedEvent.LoadReason.RESTORE, this.clientConfig, this.path, this.path));
    }
    
    public void reloadClientWorldState() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            if (!this.shadowed || this.shadowedLocal) {
                Level clientLevel = DistExecutor.unsafeRunForDist(() -> () -> Minecraft.getInstance().level, () -> () -> null);
                MinecraftServer server = DistExecutor.unsafeRunForDist(() -> Minecraft.getInstance()::getSingleplayerServer, () -> () -> null);
                if (clientLevel != null && server != null) {
                    Path configDir = server.storageSource.getWorldDir().resolve("config");
                    Path configPath = resolveConfigPath(configDir, this.id);
                    if (this.savedState == null) {
                        LibX.logger.warn("Can't load world specific config for '" + this.id + "': No captured state. This should never happen.");
                    } else {
                        try {
                            if (Files.isRegularFile(configPath)) {
                                ConfigState state = this.readFromFile(configPath, this.savedState);
                                this.shadowBy(state, true, configPath);
                            }
                        } catch (IOException e) {
                            LibX.logger.warn("Can't load world specific config for '" + this.id + "': " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
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
    
    public static Path resolveConfigPath(Path configDir, ResourceLocation id) {
        return id.getPath().equals("config") ? configDir.resolve(id.getNamespace() + ".json5") : configDir.resolve(id.getNamespace()).resolve(id.getPath() + ".json5");
    }
}
