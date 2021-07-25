package io.github.noeppi_noeppi.libx.inventory.container;

import com.google.common.collect.ImmutableList;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.impl.inventory.container.GenericContainerSlotValidationWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
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
 * A common {@link AbstractContainerMenu} with a variable amount of slots. You should not use this with more than 154 slots.
 * To show a container to a {@link Player player}, call
 * {@link #open(ServerPlayer, IItemHandlerModifiable, Component, ResourceLocation) open}
 * on the logical server.
 * As there's no way to synchronise the item validator method from the item handler modifiable, you should
 * register the validator during setup. The slot validation method in your item handler will be ignored by this.
 */
public class GenericContainerMenu extends ContainerMenuBase {

    private static final ResourceLocation EMPTY_VALIDATOR = new ResourceLocation(LibX.getInstance().modid, "nothing");
    private static final Map<ResourceLocation, BiPredicate<Integer, ItemStack>> validators = new HashMap<>(Map.of(
            EMPTY_VALIDATOR, (slot, stack) -> true
    ));

    public static final MenuType<GenericContainerMenu> TYPE = IForgeContainerType.create((id, playerInv, buffer) -> {
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
        return new GenericContainerMenu(id, handler, playerInv);
    });

    public final int width;
    public final int height;
    public final int invX;
    public final int invY;
    public final List<Pair<Integer, Integer>> slotList;

    private GenericContainerMenu(int id, IItemHandlerModifiable handler, Inventory playerContainer) {
        super(TYPE, id, playerContainer);
        Triple<Pair<Integer, Integer>, Pair<Integer, Integer>, List<Pair<Integer, Integer>>> layout = layoutSlots(handler.getSlots());
        this.width = layout.getLeft().getLeft();
        this.height = layout.getLeft().getRight();
        this.invX = layout.getMiddle().getLeft();
        this.invY = layout.getMiddle().getRight();
        this.slotList = layout.getRight();
        for (int i = 0; i < this.slotList.size(); i++) {
            this.addSlot(new SlotItemHandler(handler, i, this.slotList.get(i).getLeft(), this.slotList.get(i).getRight()));
        }
        this.layoutPlayerInventorySlots(layout.getMiddle().getLeft(), layout.getMiddle().getRight());
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();

            final int inventorySize = this.slotList.size();
            final int playerInventoryEnd = inventorySize + 27;
            final int playerHotBarEnd = playerInventoryEnd + 9;

            if (index >= inventorySize) {
                if (!this.moveItemStackTo(stack, 0, inventorySize, false)) {
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

    /**
     * Opens a container for a {@link Player player}.
     *
     * @param player      The player that should see the container.
     * @param inventory   The inventory of the container. The
     *                    slot amount of this determines how big the container is. This should not have more than 154 slots.
     * @param name        The name of the container.
     * @param validatorId The id of the slot validator registered
     *                    with {@link #registerSlotValidator(ResourceLocation, BiPredicate) registerSlotValidator}.
     *                    {@code null} disables slot validation. This will override the item handlers slot validation, so null
     *                    means no slot validation even if the item handler has the feature.
     */
    public static void open(ServerPlayer player, IItemHandlerModifiable inventory, Component name, @Nullable ResourceLocation validatorId) {
        MenuProvider provider = new MenuProvider() {

            @Nonnull
            @Override
            public Component getDisplayName() {
                return name;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, @Nonnull Inventory inv, @Nonnull Player player) {
                BiPredicate<Integer, ItemStack> validator;
                if (validators.containsKey(validatorId == null ? EMPTY_VALIDATOR : validatorId)) {
                    validator = validators.get(validatorId);
                } else {
                    LibX.logger.warn("Generic container created with invalid validator. Validator ID: " + validatorId);
                    validator = validators.get(EMPTY_VALIDATOR);
                }
                return new GenericContainerMenu(containerId, new GenericContainerSlotValidationWrapper(inventory, validator, null), inv);
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
