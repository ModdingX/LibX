package org.moddingx.libx.datagen.provider.loot;

import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.datagen.provider.loot.entry.LootModifier;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nullable;

public abstract class EntityLootProviderBase extends LootProviderBase<EntityType<?>> {

    protected EntityLootProviderBase(ModX mod, PackOutput packOutput) {
        super(mod, packOutput, "entities", LootContextParamSets.ENTITY, ForgeRegistries.ENTITY_TYPES);
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
        return this.modifier((entity, entry) -> entry.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(min, max))));
    }

    /**
     * Gets a loot condition that checks, whether the killed entity was on fire. 
     */
    public LootItemCondition.Builder fire() {
        return LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnFire(true).build()));
    }
    
    /**
     * Gets a loot modifier that smelts the item, if the killed entity was on fire.
     */
    public LootModifier<EntityType<?>> smeltOnFire() {
        return this.modifier((entity, entry) -> entry.apply(SmeltItemFunction.smelted().when(this.fire())));
    }
}
