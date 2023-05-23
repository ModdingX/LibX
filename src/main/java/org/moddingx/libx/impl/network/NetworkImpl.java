package org.moddingx.libx.impl.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.network.NetworkX;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
        return new Protocol("10", ProtocolSide.VANILLA, ProtocolSide.REQUIRED);
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
        this.registerGame(NetworkDirection.PLAY_TO_CLIENT, new BeUpdateMessage.Serializer(), () -> BeUpdateMessage.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_CLIENT, new ConfigShadowMessage.Serializer(), () -> ConfigShadowMessage.Handler::new);
       
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new BeRequestMessage.Serializer(), () -> BeRequestMessage.Handler::new);
    }
    
    public void updateBE(Level level, BlockPos pos) {
        BeUpdateMessage msg = this.getBeUpdateMessage(level, pos);
        if (msg != null) {
            this.channel.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), msg);
        }
    }
    
    void updateBE(NetworkEvent.Context context, Level level, BlockPos pos) {
        BeUpdateMessage msg = this.getBeUpdateMessage(level, pos);
        if (msg != null) {
            this.channel.reply(msg, context);
        }
    }

    @Nullable
    private BeUpdateMessage getBeUpdateMessage(Level level, BlockPos pos) {
        if (!level.isClientSide && this.canSend()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null) return null;
            CompoundTag nbt = be.getUpdateTag();
            //noinspection ConstantConditions
            if (nbt == null) return null;
            ResourceLocation id = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(be.getType());
            if (id == null) return null;
            return new BeUpdateMessage(pos, id, nbt);
        } else {
            return null;
        }
    }

    public void requestBE(Level level, BlockPos pos) {
        if (level.isClientSide && this.canSend()) {
            this.channel.sendToServer(new BeRequestMessage(pos));
        }
    }
}
