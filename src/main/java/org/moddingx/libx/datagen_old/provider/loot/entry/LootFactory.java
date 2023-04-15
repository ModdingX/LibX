package org.moddingx.libx.datagen_old.provider.loot.entry;

import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Interface to get a loot entry from an item.
 */
@FunctionalInterface
public interface LootFactory<T> {

    /**
     * Gets a loot factory that will always return the given loot entry.
     */
    static <T> LootFactory<T> from(LootPoolEntryContainer.Builder<?> builder) {
        return item -> builder;
    }

    LootPoolEntryContainer.Builder<?> build(T block);

    default LootFactory<T> with(LootItemCondition.Builder... conditions) {
        return item -> {
            LootPoolEntryContainer.Builder<?> entry = this.build(item);
            for (LootItemCondition.Builder condition : conditions) {
                entry.when(condition);
            }
            return entry;
        };
    }
}
