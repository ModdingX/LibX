package io.github.noeppi_noeppi.libx;

import io.github.noeppi_noeppi.libx.impl.network.NetworkImpl;
import io.github.noeppi_noeppi.libx.base.TileEntityBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
     * Sends the nbt tag retrieved from {@link TileEntity#getUpdateTag} from the tile entity at the given
     * position to all clients tracking the chunk. On the client the tag is passed
     * to {@link TileEntity#handleUpdateTag}. Does nothing when called on the client.
     */
    public void updateTE(World world, BlockPos pos) {
        this.network.updateTE(world, pos);
    }

    /**
     * Requests the tile entity at the given position from the server. This is automatically done when
     * a {@link TileEntityBase} is loaded. The server will
     * send an update packet as described in {@link NetworkImpl#updateTE(World, BlockPos)} to the client.
     * Does nothing when called on the server.
     */
    public void requestTE(World world, BlockPos pos) {
        this.network.requestTE(world, pos);
    }
}
