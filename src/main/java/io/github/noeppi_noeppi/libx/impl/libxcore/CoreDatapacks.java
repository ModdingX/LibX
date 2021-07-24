package io.github.noeppi_noeppi.libx.impl.libxcore;

import io.github.noeppi_noeppi.libx.event.DatapacksReloadedEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.common.MinecraftForge;

public class CoreDatapacks {

    /**
     * Patched into {@link PlayerList#reloadResources()} after the call to
     * {@link PlayerList#broadcastAll(Packet)} passing the {@code this}
     * reference.
     */
    public static void fireReload(PlayerList list) {
        MinecraftServer server = list.getServer();
        //noinspection ConstantConditions
        if (server != null && server.getServerResources() != null) {
            MinecraftForge.EVENT_BUS.post(new DatapacksReloadedEvent(server, server.getServerResources()));
        }
    }
}
