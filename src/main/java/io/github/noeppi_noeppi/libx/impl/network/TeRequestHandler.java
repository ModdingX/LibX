package io.github.noeppi_noeppi.libx.impl.network;

import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.network.NetworkHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class TeRequestHandler implements NetworkHandler<TeRequestHandler.TeRequestMessage> {

    @Override
    public Class<TeRequestMessage> messageClass() {
        return TeRequestMessage.class;
    }

    @Override
    public void encode(TeRequestMessage msg, PacketBuffer buffer) {
        buffer.writeBlockPos(msg.pos);
    }

    @Override
    public TeRequestMessage decode(PacketBuffer buffer) {
        TeRequestMessage msg = new TeRequestMessage();
        msg.pos = buffer.readBlockPos();
        return msg;
    }

    @Override
    public void handle(TeRequestMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.get().getSender();
            if (sender == null)
                return;
            ServerWorld world = sender.getServerWorld();
            //noinspection deprecation
            if (world.isBlockLoaded(msg.pos)) {
                LibX.getNetwork().updateTE(PacketDistributor.PLAYER.with(() -> sender), world, msg.pos);
            }
        });
    }

    public static class TeRequestMessage {

        public TeRequestMessage() {

        }

        public TeRequestMessage(BlockPos pos) {
            this.pos = pos;
        }

        public BlockPos pos;
    }
}
