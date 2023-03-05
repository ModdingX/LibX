package org.moddingx.libx.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import org.moddingx.libx.config.ConfigManager;

import javax.annotation.Nullable;
import java.nio.file.Path;

/**
 * Fired whenever a {@link ConfigManager LibX config} is loaded. Is fired on both client and server.
 * The event is not cancelable.
 */
public class ConfigLoadedEvent extends Event {

    private final ResourceLocation configId;
    private final Class<?> configClass;
    private final LoadReason reason;
    private final boolean clientConfig;
    private final Path configPath;
    @Nullable
    private final Path currentPath;
    
    public ConfigLoadedEvent(ResourceLocation configId, Class<?> configClass, LoadReason reason, boolean clientConfig, Path configPath, @Nullable Path currentPath) {
        this.configId = configId;
        this.configClass = configClass;
        this.reason = reason;
        this.clientConfig = clientConfig;
        this.configPath = configPath;
        this.currentPath = currentPath;
    }

    /**
     * Gets the id of the config that was loaded.
     */
    public ResourceLocation getConfigId() {
        return this.configId;
    }
    
    /**
     * Gets the class of the config that was loaded.
     */
    public Class<?> getConfigClass() {
        return this.configClass;
    }

    /**
     * Gets the reason why the config was loaded
     */
    public LoadReason getReason() {
        return this.reason;
    }

    /**
     * Gets whether the config is a client config.
     */
    public boolean isClientConfig() {
        return this.clientConfig;
    }

    /**
     * Gets the path where the config is stored by default.
     */
    public Path getConfigPath() {
        return this.configPath;
    }
    
    /**
     * Gets the path where the config is loaded from for this event. For example,
     * configs can be loaded per world. In that case {@code currentPath}
     * will be the path from the world and {@link #getConfigPath() configPath} will be the
     * default path for the config which might still be used for values not present in the
     * world-specific config.
     * 
     * The value is {@code null} when the current config is loaded from a non-file location
     * (for example on shadowing). It can also be the same as {@link #getConfigPath() configPath}
     * when the config is loaded from the default path.
     */
    @Nullable
    public Path getCurrentPath() {
        return this.currentPath;
    }

    /**
     * A reason for a config to load
     */
    public enum LoadReason {
        
        /**
         * First load of the config.
         */
        INITIAL,

        /**
         * This is the load reason when the values are synced with
         * the values from the server. This is only fired on the client.
         */
        SHADOW,
        
        /**
         * This is the load reason when the values are loaded from the
         * singleplayer world specific config folder. This is only fired
         * on the client.
         */
        LOCAL_SHADOW,

        /**
         * This is the load reason when the values are restored to their
         * state before they were shadowed by values from the server.
         * This is only fired on the client.
         */
        RESTORE,

        /**
         * An explicit reload of a config.
         */
        RELOAD,

        /**
         * Changes made to the config from the ingame config GUI.
         */
        INGAME_CHANGES
    }
}
