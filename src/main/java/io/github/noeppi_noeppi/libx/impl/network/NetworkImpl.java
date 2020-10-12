package io.github.noeppi_noeppi.libx.impl.network;

import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.network.NetworkX;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;

public final class NetworkImpl extends NetworkX {

    public NetworkImpl(ModX mod) {
        super(mod);
    }

    @Override
    protected String getProtocolVersion() {
        return "1";
    }

    @Override
    protected void registerPackets() {
        this.register(new TeUpdateHandler(), NetworkDirection.PLAY_TO_CLIENT);
        this.register(new TeRequestHandler(), NetworkDirection.PLAY_TO_SERVER);
    }

    public void updateTE(World world, BlockPos pos) {
        if (!world.isRemote) {
            this.updateTE(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)), world, pos);
        }
    }

    protected void updateTE(PacketDistributor.PacketTarget target, World world, BlockPos pos) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te == null)
                return;
            CompoundNBT nbt = te.getUpdateTag();
            //noinspection ConstantConditions
            if (nbt == null)
                return;
            ResourceLocation id = te.getType().getRegistryName();
            if (id == null)
                return;
            this.instance.send(target, new TeUpdateHandler.TeUpdateMessage(pos, id, nbt));
        }
    }

    public void requestTE(World world, BlockPos pos) {
        if (world.isRemote) {
            this.instance.sendToServer(new TeRequestHandler.TeRequestMessage(pos));
        }
    }
}
