package org.moddingx.libx.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.moddingx.libx.codec.MoreStreamCodecs;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An {@link IItemHandlerModifiable} with a {@link MutableDataComponentHolder} as backend.
 */
public class StackItemHandler implements IAdvancedItemHandlerModifiable {

    public static final DataComponentType<NonNullList<ItemStack>> INVENTORY_DATA = new DataComponentType.Builder<NonNullList<ItemStack>>()
            .persistent(NonNullList.codecOf(ItemStack.OPTIONAL_CODEC))
            .networkSynchronized(MoreStreamCodecs.listOf(ItemStack.OPTIONAL_STREAM_CODEC).map(NonNullList::copyOf, Function.identity()))
            .build();
    
    private final int size;
    protected final MutableDataComponentHolder dataComponentHolder;

    public StackItemHandler(int size, MutableDataComponentHolder dataComponentHolder) {
        this.size = size;
        this.dataComponentHolder = dataComponentHolder;
    }

    @Override
    public int getSlots() {
        return this.size;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        this.validateSlotIndex(slot);
        NonNullList<ItemStack> stacks = this.getContents();
        if (slot < stacks.size()) return stacks.get(slot);
        else return ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        this.validateSlotIndex(slot);
        NonNullList<ItemStack> newStacks = this.copyContentsPossiblyEnlarged(this.getContents());
        newStacks.set(slot, stack.copy());
        this.dataComponentHolder.set(INVENTORY_DATA, newStacks);
    }

    @Override
    public void clear() {
        this.clear(stack -> true);
    }

    @Override
    public int clear(Predicate<ItemStack> predicate) {
        NonNullList<ItemStack> newStacks = this.copyContentsPossiblyEnlarged(this.getContents());
        int cleared = 0;
        for (int i = 0; i < this.size; i ++) {
            ItemStack stack = newStacks.get(i);
            if (predicate.test(stack)) {
                cleared += stack.getCount();
                newStacks.set(i, ItemStack.EMPTY);
            }
        }
        this.dataComponentHolder.set(INVENTORY_DATA, newStacks);
        return cleared;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        this.validateSlotIndex(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (!this.isItemValid(slot, stack)) return stack;
        
        NonNullList<ItemStack> oldStacks = this.getContents();
        ItemStack existing = slot < oldStacks.size() ? oldStacks.get(slot) : ItemStack.EMPTY;
        int limit = Math.min(this.getSlotLimit(slot), stack.getMaxStackSize());

        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing)) return stack;
            limit -= existing.getCount();
        }

        int insertAmount = Math.min(stack.getCount(), limit);
        if (insertAmount <= 0) return stack;
        
        ItemStack newStack = stack.copyWithCount(existing.getCount() + insertAmount);
        ItemStack remainder = stack.copyWithCount(stack.getCount() - insertAmount);
        
        if (!simulate) this.setStackInSlot(slot, newStack);
        return remainder;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        this.validateSlotIndex(slot);
        
        NonNullList<ItemStack> oldStacks = this.getContents();
        ItemStack existing = slot < oldStacks.size() ? oldStacks.get(slot) : ItemStack.EMPTY;
        if (existing.isEmpty()) return ItemStack.EMPTY;
        int extractAmount = Math.min(existing.getCount(), amount);
        
        ItemStack extractStack = existing.copyWithCount(extractAmount);
        ItemStack remainder = existing.copyWithCount(existing.getCount() - extractAmount);
        
        if (!simulate) this.setStackInSlot(slot, remainder);
        return extractStack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Item.DEFAULT_MAX_STACK_SIZE;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return true;
    }

    private NonNullList<ItemStack> getContents() {
        return this.dataComponentHolder.getOrDefault(INVENTORY_DATA, NonNullList.create());
    }

    private void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= this.size) {
            throw new IllegalArgumentException("Slot " + slot + " not in valid range - [0," + this.size + ")");
        }
    }
    
    private NonNullList<ItemStack> copyContentsPossiblyEnlarged(NonNullList<ItemStack> oldStacks) {
        NonNullList<ItemStack> newStacks = NonNullList.withSize(Math.max(this.size, oldStacks.size()), ItemStack.EMPTY);
        for (int i = 0; i < oldStacks.size(); i++) {
            newStacks.set(i, oldStacks.get(i).copy());
        }
        return newStacks;
    }
}
