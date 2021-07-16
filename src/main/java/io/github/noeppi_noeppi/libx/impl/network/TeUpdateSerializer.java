package io.github.noeppi_noeppi.libx.impl.network;

import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class TeUpdateSerializer implements PacketSerializer<TeUpdateSerializer.TeUpdateMessage> {

    @Override
    public Class<TeUpdateMessage> messageClass() {
        return TeUpdateMessage.class;
    }

    @Override
    public void encode(TeUpdateMessage msg, PacketBuffer buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeResourceLocation(msg.id);
        buffer.writeCompoundTag(msg.nbt);
    }

    @Override
    public TeUpdateMessage decode(PacketBuffer buffer) {
        BlockPos pos = buffer.readBlockPos();
        ResourceLocation id = buffer.readResourceLocation();
        CompoundNBT nbt = buffer.readCompoundTag();
        
        return new TeUpdateMessage(pos, id, nbt);
    }

    public static class TeUpdateMessage {

        public BlockPos pos;
        public ResourceLocation id;
        public CompoundNBT nbt;

        public TeUpdateMessage(BlockPos pos, ResourceLocation id, CompoundNBT nbt) {
            this.pos = pos;
            this.id = id;
            this.nbt = nbt;
        }
    }
}
