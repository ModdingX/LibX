package org.moddingx.libx;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.moddingx.libx.base.tile.BlockEntityBase;
import org.moddingx.libx.impl.network.NetworkImpl;

/**
 * A wrapper for the network implementation of LibX. Allows for some networking functions that
 * are required very often.
 */
public class CommonNetwork {

    private final NetworkImpl network;

    CommonNetwork(NetworkImpl network) {
        this.network = network;
    }

    /**
     * Sends the nbt tag retrieved from {@link BlockEntity#getUpdateTag()} from the block entity at the given
     * position to all clients tracking the chunk. On the client the tag is passed
     * to {@link BlockEntity#handleUpdateTag(CompoundTag)}. Does nothing when called on the client.
     */
    public void updateBE(Level level, BlockPos pos) {
        this.network.updateBE(level, pos);
    }

    /**
     * Requests the block entity at the given position from the server. This is automatically done when
     * a {@link BlockEntityBase} is loaded. The server will
     * send an update packet as described in {@link #updateBE(Level, BlockPos)} to the client.
     * Does nothing when called on the server.
     */
    public void requestBE(Level level, BlockPos pos) {
        this.network.requestBE(level, pos);
    }
}
