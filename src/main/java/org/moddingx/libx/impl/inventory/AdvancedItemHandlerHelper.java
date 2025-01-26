package org.moddingx.libx.impl.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public class AdvancedItemHandlerHelper {
    
    public static boolean hasSpaceFor(IItemHandler itemHandler, List<ItemStack> stacks, int startInclusive, int endExclusive, BiPredicate<Integer, ItemStack> itemValidForSlot) {
        if (stacks.isEmpty()) {
            return true;
        } else if (stacks.size() == 1) {
            ItemStack remainder = stacks.get(0).copy();
            for (int slot = startInclusive; slot < endExclusive; slot++) {
                remainder = itemHandler.insertItem(slot, remainder, true);
                if (remainder.isEmpty()) return true;
            }
            return remainder.isEmpty();
        } else {
            Map<Integer, ItemStack> copies = new HashMap<>();
            for (ItemStack stack : stacks) {
                if (!stack.isEmpty()) {
                    int amountLeft = stack.getCount();
                    for (int slot = startInclusive; slot < endExclusive; slot++) {
                        if (itemValidForSlot.test(slot, stack)) {
                            ItemStack content = copies.getOrDefault(slot, itemHandler.getStackInSlot(slot));
                            if (content.isEmpty()) {
                                amountLeft = 0;
                                ItemStack modifiableStack = stack.copy();
                                modifiableStack.setCount(amountLeft);
                                copies.put(slot, modifiableStack);
                                break;
                            } else if (ItemStack.isSameItemSameComponents(stack, content)) {
                                int reduce = Math.max(0, Math.min(content.getMaxStackSize() - content.getCount(), amountLeft));
                                amountLeft -= reduce;
                                ItemStack modifiableStack = copies.getOrDefault(slot, itemHandler.getStackInSlot(slot).copy());
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
}
