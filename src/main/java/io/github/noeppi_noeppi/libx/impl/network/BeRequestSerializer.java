package io.github.noeppi_noeppi.libx.impl.network;

import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class BeRequestSerializer implements PacketSerializer<BeRequestSerializer.BeRequestMessage> {

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

    public record BeRequestMessage(BlockPos pos) {}
}
