package io.github.noeppi_noeppi.libx.impl.network;

import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class ConfigShadowHandler {
    
    public static void handle(ConfigShadowSerializer.ConfigShadowMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // If the server sends invalid data. On deserialization, a warning will
            // be printed. So we just ignore that here.
            if (msg.config() != null && msg.state() != null) {
                msg.config().shadowBy(msg.state());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
