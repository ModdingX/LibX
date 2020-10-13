package io.github.noeppi_noeppi.libx.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Copied from <a href = "https://github.com/BlakeBr0/Cucumber/blob/1.15/src/main/java/com/blakebr0/cucumber/inventory/BaseItemStackHandler.java">Cucumber</a>
 * and modified.
 * <p>
 * An ItemStackHandler with some extra features.
 */
public class BaseItemStackHandler extends ItemStackHandler {

    // An IItemHandlerModifiable that bypasses every slot validity check when trying to extract and insert.
    private final IItemHandlerModifiable unrestricted = new Unrestricted();
    private final Consumer<Integer> onContentsChanged;
    private final Map<Integer, Integer> slotSizeMap;
    private BiFunction<Integer, ItemStack, Boolean> slotValidator;
    private int maxStackSize = 64;
    private int[] outputSlots = null;
    private int[] inputSlots = null;

    /**
     * Creates a new BaseItemStackHandler with the given size.
     */
    public BaseItemStackHandler(int size) {
        this(size, null);
    }

    /**
     * Creates a new BaseItemStackHandler with the given size.
     *
     * @param onContentsChanged A listener that is always called when contents are changed.
     */
    public BaseItemStackHandler(int size, Consumer<Integer> onContentsChanged) {
        super(size);
        this.onContentsChanged = onContentsChanged;
        this.slotSizeMap = new HashMap<>();
        this.slotValidator = null;
    }

    /**
     * Creates a new BaseItemStackHandler with the given size.
     *
     * @param onContentsChanged A listener that is always called when contents are changed.
     * @param slotValidator     A Function that determines whether an ItemStack is valid for a given slot id.
     */
    public BaseItemStackHandler(int size, Consumer<Integer> onContentsChanged, BiFunction<Integer, ItemStack, Boolean> slotValidator) {
        super(size);
        this.onContentsChanged = onContentsChanged;
        this.slotSizeMap = new HashMap<>();
        this.slotValidator = slotValidator;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (this.outputSlots != null && ArrayUtils.contains(this.outputSlots, slot))
            return stack;
        return super.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (this.outputSlots != null && !ArrayUtils.contains(this.outputSlots, slot))
            return ItemStack.EMPTY;
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.slotSizeMap.containsKey(slot) ? this.slotSizeMap.get(slot) : this.maxStackSize;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return this.slotValidator == null || this.slotValidator.apply(slot, stack);
    }

    @Override
    public void onContentsChanged(int slot) {
        if (this.onContentsChanged != null)
            this.onContentsChanged.accept(slot);
    }

    public NonNullList<ItemStack> getStacks() {
        return this.stacks;
    }

    /**
     * Gets the slot ids of the input slots.
     */
    public int[] getInputSlots() {
        return this.inputSlots;
    }

    /**
     * Gets the slot ids of the output slots.
     */
    public int[] getOutputSlots() {
        return this.outputSlots;
    }

    /**
     * Sets the default maximum stack size for this inventory.
     */
    public void setDefaultSlotLimit(int size) {
        this.maxStackSize = size;
    }

    /**
     * Sets the maximum stack size for one given slot.
     */
    public void addSlotLimit(int slot, int size) {
        this.slotSizeMap.put(slot, size);
    }

    public void setSlotValidator(BiFunction<Integer, ItemStack, Boolean> validator) {
        this.slotValidator = validator;
    }

    /**
     * Sets the slot ids of the input slots.
     */
    public void setInputSlots(int... slots) {
        Arrays.sort(slots);
        this.inputSlots = slots;
    }

    /**
     * Sets the slot ids of the output slots.
     */
    public void setOutputSlots(int... slots) {
        this.outputSlots = slots;
    }

    /**
     * Checks whether the input slots are all empty
     */
    public boolean isInputEmpty() {
        if (this.inputSlots != null) {
            for (int i : this.inputSlots) {
                if (!this.getStackInSlot(i).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks whether the output slots are all empty
     */
    public boolean isOutputEmpty() {
        if (this.outputSlots != null) {
            for (int i : this.outputSlots) {
                if (!this.getStackInSlot(i).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Converts this BaseItemStackHandler to a vanilla inventory.
     */
    public VanillaWrapper toIInventory() {
        return new VanillaWrapper(this, null);
    }

    /**
     * Converts this BaseItemStackHandler to a vanilla inventory.
     *
     * @param dirty A runnable that is always called when {@code markDirty();} is called on the VanillaWrapper.
     */
    public VanillaWrapper toIInventory(@Nullable Runnable dirty) {
        return new VanillaWrapper(this, null);
    }

    /**
     * Gets the unrestricted wrapper for this inventory. This can bypass any slot validity limitations.
     */
    public IItemHandlerModifiable getUnrestricted() {
        return this.unrestricted;
    }

    private class Unrestricted implements IItemHandlerModifiable {

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            BaseItemStackHandler.this.setStackInSlot(slot, stack);
        }

        @Override
        public int getSlots() {
            return BaseItemStackHandler.this.getSlots();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return BaseItemStackHandler.this.getStackInSlot(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            BaseItemStackHandler.this.validateSlotIndex(slot);
            ItemStack existing = this.getStackInSlot(slot);
            int limit = BaseItemStackHandler.this.getStackLimit(slot, stack);
            if (!existing.isEmpty()) {
                if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
                    return stack;
                }
                limit -= existing.getCount();
            }
            if (limit <= 0)
                return stack;
            boolean reachedLimit = stack.getCount() > limit;
            if (!simulate) {
                if (existing.isEmpty()) {
                    this.setStackInSlot(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
                } else {
                    existing.grow(reachedLimit ? limit : stack.getCount());
                }
                BaseItemStackHandler.this.onContentsChanged(slot);
            }
            return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount == 0)
                return ItemStack.EMPTY;
            BaseItemStackHandler.this.validateSlotIndex(slot);
            ItemStack existing = this.getStackInSlot(slot);
            if (existing.isEmpty())
                return ItemStack.EMPTY;
            int toExtract = Math.min(amount, existing.getMaxStackSize());
            if (existing.getCount() <= toExtract) {
                if (!simulate) {
                    this.setStackInSlot(slot, ItemStack.EMPTY);
                    BaseItemStackHandler.this.onContentsChanged(slot);
                    return existing;
                } else {
                    return existing.copy();
                }
            } else {
                if (!simulate) {
                    this.setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
                    BaseItemStackHandler.this.onContentsChanged(slot);
                }
                return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return BaseItemStackHandler.this.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return BaseItemStackHandler.this.isItemValid(slot, stack);
        }
    }
}
