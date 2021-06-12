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

import javax.annotation.Nonnull;

public final class NetworkImpl extends NetworkX {

    private static NetworkImpl impl = null;
    
    public NetworkImpl(ModX mod) {
        super(mod);
        if (impl != null) throw new IllegalStateException("NetworkImpl created twice.");
        impl = this;
    }

    @Nonnull
    public static NetworkImpl getImpl() {
        if (impl == null) throw new IllegalStateException("NetworkImpl not yet created.");
        return impl;
    }

    @Override
    protected String getProtocolVersion() {
        return "6";
    }

    @Override
    protected void registerPackets() {
        this.register(new TeUpdateSerializer(), () -> TeUpdateHandler::handle, NetworkDirection.PLAY_TO_CLIENT);
        this.register(new ConfigShadowSerializer(), () -> ConfigShadowHandler::handle, NetworkDirection.PLAY_TO_CLIENT);
       
        this.register(new TeRequestSerializer(), () -> TeRequestHandler::handle, NetworkDirection.PLAY_TO_SERVER);
    }
    
    public void updateTE(World world, BlockPos pos) {
        if (!world.isRemote) {
            this.updateTE(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)), world, pos);
        }
    }

    void updateTE(PacketDistributor.PacketTarget target, World world, BlockPos pos) {
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
            this.instance.send(target, new TeUpdateSerializer.TeUpdateMessage(pos, id, nbt));
        }
    }

    public void requestTE(World world, BlockPos pos) {
        if (world.isRemote) {
            this.instance.sendToServer(new TeRequestSerializer.TeRequestMessage(pos));
        }
    }
}
