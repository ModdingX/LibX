package io.github.noeppi_noeppi.libx.impl.network;

import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.network.NetworkX;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

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
        this.register(new BeUpdateSerializer(), () -> BeUpdateHandler::handle, NetworkDirection.PLAY_TO_CLIENT);
        this.register(new ConfigShadowSerializer(), () -> ConfigShadowHandler::handle, NetworkDirection.PLAY_TO_CLIENT);
       
        this.register(new BeRequestSerializer(), () -> BeRequestHandler::handle, NetworkDirection.PLAY_TO_SERVER);
    }
    
    public void updateBE(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            this.updateBE(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), level, pos);
        }
    }

    void updateBE(PacketDistributor.PacketTarget target, Level level, BlockPos pos) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null)
                return;
            CompoundTag nbt = be.getUpdateTag();
            //noinspection ConstantConditions
            if (nbt == null)
                return;
            ResourceLocation id = be.getType().getRegistryName();
            if (id == null)
                return;
            this.instance.send(target, new BeUpdateSerializer.BeUpdateMessage(pos, id, nbt));
        }
    }

    public void requestBE(Level level, BlockPos pos) {
        if (level.isClientSide) {
            this.instance.sendToServer(new BeRequestSerializer.BeRequestMessage(pos));
        }
    }
}
