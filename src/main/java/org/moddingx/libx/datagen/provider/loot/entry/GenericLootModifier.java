package org.moddingx.libx.datagen.provider.loot.entry;

import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;

import java.util.function.BiFunction;

/**
 * Interface to modify a singleton loot entry to a new loot entry. This can also be used
 * as a {@link LootFactory} in which case the default item entry obtained from is passed to the
 * {@link #apply(Object, LootPoolSingletonContainer.Builder) apply} method.
 */
public interface GenericLootModifier<T> extends LootFactory<T> {
    
    /**
     * Gets a loot modifier that does nothing.
     */
    static <T> GenericLootModifier<T> identity(SimpleLootFactory<T> element) {
        return of(element, (item, entry) -> entry);
    }
    
    /**
     * Creates a new generic lot modifier.
     */
    static <T> GenericLootModifier<T> of(SimpleLootFactory<T> element, BiFunction<T, LootPoolSingletonContainer.Builder<?>, LootPoolEntryContainer.Builder<?>> function) {
        return new GenericLootModifier<>() {
            
            @Override
            public LootPoolEntryContainer.Builder<?> apply(T item, LootPoolSingletonContainer.Builder<?> entry) {
                return function.apply(item, entry);
            }

            @Override
            public SimpleLootFactory<T> element() {
                return element;
            }
        };
    }

    LootPoolEntryContainer.Builder<?> apply(T item, LootPoolSingletonContainer.Builder<?> entry);
    SimpleLootFactory<T> element();
    
    @Override
    default LootPoolEntryContainer.Builder<?> build(T item) {
        return this.apply(item, this.element().build(item));
    }
}
