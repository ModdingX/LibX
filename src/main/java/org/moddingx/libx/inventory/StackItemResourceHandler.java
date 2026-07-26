package org.moddingx.libx.inventory;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * An {@link IAdvancedItemHandlerModifiable} that stores its contents in an {@link ItemContainerContents}
 * data component on the item behind an {@link ItemAccess}.
 */
public class StackItemResourceHandler extends ItemAccessItemHandler implements IAdvancedItemHandlerModifiable {

    /**
     * The maximum number of slots such a handler can have, imposed by {@link ItemContainerContents}.
     */
    public static final int MAX_SIZE = 256;

    public static final DataComponentType<ItemContainerContents> INVENTORY_DATA = new DataComponentType.Builder<ItemContainerContents>()
            .persistent(ItemContainerContents.CODEC)
            .networkSynchronized(ItemContainerContents.STREAM_CODEC)
            .build();

    /**
     * Creates a handler backed by the default {@link #INVENTORY_DATA} component.
     */
    public StackItemResourceHandler(int size, ItemAccess itemAccess) {
        this(size, itemAccess, INVENTORY_DATA);
    }

    /**
     * Creates a handler backed by a custom {@link ItemContainerContents} component. Use this when an item
     * needs more than one inventory, or an inventory that is not shared with other LibX items.
     */
    public StackItemResourceHandler(int size, ItemAccess itemAccess, DataComponentType<ItemContainerContents> component) {
        super(itemAccess, component, size);
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        return resource.isEmpty() ? Item.DEFAULT_MAX_STACK_SIZE : Math.min(Item.DEFAULT_MAX_STACK_SIZE, resource.getMaxStackSize());
    }

    @Override
    public void set(int index, @Nonnull ItemResource resource, int amount) {
        Objects.checkIndex(index, this.size());
        int accessAmount = this.itemAccess.getAmount();
        if (accessAmount == 0) return;
        try (Transaction transaction = Transaction.openRoot()) {
            ItemResource updated = this.update(this.itemAccess.getResource(), index, resource, resource.isEmpty() ? 0 : amount);
            if (updated != null && !updated.isEmpty() && this.itemAccess.exchange(updated, accessAmount, transaction) == accessAmount) {
                transaction.commit();
            }
        }
    }
}
