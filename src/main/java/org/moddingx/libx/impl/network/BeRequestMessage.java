package org.moddingx.libx.impl.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.function.Supplier;

public record BeRequestMessage(BlockPos pos) {
    
    public static class Serializer implements PacketSerializer<BeRequestMessage> {

        @Override
        public Class<BeRequestMessage> messageClass() {
            return BeRequestMessage.class;
        }

        @Override
        public void encode(BeRequestMessage msg, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(msg.pos);
        }

        @Override
        public BeRequestMessage decode(FriendlyByteBuf buffer) {
            return new BeRequestMessage(buffer.readBlockPos());
        }
    }
    
    public static class Handler implements PacketHandler<BeRequestMessage> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(BeRequestMessage msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                ServerLevel level = sender.getLevel();
                //noinspection deprecation
                if (level.hasChunkAt(msg.pos())) {
                    NetworkImpl.getImpl().updateBE(ctx.get(), level, msg.pos());
                }
            }
            return true;
        }
    }
}
