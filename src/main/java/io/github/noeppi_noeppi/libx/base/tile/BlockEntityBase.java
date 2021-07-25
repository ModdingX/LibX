package io.github.noeppi_noeppi.libx.base.tile;

import com.google.common.collect.ImmutableSet;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.impl.BlockEntityUpdateQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * A base class for {@link BlockEntity block entities}. This provides some useful methods.
 */
public class BlockEntityBase extends BlockEntity {

    private final Set<Capability<?>> caps;

    public BlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, new Capability[0]);
    }

    /**
     * This constructor accepts some capabilities that this block entity will have. Just make sure that
     * the class also implements the required capability types or you might crash the game.
     */
    public BlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state, Capability<?>... caps) {
        super(type, pos, state);
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
     * This will update the block entity when on the client using {@link #getUpdateTag()}
     * and {@link #handleUpdateTag(CompoundTag)}.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && this.level.isClientSide) {
            LibX.getNetwork().requestBE(this.level, this.worldPosition);
        }
    }

    /**
     * This will update the block entity to all clients that are tracking it when called on the server
     * using {@link #getUpdateTag()} and {@link #handleUpdateTag(CompoundTag)} at the end of this tick.
     */
    public void markDispatchable() {
        if (this.level != null && !this.level.isClientSide) {
            BlockEntityUpdateQueue.scheduleUpdate(this.level, this.worldPosition);
        }
    }
}
