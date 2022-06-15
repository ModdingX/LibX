package org.moddingx.libx.datagen.provider.loot.entry;

import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Interface to modify a singleton loot entry to a new singleton loot entry. This can also be
 * used as a {@link SimpleLootFactory} in which case the default item entry is passed to the
 * {@link #apply(Object, LootPoolSingletonContainer.Builder) apply} method.
 */
public interface LootModifier<T> extends GenericLootModifier<T>, SimpleLootFactory<T> {

    /**
     * Gets a loot modifier that does nothing.
     */
    static <T> LootModifier<T> identity(SimpleLootFactory<T> element) {
        return of(element, (item, entry) -> entry);
    }
    
    /**
     * Creates a new generic lot modifier.
     */
    static <T> LootModifier<T> of(SimpleLootFactory<T> element, BiFunction<T, LootPoolSingletonContainer.Builder<?>, LootPoolSingletonContainer.Builder<?>> function) {
        return new LootModifier<>() {
            
            @Override
            public LootPoolSingletonContainer.Builder<?> apply(T item, LootPoolSingletonContainer.Builder<?> entry) {
                return function.apply(item, entry);
            }

            @Override
            public SimpleLootFactory<T> element() {
                return element;
            }
        };
    }

    /**
     * Get a new loot modifier that chains all the given together.
     * (Applies the first, then the second to the result of the first and so on)
     */
    static <T> LootModifier<T> chain(SimpleLootFactory<T> element, List<LootModifier<T>> children) {
        if (children.size() == 0) {
            return identity(element);
        } else if (children.size() == 1) {
            return children.get(0);
        } else {
            return of(element, (item, entry) -> {
                for (LootModifier<T> modifier : children) {
                    entry = modifier.apply(item, entry);
                }
                return entry;
            });
        }
    }

    @Override
    LootPoolSingletonContainer.Builder<?> apply(T item, LootPoolSingletonContainer.Builder<?> entry);

    @Override
    default LootPoolSingletonContainer.Builder<?> build(T item) {
        return this.apply(item, this.element().build(item));
    }

    /**
     * Same as {@link #chain(SimpleLootFactory, List)}
     */
    default LootModifier<T> andThen(LootModifier<T> other) {
        return chain(this.element(), List.of(this, other));
    }
}
