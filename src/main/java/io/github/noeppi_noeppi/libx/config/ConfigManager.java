package io.github.noeppi_noeppi.libx.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonParseException;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.impl.config.AdvancedValueMappers;
import io.github.noeppi_noeppi.libx.impl.config.ConfigImpl;
import io.github.noeppi_noeppi.libx.impl.config.ConfigState;
import io.github.noeppi_noeppi.libx.impl.config.SimpleValueMappers;
import io.github.noeppi_noeppi.libx.impl.network.ConfigShadowSerializer;
import io.github.noeppi_noeppi.libx.util.ClassUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Provides a config system for configuration files that is meant to be more easy and powerful than
 * the system by forge based on nightconfig. This system creates json files with comments based on
 * a class. That class may contain fields with {@link Config @Config} annotations. Each field with a
 * config annotation will get one value in the config file. To create sub groups, you can create static
 * nested classes inside the base class. Suppose you have the following class structure:
 * 
 * <pre>
 * {@code 
 * public class SampleConfig {
 *
 *     \@Config("A value")
 *     public static int value = 23;
 *
 *     \@Config({"Multiline Comments", "are also possible"})
 *     public static double another_value;
 *
 *     \@Config("A text component")
 *     public static IFormattableTextComponent tc = new StringTextComponent("YAY");
 *
 *     public static class SubGroup {
 *
 *         \@Config(value = "xD", elementType = Integer.class)
 *         public static List<Integer> coolValues = ImmutableList.of(1, 5, 23);
 *     }
 * }
 * }
 * </pre>
 * 
 * This would create the following config file:
 * 
 * <pre>
 * {@code 
 * {
 *   // Multiline Comments
 *   // are also possible
 *   "another_value": 0.0,
 *
 *   // A text component
 *   "tc": {
 *     "text": "YAY"
 *   },
 *
 *   // A value
 *   "value": 23,
 *
 *   "SubGroup": {
 *     // xD
 *     "coolValues": [
 *       1,
 *       5,
 *       23
 *     ]
 *   }
 * }
 * }
 * </pre>
 * 
 * The values of the fields are the default values for the config.
 * Fields can have any type you want as long as you provide a {@link ValueMapper} for that type.
 * First you need to register that type via
 * {@link ConfigManager#registerValueMapper(ResourceLocation, ValueMapper) registerValueMapper()}.
 * Then you must provide the resource location in the {@link Config @Config} annotation of that field.
 * 
 * For all builtin types you can leave that value out. Fields of the following types are supported:
 * 
 * <ul>
 *     <li>boolean</li>
 *     <li>byte</li>
 *     <li>short</li>
 *     <li>int</li>
 *     <li>long</li>
 *     <li>float</li>
 *     <li>double</li>
 *     <li>String</li>
 *     <li>List&lt;T&gt;</li>
 *     <li>Map&lt;String, T&gt;</li>
 *     <li>Ingredient</li>
 *     <li>IFormattableTextComponent</li>
 * </ul>
 * 
 * The type {@code T} can be any of the builtin types. It must be provided to the {@link Config @Config}
 * annotation because of the type erasure.
 * 
 * Custom Value Mappers can be registered with
 * {@link ConfigManager#registerValueMapper(ResourceLocation, ValueMapper) registerValueMapper()}.
 * 
 * Configs come in two different types: Common configs and client configs. Common configs are loaded on
 * both the dedicated server and the client and are synced from server to client. Client configs are
 * only loaded on the client.
 * A config is registered with {@link ConfigManager#registerConfig(ResourceLocation, Class, boolean)}.
 * You can then just use the values in the config class. Make sure to not modify them as the results
 * are unpredictable.
 */
public class ConfigManager {

    @SuppressWarnings("UnstableApiUsage")
    private static final Map<Class<?>, ValueMapper<?, ?>> globalMappers = ImmutableSet.of(
            SimpleValueMappers.BOOLEAN,
            SimpleValueMappers.BYTE,
            SimpleValueMappers.SHORT,
            SimpleValueMappers.INTEGER,
            SimpleValueMappers.LONG,
            SimpleValueMappers.FLOAT,
            SimpleValueMappers.DOUBLE,
            SimpleValueMappers.STRING,
            SimpleValueMappers.LIST,
            SimpleValueMappers.MAP,
            AdvancedValueMappers.INGREDIENT,
            AdvancedValueMappers.TEXT_COMPONENT
    ).stream().collect(ImmutableMap.toImmutableMap(ValueMapper::type, Function.identity()));
    @SuppressWarnings("UnstableApiUsage")
    private static final Map<Class<?>, ResourceLocation> globalMappersToRL = globalMappers.keySet().stream().map(key -> Pair.of(key, new ResourceLocation("minecraft", ClassUtil.boxed(key).getSimpleName().toLowerCase()))).collect(ImmutableMap.toImmutableMap(Pair::getKey, Pair::getValue));
    private static final Map<ResourceLocation, ValueMapper<?, ?>> mappers = new HashMap<>();
    private static final BiMap<ResourceLocation, Class<?>> configIds = HashBiMap.create();
    private static final Map<Class<?>, Path> configs = new HashMap<>();
    
    static {
        globalMappers.forEach((key, value) -> registerValueMapper(globalMappersToRL.get(key), value));
    }

    /**
     * Registers a new {@link ValueMapper} that can be used to serialise config values.
     */
    public static void registerValueMapper(ResourceLocation id, ValueMapper<?, ?> mapper) {
        if (mappers.containsKey(id)) {
            throw new IllegalStateException("Config mapper '" + id + "' is already registered.");
        }
        mappers.put(id, mapper);
    }

    /**
     * Gets a value mapper by values of the {@link Config @Config} annotation.
     */
    public static ResourceLocation getMapperByAnnotationValue(String annotationValue, Class<?> typeClass) {
        if (annotationValue.isEmpty()) {
            Class<?> boxed = ClassUtil.boxed(typeClass);
            if (globalMappersToRL.containsKey(boxed)) {
                return globalMappersToRL.get(boxed);
            } else if (typeClass != boxed){
                throw new IllegalStateException("No builtin JSON mapper for type " + typeClass + " (" + boxed + "). You must provide one yourself.");
            } else {
                throw new IllegalStateException("No builtin JSON mapper for type " + typeClass + ". You must provide one yourself.");
            }
        } else {
            return new ResourceLocation(annotationValue);
        }
    }

    /**
     * Resolves a value mapper. If {@code id} is null, a value mapper is detected dfrom the builtin
     * mappers. If no mapper exits, an exception is thrown.
     */
    public static <T> ValueMapper<T, ?> getMapper(@Nullable ResourceLocation id, Class<T> fieldClass) {
        if (fieldClass == void.class) {
            throw new IllegalStateException("No mapper registered for void type. Probably the element type was omitted for a list or a map. If you are trying to create nested lists or maps, create a custom mapper.");
        }
        Class<?> boxed = ClassUtil.boxed(fieldClass);
        if (id == null) {
            if (globalMappers.containsKey(boxed)) {
                //noinspection unchecked
                return (ValueMapper<T, ?>) globalMappers.get(boxed);
            } else if (fieldClass != boxed){
                throw new IllegalStateException("No builtin JSON mapper for type " + fieldClass + " (" + boxed + "). You must provide one yourself.");
            } else {
                throw new IllegalStateException("No builtin JSON mapper for type " + fieldClass + ". You must provide one yourself.");
            }
        } else {
            if (mappers.containsKey(id)) {
                ValueMapper<?, ?> mapper = mappers.get(id);
                if (mapper.type() == boxed) {
                    //noinspection unchecked
                    return (ValueMapper<T, ?>) mapper;
                } else {
                    throw new IllegalStateException("Config mapper '" + id + "' can not be used on values of type " + fieldClass);
                }
            } else {
                throw new IllegalStateException("No config mapper registered for id '" + id + "'.");
            }
        }
    }

    /**
     * Registers a config. This will register a config with the id {@code modid:config}
     * which means the config will be located in {@code config/modid.json5}.
     * @param modid The modid of the mod. 
     * @param configClass The base class for the config.
     * @param clientConfig Whether this is a client config.
     */
    public static void registerConfig(String modid, Class<?> configClass, boolean clientConfig) {
        registerConfig(new ResourceLocation(modid, "config"), configClass, clientConfig);
    }
    
    /**
     * Registers a config.
     * @param location The id of the config. The config will be located
     *                 in {@code config/namespace/path.json5}. Exception is a path of {@code config}.
     *                 In this case the config will be located at {@code config/modid.json5}.
     * @param configClass The base class for the config.
     * @param clientConfig Whether this is a client config.
     */
    public static void registerConfig(ResourceLocation location, Class<?> configClass, boolean clientConfig) {
        if (configIds.containsKey(location)) {
            throw new IllegalArgumentException("Config '" + location + "' is already bound to class " + configClass);
        } else if (configIds.containsValue(configClass)) {
            throw new IllegalArgumentException("Class " + configClass + " is already registered as '" + configIds.inverse().get(configClass) + "'");
        }
        Path path;
        if (location.getPath().equals("config")) {
            path = FMLPaths.GAMEDIR.get().resolve("config").resolve(location.getNamespace() + ".json5");
        } else {
            path = FMLPaths.GAMEDIR.get().resolve("config").resolve(location.getNamespace()).resolve(location.getPath() + ".json5");
        }
        configIds.put(location, configClass);
        configs.put(configClass, path);
        new ConfigImpl(location, configClass, path, clientConfig);
        firstLoadConfig(configClass);
    }

    /**
     * Forces a reload of al lconfigs.
     */
    public static void reloadAll() {
        for (Class<?> configClass : configs.keySet()) {
            reloadConfig(configClass);
        }
    }
    
    private static void firstLoadConfig(Class<?> configClass) {
        if (!configIds.containsValue(configClass)) {
            throw new IllegalArgumentException("Class " + configClass + " is not registered as a config.");
        }
        try {
            ConfigImpl config = ConfigImpl.getConfig(configIds.inverse().get(configClass));
            if (!config.clientConfig || FMLEnvironment.dist == Dist.CLIENT) {
                ConfigState defaultState = config.stateFromValues();
                config.setDefaultState(defaultState);
                ConfigState state = config.readFromFileOrCreateBy(defaultState);
                config.saveState(state);
                state.apply();
            }
        } catch (IOException | IllegalStateException | JsonParseException e) {
            LibX.logger.error("Failed to load config '" + configIds.inverse().get(configClass) + "' (class: " + configClass + ")", e);
        }
    }

    /**
     * Forces a reload of one config.
     */
    public static void reloadConfig(Class<?> configClass) {
        if (!configIds.containsValue(configClass)) {
            throw new IllegalArgumentException("Class " + configClass + " is not registered as a config.");
        }
        try {
            ConfigImpl config = ConfigImpl.getConfig(configIds.inverse().get(configClass));
            if (!config.clientConfig || FMLEnvironment.dist == Dist.CLIENT) {
                ConfigState state = config.readFromFile();
                config.saveState(state);
                if (!config.isShadowed()) {
                    state.apply();
                }
            }
        } catch (IOException | IllegalStateException | JsonParseException e) {
            LibX.logger.error("Failed to reload config '" + configIds.inverse().get(configClass) + "' (class: " + configClass + ")", e);
        }
    }

    /**
     * Forces a resync of all configs to one player.
     */
    public static void forceResync(@Nullable ServerPlayerEntity player) {
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            for (ResourceLocation id : ConfigManager.configs()) {
                ConfigImpl config = ConfigImpl.getConfig(id);
                if (!config.clientConfig) {
                    PacketDistributor.PacketTarget target = player == null ? PacketDistributor.ALL.noArg() : PacketDistributor.PLAYER.with(() -> player);
                    LibX.getNetwork().instance.send(target, new ConfigShadowSerializer.ConfigShadowMessage(config, config.cachedOrCurrent()));
                }
            }
        } else {
            LibX.logger.error("ConfigManager.forceResync was called on a physical client. Ignoring.");
        }
    }

    /**
     * Gets all registered config ids.
     */
    public static Set<ResourceLocation> configs() {
        return configIds.keySet();
    }
}
