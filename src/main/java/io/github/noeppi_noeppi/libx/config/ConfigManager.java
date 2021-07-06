package io.github.noeppi_noeppi.libx.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.crafting.IngredientStack;
import io.github.noeppi_noeppi.libx.event.ConfigLoadedEvent;
import io.github.noeppi_noeppi.libx.impl.config.ConfigImpl;
import io.github.noeppi_noeppi.libx.impl.config.ConfigState;
import io.github.noeppi_noeppi.libx.impl.config.ModMappers;
import io.github.noeppi_noeppi.libx.impl.network.ConfigShadowSerializer;
import io.github.noeppi_noeppi.libx.impl.network.NetworkImpl;
import io.github.noeppi_noeppi.libx.util.ResourceList;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Provides a config system for configuration files that is meant to be more easy and powerful than
 * the system by forge based on {@link com.electronwill.nightconfig NightConfig}. This system creates
 * json files with comments based on a class. That class may contain fields with {@link Config @Config}
 * annotations. Each field with a config annotation will get one value in the config file. To create sub
 * groups, you can create static nested classes inside the base class. Suppose you have the following
 * class structure:
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
 * You need to register that type via {@link ConfigManager#registerValueMapper(String, ValueMapper)} (or
 * {@link ConfigManager#registerValueMapper(String, GenericValueMapper)} for generic value mappers).
 * Then you can use that type in a config. Custom registered value mappers are unique for each mod, so
 * you and another mod can add different value mappers for the same class. However you can't add two
 * value mappers for the same class in one mod.
 * 
 * By default the following types are supported:
 * 
 * <ul>
 *     <li>boolean</li>
 *     <li>byte</li>
 *     <li>short</li>
 *     <li>int</li>
 *     <li>long</li>
 *     <li>float</li>
 *     <li>double</li>
 *     <li>{@link String String}</li>
 *     <li>{@link Optional Optional&lt;?&gt;}</li>
 *     <li>{@link List List&lt;?&gt;}</li>
 *     <li>{@link Map Map&lt;String, ?&gt;}</li>
 *     <li>{@link ResourceLocation}</li>
 *     <li>{@link Ingredient}</li>
 *     <li>{@link IngredientStack}</li>
 *     <li>{@link IFormattableTextComponent}</li>
 *     <li>{@link ResourceList}</li>
 *     <li>{@link UUID UUID}</li>
 *     <li>Any enum</li>
 *     <li>Any {@link Pair Pair&lt;?, ?&gt;}</li>
 *     <li>Any {@link Triple Triple&lt;?, ?, ?&gt;}</li>
 * </ul>
 * 
 * Configs come in two different types: Common configs and client configs. Common configs are loaded on
 * both the dedicated server and the client and are synced from server to client. Client configs are
 * only loaded on the client.
 * A config is registered with {@link ConfigManager#registerConfig(ResourceLocation, Class, boolean)}.
 * You can then just use the values in the config class. Make sure to not modify them as the results
 * are unpredictable.
 * 
 * Config values may never be null in the code. However value mappers are allowed to produce json-null
 * values. If you need a nullable value in the config, use an Optional. Empty Optionals will translate
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
        Path path = ConfigImpl.resolveConfigPath(FMLPaths.GAMEDIR.get().resolve("config"), location);
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
                MinecraftForge.EVENT_BUS.post(new ConfigLoadedEvent(config.id, config.baseClass, ConfigLoadedEvent.LoadReason.INITIAL, config.clientConfig, config.path, config.path));
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
                config.reloadClientWorldState();
                MinecraftForge.EVENT_BUS.post(new ConfigLoadedEvent(config.id, config.baseClass, ConfigLoadedEvent.LoadReason.RELOAD, config.clientConfig, config.path, config.path));
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
                NetworkImpl.getImpl().instance.send(target, new ConfigShadowSerializer.ConfigShadowMessage(config, config.cachedOrCurrent()));
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
                    NetworkImpl.getImpl().instance.send(target, new ConfigShadowSerializer.ConfigShadowMessage(config, config.cachedOrCurrent()));
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
