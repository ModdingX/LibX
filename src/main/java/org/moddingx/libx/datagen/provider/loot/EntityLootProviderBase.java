package org.moddingx.libx.datagen.provider.loot;

import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.EnchantmentPredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.predicates.entity.EntityEquipmentPredicate;
import net.minecraft.advancements.predicates.entity.EntityFlagsPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.loot.entry.LootModifier;

import javax.annotation.Nullable;
import java.util.List;

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
        HolderGetter<Enchantment> enchantments = this.registries.registry(Registries.ENCHANTMENT);

        return this.modifier((entity, entry) -> entry.apply(EnchantedCountIncreaseFunction.lootingMultiplier(enchantments, ContextFloatProviders.between(min, max))));
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
        return this.modifier((entity, entry) -> entry.apply(SmeltItemFunction.smelted().when(this.shouldSmeltLoot())));
    }

    // Same as EntityLootSubProvider#shouldSmeltLoot
    private LootItemCondition.Builder shouldSmeltLoot() {
        return AnyOfCondition.anyOf(
                this.fire(),
                LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.DIRECT_ATTACKER,
                        EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().mainhand(
                                ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(
                                        DataComponentPredicates.ENCHANTMENTS,
                                        EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(this.set(EnchantmentTags.SMELTS_LOOT), MinMaxBounds.Ints.ANY)))
                                ).build())
                        ))
                )
        );
    }
}
