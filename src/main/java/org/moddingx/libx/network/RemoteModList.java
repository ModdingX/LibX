package org.moddingx.libx.network;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeI18n;
import net.minecraftforge.network.ConnectionData;
import net.minecraftforge.network.NetworkHooks;

/**
 * Allows to query information about mods, players have installed on the server side.
 */
public class RemoteModList {

    /**
     * Gets whether a player has installed a certain mod.
     */
    public static boolean hasMod(ServerPlayer player, String modid) {
        ConnectionData data = NetworkHooks.getConnectionData(player.connection.connection);
        return data != null && data.getModList().contains(modid);
    }
    
    /**
     * Creates a {@link MutableComponent} from a translation key.
     * If the given player has the given mod installed, it will result in a {@link TranslatableComponent}.
     * If the player does not have the mod installed, the key is translated on the server and the result
     * is a {@link TextComponent} with the translated text in english.
     */
    public static MutableComponent translate(ServerPlayer player, String modid, String translationKey, Object... args) {
        if (hasMod(player, modid)) {
            return new TranslatableComponent(translationKey, args);
        } else {
            return new TextComponent(String.format(ForgeI18n.getPattern(translationKey), args));
        }
    }
}
