package io.github.noeppi_noeppi.libx.event;

import io.github.noeppi_noeppi.libx.config.ConfigManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

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
    private final Path path;
    
    public ConfigLoadedEvent(ResourceLocation configId, Class<?> configClass, LoadReason reason, boolean clientConfig, Path path) {
        this.configId = configId;
        this.configClass = configClass;
        this.reason = reason;
        this.clientConfig = clientConfig;
        this.path = path;
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
     * Gets the path where the config is stored.
     */
    public Path getPath() {
        return this.path;
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
         * This is the load reason when the values are restored to their
         * state before they were shadowed by values from the server.
         * This is only fired on the client.
         */
        RESTORE,

        /**
         * An explicit reload of a config.
         */
        RELOAD
    }
}
