package io.github.noeppi_noeppi.libx.menu;

import com.mojang.datafixers.util.Function4;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A base class for {@link AbstractContainerMenu menus}. Provides some utilities that are useful for any type
 * of menu. When using this it's important to register the player inventory slots through
 * {@link #layoutPlayerInventorySlots(int, int)} and after all the other slots.
 */
public abstract class MenuBase extends AbstractContainerMenu {
    
    public final IItemHandler playerinventory;
    
    protected MenuBase(@Nullable MenuType<?> type, int id, Inventory inventory) {
        super(type, id);
        this.playerinventory = new InvWrapper(inventory);
    }

    /**
     * Places the player inventory slots into the container.
     *
     * @param leftCol The x coordinate of the top left slot
     * @param topRow  The y coordinate of the top left slot
     */
    protected void layoutPlayerInventorySlots(int leftCol, int topRow) {
        this.addSlotBox(this.playerinventory, 9, leftCol, topRow, 9, 18, 3, 18);
        topRow += 58;
        this.addSlotRange(this.playerinventory, 0, leftCol, topRow, 9, 18);
    }

    /**
     * Adds a box of slots to the container
     *
     * @param handler   The inventory of the slot
     * @param index     The index of the first slot
     * @param x         The x coordinate of the top left slot
     * @param y         The y coordinate of the top left slot
     * @param horAmount The amount of slots in horizontal direction
     * @param dx        The space between two slots in horizontal direction. Should not be less than 16 or
     *                  you create overlapping slots. Most of the time this is 18
     * @param verAmount The amount of slots in vertical direction
     * @param dy        The space between two slots in vertical direction. Should not be less than 16 or
     *                  you create overlapping slots. Most of the time this is 18
     * @return The next index to be used to create a slot
     */
    protected int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        return this.addSlotBox(handler, index, x, y, horAmount, dx, verAmount, dy, SlotItemHandler::new);
    }

    /**
     * Adds a row of slots to the container
     *
     * @param handler The inventory of the slot
     * @param index   The index of the first slot
     * @param x       The x coordinate of the top left slot
     * @param y       The y coordinate of the top left slot
     * @param amount  The amount of slots
     * @param dx      The space between two slots. Should not be less than 16 or
     *                you create overlapping slots. Most of the time this is 18
     * @return The next index to be used to create a slot
     */
    protected int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        return this.addSlotRange(handler, index, x, y, amount, dx, SlotItemHandler::new);
    }

    /**
     * Adds a box of slots to the container
     *
     * @param handler     The inventory of the slot
     * @param index       The index of the first slot
     * @param x           The x coordinate of the top left slot
     * @param y           The y coordinate of the top left slot
     * @param horAmount   The amount of slots in horizontal direction
     * @param dx          The space between two slots in horizontal direction. Should not be less than 16 or
     *                    you create overlapping slots. Most of the time this is 18
     * @param verAmount   The amount of slots in vertical direction
     * @param dy          The space between two slots in vertical direction. Should not be less than 16 or
     *                    you create overlapping slots. Most of the time this is 18
     * @param slotFactory A factory to create a slot. This could be {@code SlotItemHandler::new}
     *                    or {@code SlotOutputOnly::new} for output slots.
     * @return The next index to be used to create a slot
     */
    protected int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy, Function4<IItemHandler, Integer, Integer, Integer, Slot> slotFactory) {
        for (int j = 0; j < verAmount; j++) {
            index = this.addSlotRange(handler, index, x, y, horAmount, dx, slotFactory);
            y += dy;
        }
        return index;
    }

    /**
     * Adds a row of slots to the container
     *
     * @param handler     The inventory of the slot
     * @param index       The index of the first slot
     * @param x           The x coordinate of the top left slot
     * @param y           The y coordinate of the top left slot
     * @param amount      The amount of slots
     * @param dx          The space between two slots. Should not be less than 16 or
     *                    you create overlapping slots. Most of the time this is 18
     * @param slotFactory A factory to create a slot. This could be {@code SlotItemHandler::new}
     *                    or {@code SlotOutputOnly::new} for output slots.
     * @return The next index to be used to create a slot
     */
    protected int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx, Function4<IItemHandler, Integer, Integer, Integer, Slot> slotFactory) {
        for (int i = 0; i < amount; i++) {
            this.addSlot(slotFactory.apply(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    // As opposed to the super method this checks for Slot#mayPlace(ItemStack)
    @Override
    protected boolean moveItemStackTo(@Nonnull ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean success = false;
        
        if (stack.isStackable()) {
            int idx = reverseDirection ? endIndex - 1 : startIndex;
            while(!stack.isEmpty()) {
                if (reverseDirection ? idx < startIndex : idx >= endIndex) {
                    break;
                }

                Slot slot = this.slots.get(idx);
                ItemStack content = slot.getItem();
                if (!content.isEmpty() && ItemStack.isSameItemSameTags(stack, content) && slot.mayPlace(stack)) {
                    int totalCount = content.getCount() + stack.getCount();
                    int maxSize = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());
                    if (totalCount <= maxSize) {
                        stack.setCount(0);
                        content.setCount(totalCount);
                        slot.setChanged();
                        success = true;
                    } else if (content.getCount() < maxSize) {
                        stack.shrink(maxSize - content.getCount());
                        content.setCount(maxSize);
                        slot.setChanged();
                        success = true;
                    }
                }

                idx += (reverseDirection ? -1 : 1);
            }
        }

        if (!stack.isEmpty()) {
            int idx = reverseDirection ? endIndex - 1 : startIndex;
            while(true) {
                if (reverseDirection ? idx < startIndex : idx >= endIndex) {
                    break;
                }

                Slot slot = this.slots.get(idx);
                ItemStack content = slot.getItem();
                if (content.isEmpty() && slot.mayPlace(stack)) {
                    if (stack.getCount() > slot.getMaxStackSize()) {
                        slot.set(stack.split(slot.getMaxStackSize()));
                    } else {
                        slot.set(stack.split(stack.getCount()));
                    }
                    slot.setChanged();
                    success = true;
                    break;
                }

                idx += (reverseDirection ? -1 : 1);
            }
        }

        return success;
    }
}
