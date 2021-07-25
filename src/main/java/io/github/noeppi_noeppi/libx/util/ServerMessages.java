package io.github.noeppi_noeppi.libx.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class ServerMessages {

    /**
     * Sends a {@link Component text component} to every player on the server.
     */
    public static void broadcast(Level level, Component message) {
        MinecraftServer server = level.getServer();
        if (server != null) {
            server.getPlayerList().getPlayers().forEach(player -> player.sendMessage(message, player.getUUID()));
        }
    }

    /**
     * Sends a {@link Component text component} to every player on the server except one.
     */
    public static void broadcastExcept(Level level, Player exclude, Component message) {
        UUID uid = exclude.getGameProfile().getId();
        MinecraftServer server = level.getServer();
        if (server != null) {
            server.getPlayerList().getPlayers().forEach(player -> {
                if (!uid.equals(player.getGameProfile().getId()))
                    player.sendMessage(message, player.getUUID());
            });
        }
    }
}
