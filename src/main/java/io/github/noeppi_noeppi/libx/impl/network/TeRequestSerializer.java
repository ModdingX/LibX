package io.github.noeppi_noeppi.libx.impl.network;

import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

public class TeRequestSerializer implements PacketSerializer<TeRequestSerializer.TeRequestMessage> {

    @Override
    public Class<TeRequestMessage> messageClass() {
        return TeRequestMessage.class;
    }

    @Override
    public void encode(TeRequestMessage msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
    }

    @Override
    public TeRequestMessage decode(FriendlyByteBuf buffer) {
        return new TeRequestMessage(buffer.readBlockPos());
    }

    public static class TeRequestMessage {

        public BlockPos pos;

        public TeRequestMessage(BlockPos pos) {
            this.pos = pos;
        }
    }
}
