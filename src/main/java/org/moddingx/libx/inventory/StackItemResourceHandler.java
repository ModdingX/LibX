package org.moddingx.libx.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.moddingx.libx.codec.MoreStreamCodecs;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class StackItemResourceHandler extends SnapshotJournal<NonNullList<ItemStack>> implements ResourceHandler<ItemResource> {

    public static final DataComponentType<NonNullList<ItemStack>> INVENTORY_DATA = new DataComponentType.Builder<NonNullList<ItemStack>>()
            .persistent(NonNullList.codecOf(ItemStack.OPTIONAL_CODEC))
            .networkSynchronized(MoreStreamCodecs.listOf(ItemStack.OPTIONAL_STREAM_CODEC).map(NonNullList::copyOf, Function.identity()))
            .build();

    private final int size;
    private final MutableDataComponentHolder dataComponentHolder;

    public StackItemResourceHandler(int size, MutableDataComponentHolder dataComponentHolder) {
        this.size = size;
        this.dataComponentHolder = dataComponentHolder;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public ItemResource getResource(int slot) {
        return ItemResource.of(this.getStackInSlot(slot));
    }

    @Override
    public long getAmountAsLong(int slot) {
        return this.getStackInSlot(slot).getCount();
    }

    @Override
    public long getCapacityAsLong(int slot, ItemResource resource) {
        this.validateSlotIndex(slot);
        if (!resource.isEmpty() && !this.isValid(slot, resource)) {
            return 0;
        }
        return resource.isEmpty() ? Item.DEFAULT_MAX_STACK_SIZE : Math.min(Item.DEFAULT_MAX_STACK_SIZE, resource.getMaxStackSize());
    }

    @Override
    public boolean isValid(int slot, ItemResource resource) {
        this.validateSlotIndex(slot);
        return !resource.isEmpty();
    }

    @Override
    public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
        this.validateSlotIndex(slot);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        ItemStack existing = this.getStackInSlot(slot);
        if (!existing.isEmpty() && !resource.matches(existing)) {
            return 0;
        }

        int inserted = Math.min(amount, this.getCapacityAsInt(slot, resource) - existing.getCount());
        if (inserted <= 0) {
            return 0;
        }

        this.updateSnapshots(transaction);
        this.setStackInSlot(slot, resource.toStack(existing.getCount() + inserted));
        return inserted;
    }

    @Override
    public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
        this.validateSlotIndex(slot);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        ItemStack existing = this.getStackInSlot(slot);
        if (!resource.matches(existing)) {
            return 0;
        }

        int extracted = Math.min(existing.getCount(), amount);
        if (extracted <= 0) {
            return 0;
        }

        this.updateSnapshots(transaction);
        this.setStackInSlot(slot, existing.copyWithCount(existing.getCount() - extracted));
        return extracted;
    }

    /**
     * Directly sets the slot content without a transaction (for use in clear operations).
     */
    public void set(int slot, ItemResource resource, int amount) {
        this.setStackInSlot(slot, resource.isEmpty() ? ItemStack.EMPTY : resource.toStack(amount));
    }

    @Override
    protected NonNullList<ItemStack> createSnapshot() {
        return this.copyContentsPossiblyEnlarged(this.getContents());
    }

    @Override
    protected void revertToSnapshot(NonNullList<ItemStack> snapshot) {
        this.dataComponentHolder.set(INVENTORY_DATA, snapshot);
    }

    @Nonnull
    protected ItemStack getStackInSlot(int slot) {
        this.validateSlotIndex(slot);
        NonNullList<ItemStack> stacks = this.getContents();
        if (slot < stacks.size()) {
            return stacks.get(slot);
        } else {
            return ItemStack.EMPTY;
        }
    }

    protected void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        this.validateSlotIndex(slot);
        NonNullList<ItemStack> newStacks = this.copyContentsPossiblyEnlarged(this.getContents());
        newStacks.set(slot, stack.copy());
        this.dataComponentHolder.set(INVENTORY_DATA, newStacks);
    }

    private NonNullList<ItemStack> getContents() {
        return this.dataComponentHolder.getOrDefault(INVENTORY_DATA, NonNullList.create());
    }

    private NonNullList<ItemStack> copyContentsPossiblyEnlarged(NonNullList<ItemStack> oldStacks) {
        NonNullList<ItemStack> newStacks = NonNullList.withSize(Math.max(this.size, oldStacks.size()), ItemStack.EMPTY);
        for (int i = 0; i < oldStacks.size(); i++) {
            newStacks.set(i, oldStacks.get(i).copy());
        }
        return newStacks;
    }

    private void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= this.size) {
            throw new IllegalArgumentException("Slot " + slot + " not in valid range - [0," + this.size + ")");
        }
    }
}
