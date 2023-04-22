package org.moddingx.libx.datagen.provider.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.AlternativeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.common.loot.LootTableIdCondition;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.impl.loot.modifier.AdditionLootModifier;
import org.moddingx.libx.impl.loot.modifier.RemovalLootModifier;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Data provider for global loot modifiers.
 */
public abstract class GlobalLootProviderBase extends GlobalLootModifierProvider {
    
    protected final ModX mod;
    
    public GlobalLootProviderBase(DatagenContext ctx) {
        super(ctx.output(), ctx.mod().modid);
        this.mod = ctx.mod();
    }

    protected abstract void setup();
    
    @Override
    protected final void start() {
        this.setup();
    }

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " loot modifiers";
    }

    /**
     * Creates a loot modifier builder for a modifier that adds the given loot table as loot.
     */
    public LootModifierBuilder addition(String name, ResourceLocation lootTable) {
        return this.modifier(name, conditions -> new AdditionLootModifier(lootTable, conditions));
    }
    
    /**
     * Creates a loot modifier builder for a modifier that removes the given items from the loot.
     */
    public LootModifierBuilder removal(String name, Item... items) {
        return this.modifier(name, conditions -> new RemovalLootModifier(List.of(items), conditions));
    }

    /**
     * Creates a new loot modifier builder with a name and factory.
     */
    public LootModifierBuilder modifier(String name, Function<LootItemCondition[], ? extends LootModifier> factory) {
        return new LootModifierBuilder(name, factory);
    }
    
    /**
     * A standalone builder for loot conditions.
     */
    public LootConditionsBuilder conditions() {
        return new LootConditionsBuilder();
    }

    /**
     * A builder for a loot modifier that accepts some conditions.
     */
    public class LootModifierBuilder extends AnyLootConditionsBuilder<LootModifierBuilder> {
        
        private final String name;
        private final Function<LootItemCondition[], ? extends LootModifier> factory;

        private LootModifierBuilder(String name, Function<LootItemCondition[], ? extends LootModifier> factory) {
            this.name = name;
            this.factory = factory;
        }

        @Override
        protected LootModifierBuilder self() {
            return this;
        }

        /**
         * Builds the loot modifier and adds it to the provider.
         */
        public void build() {
            GlobalLootProviderBase.this.add(this.name, this.factory.apply(this.buildConditions().toArray(LootItemCondition[]::new)));
        }
    }
    
    public static class LootConditionsBuilder extends AnyLootConditionsBuilder<LootConditionsBuilder> {
        
        private LootConditionsBuilder() {
            
        }

        @Override
        protected LootConditionsBuilder self() {
            return this;
        }
    }

    /**
     * A builder class for loot conditions.
     */
    public abstract static class AnyLootConditionsBuilder<T extends AnyLootConditionsBuilder<T>> {
        
        private final List<LootItemCondition> conditions;

        private AnyLootConditionsBuilder() {
            this.conditions = new ArrayList<>();
        }
        
        protected abstract T self();

        /**
         * Adds a loot condition that matches if any of the given conditions matches.
         */
        public T or(LootConditionsBuilder conditions) {
            return this.condition(AlternativeLootItemCondition.alternative(((AnyLootConditionsBuilder<?>) conditions).buildConditionBuilders()).build());
        }

        /**
         * Adds a condition for the queried loot table.
         */
        public T forLootTable(String lootTableNamespace, String lootTablePath) {
             return this.forLootTable(new ResourceLocation(lootTableNamespace, lootTablePath));
        }

        /**
         * Adds a condition for the queried loot table.
         */
        public T forLootTable(ResourceLocation lootTable) {
            return this.condition(LootTableIdCondition.builder(lootTable).build());
        }

        /**
         * Adds a new condition to the list.
         */
        public T condition(LootItemCondition condition) {
            this.conditions.add(condition);
            return this.self();
        }

        /**
         * Inverts the last added condition.
         */
        public T inverted() {
            if (this.conditions.isEmpty()) {
                throw new IllegalStateException("Can't invert last loot condition: No conditions.");
            }
            LootItemCondition cond = this.conditions.remove(this.conditions.size() - 1);
            this.conditions.add(InvertedLootItemCondition.invert(() -> cond).build());
            return this.self();
        }
        
        public List<LootItemCondition> buildConditions() {
            return List.copyOf(this.conditions);
        }
        
        private LootItemCondition.Builder[] buildConditionBuilders() {
            return this.conditions.stream().<LootItemCondition.Builder>map(condition -> () -> condition).toArray(LootItemCondition.Builder[]::new);
        }
    }
}
