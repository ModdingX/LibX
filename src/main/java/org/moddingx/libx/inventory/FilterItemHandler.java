package org.moddingx.libx.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * A simple wrapper around an {@link IItemHandler} that limits the possibility to insert or extract items. This is
 * especially useful for {@link Capabilities.ItemHandler item handler capabilities}.
 */
public class FilterItemHandler implements IAdvancedItemHandler {

    private final IItemHandler handler;
    private final Predicate<Integer> extract;
    private final BiPredicate<Integer, ItemStack> insert;

    /**
     * Creates a new {@link FilterItemHandler}.
     * 
     * @param handler The {@link IItemHandler} this {@link FilterItemHandler} wraps around.
     * @param extract A predicate that tests whether extraction should be allowed from the provided slot.
     * @param insert A predicate that tests whether insertion of the provided item should be allowed into the provided slot.
     */
    public FilterItemHandler(IItemHandler handler, @Nullable Predicate<Integer> extract, @Nullable BiPredicate<Integer, ItemStack> insert) {
        this.handler = handler;
        this.extract = extract == null ? slot -> true : extract;
        this.insert = insert == null ? (slot, stack) -> true : insert;
    }

    @Override
    public int getSlots() {
        return this.handler.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.handler.getStackInSlot(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return this.insert.test(slot, stack) ? this.handler.insertItem(slot, stack, simulate) : stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.extract.test(slot) ? this.handler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.handler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return this.insert.test(slot, stack) && this.handler.isItemValid(slot, stack);
    }
}
