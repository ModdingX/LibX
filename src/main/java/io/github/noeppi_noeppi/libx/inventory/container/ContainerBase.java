package io.github.noeppi_noeppi.libx.inventory.container;

import com.mojang.datafixers.util.Function4;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A base class for {@link Container containers}. Provides some utilities that are useful for any type
 * of container. When using this it's important to register the player inventory slots through
 * {@link ContainerBase#layoutPlayerInventorySlots(int, int)} and after all other slots.
 */
public abstract class ContainerBase extends Container {
    
    public final IItemHandler playerInventory;
    
    protected ContainerBase(@Nullable ContainerType<?> type, int id, PlayerInventory playerInventory) {
        super(type, id);
        this.playerInventory = new InvWrapper(playerInventory);
    }

    /**
     * Places the player inventory slots into the container.
     *
     * @param leftCol The x coordinate of the top left slot
     * @param topRow  The y coordinate of the top left slot
     */
    protected void layoutPlayerInventorySlots(int leftCol, int topRow) {
        this.addSlotBox(this.playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);
        topRow += 58;
        this.addSlotRange(this.playerInventory, 0, leftCol, topRow, 9, 18);
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

    // As opposed to the super method this checks for Slot#isValid(ItemStack)
    @Override
    protected boolean mergeItemStack(@Nonnull ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }

        if (stack.isStackable()) {
            while (!stack.isEmpty()) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                Slot slot = this.inventorySlots.get(i);
                ItemStack itemstack = slot.getStack();
                if (!itemstack.isEmpty() && areItemsAndTagsEqual(stack, itemstack) && slot.isItemValid(stack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.onSlotChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.onSlotChanged();
                        flag = true;
                    }
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while (true) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                Slot slot1 = this.inventorySlots.get(i);
                ItemStack itemstack1 = slot1.getStack();
                if (itemstack1.isEmpty() && slot1.isItemValid(stack)) {
                    if (stack.getCount() > slot1.getSlotStackLimit()) {
                        slot1.putStack(stack.split(slot1.getSlotStackLimit()));
                    } else {
                        slot1.putStack(stack.split(stack.getCount()));
                    }

                    slot1.onSlotChanged();
                    flag = true;
                    break;
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }
}
