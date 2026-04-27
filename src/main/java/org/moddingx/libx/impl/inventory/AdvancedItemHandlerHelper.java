package org.moddingx.libx.impl.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

public class AdvancedItemHandlerHelper {

    public static boolean hasSpaceFor(ResourceHandler<ItemResource> handler, List<ItemStack> stacks, int startInclusive, int endExclusive) {
        if (stacks.isEmpty()) return true;
        try (Transaction transaction = Transaction.openRoot()) {
            for (ItemStack stack : stacks) {
                if (stack.isEmpty()) continue;
                ItemResource resource = ItemResource.of(stack);
                int remaining = stack.getCount();
                for (int slot = startInclusive; slot < endExclusive && remaining > 0; slot++) {
                    remaining -= handler.insert(slot, resource, remaining, transaction);
                }
                if (remaining > 0) return false;
            }
            return true;
        }
    }
}
