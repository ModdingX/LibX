package io.github.noeppi_noeppi.libx.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Rich interface that provides some default methods to an {@link IItemHandler}.
 * Just implement this together with {@link IItemHandler}.
 * 
 * If you have an {@link IItemHandlerModifiable}, use {@link IAdvancedItemHandlerModifiable}
 * instead.
 */
public interface IAdvancedItemHandler extends IItemHandler {

    /**
     * Works like {@link IItemHandler#insertItem(int, ItemStack, boolean)} but without a specific slot.
     */
    default ItemStack insertItem(ItemStack stack, boolean simulate) {
        ItemStack remainder = stack.copy();
        for (int slot = 0; slot < this.getSlots(); slot++) {
            remainder = this.insertItem(slot, stack, simulate);
            if (remainder.isEmpty()) break;
        }
        return remainder;
    }

    /**
     * Gets whether the item handler has space for all the items given. <b>This does not check whether
     * the item handler can take the stacks via {@link IItemHandler#insertItem(int, ItemStack, boolean)}.
     * However it checks for {@link IItemHandler#isItemValid(int, ItemStack)}.</b>
     */
    default boolean hasSpaceFor(List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            return true;
        } else if (stacks.size() == 1) {
            return this.insertItem(stacks.get(0), true).isEmpty();
        } else {
            Map<Integer, ItemStack> copies = new HashMap<>();
            for (ItemStack stack : stacks) {
                if (!stack.isEmpty()) {
                    int amountLeft = stack.getCount();
                    for (int slot = 0; slot < this.getSlots(); slot++) {
                        if (this.isItemValid(slot, stack)) {
                            ItemStack content = copies.getOrDefault(slot, this.getStackInSlot(slot));
                            if (content.isEmpty()) {
                                amountLeft = 0;
                                ItemStack modifiableStack = stack.copy();
                                modifiableStack.setCount(amountLeft);
                                copies.put(slot, modifiableStack);
                                break;
                            } else if (ItemStack.isSame(stack, content) && ItemStack.tagMatches(stack, content)) {
                                int reduce = Math.max(0, Math.min(content.getMaxStackSize() - content.getCount(), amountLeft));
                                amountLeft -= reduce;
                                ItemStack modifiableStack = copies.getOrDefault(slot, this.getStackInSlot(slot).copy());
                                modifiableStack.grow(reduce);
                                copies.put(slot, modifiableStack);
                                if (amountLeft <= 0) break;
                            }
                        }
                    }
                    if (amountLeft > 0) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     * Works like {@link IItemHandler#extractItem(int, int, boolean)} but without a specific slot.
     */
    default ItemStack extractItem(int amount, boolean simulate) {
        return this.extractItem(stack -> true, amount, simulate);
    }

    /**
     * Works like {@link IItemHandler#extractItem(int, int, boolean)} but with a predicate instead of a slot.
     */
    default ItemStack extractItem(Predicate<ItemStack> predicate, int amount, boolean simulate) {
        ItemStack extracted = ItemStack.EMPTY;
        for (int slot = 0; slot < this.getSlots(); slot++) {
            int amountToExtract = Math.max(0, amount - extracted.getCount());
            ItemStack content = this.extractItem(slot, amountToExtract, true);
            if (extracted.isEmpty()) {
                if (predicate.test(content)) {
                    extracted = content;
                    if (!simulate) {
                        this.extractItem(slot, amountToExtract, true);
                    }
                }
            } else {
                if (ItemStack.isSame(extracted, content) && ItemStack.tagMatches(extracted, content)) {
                    extracted.grow(content.getCount());
                }
                if (!simulate) {
                    this.extractItem(slot, amountToExtract, true);
                }
            }
            if (extracted.getCount() >= amount) break;
        }
        return extracted;
    }

    /**
     * Creates a new IAdvancedItemHandler from an {@link IItemHandler}.
     */
    static IAdvancedItemHandler wrap(IItemHandler handler) {
        if (handler instanceof IAdvancedItemHandler advanced) {
            return advanced;
        } else {
            return new IAdvancedItemHandler() {

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
            };
        }
    }
}
