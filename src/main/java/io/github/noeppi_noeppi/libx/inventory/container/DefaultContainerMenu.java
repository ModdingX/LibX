package io.github.noeppi_noeppi.libx.inventory.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A base class for containers that handles basic container logic such as shift-clicks,
 * and laying out slots.
 * <p>
 * There are some things you need to pay attention to if you want to use this: <br>
 * Always register player inventory slots with layoutPlayerInventorySlots <br>
 * Register input slots, THEN output slots and THEN player inventory. <br>
 * </p>
 * <p>
 * Call the super constructor with <br>
 * firstOutputSlot    =  the number of input slot you have / the first output slot number <br>
 * firstInventorySlot =  the number of input slots and output slots you have / the first player inventory slot number. <br>
 * </p>
 */
public abstract class DefaultContainerMenu extends ContainerMenuBase {

    protected final Player player;
    protected final Level level;

    // Used for automatic transferStackInSlot. To further restrict this use Slot#isItemValid.
    public final int firstOutputSlot;
    public final int firstInventorySlot;

    protected DefaultContainerMenu(@Nullable MenuType<?> type, int windowId, Level level, Inventory playerContainer, Player player, int firstOutputSlot, int firstInventorySlot) {
        super(type, windowId, playerContainer);
        this.player = player;
        this.level = level;
        this.firstOutputSlot = firstOutputSlot;
        this.firstInventorySlot = firstInventorySlot;
    }

    public Level getLevel() {
        return this.level;
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();

            final int inventorySize = this.firstInventorySlot;
            final int playerInventoryEnd = inventorySize + 27;
            final int playerHotBarEnd = playerInventoryEnd + 9;

            if (index < this.firstOutputSlot) {
                if (!this.moveItemStackTo(stack, inventorySize, playerHotBarEnd, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(stack, itemstack);
            } else if (index >= inventorySize) {
                if (!this.moveItemStackTo(stack, 0, this.firstOutputSlot, false)) {
                    return ItemStack.EMPTY;
                } else if (index < playerInventoryEnd) {
                    if (!this.moveItemStackTo(stack, playerInventoryEnd, playerHotBarEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < playerHotBarEnd && !this.moveItemStackTo(stack, inventorySize, playerInventoryEnd, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, inventorySize, playerHotBarEnd, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return itemstack;
    }
}
