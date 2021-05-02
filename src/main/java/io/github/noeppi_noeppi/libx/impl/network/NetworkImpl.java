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

/**
 * The network implementation of LibX. Allows for some networking functions that are required very often.
 */
public final class NetworkImpl extends NetworkX {

    public NetworkImpl(ModX mod) {
        super(mod);
    }

    @Override
    protected String getProtocolVersion() {
        return "4";
    }

    @Override
    protected void registerPackets() {
        this.register(new TeUpdateSerializer(), () -> TeUpdateHandler::handle, NetworkDirection.PLAY_TO_CLIENT);
        this.register(new ConfigShadowSerializer(), () -> ConfigShadowHandler::handle, NetworkDirection.PLAY_TO_CLIENT);
       
        this.register(new TeRequestSerializer(), () -> TeRequestHandler::handle, NetworkDirection.PLAY_TO_SERVER);
    }

    /**
     * Sends the nbt tag retrieved from {@code TileEntity#getUpdateTag} from the tile entity at the given
     * position to all clients tracking the chunk. On the client the tag is passed
     * to {@code TileEntity#handleUpdateTag}. Does nothing when called on the client.
     */
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

    /**
     * Requests the tile entity at the given position from the server. This is automatically done when
     * a {@link io.github.noeppi_noeppi.libx.mod.registration.TileEntityBase} is loaded. The server will
     * send an update packet as described in {@link NetworkImpl#updateTE(World, BlockPos)} to the client.
     * Does nothing when called on the server.
     */
    public void requestTE(World world, BlockPos pos) {
        if (world.isRemote) {
            this.instance.sendToServer(new TeRequestSerializer.TeRequestMessage(pos));
        }
    }
}
