package io.github.noeppi_noeppi.libx.impl.network;

import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

public class BeUpdateSerializer implements PacketSerializer<BeUpdateSerializer.BeUpdateMessage> {

    @Override
    public Class<BeUpdateMessage> messageClass() {
        return BeUpdateMessage.class;
    }

    @Override
    public void encode(BeUpdateMessage msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeResourceLocation(msg.id);
        buffer.writeNbt(msg.nbt);
    }

    @Override
    public BeUpdateMessage decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        ResourceLocation id = buffer.readResourceLocation();
        CompoundTag nbt = buffer.readNbt();
        
        return new BeUpdateMessage(pos, id, nbt);
    }

    public record BeUpdateMessage(BlockPos pos, ResourceLocation id, CompoundTag nbt) {}
}
