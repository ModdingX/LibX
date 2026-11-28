package org.moddingx.libx.datagen.provider.loot.entry;

import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
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
    static <T> SimpleLootFactory<T> from(UniformContainerBase.Builder<?> builder) {
        return item -> builder;
    }

    @Override
    UniformContainerBase.Builder<?> build(T item);

    default LootFactory<T> withFinal(GenericLootModifier<T> finalModifier) {
        return b -> finalModifier.apply(b, this.build(b));
    }

    @SuppressWarnings("unchecked")
    default SimpleLootFactory<T> with(LootModifier<T>... modifiers) {
        return this.with(Arrays.stream(modifiers).toList());
    }

    default SimpleLootFactory<T> with(List<LootModifier<T>> modifiers) {
        if (modifiers.isEmpty()) return this;
        LootModifier<T> chained = LootModifier.chain(modifiers.getFirst().element(), modifiers);
        return b -> chained.apply(b, this.build(b));
    }

    @Override
    default SimpleLootFactory<T> with(LootItemCondition.Builder... conditions) {
        return b -> {
            UniformContainerBase.Builder<?> entry = this.build(b);
            for (LootItemCondition.Builder condition : conditions) {
                entry.when(condition);
            }
            return entry;
        };
    }

    default SimpleLootFactory<T> with(LootItemConditionalFunction.Builder<?>... functions) {
        return item -> {
            UniformContainerBase.Builder<?> entry = this.build(item);
            for (LootItemConditionalFunction.Builder<?> function : functions) {
                entry = entry.apply(function);
            }
            return entry;
        };
    }
}
