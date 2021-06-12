package io.github.noeppi_noeppi.libx.inventory.container;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.impl.inventory.container.GenericContainerSlotValidationWrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * A common {@link Container} with a variable amount of slots. You should not use this with more than 154 slots.
 * To show a container to a {@link PlayerEntity player}, call
 * {@link GenericContainer#open(ServerPlayerEntity, IItemHandlerModifiable, ITextComponent, ResourceLocation) open}
 * on the logical server.
 * As there's no way to synchronise the item validator method from the item handler modifiable, you should
 * register the validator during setup. The slot validation method in your item handler will be ignored by this.
 */
public class GenericContainer extends CommonContainer {

    private static final ResourceLocation EMPTY_VALIDATOR = new ResourceLocation(LibX.getInstance().modid, "nothing");
    private static final Map<ResourceLocation, BiPredicate<Integer, ItemStack>> validators = new HashMap<>(ImmutableMap.of(
            EMPTY_VALIDATOR, (slot, stack) -> true
    ));

    public static final ContainerType<GenericContainer> TYPE = IForgeContainerType.create((id, playerInv, buffer) -> {
        int size = buffer.readVarInt();
        ResourceLocation validatorId = buffer.readResourceLocation();
        BiPredicate<Integer, ItemStack> validator;
        if (validators.containsKey(validatorId)) {
            validator = validators.get(validatorId);
        } else {
            LibX.logger.warn("Received invalid validator for generic container. Validator: " + validatorId);
            validator = validators.get(EMPTY_VALIDATOR);
        }
        int[] slotLimits = new int[size];
        for (int i = 0; i < size; i++) {
            slotLimits[i] = buffer.readVarInt();
        }
        IItemHandlerModifiable handler = new GenericContainerSlotValidationWrapper(new ItemStackHandler(size), validator, slotLimits);
        return new GenericContainer(id, handler, playerInv);
    });

    public final int width;
    public final int height;
    public final int invX;
    public final int invY;
    public final List<Pair<Integer, Integer>> slots;

    private GenericContainer(int id, IItemHandlerModifiable handler, PlayerInventory playerInventory) {
        super(TYPE, id, playerInventory);
        Triple<Pair<Integer, Integer>, Pair<Integer, Integer>, List<Pair<Integer, Integer>>> layout = layoutSlots(handler.getSlots());
        this.width = layout.getLeft().getLeft();
        this.height = layout.getLeft().getRight();
        this.invX = layout.getMiddle().getLeft();
        this.invY = layout.getMiddle().getRight();
        this.slots = layout.getRight();
        for (int i = 0; i < this.slots.size(); i++) {
            this.addSlot(new SlotItemHandler(handler, i, this.slots.get(i).getLeft(), this.slots.get(i).getRight()));
        }
        this.layoutPlayerInventorySlots(layout.getMiddle().getLeft(), layout.getMiddle().getRight());
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(@Nonnull PlayerEntity player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            itemstack = stack.copy();

            final int inventorySize = this.slots.size();
            final int playerInventoryEnd = inventorySize + 27;
            final int playerHotBarEnd = playerInventoryEnd + 9;

            if (index >= inventorySize) {
                if (!this.mergeItemStack(stack, 0, inventorySize, false)) {
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
     * Opens a container for a {@link PlayerEntity player}.
     *
     * @param player      The player that should see the container.
     * @param inventory   The inventory of the container. The
     *                    slot amount of this determines how big the container is. This should not have more than 154 slots.
     * @param name        The name of the container.
     * @param validatorId The id of the slot validator registered
     *                    with {@link GenericContainer#registerSlotValidator(ResourceLocation, BiPredicate) registerSlotValidator}.
     *                    {@code null} disables slot validation. This will override the item handlers slot validation, so null
     *                    means no slot validation even if the item handler has the feature.
     */
    public static void open(ServerPlayerEntity player, IItemHandlerModifiable inventory, ITextComponent name, @Nullable ResourceLocation validatorId) {
        INamedContainerProvider provider = new INamedContainerProvider() {

            @Nonnull
            @Override
            public ITextComponent getDisplayName() {
                return name;
            }

            @Override
            public Container createMenu(int id, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity player) {
                BiPredicate<Integer, ItemStack> validator;
                if (validators.containsKey(validatorId == null ? EMPTY_VALIDATOR : validatorId)) {
                    validator = validators.get(validatorId);
                } else {
                    LibX.logger.warn("Generic container created with invalid validator. Validator ID: " + validatorId);
                    validator = validators.get(EMPTY_VALIDATOR);
                }
                return new GenericContainer(id, new GenericContainerSlotValidationWrapper(inventory, validator, null), playerInventory);
            }
        };
        NetworkHooks.openGui(player, provider, buffer -> {
            buffer.writeVarInt(inventory.getSlots());
            buffer.writeResourceLocation(validatorId == null ? EMPTY_VALIDATOR : validatorId);
            for (int i = 0; i < inventory.getSlots(); i++) {
                buffer.writeVarInt(inventory.getSlotLimit(i));
            }
        });
    }

    /**
     * Registers a slot validator. This is required as the item handler can not be synced to the client,
     * so the slot validation method of the item handler can not be used. This should be called during setup.
     */
    public static void registerSlotValidator(ResourceLocation validatorId, BiPredicate<Integer, ItemStack> validator) {
        synchronized (validators) {
            if (validators.containsKey(validatorId)) {
                throw new IllegalStateException("Slot validator for generic container registered: " + validatorId);
            }
            validators.put(validatorId, validator);
        }
    }

    private static Triple<Pair<Integer, Integer>, Pair<Integer, Integer>, List<Pair<Integer, Integer>>> layoutSlots(int size) {
        // We try some special cases here for the best possible results.
        // If nothing works we just put them in a rectangle
        if (size < 9) {
            return layoutRectangle(size, 1, size);
        } else if (size % 9 == 0 && size <= 9 * 8) {
            return layoutRectangle(9, size / 9, size);
        } else if (size % 11 == 0 && size <= 11 * 8) {
            return layoutRectangle(11, size / 11, size);
        } else if (size % 12 == 0 && size <= 12 * 8) {
            return layoutRectangle(12, size / 12, size);
        } else if (size % 8 == 0 && size <= 8 * 8) {
            return layoutRectangle(8, size / 8, size);
        } else if (size % 13 == 0 && size <= 13 * 8) {
            return layoutRectangle(13, size / 13, size);
        } else if (size % 14 == 0 && size <= 14 * 8) {
            return layoutRectangle(14, size / 14, size);
        } else if (size <= 9 * 8) {
            return layoutRectangle(9, size % 9 == 0 ? size / 9 : (size / 9) + 1, size);
        } else if (size <= 11 * 8) {
            return layoutRectangle(11, size % 11 == 0 ? size / 11 : (size / 11) + 1, size);
        } else if (size <= 12 * 8) {
            return layoutRectangle(12, size % 12 == 0 ? size / 12 : (size / 12) + 1, size);
        } else if (size <= 13 * 8) {
            return layoutRectangle(13, size % 13 == 0 ? size / 13 : (size / 13) + 1, size);
        } else {
            return layoutRectangle(14, size % 14 == 0 ? size / 14 : (size / 14) + 1, size);
        }
    }

    private static Triple<Pair<Integer, Integer>, Pair<Integer, Integer>, List<Pair<Integer, Integer>>> layoutRectangle(int width, int height, int maxSize) {
        int invX;
        int paddingX;
        if (width < 9) {
            invX = 0;
            paddingX = (9 - width) * 9;
        } else {
            invX = (width - 9) * 9;
            paddingX = 0;
        }
        ImmutableList.Builder<Pair<Integer, Integer>> builder = ImmutableList.builder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (((y * width) + x) < maxSize)
                    builder.add(Pair.of(7 + paddingX + (18 * x) + 1, 17 + (18 * y) + 1));
            }
        }
        return Triple.of(
                Pair.of(
                        Math.max((2 * (7 + invX)) + (9 * 18), (2 * (7 + paddingX)) + (width * 18)),
                        17 + (18 * height) + 14 + 83
                ),
                Pair.of(7 + invX + 1, 17 + (height * 18) + 14 + 1),
                builder.build()
        );
    }
}
