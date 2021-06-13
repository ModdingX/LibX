package io.github.noeppi_noeppi.libx.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ItemCapabilities {

    /**
     * Creates a new {@link LazyOptional} for an {@link IItemHandlerModifiable}.
     */
    public static LazyOptional<IAdvancedItemHandlerModifiable> create(IItemHandlerModifiable handler) {
        return create(() -> handler);
    }

    /**
     * Creates a new {@link LazyOptional} for an {@link IItemHandlerModifiable}.
     * 
     * @param extract A predicate on whether an item can be extracted through this {@link LazyOptional}. This gets passed the slot to extract from.
     * @param insert A predicate on whether an item can be inserted through this {@link LazyOptional}. This gets passed the slot to insert to and the stack that should be inserted..
     */
    public static LazyOptional<IAdvancedItemHandlerModifiable> create(IItemHandlerModifiable handler, @Nullable Predicate<Integer> extract, @Nullable BiPredicate<Integer, ItemStack> insert) {
        return create(() -> handler, extract, insert);
    }

    /**
     * Creates a new {@link LazyOptional} for an {@link IItemHandlerModifiable}.
     */
    public static LazyOptional<IAdvancedItemHandlerModifiable> create(Supplier<IItemHandlerModifiable> handler) {
        return LazyOptional.of(() -> IAdvancedItemHandlerModifiable.wrap(handler.get()));
    }

    /**
     * Creates a new {@link LazyOptional} for an {@link IItemHandlerModifiable}.
     *
     * @param extract A predicate on whether an item can be extracted through this {@link LazyOptional}. This gets passed the slot to extract from.
     * @param insert A predicate on whether an item can be inserted through this {@link LazyOptional}. This gets passed the slot to insert to and the stack that should be inserted..
     */
    public static LazyOptional<IAdvancedItemHandlerModifiable> create(Supplier<IItemHandlerModifiable> handler, @Nullable Predicate<Integer> extract, @Nullable BiPredicate<Integer, ItemStack> insert) {
        return LazyOptional.of(() -> new WrappedHandler(handler.get(), extract == null ? slot -> true : extract, insert == null ? (slot, stack) -> true : insert));
    }
    
    private static class WrappedHandler implements IAdvancedItemHandlerModifiable {

        private final IItemHandlerModifiable handler;
        private final Predicate<Integer> extract;
        private final BiPredicate<Integer, ItemStack> insert;

        public WrappedHandler(IItemHandlerModifiable handler, Predicate<Integer> extract, BiPredicate<Integer, ItemStack> insert) {
            this.handler = handler;
            this.extract = extract;
            this.insert = insert;
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            this.handler.setStackInSlot(slot, stack);
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
}
