package org.moddingx.libx.datagen.provider.loot.entry;

import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.List;

/**
 * Interface to get a singleton loot entry from an item.
 */
@FunctionalInterface
public interface SimpleLootFactory<T> extends LootFactory<T> {

    /**
     * Gets a simple loot factory that will always return the given loot entry.
     */
    static <T> SimpleLootFactory<T> from(LootPoolSingletonContainer.Builder<?> builder) {
        return item -> builder;
    }

    @Override
    LootPoolSingletonContainer.Builder<?> build(T item);

    default LootFactory<T> withFinal(GenericLootModifier<T> finalModifier) {
        return b -> finalModifier.apply(b, this.build(b));
    }

    @SuppressWarnings("unchecked")
    default SimpleLootFactory<T> with(LootModifier<T>... modifiers) {
        return this.with(Arrays.stream(modifiers).toList());
    }
    
    default SimpleLootFactory<T> with(List<LootModifier<T>> modifiers) {
        if (modifiers.isEmpty()) return this;
        LootModifier<T> chained = LootModifier.chain(modifiers.get(0).element(), modifiers);
        return b -> chained.apply(b, this.build(b));
    }

    @Override
    default SimpleLootFactory<T> with(LootItemCondition.Builder... conditions) {
        return b -> {
            LootPoolSingletonContainer.Builder<?> entry = this.build(b);
            for (LootItemCondition.Builder condition : conditions) {
                entry.when(condition);
            }
            return entry;
        };
    }

    default SimpleLootFactory<T> with(LootItemConditionalFunction.Builder<?>... functions) {
        return item -> {
            LootPoolSingletonContainer.Builder<?> entry = this.build(item);
            for (LootItemConditionalFunction.Builder<?> function : functions) {
                entry = entry.apply(function);
            }
            return entry;
        };
    }
}
