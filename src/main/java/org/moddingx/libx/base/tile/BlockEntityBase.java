package org.moddingx.libx.base.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.BlockEntityUpdateQueue;

import javax.annotation.Nullable;

/**
 * A base class for {@link BlockEntity block entities}. This provides some useful methods.
 */
public class BlockEntityBase extends BlockEntity {

    public BlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Nullable
    public final <T, C> T getCapability(BlockCapability<T, C> capability, C context) {
        if (this.level == null) return null;
        return this.level.getCapability(capability, this.getBlockPos(), this.getBlockState(), this, context);
    }

    /**
     * If the block entity is loaded on the logical client, this will update the block entity using
     * {@link #getUpdateTag(HolderLookup.Provider)} and {@link #handleUpdateTag(CompoundTag, HolderLookup.Provider)}.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && this.level.isClientSide) {
            LibX.getNetwork().requestBE(this.level, this.worldPosition);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null) {
            this.level.invalidateCapabilities(this.getBlockPos());
        }
    }

    /**
     * When called on the logical server, this will update the block entity to all clients that are
     * tracking it using {@link #getUpdateTag(HolderLookup.Provider)} and
     * {@link #handleUpdateTag(CompoundTag, HolderLookup.Provider)} at the end of the current tick.
     */
    public void setDispatchable() {
        if (this.level != null && !this.level.isClientSide) {
            BlockEntityUpdateQueue.scheduleUpdate(this.level, this.worldPosition);
        }
    }
}
