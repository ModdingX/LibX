package io.github.noeppi_noeppi.libx.impl.network;

import io.github.noeppi_noeppi.libx.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TeUpdateHandler implements NetworkHandler<TeUpdateHandler.TeUpdateMessage> {

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
        TeUpdateMessage msg = new TeUpdateMessage();
        msg.pos = buffer.readBlockPos();
        msg.id = buffer.readResourceLocation();
        msg.nbt = buffer.readCompoundTag();
        return msg;
    }

    @Override
    public void handle(TeUpdateMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world = Minecraft.getInstance().world;
            if (world == null)
                return;
            TileEntity te = world.getTileEntity(msg.pos);
            if (te != null && msg.id.equals(te.getType().getRegistryName())) {
                te.handleUpdateTag(world.getBlockState(msg.pos), msg.nbt);
            }
        });
    }

    public static class TeUpdateMessage {

        public TeUpdateMessage() {
        }

        public TeUpdateMessage(BlockPos pos, ResourceLocation id, CompoundNBT nbt) {
            this.pos = pos;
            this.id = id;
            this.nbt = nbt;
        }

        public BlockPos pos;
        public ResourceLocation id;
        public CompoundNBT nbt;
    }
}
