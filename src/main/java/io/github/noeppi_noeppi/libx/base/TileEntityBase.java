package io.github.noeppi_noeppi.libx.base;

import com.google.common.collect.ImmutableSet;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.impl.TileEntityUpdateQueue;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * A base class for {@link TileEntity tile entities}. This provides some useful methods for tile entities.
 */
public class TileEntityBase extends TileEntity {

    private final Set<Capability<?>> caps;

    public TileEntityBase(TileEntityType<?> tileEntityTypeIn) {
        this(tileEntityTypeIn, new Capability[0]);
    }

    /**
     * This constructor accepts some capabilities that this tile entity will have. Just make sure that
     * the class also implements the required capability types or you might crash the game.
     */
    public TileEntityBase(TileEntityType<?> tileEntityTypeIn, Capability<?>... caps) {
        super(tileEntityTypeIn);
        this.caps = ImmutableSet.copyOf(caps);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (this.caps.contains(cap)) {
            //noinspection unchecked
            return LazyOptional.of(() -> (T) this);
        } else {
            return super.getCapability(cap, side);
        }
    }

    /**
     * This will update the tile entity when on the client using {@link #getUpdateTag()}
     * and {@link #handleUpdateTag(BlockState, CompoundNBT)}.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (this.world != null && this.pos != null && this.world.isRemote) {
            LibX.getNetwork().requestTE(this.world, this.pos);
        }
    }

    /**
     * This will update the tile entity to all clients that are tracking it when called on the server
     * using {@link #getUpdateTag()} and {@link #handleUpdateTag(BlockState, CompoundNBT)}
     * at the end of this tick.
     */
    public void markDispatchable() {
        if (this.world != null && this.pos != null && !this.world.isRemote) {
            TileEntityUpdateQueue.scheduleUpdate(this.world, this.pos);
        }
    }
}
