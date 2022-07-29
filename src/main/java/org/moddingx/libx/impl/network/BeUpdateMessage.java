package org.moddingx.libx.impl.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.function.Supplier;

public record BeUpdateMessage(BlockPos pos, ResourceLocation id, CompoundTag nbt) {
    
    public static class Serializer implements PacketSerializer<BeUpdateMessage> {

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
    }
    
    public static class Handler implements PacketHandler<BeUpdateMessage> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(BeUpdateMessage msg, Supplier<NetworkEvent.Context> ctx) {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                BlockEntity be = level.getBlockEntity(msg.pos());
                if (be != null && msg.id().equals(ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(be.getType()))) {
                    be.handleUpdateTag(msg.nbt());
                }
            }
            return true;
        }
    }
}
