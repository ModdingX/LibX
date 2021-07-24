package io.github.noeppi_noeppi.libx.impl.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import java.util.function.Supplier;

public class BeRequestHandler {

    public static void handle(BeRequestSerializer.BeRequestMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null)
                return;
            ServerLevel level = sender.getLevel();
            //noinspection deprecation
            if (level.hasChunkAt(msg.pos)) {
                NetworkImpl.getImpl().updateBE(PacketDistributor.PLAYER.with(() -> sender), level, msg.pos);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
