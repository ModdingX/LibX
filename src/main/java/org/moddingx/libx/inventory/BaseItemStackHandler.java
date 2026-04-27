package org.moddingx.libx.inventory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.moddingx.libx.impl.inventory.AdvancedItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * An {@link ItemStacksResourceHandler} that can be configured with common things required for many inventories.
 * To get a BaseItemStackHandler, use {@link #builder(int)}.
 */
public class BaseItemStackHandler extends ItemStacksResourceHandler implements IAdvancedItemHandlerModifiable {

    private final int size;
    private final int defaultSlotLimit;
    private final Set<Integer> insertionOnlySlots;
    private final Set<Integer> outputSlots;
    private final Map<Integer, Integer> slotLimits;
    private final Map<Integer, Predicate<ItemStack>> slotValidators;
    private final Consumer<Integer> contentsChanged;
    private final List<SlotJournal> slotJournals;

    private Container vanilla = null;
    private Unrestricted unrestricted = null;

    private BaseItemStackHandler(int size, int defaultSlotLimit, Set<Integer> insertionOnlySlots, Set<Integer> outputSlots, Map<Integer, Integer> slotLimits, Map<Integer, Predicate<ItemStack>> slotValidators, Consumer<Integer> contentsChanged) {
        super(size);
        this.size = size;
        this.defaultSlotLimit = defaultSlotLimit;
        this.insertionOnlySlots = ImmutableSet.copyOf(insertionOnlySlots);
        this.outputSlots = ImmutableSet.copyOf(outputSlots);
        this.slotLimits = ImmutableMap.copyOf(slotLimits);
        this.slotValidators = ImmutableMap.copyOf(slotValidators);
        this.contentsChanged = contentsChanged;
        List<SlotJournal> journals = new ArrayList<>(size);
        for (int i = 0; i < size; i++) journals.add(new SlotJournal(i));
        this.slotJournals = Collections.unmodifiableList(journals);
    }

    void updateSnapshots(int index, TransactionContext transaction) {
        this.slotJournals.get(index).updateSnapshots(transaction);
    }

    private class SlotJournal extends SnapshotJournal<ItemStack> {
        private final int index;

        SlotJournal(int index) {
            this.index = index;
        }

        @Override
        protected ItemStack createSnapshot() {
            return BaseItemStackHandler.this.stacks.get(this.index).copy();
        }

        @Override
        protected void revertToSnapshot(ItemStack snapshot) {
            BaseItemStackHandler.this.stacks.set(this.index, snapshot);
        }

        @Override
        protected void onRootCommit(ItemStack originalState) {
            BaseItemStackHandler.this.onContentsChanged(this.index, originalState);
        }
    }

    @Override
    public boolean isValid(int slot, ItemResource resource) {
        return !resource.isEmpty() && (!this.slotValidators.containsKey(slot) || this.slotValidators.get(slot).test(resource.toStack()));
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        if (resource.isEmpty()) {
            return this.slotLimits.getOrDefault(index, this.defaultSlotLimit);
        }
        return Math.min(this.slotLimits.getOrDefault(index, this.defaultSlotLimit), resource.getMaxStackSize());
    }

    @Override
    public int insert(int index, @Nonnull ItemResource resource, int amount, @Nonnull TransactionContext tx) {
        return this.outputSlots.contains(index) ? 0 : super.insert(index, resource, amount, tx);
    }

    @Override
    public int extract(int index, @Nonnull ItemResource resource, int amount, @Nonnull TransactionContext tx) {
        return this.insertionOnlySlots.contains(index) ? 0 : super.extract(index, resource, amount, tx);
    }

    @Override
    protected void onContentsChanged(int index, @Nonnull ItemStack previousContents) {
        if (this.contentsChanged != null) {
            this.contentsChanged.accept(index);
        }
    }

    @Override
    public void deserialize(@Nonnull ValueInput input) {
        super.deserialize(input);
        if (this.stacks.size() != this.size) {
            // BaseItemStackHandler does not allow setting the size through NBT
            // Don't clear the contents, just resize
            NonNullList<ItemStack> oldStacks = this.stacks;
            this.stacks = NonNullList.withSize(this.size, ItemStack.EMPTY);
            for (int slot = 0; slot < Math.min(oldStacks.size(), this.size); slot++) {
                this.stacks.set(slot, oldStacks.get(slot));
            }
        }
    }

    @Override
    public boolean hasSpaceFor(List<ItemStack> stacks, int startInclusive, int endExclusive) {
        return AdvancedItemHandlerHelper.hasSpaceFor(this, stacks, startInclusive, endExclusive);
    }

    /**
     * Gets a vanilla container that wraps around this item handler.
     */
    public Container toVanilla() {
        if (this.vanilla == null) this.vanilla = new VanillaWrapper(this, null);
        return this.vanilla;
    }

    /**
     * Gets an item handler that wraps around this item handler but has no checks on which items are
     * valid for a slot.
     */
    public IAdvancedItemHandlerModifiable getUnrestricted() {
        if (this.unrestricted == null) this.unrestricted = new Unrestricted();
        return this.unrestricted;
    }

    /**
     * Creates a new {@link Builder} for an item handler with the given size.
     */
    public static Builder builder(int size) {
        return new Builder(size);
    }

    private class Unrestricted implements IAdvancedItemHandlerModifiable {

        @Override
        public void set(int index, ItemResource resource, int amount) {
            BaseItemStackHandler.this.set(index, resource, amount);
        }

        @Override
        public int size() {
            return BaseItemStackHandler.this.size();
        }

        @Nonnull
        @Override
        public ItemResource getResource(int index) {
            return BaseItemStackHandler.this.getResource(index);
        }

        @Override
        public long getAmountAsLong(int index) {
            return BaseItemStackHandler.this.getAmountAsLong(index);
        }

        @Override
        public long getCapacityAsLong(int index, @Nonnull ItemResource resource) {
            return BaseItemStackHandler.this.getCapacityAsLong(index, resource);
        }

        @Override
        public boolean isValid(int index, @Nonnull ItemResource resource) {
            return true;
        }

        @Override
        public int insert(int index, @Nonnull ItemResource resource, int amount, @Nonnull TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0) return 0;
            ItemStack current = BaseItemStackHandler.this.stacks.get(index);
            int capacity = BaseItemStackHandler.this.getCapacity(index, resource);
            if (!current.isEmpty()) {
                if (!ItemResource.of(current).equals(resource)) return 0;
                capacity -= current.getCount();
            }
            if (capacity <= 0) return 0;
            int toInsert = Math.min(amount, capacity);
            BaseItemStackHandler.this.updateSnapshots(index, transaction);
            if (current.isEmpty()) {
                BaseItemStackHandler.this.stacks.set(index, resource.toStack(toInsert));
            } else {
                current.grow(toInsert);
            }
            return toInsert;
        }

        @Override
        public int extract(int index, @Nonnull ItemResource resource, int amount, @Nonnull TransactionContext transaction) {
            if (amount <= 0) return 0;
            ItemStack current = BaseItemStackHandler.this.stacks.get(index);
            if (current.isEmpty() || !ItemResource.of(current).equals(resource)) return 0;
            int toExtract = Math.min(current.getCount(), amount);
            BaseItemStackHandler.this.updateSnapshots(index, transaction);
            BaseItemStackHandler.this.stacks.set(index, current.copyWithCount(current.getCount() - toExtract));
            return toExtract;
        }
    }

    /**
     * Builder for a {@link BaseItemStackHandler}
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
         * Adds an action that runs whenever the contents of the inventory change.
         */
        public Builder contentsChanged(Runnable action) {
            return this.contentsChanged(slot -> action.run());
        }

        /**
         * Adds an action that runs whenever the contents of the inventory change. The action
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
         * Marks the given slots as outputs. That means it's not possible to insert items
         * into these slots. Marking a slot as insertion only and output at the same time
         * will cause an exception.
         */
        public Builder output(Range<Integer> slots) {
            IntStream.range(0, this.size).filter(slots::contains).forEach(this.outputSlots::add);
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
         * Marks the given slots as insertion only. That means it's not possible to extract
         * items from these slots. Marking a slot as insertion only and output at the same
         * time will cause an exception.
         */
        public Builder insertionOnly(Range<Integer> slots) {
            IntStream.range(0, this.size).filter(slots::contains).forEach(this.insertionOnlySlots::add);
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
         * Sets a maximum stack size for some slots.
         */
        public Builder slotLimit(int slotLimit, Range<Integer> slots) {
            IntStream.range(0, this.size).filter(slots::contains).forEach(slot -> this.slotLimits.put(slot, slotLimit));
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
         * Sets a slot validator for some slots.
         */
        public Builder validator(Predicate<ItemStack> validator, Range<Integer> slots) {
            IntStream.range(0, this.size).filter(slots::contains).forEach(slot -> this.slotValidators.put(slot, validator));
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
