package io.github.noeppi_noeppi.libx.impl.network;

import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ConfigShadowHandler {
    
    public static void handle(ConfigShadowSerializer.ConfigShadowMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> msg.config.shadowBy(msg.state));
        ctx.get().setPacketHandled(true);
    }
}
