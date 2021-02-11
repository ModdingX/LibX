package io.github.noeppi_noeppi.libx.event;

import net.minecraft.resources.DataPackRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired on the server after datapacks have been reloaded.
 * This event is fired on the MinecraftForge.EVENT_BUS
 * This event may be fired on the Server Thread, or an async reloader thread.
 * <b>This event is NOT fired on client or on server startup, only after a
 * reload via the {@code reload} or the {@code datapack} command.</b>
 * This event is not Cancelable and has no Result.
 */
public class DatapacksReloadedEvent extends Event {
    
    private final MinecraftServer server;
    private final DataPackRegistries datapacks;

    public DatapacksReloadedEvent(MinecraftServer server, DataPackRegistries datapacks) {
        this.server = server;
        this.datapacks = datapacks;
        System.out.println("RELOADED");
    }

    /**
     * Gets the MinecraftServer whose datapacks were reloaded.
     */
    public MinecraftServer getServer() {
        return this.server;
    }

    /**
     * Gets the new datapack registries.
     */
    public DataPackRegistries getDatapacks() {
        return this.datapacks;
    }
}
