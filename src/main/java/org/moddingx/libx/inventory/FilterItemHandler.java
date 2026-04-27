package org.moddingx.libx.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * A simple wrapper around a {@link ResourceHandler}&lt;{@link ItemResource}&gt; that limits the possibility
 * to insert or extract items. This is especially useful for item handler capabilities.
 */
public class FilterItemHandler implements IAdvancedItemHandler {

    private final ResourceHandler<ItemResource> handler;
    private final Predicate<Integer> extract;
    private final BiPredicate<Integer, ItemStack> insert;

    /**
     * Creates a new {@link FilterItemHandler}.
     *
     * @param handler The {@link ResourceHandler} this {@link FilterItemHandler} wraps around.
     * @param extract A predicate that tests whether extraction should be allowed from the provided slot.
     * @param insert A predicate that tests whether insertion of the provided item should be allowed into the provided slot.
     */
    public FilterItemHandler(ResourceHandler<ItemResource> handler, @Nullable Predicate<Integer> extract, @Nullable BiPredicate<Integer, ItemStack> insert) {
        this.handler = handler;
        this.extract = extract == null ? slot -> true : extract;
        this.insert = insert == null ? (slot, stack) -> true : insert;
    }

    @Override
    public int size() {
        return this.handler.size();
    }

    @Override
    public ItemResource getResource(int slot) {
        return this.handler.getResource(slot);
    }

    @Override
    public long getAmountAsLong(int slot) {
        return this.handler.getAmountAsLong(slot);
    }

    @Override
    public long getCapacityAsLong(int slot, ItemResource resource) {
        return this.handler.getCapacityAsLong(slot, resource);
    }

    @Override
    public boolean isValid(int slot, ItemResource resource) {
        return this.insert.test(slot, resource.toStack()) && this.handler.isValid(slot, resource);
    }

    @Override
    public int insert(int slot, ItemResource resource, int amount, TransactionContext tx) {
        return this.insert.test(slot, resource.toStack()) ? this.handler.insert(slot, resource, amount, tx) : 0;
    }

    @Override
    public int extract(int slot, ItemResource resource, int amount, TransactionContext tx) {
        return this.extract.test(slot) ? this.handler.extract(slot, resource, amount, tx) : 0;
    }
}
