package org.moddingx.libx.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;

import java.util.function.Predicate;

/**
 * Rich interface that provides some default methods to an {@link IAdvancedItemHandler} with direct
 * slot modification support.
 */
public interface IAdvancedItemHandlerModifiable extends IAdvancedItemHandler {

    /**
     * Directly sets a slot to the given resource and amount without a transaction.
     */
    void set(int index, ItemResource resource, int amount);

    /**
     * Clears all slots from the item handler.
     */
    default void clear() {
        for (int i = 0; i < this.size(); i++) {
            this.set(i, ItemResource.EMPTY, 0);
        }
    }

    /**
     * Clears all stacks from the item handler that match a predicate.
     *
     * @return The amount of items cleared.
     */
    default int clear(Predicate<ItemStack> predicate) {
        int amount = 0;
        for (int i = 0; i < this.size(); i++) {
            ItemStack stack = ItemUtil.getStack(this, i);
            if (predicate.test(stack)) {
                amount += stack.getCount();
                this.set(i, ItemResource.EMPTY, 0);
            }
        }
        return amount;
    }
}
