package io.github.noeppi_noeppi.libx.impl.network;

import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.network.NetworkX;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;

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
    protected Protocol getProtocol() {
        // Not required on the client, so LibX can be used by client only mods
        return new Protocol("8", ProtocolSide.VANILLA, ProtocolSide.REQUIRED);
    }
    
    // Gets whether a packet can be currently sent.
    // Will return false on clients connected to a non-LibX server
    public boolean canSend() {
        return DistExecutor.unsafeRunForDist(
                () -> () -> Minecraft.getInstance().getConnection() != null && this.channel.isRemotePresent(Minecraft.getInstance().getConnection().getConnection()),
                () -> () -> true
        );
    }

    @Override
    protected void registerPackets() {
        this.register(new BeUpdateSerializer(), () -> BeUpdateHandler::handle, NetworkDirection.PLAY_TO_CLIENT);
        this.register(new ConfigShadowSerializer(), () -> ConfigShadowHandler::handle, NetworkDirection.PLAY_TO_CLIENT);
       
        this.register(new BeRequestSerializer(), () -> BeRequestHandler::handle, NetworkDirection.PLAY_TO_SERVER);
    }
    
    public void updateBE(Level level, BlockPos pos) {
        if (!level.isClientSide && this.canSend()) {
            this.updateBE(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), level, pos);
        }
    }

    void updateBE(PacketDistributor.PacketTarget target, Level level, BlockPos pos) {
        if (!level.isClientSide && this.canSend()) {
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
            this.channel.send(target, new BeUpdateSerializer.BeUpdateMessage(pos, id, nbt));
        }
    }

    public void requestBE(Level level, BlockPos pos) {
        if (level.isClientSide && this.canSend()) {
            this.channel.sendToServer(new BeRequestSerializer.BeRequestMessage(pos));
        }
    }
}
