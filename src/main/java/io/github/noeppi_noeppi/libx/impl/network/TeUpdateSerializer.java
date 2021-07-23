package io.github.noeppi_noeppi.libx.impl.network;

import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

public class TeUpdateSerializer implements PacketSerializer<TeUpdateSerializer.TeUpdateMessage> {

    @Override
    public Class<TeUpdateMessage> messageClass() {
        return TeUpdateMessage.class;
    }

    @Override
    public void encode(TeUpdateMessage msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeResourceLocation(msg.id);
        buffer.writeNbt(msg.nbt);
    }

    @Override
    public TeUpdateMessage decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        ResourceLocation id = buffer.readResourceLocation();
        CompoundTag nbt = buffer.readNbt();
        
        return new TeUpdateMessage(pos, id, nbt);
    }

    public static class TeUpdateMessage {

        public BlockPos pos;
        public ResourceLocation id;
        public CompoundTag nbt;

        public TeUpdateMessage(BlockPos pos, ResourceLocation id, CompoundTag nbt) {
            this.pos = pos;
            this.id = id;
            this.nbt = nbt;
        }
    }
}
