package org.moddingx.libx.impl.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.moddingx.libx.impl.config.ConfigImpl;
import org.moddingx.libx.impl.config.ConfigState;
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
        
        this.registerOptional(new ConfigShadowHandler());
        this.registerOptional(new BeRequestHandler());
        this.registerOptional(new BeUpdateHandler());
    }

    @Nonnull
    public static NetworkImpl getImpl() {
        if (impl == null) throw new IllegalStateException("NetworkImpl not yet created.");
        return impl;
    }

    @Override
    protected String getVersion() {
        return "11";
    }

    public void requestBE(Level level, BlockPos pos) {
        if (level.isClientSide && this.canSend(BeRequestHandler.TYPE)) {
            ClientPacketDistributor.sendToServer(new BeRequestHandler.Message(pos));
        }
    }
    
    public void updateBE(ServerLevel level, BlockPos pos) {
        BeUpdateHandler.Message msg = this.getBeUpdateMessage(level, pos);
        if (msg != null) {
            // We don't use PacketDistributor.sendToPlayersTrackingChunk here because our payload is optional
            for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false)) {
                if (this.canSend(player, BeUpdateHandler.TYPE)) {
                    PacketDistributor.sendToPlayer(player, msg);
                }
            }
        }
    }
    
    public void syncConfig(MinecraftServer server, @Nullable ServerPlayer receiver, ConfigImpl config, ConfigState state) {
        ConfigShadowHandler.Message msg = new ConfigShadowHandler.Message(config, state);
        if (receiver != null) {
            if (this.canSend(receiver, ConfigShadowHandler.TYPE)) {
                PacketDistributor.sendToPlayer(receiver, msg);
            }
        } else {
            // We don't use PacketDistributor.sendToAllPlayers here because our payload is optional
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (this.canSend(player, BeUpdateHandler.TYPE)) {
                    player.connection.send(msg);
                }
            }
        }
    }

    @Nullable
    BeUpdateHandler.Message getBeUpdateMessage(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null) return null;
            CompoundTag nbt = be.getUpdateTag(level.registryAccess());
            //noinspection ConstantConditions
            if (nbt == null) return null;
            ResourceLocation id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType());
            if (id == null) return null;
            return new BeUpdateHandler.Message(pos, id, nbt);
        } else {
            return null;
        }
    }
}
