package io.github.noeppi_noeppi.libx.impl.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class TeRequestHandler {

    public static void handle(TeRequestSerializer.TeRequestMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null)
                return;
            ServerLevel level = sender.getLevel();
            //noinspection deprecation
            if (level.hasChunkAt(msg.pos)) {
                NetworkImpl.getImpl().updateTE(PacketDistributor.PLAYER.with(() -> sender), level, msg.pos);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
