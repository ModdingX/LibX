package org.moddingx.libx.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.moddingx.libx.LibX;
import org.moddingx.libx.config.mapper.GenericValueMapper;
import org.moddingx.libx.config.mapper.MapperFactory;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validate.DoubleRange;
import org.moddingx.libx.config.validator.ConfigValidator;
import org.moddingx.libx.crafting.IngredientStack;
import org.moddingx.libx.event.ConfigLoadedEvent;
import org.moddingx.libx.impl.config.ConfigImpl;
import org.moddingx.libx.impl.config.ConfigState;
import org.moddingx.libx.impl.config.ModMappers;
import org.moddingx.libx.impl.network.ConfigShadowMessage;
import org.moddingx.libx.impl.network.NetworkImpl;
import org.moddingx.libx.util.data.ResourceList;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Provides a config system for configuration files that is meant to be more easy and powerful than
 * the system by forge based on {@link com.electronwill.nightconfig NightConfig}. This system creates
 * json files with comments based on a class. That class may contain fields with {@link Config @Config}
 * annotations. Each field with a config annotation will get one value in the config file. To create sub
 * groups, you can create static nested classes inside the base class. Suppose you have the following
 * class structure:
 * 
 * <pre>
 * <code>
 * public class ExampleConfig {
 *
 *     {@link Config @Config}("A value")
 *     public static {@link Integer int} value = 23;
 *
 *     {@link Config @Config}({"Multiline Comments", "are also possible"})
 *     {@link DoubleRange @DoubleRange}({@link DoubleRange#min() min} = 0)
 *     public static {@link Double double} another_value;
 *
 *     {@link Config @Config}("A component")
 *     public static {@link Component Component} component = {@link Component Component}.{@link Component#literal(String) literal}("LibX is fancy");
 *
 *     public static class SubGroup {
 *
 *         {@link Config @Config}
 *         public static {@link List List}&lt;{@link Integer Integer}&gt; valueList = {@link List List}.{@link List#of() of}(1, 5, 23);
 *     }
 * }
 * </code>
 * </pre>
 * 
 * This would create the following config file:
 * 
 * <pre>
 * <code>
 * {
 *   // Multiline Comments
 *   // are also possible
 *   // Minimum: 0
 *   "another_value": 0.0,
 *
 *   // A component
 *   "tc": {
 *     "text": "LibX is fancy"
 *   },
 *
 *   // A value
 *   "value": 23,
 *
 *   "SubGroup": {
 *     "coolValues": [
 *       1, 5, 23
 *     ]
 *   }
 * }
 * </code>
 * </pre>
 * 
 * The values of the fields are the default values for the config.
 * Fields can have any type you want as long as you provide a {@link ValueMapper} for that type.
 * You need to register that type via {@link ConfigManager#registerValueMapper(String, ValueMapper)} (or
 * {@link ConfigManager#registerValueMapper(String, GenericValueMapper)} for generic value mappers).
 * Then you can use that type in a config. Custom registered value mappers are unique for each mod, so
 * you and another mod can add different value mappers for the same class. However, you can't add two
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
 *     <li>{@link Component}</li>
 *     <li>{@link ResourceList}</li>
 *     <li>{@link UUID UUID}</li>
 *     <li>Any {@link Enum enum}</li>
 *     <li>Any {@link Record record}</li>
 *     <li>Any {@link Pair Pair&lt;?, ?&gt;}</li>
 *     <li>Any {@link Triple Triple&lt;?, ?, ?&gt;}</li>
 * </ul>
 * 
 * If a class uses generics, the {@code ?} can be any type that is supported by the config system. So
 * you can also use a <code>{@link List List}&lt;{@link Pair Pair}&lt;{@link List List}&lt;{@link Integer Integer}&gt;, {@link String String}&gt;&gt;</code>.
 * 
 * Each config field can also have <b>one</b> validator annotation applied to validate a value. You can
 * find builtin validator annotations in {@link org.moddingx.libx.config.validator}. To register
 * you own validator, use {@link #registerConfigValidator(String, ConfigValidator)}.
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
        if (!Objects.equals(modid, ModLoadingContext.get().getActiveNamespace())) {
            LibX.logger.error("Wrong modid for value mapper, expected " + ModLoadingContext.get().getActiveNamespace() + " got " + modid);
        }
        ModMappers.get(modid).registerValueMapper(mapper);
    }
    
    /**
     * Registers a new {@link GenericValueMapper} that can be used to serialise config values.
     */
    public static void registerValueMapper(String modid, GenericValueMapper<?, ?, ?> mapper) {
        if (!Objects.equals(modid, ModLoadingContext.get().getActiveNamespace())) {
            LibX.logger.error("Wrong modid for generic value mapper, expected " + ModLoadingContext.get().getActiveNamespace() + " got " + modid);
        }
        ModMappers.get(modid).registerValueMapper(mapper);
    }
    
    /**
     * Registers a new {@link MapperFactory} that can be used to create value mappers based on the generic type of the config key.
     */
    public static void registerValueMapperFactory(String modid, MapperFactory<?> factory) {
        if (!Objects.equals(modid, ModLoadingContext.get().getActiveNamespace())) {
            LibX.logger.error("Wrong modid for value mapper factory, expected " + ModLoadingContext.get().getActiveNamespace() + " got " + modid);
        }
        ModMappers.get(modid).registerValueMapperFactory(factory);
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
     * 
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
        ModMappers.get(location.getNamespace()).configRegistered();
        firstLoadConfig(configClass);
    }

    /**
     * Forces reload of all common configs. <b>This will not sync the config though. Use {@link #forceResync(ServerPlayer)} for this.</b>
     */
    public static void reloadCommon() {
        for (Class<?> configClass : configs.keySet()) {
            reloadConfig(configClass, true, false);
        }
    }
    
    /**
     * Forces reload of all client configs.
     */
    public static void reloadClient() {
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) return;
        for (Class<?> configClass : configs.keySet()) {
            reloadConfig(configClass, false, true);
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
     * Forces reload of one config. <b>This will not sync the config though. Use {@link #forceResync(ServerPlayer, Class)} for this.</b>
     */
    public static void reloadConfig(Class<?> configClass) {
        reloadConfig(configClass, true, true);
    }
    
    private static void reloadConfig(Class<?> configClass, boolean allowCommon, boolean allowClient) {
        if (!allowCommon && !allowClient) return;
        if (!configIds.containsValue(configClass)) {
            throw new IllegalArgumentException("Class " + configClass + " is not registered as a config.");
        }
        try {
            ConfigImpl config = ConfigImpl.getConfig(configIds.inverse().get(configClass));
            if (config.clientConfig ? allowClient : allowCommon) {
                if (!config.clientConfig || FMLEnvironment.dist == Dist.CLIENT) {
                    ConfigState state = config.readFromFileOrCreateByDefault();
                    config.saveState(state);
                    if (!config.isShadowed()) {
                        state.apply();
                    }
                    config.reloadClientWorldState();
                    MinecraftForge.EVENT_BUS.post(new ConfigLoadedEvent(config.id, config.baseClass, ConfigLoadedEvent.LoadReason.RELOAD, config.clientConfig, config.path, config.path));
                }
            }
        } catch (IOException | IllegalStateException | JsonParseException e) {
            LibX.logger.error("Failed to reload config '" + configIds.inverse().get(configClass) + "' (class: " + configClass + ")", e);
        }
    }

    /**
     * Forces a resync of one config to one player.
     */
    public static void forceResync(@Nullable ServerPlayer player, Class<?> configClass) {
        if (!configIds.containsValue(configClass)) {
            throw new IllegalArgumentException("Class " + configClass + " is not registered as a config.");
        }
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            ResourceLocation id = configIds.inverse().get(configClass);
            ConfigImpl config = ConfigImpl.getConfig(id);
            if (!config.clientConfig && NetworkImpl.getImpl().canSend()) {
                PacketDistributor.PacketTarget target = player == null ? PacketDistributor.ALL.noArg() : PacketDistributor.PLAYER.with(() -> player);
                NetworkImpl.getImpl().channel.send(target, new ConfigShadowMessage(config, config.cachedOrCurrent()));
            }
        } else {
            LibX.logger.error("ConfigManager.forceResync was called on a physical client. Ignoring.");
        }
    }

    /**
     * Forces a resync of all configs to one player.
     */
    public static void forceResync(@Nullable ServerPlayer player) {
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            for (ResourceLocation id : ConfigManager.configs()) {
                ConfigImpl config = ConfigImpl.getConfig(id);
                if (!config.clientConfig && NetworkImpl.getImpl().canSend()) {
                    PacketDistributor.PacketTarget target = player == null ? PacketDistributor.ALL.noArg() : PacketDistributor.PLAYER.with(() -> player);
                    NetworkImpl.getImpl().channel.send(target, new ConfigShadowMessage(config, config.cachedOrCurrent()));
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
