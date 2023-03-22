package org.moddingx.libx.datagen.provider.loot;

import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.datagen.LootBuilders;
import org.moddingx.libx.datagen.provider.loot.entry.GenericLootModifier;
import org.moddingx.libx.datagen.provider.loot.entry.LootFactory;
import org.moddingx.libx.datagen.provider.loot.entry.LootModifier;
import org.moddingx.libx.datagen.provider.loot.entry.SimpleLootFactory;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public abstract class BlockLootProviderBase extends LootProviderBase<Block> {

    protected BlockLootProviderBase(ModX mod, PackOutput packOutput) {
        super(mod, packOutput, "blocks", LootContextParamSets.BLOCK, ForgeRegistries.BLOCKS);
    }
    
    @Nullable
    @Override
    protected LootTable.Builder defaultBehavior(Block block) {
        if (block.getStateDefinition().getPossibleStates().stream().anyMatch(this::needsLootTable)) {
            LootPoolEntryContainer.Builder<?> entry = LootItem.lootTableItem(block);
            LootPool.Builder pool = LootPool.lootPool().name("main").setRolls(ConstantValue.exactly(1)).add(entry)
                    .when(ExplosionCondition.survivesExplosion());
            if (block.defaultBlockState().hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                pool = pool.when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(
                        StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
                ));
            }
            if (block.defaultBlockState().hasProperty(BlockStateProperties.BED_PART)) {
                pool = pool.when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(
                        StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.BED_PART, BedPart.HEAD)
                ));
            }
            return LootTable.lootTable().withPool(pool);
        } else {
            return null;
        }
    }

    /**
     * Returns whether this block state needs a loot table. If all block states of a block don't
     * need a loot table, defaultBehavior will return null for that block. Can be overridden to
     * alter the behaviour.
     */
    protected boolean needsLootTable(BlockState state) {
        return !state.isAir() && state.getFluidState().createLegacyBlock().getBlock() != state.getBlock()
                && !BuiltInLootTables.EMPTY.equals(state.getBlock().getLootTable());
    }

    @Override
    public void generateBaseTable(Block block, LootPoolEntryContainer.Builder<?> entry) {
        LootPool.Builder pool = LootPool.lootPool().name("main")
                .setRolls(ConstantValue.exactly(1)).add(entry)
                .when(ExplosionCondition.survivesExplosion());
        this.customLootTable(block, LootTable.lootTable().withPool(pool));
    }

    @Override
    protected SimpleLootFactory<Block> element() {
        return LootItem::lootTableItem;
    }

    @Override
    public void drops(Block block, ItemStack... drops) {
        this.drops(block, true, drops);
    }

    @Override
    public void drops(Block block, List<LootFactory<Block>> loot) {
        this.drops(block, true, loot);
    }

    public void drops(Block block, boolean silkTouch, ItemStack... drops) {
        this.drops(block, silkTouch ? this.silk(this.identity()) : this.noSilk(), drops);
    }
    
    @SafeVarargs
    public final void drops(Block block, boolean silkTouch, LootFactory<Block>... drops) {
        this.drops(block, silkTouch, Arrays.stream(drops).toList());
    }
    
    public void drops(Block block, boolean silkTouch, List<LootFactory<Block>> drops) {
        this.drops(block, silkTouch ? this.silk(this.identity()) : this.noSilk(), drops);
    }
    
    public void drops(Block block, SilkModifier silkTouch, ItemStack... drops) {
        this.drops(block, silkTouch, Arrays.stream(drops).<LootFactory<Block>>map(this::stack).toList());
    }
    
    @SafeVarargs
    public final void drops(Block block, SilkModifier silkTouch, LootFactory<Block>... drops) {
        this.drops(block, silkTouch, Arrays.stream(drops).toList());
    }
    
    public void drops(Block block, SilkModifier silkTouch, List<LootFactory<Block>> drops) {
        LootPoolEntryContainer.Builder<?> entry = this.combine(drops).build(block);
        if (silkTouch.modifier != null) {
            LootPoolEntryContainer.Builder<?> silkBuilder = silkTouch.modifier.apply(block, this.element().build(block)).when(this.silkCondition());
            entry = LootBuilders.alternative(List.of(silkBuilder, entry));
        }
        this.generateBaseTable(block, entry);
    }
    
    /**
     * Turns a generic loot modifier into a silk modifier. This exists to reduce ambiguity.
     * A silk modifier does not extend {@link GenericLootModifier} for this reason.
     */
    public SilkModifier silk(GenericLootModifier<Block> modifier) {
        return new SilkModifier(modifier);
    }
    
    /**
     * Gets a new silk modifier that means: No special silk touch behaviour.
     */
    public SilkModifier noSilk() {
        return new SilkModifier(null);
    }
    
    /**
     * A loot modifier to apply fortune based on the formula used for ores.
     */
    public LootModifier<Block> fortuneOres() {
        return this.modifier((block, entry) -> entry.apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)));
    }
    
    /**
     * A loot modifier to apply fortune based on a uniform formula.
     */
    public LootModifier<Block> fortuneUniform() {
        return this.fortuneUniform(1);
    }
    
    /**
     * A loot modifier to apply fortune based on a uniform formula.
     */
    public LootModifier<Block> fortuneUniform(int multiplier) {
        return this.modifier((block, entry) -> entry.apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, multiplier)));
    }

    /**
     * A loot modifier to apply fortune based on a binomial formula.
     */
    public LootModifier<Block> fortuneBinomial(float probability) {
        return this.fortuneBinomial(probability, 0);
    }
    
    /**
     * A loot modifier to apply fortune based on a binomial formula.
     */
    public LootModifier<Block> fortuneBinomial(float probability, int bonus) {
        return this.modifier((block, entry) -> entry.apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, probability, bonus)));
    }
    
    
    /**
     * A condition that is random with a chance and optionally different chances for
     * different fortune levels. Chances for different levels are computed automatically.
     */
    public LootItemCondition.Builder randomFortune(float baseChance) {
        return this.randomFortune(baseChance, baseChance * (10/9f), baseChance * 1.25f, baseChance * (5/3f), baseChance * 5);
    }
    
    /**
     * A condition that is random with a chance and optionally different chances for
     * different fortune levels.
     * 
     * @param baseChance The chance without fortune.
     * @param levelChances the chances with fortune.
     */
    public LootItemCondition.Builder randomFortune(float baseChance, float... levelChances) {
        float[] chances = new float[levelChances.length + 1];
        chances[0] = baseChance;
        System.arraycopy(levelChances, 0, chances, 1, levelChances.length);
        return BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, chances);
    }

    /**
     * Gets a loot condition builder for a match tool condition.
     */
    public MatchToolBuilder matchTool(ItemLike item) {
        return new MatchToolBuilder(ItemPredicate.Builder.item().of(item));
    }

    /**
     * Gets a loot condition builder for a match tool condition.
     */
    public MatchToolBuilder matchTool(TagKey<Item> item) {
        return new MatchToolBuilder(ItemPredicate.Builder.item().of(item));
    }

    /**
     * Gets a loot modifier builder for a match state condition.
     */
    public MatchStateBuilder matchState() {
        return new MatchStateBuilder(StatePropertiesPredicate.Builder.properties());
    }

    /**
     * Gets a loot condition for silk touch tools.
     */
    public LootItemCondition.Builder silkCondition() {
        ItemPredicate.Builder predicate = ItemPredicate.Builder.item()
                .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1)));
        return MatchTool.toolMatches(predicate);
    }

    /**
     * Creates a loot modifier that copies NBT-Data from a block entity into the dropped item.
     *
     * @param tags The toplevel tags of the block entity to be copied.
     */
    public LootModifier<Block> copyNBT(String... tags) {
        return this.modifier((block, entry) -> {
            CopyNbtFunction.Builder func = CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY);
            for (String tag : tags) {
                func = func.copy(tag, "BlockEntityTag." + tag);
            }
            return entry.apply(func);
        });
    }

    /**
     * Creates a loot modifier that copies properties from a block state into the dropped item.
     *
     * @param properties The properties of the block state to be copied.
     */
    public LootModifier<Block> copyProperties(Property<?>... properties) {
        return this.modifier((block, entry) -> {
            CopyBlockState.Builder func = CopyBlockState.copyState(block);
            for (Property<?> property : properties) {
                func = func.copy(property);
            }
            return entry.apply(func);
        });
    }
    
     /**
     * A class used in the drops method to reduce ambiguity and make the code more readable. Just call
     * the {@link #silk(GenericLootModifier)} method wih a generic loot modifier to get a silk modifier.
     * A silk modifier does not extend {@link GenericLootModifier} for this reason.
     */
    public static class SilkModifier {
        
        @Nullable
        public final GenericLootModifier<Block> modifier;

        private SilkModifier(@Nullable GenericLootModifier<Block> modifier) {
            this.modifier = modifier;
        }
    }

    /**
     * This serves as a builder for a loot condition and a builder for a match tool predicate
     * in one.
     */
    public static class MatchToolBuilder implements LootItemCondition.Builder {
        
        private final ItemPredicate.Builder builder;

        private MatchToolBuilder(ItemPredicate.Builder builder) {
            this.builder = builder;
        }

        @Nonnull
        @Override
        public LootItemCondition build() {
            return MatchTool.toolMatches(this.builder).build();
        }

        /**
         * Adds a required enchantment to this builder.
         */
        public MatchToolBuilder enchantment(Enchantment enchantment) {
            return this.enchantment(enchantment, MinMaxBounds.Ints.atLeast(1));
        }
        
        /**
         * Adds a required enchantment to this builder.
         * 
         * @param minLevel The minimum level of the enchantment that must be present.
         */
        public MatchToolBuilder enchantment(Enchantment enchantment, int minLevel) {
            return this.enchantment(enchantment, MinMaxBounds.Ints.atLeast(minLevel));
        }
        
        /**
         * Adds a required enchantment to this builder.
         * 
         * @param level The exact level of the enchantment that must be present.
         */
        public MatchToolBuilder enchantmentExact(Enchantment enchantment, int level) {
            return this.enchantment(enchantment, MinMaxBounds.Ints.exactly(level));
        }
        
        private MatchToolBuilder enchantment(Enchantment enchantment, MinMaxBounds.Ints bounds) {
            this.builder.hasEnchantment(new EnchantmentPredicate(enchantment, bounds));
            return this;
        }

        /**
         * Adds required NBT data to this builder.
         */
        public MatchToolBuilder nbt(CompoundTag nbt) {
            this.builder.hasNbt(nbt);
            return this;
        }
    }

    /**
     * This serves as a builder for a loot modifier and a builder for a block state predicate
     * in one.
     */
    public class MatchStateBuilder implements GenericLootModifier<Block> {
        
        private final StatePropertiesPredicate.Builder builder;

        private MatchStateBuilder(StatePropertiesPredicate.Builder builder) {
            this.builder = builder;
        }

        @Override
        public LootPoolEntryContainer.Builder<?> apply(Block item, LootPoolSingletonContainer.Builder<?> entry) {
            return entry.when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(item).setProperties(this.builder));
        }

        @Override
        public SimpleLootFactory<Block> element() {
            return BlockLootProviderBase.this.element();
        }

         public MatchStateBuilder hasProperty(Property<?> property, String value) {
             this.builder.hasProperty(property, value);
             return this;
          }

          public MatchStateBuilder hasProperty(Property<Integer> property, int value) {
              this.builder.hasProperty(property, value);
              return this;
          }

          public MatchStateBuilder hasProperty(Property<Boolean> property, boolean value) {
              this.builder.hasProperty(property, value);
              return this;
          }

          public <T extends Comparable<T> & StringRepresentable> MatchStateBuilder hasProperty(Property<T> property, T value) {
              this.builder.hasProperty(property, value);
              return this;
          }
    }
}
