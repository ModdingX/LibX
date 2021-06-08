package io.github.noeppi_noeppi.libx.config;

import com.google.common.collect.*;
import com.google.gson.JsonParseException;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.event.ConfigLoadedEvent;
import io.github.noeppi_noeppi.libx.impl.config.*;
import io.github.noeppi_noeppi.libx.impl.network.ConfigShadowSerializer;
import io.github.noeppi_noeppi.libx.util.ClassUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.OnlyIns;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

//FIXME fix config javadoc
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
 * { @link ConfigManager#registerValueMapper(ResourceLocation, ValueMapper) registerValueMapper()}.
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
 *     <li>Optional&lt;T&gt;</li>
 *     <li>List&lt;T&gt;</li>
 *     <li>Map&lt;String, T&gt;</li>
 *     <li>ResourceLocation</li>
 *     <li>Ingredient</li>
 *     <li>IFormattableTextComponent</li>
 *     <li>ResourceList</li>
 *     <li>UUID</li>
 *     <li>Any enum</li>
 * </ul>
 * 
 * The type {@code T} can be any of the builtin types. It must be provided to the {@link Config @Config}
 * annotation because of the type erasure.
 * 
 * Custom Value Mappers can be registered with
 * { @link ConfigManager#registerValueMapper(ResourceLocation, ValueMapper) registerValueMapper()}.
 * 
 * Configs come in two different types: Common configs and client configs. Common configs are loaded on
 * both the dedicated server and the client and are synced from server to client. Client configs are
 * only loaded on the client.
 * A config is registered with {@link ConfigManager#registerConfig(ResourceLocation, Class, boolean)}.
 * You can then just use the values in the config class. Make sure to not modify them as the results
 * are unpredictable.
 * 
 * Config values may never be null in the code. However value mappers are allowed to produce json-null
 * values.If you need a nullable value in the config, use an Optional. Empty Optionals will translate
 * to null in the JSON.
 */
public class ConfigManager {
    
    private static final BiMap<ResourceLocation, Class<?>> configIds = Maps.synchronizedBiMap(HashBiMap.create());
    private static final Map<Class<?>, Path> configs = Collections.synchronizedMap(new HashMap<>());

    /**
     * Registers a new {@link ValueMapper} that can be used to serialise config values.
     */
    public static void registerValueMapper(String modid, ValueMapper<?, ?> mapper) {
        ModMappers.get(modid).registerValueMapper(mapper);
    }
    
    /**
     * Registers a new {@link GenericValueMapper} that can be used to serialise config values.
     */
    public static void registerValueMapper(String modid, GenericValueMapper<?, ?, ?> mapper) {
        ModMappers.get(modid).registerValueMapper(mapper);
    }
    
    /**
     * Registers a new {@link ConfigValidator} that can be used to validate config values.
     */
    public static void registerConfigValidator(String modid, ConfigValidator<?, ?> validator) {
        ModMappers.get(modid).registerConfigValidator(validator);
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
     * Forces a reload of all configs. <b>This will not sync the config tough. Use forceResync for this.</b>
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
                MinecraftForge.EVENT_BUS.post(new ConfigLoadedEvent(config.id, config.baseClass, ConfigLoadedEvent.LoadReason.INITIAL, config.clientConfig, config.path));
            }
        } catch (IOException | IllegalStateException | JsonParseException e) {
            LibX.logger.error("Failed to load config '" + configIds.inverse().get(configClass) + "' (class: " + configClass + ")", e);
        }
    }

    /**
     * Forces a reload of one config. <b>This will not sync the config tough. Use forceResync for this.</b>
     */
    public static void reloadConfig(Class<?> configClass) {
        if (!configIds.containsValue(configClass)) {
            throw new IllegalArgumentException("Class " + configClass + " is not registered as a config.");
        }
        try {
            ConfigImpl config = ConfigImpl.getConfig(configIds.inverse().get(configClass));
            if (!config.clientConfig || FMLEnvironment.dist == Dist.CLIENT) {
                ConfigState state = config.readFromFileOrCreateByDefault();
                config.saveState(state);
                if (!config.isShadowed()) {
                    state.apply();
                }
                MinecraftForge.EVENT_BUS.post(new ConfigLoadedEvent(config.id, config.baseClass, ConfigLoadedEvent.LoadReason.RELOAD, config.clientConfig, config.path));
            }
        } catch (IOException | IllegalStateException | JsonParseException e) {
            LibX.logger.error("Failed to reload config '" + configIds.inverse().get(configClass) + "' (class: " + configClass + ")", e);
        }
    }

    /**
     * Forces a resync of one config to one player.
     */
    public static void forceResync(@Nullable ServerPlayerEntity player, Class<?> configClass) {
        if (!configIds.containsValue(configClass)) {
            throw new IllegalArgumentException("Class " + configClass + " is not registered as a config.");
        }
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            ResourceLocation id = configIds.inverse().get(configClass);
            ConfigImpl config = ConfigImpl.getConfig(id);
            if (!config.clientConfig) {
                PacketDistributor.PacketTarget target = player == null ? PacketDistributor.ALL.noArg() : PacketDistributor.PLAYER.with(() -> player);
                LibX.getNetwork().instance.send(target, new ConfigShadowSerializer.ConfigShadowMessage(config, config.cachedOrCurrent()));
            }
        } else {
            LibX.logger.error("ConfigManager.forceResync was called on a physical client. Ignoring.");
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
        return Collections.unmodifiableSet(configIds.keySet());
    }
}
