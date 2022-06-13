package org.moddingx.libx.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * Utilities to send messages to a set of {@link Player players} on the {@link MinecraftServer server}.
 */
public class ServerMessages {

    /**
     * Sends a {@link Component text component} to every {@link Player player} on the server.
     */
    public static void broadcast(Level level, Component message) {
        broadcastTo(level, p -> true, message);
    }

    /**
     * Sends a {@link Component text component} to every {@link Player player} on the server except one.
     */
    public static void broadcastExcept(Level level, Player exclude, Component message) {
        UUID uid = exclude.getGameProfile().getId();
        broadcastTo(level, p -> !uid.equals(p.getGameProfile().getId()), message);
    }
    
    /**
     * Sends a {@link Component text component} to all {@link Player player} matching a predicate.
     */
    public static void broadcastTo(Level level, Predicate<? super ServerPlayer> condition, Component message) {
        MinecraftServer server = level.getServer();
        if (server != null) {
            server.getPlayerList().getPlayers().forEach(player -> {
                if (condition.test(player)) {
                    player.sendSystemMessage(message);
                }
            });
        }
    }
}
