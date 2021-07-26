package io.github.noeppi_noeppi.libx.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A base class for menus that handles basic menu logic such as shift-clicks,
 * and laying out slots.
 * 
 * There are some things you need to pay attention to if you want to use this:
 * 
 * <ul>
 *     <li>Always register player inventory slots with {@link #layoutPlayerInventorySlots(int, int)}</li>
 *     <li>Register input slots, THEN output slots and THEN player inventory.</li>
 * </ul>
 * 
 * Call the super constructor with
 * 
 * <ul>
 *     <li>firstOutputSlot = the number of input slot you have / the first output slot number</li>
 *     <li>firstInventorySlot = the number of input slots and output slots you have / the first player inventory slot number</li>
 * </ul>
 */
public abstract class DefaultMenu extends MenuBase {

    protected final Player player;
    protected final Level level;

    // Used for automatic transferStackInSlot. To further restrict this use Slot#isItemValid.
    public final int firstOutputSlot;
    public final int firstInventorySlot;

    protected DefaultMenu(@Nullable MenuType<?> type, int windowId, Level level, Inventory inventory, Player player, int firstOutputSlot, int firstInventorySlot) {
        super(type, windowId, inventory);
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
        //noinspection ConstantConditions
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
