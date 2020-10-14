package io.github.noeppi_noeppi.libx.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.UUID;

public class ServerMessages {

    /**
     * Sends a TextComponent to every player on the server.
     */
    public static void broadcast(World world, ITextComponent message) {
        MinecraftServer server = world.getServer();
        if (server != null) {
            server.getPlayerList().getPlayers().forEach(player -> player.sendMessage(message, player.getUniqueID()));
        }
    }

    /**
     * Sends a TextComponent to every player on the server except one.
     */
    public static void broadcastExcept(World world, PlayerEntity exclude, ITextComponent message) {
        UUID uid = exclude.getGameProfile().getId();
        MinecraftServer server = world.getServer();
        if (server != null) {
            server.getPlayerList().getPlayers().forEach(player -> {
                if (!uid.equals(player.getGameProfile().getId()))
                    player.sendMessage(message, player.getUniqueID());
            });
        }
    }
}
