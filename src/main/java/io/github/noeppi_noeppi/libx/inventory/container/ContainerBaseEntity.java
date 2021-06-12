package io.github.noeppi_noeppi.libx.inventory.container;

import com.mojang.datafixers.util.Function5;
import io.github.noeppi_noeppi.libx.fi.Function6;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeContainerType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Just like {@link ContainerBase} but for entities instead of tiles.
 */
// TODO delete (but retain functionality somewhere else)
@Deprecated
public abstract class ContainerBaseEntity<T extends Entity> extends CommonContainer {

    public final T entity;
    public final PlayerEntity player;
    public final World world;

    // Used for automatic transferStackInSlot. To further restrict this use Slot#isItemValid.
    public final int firstOutputSlot;
    public final int firstInventorySlot;

    protected ContainerBaseEntity(@Nullable ContainerType<?> type, int windowId, World world, int entityId, PlayerInventory playerInventory, PlayerEntity player, int firstOutputSlot, int firstInventorySlot) {
        super(type, windowId, playerInventory);
        // This should always work. If it doesn't something is very wrong.
        //noinspection unchecked
        this.entity = (T) world.getEntityByID(entityId);
        this.player = player;
        this.world = world;
        this.firstOutputSlot = firstOutputSlot;
        this.firstInventorySlot = firstInventorySlot;
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        return isWithinUsableDistance(IWorldPosCallable.of(this.world, this.entity.getPosition()), this.player, this.world.getBlockState(this.entity.getPosition()).getBlock());
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

    /**
     * Creates a container type for a container.
     *
     * @param constructor A method reference to the container's constructor.
     */
    public static <T extends Container> ContainerType<T> createContainerType(Function5<Integer, World, Integer, PlayerInventory, PlayerEntity, T> constructor) {
        return IForgeContainerType.create((windowId1, inv, data) -> {
            int entityId1 = data.readInt();
            World world1 = inv.player.getEntityWorld();
            return constructor.apply(windowId1, world1, entityId1, inv, inv.player);
        });
    }

    /**
     * Creates a container type for a container.
     *
     * @param constructor A method reference to the container's constructor.
     */
    public static <T extends Container> ContainerType<T> createContainerType(Function6<ContainerType<T>, Integer, World, Integer, PlayerInventory, PlayerEntity, T> constructor) {
        AtomicReference<ContainerType<T>> typeRef = new AtomicReference<>(null);
        ContainerType<T> type = IForgeContainerType.create((windowId1, inv, data) -> {
            int entityId1 = data.readInt();
            World world1 = inv.player.getEntityWorld();
            return constructor.apply(typeRef.get(), windowId1, world1, entityId1, inv, inv.player);
        });
        typeRef.set(type);
        return type;
    }
}
