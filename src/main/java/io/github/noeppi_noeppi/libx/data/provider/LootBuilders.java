package io.github.noeppi_noeppi.libx.data.provider;

import io.github.noeppi_noeppi.libx.impl.loot.AllLootEntry;
import net.minecraft.world.level.storage.loot.entries.EntryGroup;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.SequentialEntry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class LootBuilders {

    public static class AllLootBuilder extends LootPoolEntryContainer.Builder<AllLootBuilder> {

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

    public static class GroupLootBuilder extends LootPoolEntryContainer.Builder<GroupLootBuilder> {

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

    public static class SequenceLootBuilder extends LootPoolEntryContainer.Builder<SequenceLootBuilder> {

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
