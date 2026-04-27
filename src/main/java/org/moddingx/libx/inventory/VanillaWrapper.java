package org.moddingx.libx.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps an {@link IAdvancedItemHandlerModifiable} to a vanilla {@link Container}.
 */
public class VanillaWrapper implements Container {

    public final IAdvancedItemHandlerModifiable handler;

    @Nullable
    public final Runnable changed;

    /**
     * Wraps the given {@link IAdvancedItemHandlerModifiable} to a vanilla {@link Container}.
     *
     * @param changed A runnable which is always called when {@link Container#setChanged()}
     *                is called on the vanilla container.
     */
    public VanillaWrapper(IAdvancedItemHandlerModifiable handler, @Nullable Runnable changed) {
        this.handler = handler;
        this.changed = changed;
    }

    @Override
    public int getContainerSize() {
        return this.handler.size();
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < this.handler.size(); slot++) {
            if (!this.handler.getResource(slot).isEmpty()) return false;
        }
        return true;
    }

    @Override
    @Nonnull
    public ItemStack getItem(int index) {
        return ItemUtil.getStack(this.handler, index);
    }

    @Override
    @Nonnull
    public ItemStack removeItem(int index, int count) {
        ItemResource resource = this.handler.getResource(index);
        if (resource.isEmpty()) return ItemStack.EMPTY;
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = this.handler.extract(index, resource, count, transaction);
            transaction.commit();
            this.setChanged();
            return resource.toStack(extracted);
        }
    }

    @Override
    @Nonnull
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = ItemUtil.getStack(this.handler, index).copy();
        this.handler.set(index, ItemResource.EMPTY, 0);
        this.setChanged();
        return stack;
    }

    @Override
    public void setItem(int index, @Nonnull ItemStack stack) {
        this.handler.set(index, ItemResource.of(stack), stack.getCount());
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
        return this.handler.isValid(index, ItemResource.of(stack));
    }

    @Override
    public void clearContent() {
        this.handler.clear();
    }
}
