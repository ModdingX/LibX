package io.github.noeppi_noeppi.libx.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * Rich interface that provides some default methods to an {@link IItemHandlerModifiable}.
 * Just implement this together with {@link IItemHandlerModifiable}.
 */
public interface IAdvancedItemHandlerModifiable extends IItemHandlerModifiable, IAdvancedItemHandler {

    /**
     * Clears all slots from the inventory.
     */
    default void clear() {
        for (int slot = 0; slot < this.getSlots(); slot++) {
            this.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }
    
    /**
     * Clears all stacks from the inventory that match a predicate.
     * 
     * @return The amount of items cleared.
     */
    default int clear(Predicate<ItemStack> predicate) {
        int amount = 0;
        for (int slot = 0; slot < this.getSlots(); slot++) {
            ItemStack stack = this.getStackInSlot(slot);
            if (predicate.test(stack)) {
                amount += stack.getCount();
                this.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        return amount;
    }

    /**
     * Creates a new IAdvancedItemHandlerModifiable from an {@link IItemHandlerModifiable}.
     */
    static IAdvancedItemHandlerModifiable wrap(IItemHandlerModifiable handler) {
        if (handler instanceof IAdvancedItemHandlerModifiable advanced) {
            return advanced;
        } else {
            return new IAdvancedItemHandlerModifiable() {

                @Override
                public int getSlots() {
                    return handler.getSlots();
                }

                @Nonnull
                @Override
                public ItemStack getStackInSlot(int slot) {
                    return handler.getStackInSlot(slot);
                }

                @Nonnull
                @Override
                public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                    return handler.insertItem(slot, stack, simulate);
                }

                @Nonnull
                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    return handler.extractItem(slot, amount, simulate);
                }

                @Override
                public int getSlotLimit(int slot) {
                    return handler.getSlotLimit(slot);
                }

                @Override
                public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                    return handler.isItemValid(slot, stack);
                }

                @Override
                public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
                    handler.setStackInSlot(slot, stack);
                }
            };
        }
    }
}
