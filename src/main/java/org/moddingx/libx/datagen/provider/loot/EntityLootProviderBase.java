package org.moddingx.libx.datagen.provider.loot;

import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.loot.entry.LootModifier;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public abstract class EntityLootProviderBase extends LootProviderBase<EntityType<?>> {

    protected EntityLootProviderBase(DatagenContext ctx) {
        super(ctx, "entities", LootContextParamSets.ENTITY, Registries.ENTITY_TYPE);
    }

    @Nullable
    @Override
    protected LootTable.Builder defaultBehavior(EntityType<?> item) {
        return null;
    }
    
    /**
     * Gets a loot modifier for the looting enchantment.
     * 
     * @param max The maximum amount of additional drops.
     */
    public LootModifier<EntityType<?>> looting(int max) {
        return this.looting(0, max);
    }
    
    /**
     * Gets a loot modifier for the looting enchantment.
     * 
     * @param min The minimum amount of additional drops.
     * @param max The maximum amount of additional drops.
     */
    public LootModifier<EntityType<?>> looting(int min, int max) {
        HolderLookup.Provider enchantmentHolderProvider = HolderLookup.Provider.create(Stream.of(this.registries.registry(Registries.ENCHANTMENT)));
        return this.modifier((entity, entry) -> entry.apply(EnchantedCountIncreaseFunction.lootingMultiplier(enchantmentHolderProvider, UniformGenerator.between(min, max))));
    }

    /**
     * Gets a loot condition that checks, whether the killed entity was on fire. 
     */
    public LootItemCondition.Builder fire() {
        return LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnFire(true)));
    }
    
    /**
     * Gets a loot modifier that smelts the item, if the killed entity was on fire.
     */
    public LootModifier<EntityType<?>> smeltOnFire() {
        return this.modifier((entity, entry) -> entry.apply(SmeltItemFunction.smelted().when(new GiveMeAccessToShouldSmeltLoot().accessibleShouldSmeltLoot())));
    }
    
    private static class GiveMeAccessToShouldSmeltLoot extends EntityLootSubProvider {

        protected GiveMeAccessToShouldSmeltLoot() {
            super(FeatureFlagSet.of(), HolderLookup.Provider.create(Stream.of()));
        }

        @Override
        public void generate() {
            //
        }
        
        public LootItemCondition.Builder accessibleShouldSmeltLoot() {
            return this.shouldSmeltLoot();
        }
    }
}
