package io.github.noeppi_noeppi.libx.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps an {@link IItemHandlerModifiable} to a vanilla {@link IInventory}.
 */
public class VanillaWrapper implements IInventory {

    public final IItemHandlerModifiable handler;

    @Nullable
    public final Runnable dirty;

    /**
     * Wraps the given {@link IItemHandlerModifiable} to a vanilla {@link IInventory}.
     *
     * @param dirty A runnable which is always called when {@link IInventory#markDirty() markDirty()}
     *              is called on the vanilla inventory.
     */
    public VanillaWrapper(IItemHandlerModifiable handler, @Nullable Runnable dirty) {
        this.handler = handler;
        this.dirty = dirty;
    }

    @Override
    public int getSizeInventory() {
        return this.handler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < this.handler.getSlots(); slot++) {
            if (!this.handler.getStackInSlot(slot).isEmpty())
                return false;
        }
        return true;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int index) {
        return this.handler.getStackInSlot(index);
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = this.handler.extractItem(index, count, false);
        this.markDirty();
        return stack;
    }

    @Override
    @Nonnull
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = this.handler.getStackInSlot(index).copy();
        this.handler.setStackInSlot(index, ItemStack.EMPTY);
        this.markDirty();
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
        this.handler.setStackInSlot(index, stack);
        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        if (this.dirty != null) {
            this.dirty.run();
        }
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull PlayerEntity player) {
        return true;
    }

    @Override
    public void openInventory(@Nonnull PlayerEntity player) {

    }

    @Override
    public void closeInventory(@Nonnull PlayerEntity player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
        return this.handler.isItemValid(index, stack);
    }

    @Override
    public void clear() {
        for (int i = 0; i < this.handler.getSlots(); i++) {
            this.handler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
}
