package io.github.noeppi_noeppi.libx.data.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.noeppi_noeppi.libx.impl.data.LootData;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.AlternativeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

/**
 * A base class for block loot providers. When overriding this you should call the
 * {@link #customLootTable(Block) customLootTable} methods in {@link #setup() setup}
 * to adjust the loot tables. Every block of you mod that is left untouched will get
 * a default loot table.
 */
public abstract class BlockLootProviderBase implements DataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    protected final ModX mod;
    protected final DataGenerator generator;

    private final Set<Block> blacklist = new HashSet<>();
    private final Map<Block, Function<Block, LootTable.Builder>> functionMap = new HashMap<>();

    public BlockLootProviderBase(ModX mod, DataGenerator generator) {
        this.mod = mod;
        this.generator = generator;
    }

    /**
     * The given block will not be processed by this provider. Useful when you want to create the loot table manually.
     */
    protected void customLootTable(Block block) {
        this.blacklist.add(block);
    }

    /**
     * The given block will get the given loot table.
     */
    protected void customLootTable(Block block, LootTable.Builder loot) {
        this.functionMap.put(block, b -> loot);
    }

    /**
     * The given block will get the given loot table function.
     */
    protected void customLootTable(Block block, Function<Block, LootTable.Builder> loot) {
        this.functionMap.put(block, loot);
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " block loot tables";
    }

    @Override
    public void run(@Nonnull HashCache cache) throws IOException {
        this.setup();

        Map<ResourceLocation, LootTable.Builder> tables = new HashMap<>();

        for (ResourceLocation id : ForgeRegistries.BLOCKS.getKeys()) {
            Block block = ForgeRegistries.BLOCKS.getValue(id);
            if (block != null && this.mod.modid.equals(id.getNamespace()) && !this.blacklist.contains(block)) {
                Function<Block, LootTable.Builder> loot;
                if (this.functionMap.containsKey(block)) {
                    loot = this.functionMap.get(block);
                } else {
                    LootTable.Builder builder = this.defaultBehavior(block);
                    loot = builder == null ? null : b -> builder;
                }
                if (loot != null) {
                    tables.put(id, loot.apply(block));
                }
            }
        }

        for (Map.Entry<ResourceLocation, LootTable.Builder> e : tables.entrySet()) {
            Path path = getPath(this.generator.getOutputFolder(), e.getKey());
            DataProvider.save(GSON, cache, LootTables.serialize(e.getValue().setParamSet(LootContextParamSets.BLOCK).build()), path);
        }
    }

    protected abstract void setup();

    private static Path getPath(Path root, ResourceLocation id) {
        return root.resolve("data/" + id.getNamespace() + "/loot_tables/blocks/" + id.getPath() + ".json");
    }

    /**
     * Creates a loot modifier that copies NBT-Data from a block entity into the dropped item. Use this
     * with one of the {@link #drops(Block, SilkModifier, LootFactory...) drops} methods.
     *
     * @param tags The toplevel tags of the block entity to be copied.
     */
    public LootModifier copyNBT(String... tags) {
        return (b, entry) -> {
            CopyNbtFunction.Builder func = CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY);
            for (String tag : tags) {
                func = func.copy(tag, "BlockEntityTag." + tag);
            }
            return entry.apply(func);
        };
    }

    /**
     * Creates a loot modifier that copies properties from a block state into the dropped item. Use this
     * with one of the {@link #drops(Block, SilkModifier, LootFactory...) drops} methods.
     *
     * @param properties The properties of the block state to be copied.
     */
    public LootModifier copyProperties(Property<?>... properties) {
        return (b, entry) -> {
            CopyBlockState.Builder func = CopyBlockState.copyState(b);
            for (Property<?> property : properties) {
                func = func.copy(property);
            }
            return entry.apply(func);
        };
    }

    /**
     * Method to add a custom loot table for a block.
     *
     * @param b     The block to add the loot table to.
     * @param silk  Whether the block can be mined with silk touch to drop itself.
     * @param drops A list of stacks that will all be dropped if silk touch is false or the block
     *              is mined without silk touch.
     */
    public void drops(Block b, boolean silk, ItemStack... drops) {
        LootFactory[] loot = new LootFactory[drops.length];
        for (int i = 0; i < drops.length; i++) {
            loot[i] = this.stack(drops[i]);
        }
        this.drops(b, silk, loot);
    }

    /**
     * Method to add a custom loot table for a block.
     *
     * @param b    The block to add the loot table to.
     * @param silk Whether the block can be mined with silk touch to drop itself.
     * @param loot A list of loot factories that will all be applied if silk touch is false or the block
     *             is mined without silk touch.
     */
    public void drops(Block b, boolean silk, LootPoolEntryContainer.Builder<?>... loot) {
        this.drops(b, silk, LootFactory.from(loot));
    }
    
    /**
     * Method to add a custom loot table for a block.
     *
     * @param b    The block to add the loot table to.
     * @param silk Whether the block can be mined with silk touch to drop itself.
     * @param loot A list of loot factories that will all be applied if silk touch is false or the block
     *             is mined without silk touch.
     */
    public void drops(Block b, boolean silk, LootFactory... loot) {
        this.drops(b, silk ? this.silk(LootModifier.identity()) : this.noSilk(), loot);
    }

    /**
     * Method to add a custom loot table for a block.
     *
     * @param b    The block to add the loot table to.
     * @param loot A list of loot factories that will all be applied.
     */
    public void drops(Block b, LootFactory... loot) {
        this.drops(b, this.noSilk(), loot);
    }

    /**
     * Method to add a custom loot table for a block.
     *
     * @param b    The block to add the loot table to.
     * @param silk Either null in which case no special handling of silk touch is done or a LootModifier that
     *             can modify the stack after mining with silk touch.
     * @param loot A list of loot factories that are used when silk ouch is either null or the block is mined
     *             without silk touch. All of them will be applied.
     */
    public void drops(Block b, SilkModifier silk, LootFactory... loot) {
        LootPoolEntryContainer.Builder<?> entry = this.combine(LootFactory.resolve(b, loot));
        if (silk.modifier != null) {
            LootPoolEntryContainer.Builder<?> silkBuilder = silk.modifier.apply(b, LootItem.lootTableItem(b)
                    .when(this.silkCondition()));
            entry = AlternativesEntry.alternatives(silkBuilder, entry);
        }
        LootPool.Builder pool = LootPool.lootPool().name("main")
                .setRolls(ConstantValue.exactly(1)).add(entry)
                .when(ExplosionCondition.survivesExplosion());
        this.customLootTable(b, LootTable.lootTable().withPool(pool));
    }

    /**
     * Gets a simple loot factory to always drop the block as item.
     */
    public SimpleLootFactory item() {
        return SimpleLootFactory.item();
    }
    
    /**
     * Turns a generic loot modifier into a silk modifier. This exists to reduce ambiguity.
     * A silk modifier does not extend {@link GenericLootModifier} for this reason.
     */
    public SilkModifier silk(GenericLootModifier modifier) {
        return new SilkModifier(modifier);
    }
    
    /**
     * Gets a new silk modifier that means: No special silk touch behaviour.
     */
    public SilkModifier noSilk() {
        return new SilkModifier(null);
    }
    
    /**
     * Repeats a loot factory a fixed amount of times.
     */
    public LootFactory repeat(LootFactory factory, int times) {
        LootFactory[] factories = new LootFactory[times];
        for (int i = 0; i < times; i++) {
            factories[i] = factory;
        }
        return this.combine(factories);
    }

    /**
     * Turns a singleton loot entry into a simple loot factory.
     */
    public WrappedLootEntry from(LootPoolSingletonContainer.Builder<?> entry) {
        return new WrappedLootEntry(entry);
    }
    
    /**
     * Turns a loot entry into a loot factory.
     */
    public LootFactory from(LootPoolEntryContainer.Builder<?> entry) {
        return LootFactory.from(entry);
    }
    
    /**
     * Turns a loot function into a loot modifier.
     */
    public LootModifier from(LootItemConditionalFunction.Builder<?> function) {
        return (b, e) -> e.apply(function);
    }

    /**
     * A loot modifier to apply fortune based on the formula used for ores.
     */
    public LootModifier fortuneOres() {
        return (b, e) -> e.apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE));
    }
    
    /**
     * A loot modifier to apply fortune based on a uniform formula.
     */
    public LootModifier fortuneUniform() {
        return this.fortuneUniform(1);
    }
    
    /**
     * A loot modifier to apply fortune based on a uniform formula.
     */
    public LootModifier fortuneUniform(int multiplier) {
        return (b, e) -> e.apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, multiplier));
    }

    /**
     * A loot modifier to apply fortune based on a binomial formula.
     */
    public LootModifier fortuneBinomial(float probability) {
        return this.fortuneBinomial(probability, 0);
    }
    
    /**
     * A loot modifier to apply fortune based on a binomial formula.
     */
    public LootModifier fortuneBinomial(float probability, int bonus) {
        return (b, e) -> e.apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, probability, bonus));
    }

    /**
     * A condition that is random with a chance.
     */
    public LootItemCondition.Builder random(float chance) {
        return LootItemRandomChanceCondition.randomChance(chance);
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
     * A loot modifier that sets the count of a stack.
     */
    public LootModifier count(int count) {
        return this.from(SetItemCountFunction.setCount(ConstantValue.exactly(count)));
    }
    
    /**
     * A loot modifier that uniformly sets the count of a stack between two values.
     */
    public LootModifier count(int min, int max) {
        if (min == max) {
            return this.from(SetItemCountFunction.setCount(ConstantValue.exactly(min)));
        } else {
            return this.from(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
        }
    }
    
    /**
     * A loot modifier that sets the count of a stack based on a binomial formula.
     */
    public LootModifier countBinomial(float chance, int num) {
        return this.from(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(num, chance)));
    }
    
    /**
     * Inverts a loot condition
     */
    public LootItemCondition.Builder not(LootItemCondition.Builder condition) {
        return InvertedLootItemCondition.invert(condition);
    }
    
    /**
     * Joins conditions with OR.
     */
    public LootItemCondition.Builder or(LootItemCondition.Builder... conditions) {
        return AlternativeLootItemCondition.alternative(conditions);
    }

    /**
     * A builder for loot groups where every member is selected.
     */
    public LootBuilders.AllLootBuilder all() {
        return new LootBuilders.AllLootBuilder();
    }
    
    /**
     * Minecraft does not seem to have a loot builder for groups. So here you have one.
     * This will select only one element per roll.
     */
    public LootBuilders.GroupLootBuilder group() {
        return new LootBuilders.GroupLootBuilder();
    }
    
    /**
     * Minecraft does not seem to have a loot builder for sequences. So here you have one.
     * This will select only one element per roll.
     */
    public LootBuilders.SequenceLootBuilder sequence() {
        return new LootBuilders.SequenceLootBuilder();
    }

    /**
     * Combines the given loot builders into one. (All loot builders will be applied).
     */
    public LootPoolEntryContainer.Builder<?> combine(LootPoolEntryContainer.Builder<?>... loot) {
        return LootData.combineBy(LootBuilders.AllLootBuilder::new, loot);
    }

    /**
     * Combines the given loot factories into one. (All loot factories will be applied).
     */
    public LootFactory combine(LootFactory... loot) {
        return e -> LootData.combineBy(LootBuilders.AllLootBuilder::new, l -> l.build(e), loot);
    }
    
    /**
     * Combines the given loot builders into one. (One loot builder will be applied).
     */
    public LootPoolEntryContainer.Builder<?> random(LootPoolEntryContainer.Builder<?>... loot) {
        return LootData.combineBy(LootBuilders.GroupLootBuilder::new, loot);
    }

    /**
     * Combines the given loot factories into one. (One loot factory will be applied).
     */
    public LootFactory random(LootFactory... loot) {
        return e -> LootData.combineBy(LootBuilders.GroupLootBuilder::new, l -> l.build(e), loot);
    }

    /**
     * Combines the given loot builders into one. Only the first matching builder is applied.
     */
    public LootPoolEntryContainer.Builder<?> first(LootPoolEntryContainer.Builder<?>... loot) {
        return LootData.combineBy(AlternativesEntry::alternatives, loot);
    }

    /**
     * Combines the given loot factories into one. Only the first matching factory is applied.
     */
    public LootFactory first(LootFactory... loot) {
        return e -> LootData.combineBy(AlternativesEntry::alternatives, l -> l.build(e), loot);
    }
    
    /**
     * Combines the given loot builders into one.
     * From all the loot entries until the first one not matching, one is selected.
     */
    public LootPoolEntryContainer.Builder<?> whileMatch(LootPoolEntryContainer.Builder<?>... loot) {
        return LootData.combineBy(LootBuilders.SequenceLootBuilder::new, loot);
    }

    /**
     * Combines the given loot factories into one.
     * From all the loot factories until the first one not matching, one is selected.
     */
    public LootFactory whileMatch(LootFactory... loot) {
        return e -> LootData.combineBy(LootBuilders.SequenceLootBuilder::new, l -> l.build(e), loot);
    }

    /**
     * A loot factory for a specific item.
     */
    public WrappedLootEntry stack(ItemLike item) {
        return new WrappedLootEntry(LootItem.lootTableItem(item));
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
    public MatchToolBuilder matchTool(Tag<Item> item) {
        return new MatchToolBuilder(ItemPredicate.Builder.item().of(item));
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
     * Tries to create the best possible representation of stack in a loot entry.
     */
    public WrappedLootEntry stack(ItemStack stack) {
        return new WrappedLootEntry(LootData.stack(stack));
    }

    /**
     * Creates a default loot table for the given block. Can be overridden to alter
     * default behaviour. Should return null if no loot table should be generated.
     */
    @Nullable
    protected LootTable.Builder defaultBehavior(Block b) {
        if (b.getStateDefinition().getPossibleStates().stream().anyMatch(this::needsLootTable)) {
            LootPoolEntryContainer.Builder<?> entry = LootItem.lootTableItem(b);
            LootPool.Builder pool = LootPool.lootPool().name("main").setRolls(ConstantValue.exactly(1)).add(entry)
                    .when(ExplosionCondition.survivesExplosion());
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
        //noinspection deprecation
        return !state.isAir() && state.getFluidState().createLegacyBlock().getBlock() != state.getBlock()
                && !BuiltInLootTables.EMPTY.equals(state.getBlock().getLootTable());
    }

    /**
     * Interface to get a loot entry from a block.
     */
    @FunctionalInterface
    public interface LootFactory {

        /**
         * Gets a loot factory that will always return the given loot entry.
         */
        static LootFactory from(LootPoolEntryContainer.Builder<?> builder) {
            return b -> builder;
        }

        /**
         * Gets an array of loot factories that will always return the given loot entries.
         */
        static LootFactory[] from(LootPoolEntryContainer.Builder<?>[] builders) {
            LootFactory[] factories = new LootFactory[builders.length];
            for (int i = 0; i < builders.length; i++) {
                LootPoolEntryContainer.Builder<?> builder = builders[i];
                factories[i] = b -> builder;
            }
            return factories;
        }

        /**
         * Calls {@link #build(Block)} on all factories with the given block and returns a new array.
         */
        static LootPoolEntryContainer.Builder<?>[] resolve(Block b, LootFactory[] factories) {
            LootPoolEntryContainer.Builder<?>[] entries = new LootPoolEntryContainer.Builder<?>[factories.length];
            for (int i = 0; i < factories.length; i++) {
                entries[i] = factories[i].build(b);
            }
            return entries;
        }

        LootPoolEntryContainer.Builder<?> build(Block block);
        
        default LootFactory with(LootItemCondition.Builder... conditions) {
            return b -> {
                LootPoolEntryContainer.Builder<?> entry = this.build(b);
                for (LootItemCondition.Builder condition : conditions) {
                    entry.when(condition);
                }
                return entry;
            };
        }
    }

    /**
     * Interface to get a singleton loot entry from a block.
     */
    @FunctionalInterface
    public interface SimpleLootFactory extends LootFactory {

        /**
         * Gets a simple loot factory that always returns the given block as item.
         */
        static SimpleLootFactory item() {
            return LootItem::lootTableItem;
        }

        /**
         * Gets a simple loot factory that will always return the given loot entry.
         */
        static SimpleLootFactory from(LootPoolSingletonContainer.Builder<?> builder) {
            return b -> builder;
        }

        /**
         * Gets an array of simple loot factories that will always return the given loot entries.
         */
        static SimpleLootFactory[] from(LootPoolSingletonContainer.Builder<?>[] builders) {
            SimpleLootFactory[] factories = new SimpleLootFactory[builders.length];
            for (int i = 0; i < builders.length; i++) {
                LootPoolSingletonContainer.Builder<?> builder = builders[i];
                factories[i] = b -> builder;
            }
            return factories;
        }

        /**
         * Calls {@link #build(Block)} on all factories with the given block and returns a new array.
         */
        static LootPoolSingletonContainer.Builder<?>[] resolve(Block b, SimpleLootFactory[] factories) {
            LootPoolSingletonContainer.Builder<?>[] entries = new LootPoolSingletonContainer.Builder<?>[factories.length];
            for (int i = 0; i < factories.length; i++) {
                entries[i] = factories[i].build(b);
            }
            return entries;
        }

        @Override
        LootPoolSingletonContainer.Builder<?> build(Block block);
        
        default LootFactory withFinal(GenericLootModifier finalModifier) {
            return b -> finalModifier.apply(b, this.build(b));
        }
        
        default SimpleLootFactory with(LootModifier... modifiers) {
            LootModifier chained = LootModifier.chain(modifiers);
            return b -> chained.apply(b, this.build(b));
        }

        @Override
        default SimpleLootFactory with(LootItemCondition.Builder... conditions) {
            return b -> {
                LootPoolSingletonContainer.Builder<?> entry = this.build(b);
                for (LootItemCondition.Builder condition : conditions) {
                    entry.when(condition);
                }
                return entry;
            };
        }
        
        default SimpleLootFactory with(LootItemConditionalFunction.Builder<?>... functions) {
            LootModifier[] modifiers = new LootModifier[functions.length];
            for (int i = 0; i < functions.length; i++) {
                LootItemConditionalFunction.Builder<?> function = functions[i];
                modifiers[i] = (b, e) -> e.apply(function);
            }
            LootModifier chained = LootModifier.chain(modifiers);
            return b -> chained.apply(b, this.build(b));
        }
    }
    
    public static class WrappedLootEntry implements SimpleLootFactory {
        
        public final LootPoolSingletonContainer.Builder<?> entry;

        private WrappedLootEntry(LootPoolSingletonContainer.Builder<?> entry) {
            this.entry = entry;
        }

        @Override
        public LootPoolSingletonContainer.Builder<?> build(Block block) {
            return this.entry;
        }
    }

    /**
     * Interface to modify a singleton loot entry to a new loot entry. This can also be used
     * as a {@link LootFactory} in which case the default item entry obtained from
     * {@link SimpleLootFactory#item()} is passed to the {@link #apply(Block, LootPoolSingletonContainer.Builder) apply} method.
     */
    @FunctionalInterface
    public interface GenericLootModifier extends LootFactory {

        /**
         * Gets a loot modifier that does nothing.
         */
        static GenericLootModifier identity() {
            return (b, e) -> e;
        }

        LootPoolEntryContainer.Builder<?> apply(Block block, LootPoolSingletonContainer.Builder<?> entry);

        @Override
        default LootPoolEntryContainer.Builder<?> build(Block block) {
            return this.apply(block, SimpleLootFactory.item().build(block));
        }
    }

    /**
     * Interface to modify a singleton loot entry to a new singleton loot entry. This can also be
     * used as a {@link SimpleLootFactory} in which case the default item entry obtained from
     * {@link SimpleLootFactory#item()} is passed to the {@link #apply(Block, LootPoolSingletonContainer.Builder) apply} method.
     */
    @FunctionalInterface
    public interface LootModifier extends GenericLootModifier, SimpleLootFactory {

        /**
         * Gets a loot modifier that does nothing.
         */
        static LootModifier identity() {
            return (b, e) -> e;
        }

        /**
         * Get a new loot modifier that chains all the given together.
         * (Applies the first, then the second to the result of the first and so on)
         */
        static LootModifier chain(LootModifier... children) {
            if (children.length == 0) {
                return identity();
            } else if (children.length == 1) {
                return children[0];
            } else {
                return (b, e) -> {
                    LootPoolSingletonContainer.Builder<?> entry = e;
                    for (LootModifier modifier : children) {
                        entry = modifier.apply(b, entry);
                    }
                    return entry;
                };
            }
        }

        @Override
        LootPoolSingletonContainer.Builder<?> apply(Block block, LootPoolSingletonContainer.Builder<?> entry);

        @Override
        default LootPoolSingletonContainer.Builder<?> build(Block block) {
            return this.apply(block, SimpleLootFactory.item().build(block));
        }

        /**
         * Same as {@link #chain(LootModifier...)}
         */
        default LootModifier andThen(LootModifier other) {
            return chain(this, other);
        }
    }

    /**
     * A class used in the drops method to reduce ambiguity and make the code more readable. Just call
     * the {@link #silk(GenericLootModifier)} method wih a generic loot modifier to get a silk modifier.
     * A silk modifier does not extend {@link GenericLootModifier} for this reason.
     */
    public static class SilkModifier {
        
        @Nullable
        public final GenericLootModifier modifier;

        private SilkModifier(@Nullable GenericLootModifier modifier) {
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
        public MatchToolBuilder ench(Enchantment ench) {
            return this.ench(ench, MinMaxBounds.Ints.atLeast(1));
        }
        
        /**
         * Adds a required enchantment to this builder.
         * 
         * @param minLevel The minimum level of the enchantment that must be present.
         */
        public MatchToolBuilder ench(Enchantment ench, int minLevel) {
            return this.ench(ench, MinMaxBounds.Ints.atLeast(minLevel));
        }
        
        /**
         * Adds a required enchantment to this builder.
         * 
         * @param level The exact level of the enchantment that must be present.
         */
        public MatchToolBuilder enchExact(Enchantment ench, int level) {
            return this.ench(ench, MinMaxBounds.Ints.exactly(level));
        }
        
        private MatchToolBuilder ench(Enchantment ench, MinMaxBounds.Ints bounds) {
            this.builder.hasEnchantment(new EnchantmentPredicate(ench, bounds));
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
}
