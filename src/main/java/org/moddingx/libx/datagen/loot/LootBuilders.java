package org.moddingx.libx.datagen.loot;

import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.EntryGroup;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.SequentialEntry;
import org.moddingx.libx.impl.loot.AllLootEntry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains builders vor various loot table entry containers.
 */
public class LootBuilders {

    /**
     * Gets a loot builder for a loot entry that selects all items fro mall its children at once.
     */
    public static LootPoolEntryContainer.Builder<?> all(List<LootPoolEntryContainer.Builder<?>> entries) {
        return new AllLootBuilder(entries.toArray(new LootPoolEntryContainer.Builder[0]));
    }
    
    /**
     * Gets a loot builder for group loot.
     */
    public static LootPoolEntryContainer.Builder<?> group(List<LootPoolEntryContainer.Builder<?>> entries) {
        return new GroupLootBuilder(entries.toArray(new LootPoolEntryContainer.Builder[0]));
    }
    
    /**
     * Gets a loot builder for alternative loot.
     */
    public static LootPoolEntryContainer.Builder<?> alternative(List<LootPoolEntryContainer.Builder<?>> entries) {
        return AlternativesEntry.alternatives(entries.toArray(new LootPoolEntryContainer.Builder[0]));
    }
    
    /**
     * Gets a loot builder for sequence loot.
     */
    public static LootPoolEntryContainer.Builder<?> sequence(List<LootPoolEntryContainer.Builder<?>> entries) {
        return new SequenceLootBuilder(entries.toArray(new LootPoolEntryContainer.Builder[0]));
    }
    
    private static class AllLootBuilder extends LootPoolEntryContainer.Builder<AllLootBuilder> {

        private final List<LootPoolEntryContainer> lootEntries = new ArrayList<>();

        public AllLootBuilder(LootPoolEntryContainer.Builder<?>... entries) {
            for (LootPoolEntryContainer.Builder<?> builder : entries) {
                this.lootEntries.add(builder.build());
            }
        }

        @Nonnull
        @Override
        protected AllLootBuilder getThis() {
            return this;
        }

        public AllLootBuilder add(LootPoolEntryContainer.Builder<?> entry) {
            this.lootEntries.add(entry.build());
            return this;
        }

        @Nonnull
        @Override
        public LootPoolEntryContainer build() {
            return new AllLootEntry(this.lootEntries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
        }
    }

    private static class GroupLootBuilder extends LootPoolEntryContainer.Builder<GroupLootBuilder> {

        private final List<LootPoolEntryContainer> lootEntries = new ArrayList<>();

        public GroupLootBuilder(LootPoolEntryContainer.Builder<?>... entries) {
            for (LootPoolEntryContainer.Builder<?> builder : entries) {
                this.lootEntries.add(builder.build());
            }
        }

        @Nonnull
        @Override
        protected GroupLootBuilder getThis() {
            return this;
        }

        public GroupLootBuilder add(LootPoolEntryContainer.Builder<?> entry) {
            this.lootEntries.add(entry.build());
            return this;
        }

        @Nonnull
        @Override
        public LootPoolEntryContainer build() {
            return new EntryGroup(this.lootEntries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
        }
    }

    private static class SequenceLootBuilder extends LootPoolEntryContainer.Builder<SequenceLootBuilder> {

        private final List<LootPoolEntryContainer> lootEntries = new ArrayList<>();

        public SequenceLootBuilder(LootPoolEntryContainer.Builder<?>... entries) {
            for (LootPoolEntryContainer.Builder<?> builder : entries) {
                this.lootEntries.add(builder.build());
            }
        }

        @Nonnull
        @Override
        protected SequenceLootBuilder getThis() {
            return this;
        }

        public SequenceLootBuilder add(LootPoolEntryContainer.Builder<?> entry) {
            this.lootEntries.add(entry.build());
            return this;
        }

        @Nonnull
        @Override
        public LootPoolEntryContainer build() {
            return new SequentialEntry(this.lootEntries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
        }
    }
}
