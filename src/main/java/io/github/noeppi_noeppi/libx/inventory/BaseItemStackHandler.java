package io.github.noeppi_noeppi.libx.inventory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An {@link ItemStackHandler} that can be configured with common things required for inventories.
 * To get a BaseItemStackHandler, use {@link #builder(int)}.
 */
public class BaseItemStackHandler extends ItemStackHandler implements IAdvancedItemHandler {

    private final int defaultSlotLimit;
    private final Set<Integer> insertionOnlySlots;
    private final Set<Integer> outputSlots;
    private final Map<Integer, Integer> slotLimits;
    private final Map<Integer, Predicate<ItemStack>> slotValidators;
    private final Consumer<Integer> contentsChanged;

    private Container vanilla = null;
    private Unrestricted unrestricted = null;

    private BaseItemStackHandler(int size, int defaultSlotLimit, Set<Integer> insertionOnlySlots, Set<Integer> outputSlots, Map<Integer, Integer> slotLimits, Map<Integer, Predicate<ItemStack>> slotValidators, Consumer<Integer> contentsChanged) {
        super(size);
        this.defaultSlotLimit = defaultSlotLimit;
        this.insertionOnlySlots = ImmutableSet.copyOf(insertionOnlySlots);
        this.outputSlots = ImmutableSet.copyOf(outputSlots);
        this.slotLimits = ImmutableMap.copyOf(slotLimits);
        this.slotValidators = ImmutableMap.copyOf(slotValidators);
        this.contentsChanged = contentsChanged;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return this.outputSlots.contains(slot) ? stack : super.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.insertionOnlySlots.contains(slot) ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.slotLimits.getOrDefault(slot, this.defaultSlotLimit);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return this.slotValidators.containsKey(slot) && this.slotValidators.get(slot).test(stack);
    }

    @Override
    public void onContentsChanged(int slot) {
        this.contentsChanged.accept(slot);
    }

    /**
     * Gets a vanilla inventory that wraps around this inventory. Marking the vanilla inventory dirty
     * will notify an content change for every slot of this item handler.
     */
    public Container toVanilla() {
        if (this.vanilla == null) this.vanilla = new VanillaWrapper(this, null);
        return this.vanilla;
    }

    /**
     * Gets an inventory that wraps around this item handler but has no checks on which items are
     * valid for a slot.
     */
    public IAdvancedItemHandlerModifiable getUnrestricted() {
        if (this.unrestricted == null) this.unrestricted = new Unrestricted();
        return this.unrestricted;
    }

    /**
     * Creates a new {@link LazyOptional} for this inventory.
     */
    public LazyOptional<IAdvancedItemHandlerModifiable> createCapability() {
        return ItemCapabilities.create(this);
    }
    
    /**
     * Creates a new {@link LazyOptional} for this inventory but without slot validation.
     * 
     * @see #getUnrestricted() 
     */
    public LazyOptional<IAdvancedItemHandlerModifiable> createUnrestrictedCapability() {
        return ItemCapabilities.create(this::getUnrestricted);
    }

    /**
     * Creates a new {@link LazyOptional} for this inventory.
     * 
     * @param extract A predicate on whether an item can be extracted through this {@link LazyOptional}. This gets passed the slot to extract from.
     * @param insert A predicate on whether an item can be inserted through this {@link LazyOptional}. This gets passed the slot to insert to and the stack that should be inserted..
     */
    public LazyOptional<IAdvancedItemHandlerModifiable> createCapability(@Nullable Predicate<Integer> extract, @Nullable BiPredicate<Integer, ItemStack> insert) {
        return ItemCapabilities.create(this, extract, insert);
    }

    /**
     * Creates a new {@link Builder} for an inventory with the given size.
     */
    public static Builder builder(int size) {
        return new Builder(size);
    }

    private class Unrestricted implements IAdvancedItemHandlerModifiable {

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
            return BaseItemStackHandler.super.insertItem(slot, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return BaseItemStackHandler.super.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return BaseItemStackHandler.this.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
        }
    }

    /**
     * Builder for {@link BaseItemStackHandler}
     */
    public static class Builder {

        private final int size;
        private int defaultSlotLimit = 64;
        private final Set<Integer> insertionOnlySlots = new HashSet<>();
        private final Set<Integer> outputSlots = new HashSet<>();
        private final Map<Integer, Integer> slotLimits = new HashMap<>();
        private final Map<Integer, Predicate<ItemStack>> slotValidators = new HashMap<>();
        private Consumer<Integer> contentsChanged = null;

        private Builder(int size) {
            this.size = size;
        }

        /**
         * Adds an action that is run whenever the contents of the inventory change.
         */
        public Builder contentsChanged(Runnable action) {
            return this.contentsChanged(slot -> action.run());
        }
        
        /**
         * Adds an action that is run whenever the contents of the inventory change. The action
         * will get passed the slot that was changed.
         */
        public Builder contentsChanged(Consumer<Integer> action) {
            if (this.contentsChanged == null) {
                this.contentsChanged = action;
            } else {
                // We need to merge. First added should be called first
                Consumer<Integer> old = this.contentsChanged;
                this.contentsChanged = slot -> {
                    old.accept(slot);
                    action.accept(slot);
                };
            }
            return this;
        }

        /**
         * Marks the given slots as outputs. That means it's not possible to insert items
         * into these slots. Marking a slot as insertion only and output at the same time
         * will cause an exception.
         */
        public Builder output(int... slots) {
            for (int slot : slots) {
                this.outputSlots.add(slot);
            }
            return this;
        }
        
        /**
         * Marks the given slots as outputs. That means it's not possible to insert items
         * into these slots. Marking a slot as insertion only and output at the same time
         * will cause an exception.
         */
        public Builder output(Set<Integer> slots) {
            this.outputSlots.addAll(slots);
            return this;
        }
        
        /**
         * Marks the given slots as insertion only. That means it's not possible to extract
         * items from these slots. Marking a slot as insertion only and output at the same
         * time will cause an exception.
         */
        public Builder insertionOnly(int... slots) {
            for (int slot : slots) {
                this.insertionOnlySlots.add(slot);
            }
            return this;
        }

        /**
         * Marks the given slots as insertion only. That means it's not possible to extract
         * items from these slots. Marking a slot as insertion only and output at the same
         * time will cause an exception.
         */
        public Builder insertionOnly(Set<Integer> slots) {
            this.insertionOnlySlots.addAll(slots);
            return this;
        }

        /**
         * Sets the default maximum stack size for the item handler.
         */
        public Builder defaultSlotLimit(int defaultSlotLimit) {
            this.defaultSlotLimit = defaultSlotLimit;
            return this;
        }
        
        /**
         * Sets a maximum stack size for some slots.
         */
        public Builder slotLimit(int slotLimit, int... slots) {
            for (int slot : slots) {
                this.slotLimits.put(slot, slotLimit);
            }
            return this;
        }
        
        /**
         * Sets a maximum stack size for some slots.
         */
        public Builder slotLimit(int slotLimit, Set<Integer> slots) {
            for (int slot : slots) {
                this.slotLimits.put(slot, slotLimit);
            }
            return this;
        }
        
        /**
         * Sets a slot validator for some slots.
         */
        public Builder validator(Predicate<ItemStack> validator, int... slots) {
            for (int slot : slots) {
                this.slotValidators.put(slot, validator);
            }
            return this;
        }
        
        /**
         * Sets a slot validator for some slots.
         */
        public Builder validator(Predicate<ItemStack> validator, Set<Integer> slots) {
            for (int slot : slots) {
                this.slotValidators.put(slot, validator);
            }
            return this;
        }

        /**
         * Build the item handler.
         */
        public BaseItemStackHandler build() {
            if (this.outputSlots.stream().anyMatch(this.insertionOnlySlots::contains)) {
                throw new IllegalStateException("Can't build BaseItemStackHandler: A slot can not be an insertion only and an output slot at the same time.");
            }
            return new BaseItemStackHandler(this.size, this.defaultSlotLimit, this.insertionOnlySlots, this.outputSlots, this.slotLimits, this.slotValidators, this.contentsChanged);
        }
    }
}
