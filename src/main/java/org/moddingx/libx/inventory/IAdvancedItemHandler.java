package org.moddingx.libx.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.moddingx.libx.impl.inventory.AdvancedItemHandlerHelper;

import java.util.List;
import java.util.function.Predicate;

/**
 * Rich interface that provides some default methods to a {@link ResourceHandler}&lt;{@link ItemResource}&gt;.
 * Just implement this together with {@link ResourceHandler}.
 *
 * If you also need direct slot modification, use {@link IAdvancedItemHandlerModifiable} instead.
 */
public interface IAdvancedItemHandler extends ResourceHandler<ItemResource> {

    /**
     * Works like {@code insert} but without a specific slot.
     */
    default ItemStack insertItem(ItemStack stack, boolean simulate) {
        return ItemUtil.insertItemReturnRemaining(this, stack, simulate, null);
    }

    /**
     * Gets whether the item handler has space for all the items given.
     */
    default boolean hasSpaceFor(List<ItemStack> stacks) {
        return this.hasSpaceFor(stacks, 0, this.size());
    }

    /**
     * Gets whether the item handler has space for all the items given.
     *
     * @param startInclusive The first slot to test.
     * @param endExclusive The first slot after the range of slots to test.
     */
    default boolean hasSpaceFor(List<ItemStack> stacks, int startInclusive, int endExclusive) {
        return AdvancedItemHandlerHelper.hasSpaceFor(this, stacks, startInclusive, endExclusive);
    }

    /**
     * Extracts up to {@code amount} items from any slot.
     */
    default ItemStack extractItem(int amount, boolean simulate) {
        return this.extractItem(stack -> true, amount, simulate);
    }

    /**
     * Extracts up to {@code amount} items matching a predicate from any slot.
     */
    default ItemStack extractItem(Predicate<ItemStack> predicate, int amount, boolean simulate) {
        try (Transaction transaction = Transaction.openRoot()) {
            ItemStack extracted = ItemStack.EMPTY;
            for (int i = 0; i < this.size(); i++) {
                if (extracted.getCount() >= amount) break;
                ItemResource resource = this.getResource(i);
                if (resource.isEmpty()) continue;
                int slotAmount = this.getAmountAsInt(i);
                ItemStack stack = resource.toStack(slotAmount);
                if (extracted.isEmpty()) {
                    if (!predicate.test(stack)) continue;
                    int toExtract = Math.min(slotAmount, amount);
                    int extractedAmt = this.extract(i, resource, toExtract, transaction);
                    if (extractedAmt > 0) {
                        extracted = resource.toStack(extractedAmt);
                    }
                } else {
                    if (!ItemStack.isSameItemSameComponents(extracted, stack)) continue;
                    int toExtract = Math.min(slotAmount, amount - extracted.getCount());
                    int extractedAmt = this.extract(i, resource, toExtract, transaction);
                    extracted.grow(extractedAmt);
                }
            }
            if (!simulate) transaction.commit();
            return extracted;
        }
    }
}
