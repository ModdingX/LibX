package org.moddingx.libx.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps an {@link IItemHandlerModifiable} to a vanilla {@link Container}.
 */
public class VanillaWrapper implements Container {

    public final IItemHandlerModifiable handler;

    @Nullable
    public final Runnable changed;

    /**
     * Wraps the given {@link IItemHandlerModifiable} to a vanilla {@link Container}.
     *
     * @param changed A runnable which is always called when {@link Container#setChanged()}
     *                is called on the vanilla container.
     */
    public VanillaWrapper(IItemHandlerModifiable handler, @Nullable Runnable changed) {
        this.handler = handler;
        this.changed = changed;
    }

    @Override
    public int getContainerSize() {
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
    public ItemStack getItem(int index) {
        return this.handler.getStackInSlot(index);
    }

    @Override
    @Nonnull
    public ItemStack removeItem(int index, int count) {
        ItemStack stack = this.handler.extractItem(index, count, false);
        this.setChanged();
        return stack;
    }

    @Override
    @Nonnull
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = this.handler.getStackInSlot(index).copy();
        this.handler.setStackInSlot(index, ItemStack.EMPTY);
        this.setChanged();
        return stack;
    }

    @Override
    public void setItem(int index, @Nonnull ItemStack stack) {
        this.handler.setStackInSlot(index, stack);
        this.setChanged();
    }

    @Override
    public void setChanged() {
        if (this.changed != null) {
            this.changed.run();
        }
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int index, @Nonnull ItemStack stack) {
        return this.handler.isItemValid(index, stack);
    }

    @Override
    public void clearContent() {
        if (this.handler instanceof IAdvancedItemHandlerModifiable adv) {
            adv.clear();
        } else {
            for (int i = 0; i < this.handler.getSlots(); i++) {
                this.handler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }
}
