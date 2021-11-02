package io.github.noeppi_noeppi.libx.menu.slot;

import io.github.noeppi_noeppi.libx.inventory.BaseItemStackHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

/**
 * A slot that is meant to be used together with {@link BaseItemStackHandler}. the slot
 * will use the unrestricted item handler as inventory, but it will use the base inventory
 * for {@link #mayPlace(ItemStack)} to allow validators to run.
 */
public class BaseSlot extends SlotItemHandler {
    
    private final IItemHandler baseInventory;
    private final int index;

    /**
     * Creates a new BaseSlot with the given {@link BaseItemStackHandler}
     */
    public BaseSlot(BaseItemStackHandler inventory, int index, int x, int y) {
        this(inventory, inventory.getUnrestricted(), index, x, y);
    }

    /**
     * Creates a new BaseSlot with the given base and unrestricted inventory.
     */
    public BaseSlot(IItemHandler inventory, IItemHandler unrestricted, int index, int x, int y) {
        super(unrestricted, index, x, y);
        this.baseInventory = inventory;
        this.index = index;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && this.baseInventory.isItemValid(this.index, stack);
    }
}
