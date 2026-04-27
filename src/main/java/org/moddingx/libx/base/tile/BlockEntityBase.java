package org.moddingx.libx.base.tile;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.BlockEntityUpdateQueue;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A base class for {@link BlockEntity block entities}. This provides some useful methods.
 */
public class BlockEntityBase extends BlockEntity {

    protected static final Logger LOGGER = LogUtils.getLogger();

    public BlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void preRemoveSideEffects(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        if (this.level != null && !this.level.isClientSide() && state.getBlock() instanceof BlockBE<?> block) {
            if (!block.shouldDropInventory(this.level, pos, state)) {
                return;
            }
            ResourceHandler<ItemResource> handler = this.level.getCapability(Capabilities.Item.BLOCK, pos, state, this, null);
            if (handler != null) {
                try (Transaction transaction = Transaction.openRoot()) {
                    for (int i = 0; i < handler.size(); i++) {
                        ItemResource resource = handler.getResource(i);
                        int amount = handler.getAmountAsInt(i);
                        if (!resource.isEmpty()) {
                            int extracted = handler.extract(i, resource, amount, transaction);
                            if (extracted > 0) {
                                ItemEntity entity = new ItemEntity(this.level, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, resource.toStack(extracted));
                                this.level.addFreshEntity(entity);
                            }
                        }
                    }
                    transaction.commit();
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

    @Nonnull
    @Override
    public CompoundTag getUpdateTag(@Nonnull HolderLookup.Provider registries) {
        return this.saveWithFullMetadata(registries);
    }

    /**
     * If the block entity is loaded on the logical client, this will update the block entity using
     * {@link #getUpdateTag(HolderLookup.Provider)} and {@link #handleUpdateTag(ValueInput)}.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && this.level.isClientSide()) {
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
     * {@link #handleUpdateTag(ValueInput)} at the end of the current tick.
     */
    public void setDispatchable() {
        if (this.level != null && !this.level.isClientSide()) {
            BlockEntityUpdateQueue.scheduleUpdate(this.level, this.worldPosition);
        }
    }
}
