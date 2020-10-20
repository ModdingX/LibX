package io.github.noeppi_noeppi.libx.inventory.container;

import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A base class for containers that handles basic container logic such as shift-clicks,
 * and laying out slots.
 * <p>
 * There are some things you need to pay attention to if you want to use this:
 * Always register player inventory slots with layoutPlayerInventorySlots
 * Register input slots, THEN output slots and THEN player inventory.
 * <p>
 * Call the super constructor with
 * firstOutputSlot    =  the number of input slot you have / the first output slot number
 * firstInventorySlot =  the number of input slots and output slots you have / the first player inventory slot number.
 */
public abstract class ContainerBase<T extends TileEntity> extends Container {

    public final T tile;
    public final PlayerEntity player;
    public final IItemHandler playerInventory;
    public final BlockPos pos;
    public final World world;

    // Used for automatic transferStackInSlot. To further restrict this use Slot#isItemValid.
    public final int firstOutputSlot;
    public final int firstInventorySlot;

    protected ContainerBase(@Nullable ContainerType<?> type, int windowId, World world, BlockPos pos, PlayerInventory playerInventory, PlayerEntity player, int firstOutputSlot, int firstInventorySlot) {
        super(type, windowId);
        // This should always work. If it doesn't something is very wrong.
        //noinspection unchecked
        this.tile = (T) world.getTileEntity(pos);
        this.player = player;
        this.playerInventory = new InvWrapper(playerInventory);
        this.pos = pos;
        this.world = world;
        this.firstOutputSlot = firstOutputSlot;
        this.firstInventorySlot = firstInventorySlot;
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        //noinspection ConstantConditions
        return isWithinUsableDistance(IWorldPosCallable.of(this.tile.getWorld(), this.tile.getPos()), this.player, this.tile.getBlockState().getBlock());
    }

    /**
     * Places the player inventory slots into the container.
     *
     * @param leftCol The x coordinate of the top left slot
     * @param topRow  The y coordinate of the top left lot
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
     * @param y         The y coordinate of the top left lot
     * @param horAmount The amount of slots in horizontal direction
     * @param dx        The space between two slots in horizontal direction. Should not be less that 16 or
     *                  you create overlapping slots. Most of the time this is 18
     * @param verAmount The amount of slots in vertical direction
     * @param dy        The space between two slots in vertical direction. Should not be less that 16 or
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
     * @param y       The y coordinate of the top left lot
     * @param amount  The amount of slots
     * @param dx      The space between two slots. Should not be less that 16 or
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
     * @param y           The y coordinate of the top left lot
     * @param horAmount   The amount of slots in horizontal direction
     * @param dx          The space between two slots in horizontal direction. Should not be less that 16 or
     *                    you create overlapping slots. Most of the time this is 18
     * @param verAmount   The amount of slots in vertical direction
     * @param dy          The space between two slots in vertical direction. Should not be less that 16 or
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
     * @param y           The y coordinate of the top left lot
     * @param amount      The amount of slots
     * @param dx          The space between two slots. Should not be less that 16 or
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

    public BlockPos getPos() {
        return this.pos;
    }

    public World getWorld() {
        return this.world;
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(@Nonnull PlayerEntity player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            itemstack = stack.copy();

            final int inventorySize = this.firstInventorySlot;
            final int playerInventoryEnd = inventorySize + 27;
            final int playerHotBarEnd = playerInventoryEnd + 9;

            if (index < this.firstOutputSlot) {
                if (!this.mergeItemStack(stack, inventorySize, playerHotBarEnd, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(stack, itemstack);
            } else if (index >= inventorySize) {
                if (!this.mergeItemStack(stack, 0, this.firstOutputSlot, false)) {
                    return ItemStack.EMPTY;
                } else if (index < playerInventoryEnd) {
                    if (!this.mergeItemStack(stack, playerInventoryEnd, playerHotBarEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < playerHotBarEnd && !this.mergeItemStack(stack, inventorySize, playerInventoryEnd, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(stack, inventorySize, playerHotBarEnd, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return itemstack;
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

    /**
     * Creates a container type for a container.
     *
     * @param constructor A method reference to the container's constructor.
     */
    public static <T extends Container> ContainerType<T> createContainerType(Function5<Integer, World, BlockPos, PlayerInventory, PlayerEntity, T> constructor) {
        return IForgeContainerType.create((windowId1, inv, data) -> {
            BlockPos pos1 = data.readBlockPos();
            World world1 = inv.player.getEntityWorld();
            return constructor.apply(windowId1, world1, pos1, inv, inv.player);
        });
    }
}
