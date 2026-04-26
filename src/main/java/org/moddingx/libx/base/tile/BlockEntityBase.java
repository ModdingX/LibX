package org.moddingx.libx.base.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.BlockEntityUpdateQueue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A base class for {@link BlockEntity block entities}. This provides some useful methods.
 */
public class BlockEntityBase extends BlockEntity {

    public BlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void preRemoveSideEffects(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        if (this.level != null && !this.level.isClientSide && state.getBlock() instanceof BlockBE<?> block) {
            if (!block.shouldDropInventory(this.level, pos, state)) {
                return;
            }
            if (this.level.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, this, null) instanceof IItemHandlerModifiable modifiable) {
                for (int i = 0; i < modifiable.getSlots(); i++) {
                    ItemStack stack = modifiable.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        ItemEntity entity = new ItemEntity(this.level, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, stack.copy());
                        this.level.addFreshEntity(entity);
                        modifiable.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
                return;
            }
        }
        super.preRemoveSideEffects(pos, state);
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
