package org.moddingx.libx.menu.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.moddingx.libx.inventory.BaseItemStackHandler;
import org.moddingx.libx.inventory.IAdvancedItemHandlerModifiable;

import javax.annotation.Nonnull;

/**
 * A slot that is meant to be used together with {@link BaseItemStackHandler}. The slot
 * will use the handler for item operations, but {@link #mayPlace(ItemStack)} only checks
 * the item validators without the output slot restriction, and {@link #mayPickup(Player)}
 * always allows pickup from non-empty slots.
 */
public class BaseSlot extends ResourceHandlerSlot {

    private final ResourceHandler<ItemResource> baseInventory;
    private final int handlerIndex;

    /**
     * Creates a new BaseSlot with the given {@link BaseItemStackHandler}
     */
    public BaseSlot(BaseItemStackHandler inventory, int index, int x, int y) {
        this(inventory, inventory, index, x, y);
    }

    /**
     * Creates a new BaseSlot with the given base and modifiable inventory.
     */
    public BaseSlot(ResourceHandler<ItemResource> inventory, IAdvancedItemHandlerModifiable modifiable, int index, int x, int y) {
        super(modifiable, modifiable::set, index, x, y);
        this.baseInventory = inventory;
        this.handlerIndex = index;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && this.baseInventory.isValid(this.handlerIndex, ItemResource.of(stack));
    }

    @Override
    public boolean mayPickup(@Nonnull Player player) {
        return !getStackCopy().isEmpty();
    }
}
