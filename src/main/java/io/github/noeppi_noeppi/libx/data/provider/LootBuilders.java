package io.github.noeppi_noeppi.libx.data.provider;

import io.github.noeppi_noeppi.libx.impl.loot.AllLootEntry;
import net.minecraft.loot.GroupLootEntry;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.SequenceLootEntry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class LootBuilders {

    public static class AllLootBuilder extends LootEntry.Builder<AllLootBuilder> {

        private final List<LootEntry> lootEntries = new ArrayList<>();

        public AllLootBuilder(LootEntry.Builder<?>... entries) {
            for (LootEntry.Builder<?> builder : entries) {
                this.lootEntries.add(builder.build());
            }
        }

        @Nonnull
        @Override
        protected AllLootBuilder getSelf() {
            return this;
        }

        public AllLootBuilder add(LootEntry.Builder<?> entry) {
            this.lootEntries.add(entry.build());
            return this;
        }

        @Nonnull
        @Override
        public LootEntry build() {
            return new AllLootEntry(this.lootEntries.toArray(new LootEntry[0]), this.getConditions());
        }
    }

    public static class GroupLootBuilder extends LootEntry.Builder<GroupLootBuilder> {

        private final List<LootEntry> lootEntries = new ArrayList<>();

        public GroupLootBuilder(LootEntry.Builder<?>... entries) {
            for (LootEntry.Builder<?> builder : entries) {
                this.lootEntries.add(builder.build());
            }
        }

        @Nonnull
        @Override
        protected GroupLootBuilder getSelf() {
            return this;
        }

        public GroupLootBuilder add(LootEntry.Builder<?> entry) {
            this.lootEntries.add(entry.build());
            return this;
        }

        @Nonnull
        @Override
        public LootEntry build() {
            return new GroupLootEntry(this.lootEntries.toArray(new LootEntry[0]), this.getConditions());
        }
    }

    public static class SequenceLootBuilder extends LootEntry.Builder<SequenceLootBuilder> {

        private final List<LootEntry> lootEntries = new ArrayList<>();

        public SequenceLootBuilder(LootEntry.Builder<?>... entries) {
            for (LootEntry.Builder<?> builder : entries) {
                this.lootEntries.add(builder.build());
            }
        }

        @Nonnull
        @Override
        protected SequenceLootBuilder getSelf() {
            return this;
        }

        public SequenceLootBuilder add(LootEntry.Builder<?> entry) {
            this.lootEntries.add(entry.build());
            return this;
        }

        @Nonnull
        @Override
        public LootEntry build() {
            return new SequenceLootEntry(this.lootEntries.toArray(new LootEntry[0]), this.getConditions());
        }
    }
}
