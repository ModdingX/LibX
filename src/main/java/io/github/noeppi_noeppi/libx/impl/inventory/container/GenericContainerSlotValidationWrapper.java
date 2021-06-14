package io.github.noeppi_noeppi.libx.impl.inventory.container;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiPredicate;

// Used to provide a validity check for items when using a fake handler
// on the client.
public class GenericContainerSlotValidationWrapper implements IItemHandlerModifiable {

    private final IItemHandlerModifiable handler;
    @Nullable
    private final BiPredicate<Integer, ItemStack> validator;
    @Nullable // Null on server side as we have access to the method directly.
    private final int[] slotLimits;

    public GenericContainerSlotValidationWrapper(IItemHandlerModifiable handler, @Nullable BiPredicate<Integer, ItemStack> validator, @Nullable int[] slotLimits) {
        this.handler = handler;
        this.validator = validator;
        this.slotLimits = slotLimits;
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
        return this.handler.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.handler.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        if (this.slotLimits != null && slot >= 0 && slot < this.slotLimits.length) {
            return this.slotLimits[slot];
        } else {
            return this.handler.getSlotLimit(slot);
        }
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return this.validator == null || this.validator.test(slot, stack);
    }
}
