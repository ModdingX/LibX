package io.github.noeppi_noeppi.libx.data.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.noeppi_noeppi.libx.impl.data.LootData;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.*;
import net.minecraft.loot.functions.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
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

/**
 * A base class for block loot providers. When overriding this you should call the
 * {@link #customLootTable(Block) customLootTable} methods in {@link #setup() setup}
 * to adjust the loot tables. Every block of you mod that is left untouched will get
 * a default loot table.
 */
public abstract class BlockLootProviderBase implements IDataProvider {

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
    public void act(@Nonnull DirectoryCache cache) throws IOException {
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
            IDataProvider.save(GSON, cache, LootTableManager.toJson(e.getValue().setParameterSet(LootParameterSets.BLOCK).build()), path);
        }
    }

    protected abstract void setup();

    private static Path getPath(Path root, ResourceLocation id) {
        return root.resolve("data/" + id.getNamespace() + "/loot_tables/blocks/" + id.getPath() + ".json");
    }

    /**
     * Creates a loot modifier that copies NBT-Data from a tile entity into the dropped item. Use this
     * with one of the {@link #drops(Block, SilkModifier, LootFactory...) drops} methods.
     *
     * @param tags The toplevel tags of the tile entity to be copied.
     */
    public LootModifier copyNBT(String... tags) {
        return (b, entry) -> {
            CopyNbt.Builder func = CopyNbt.builder(CopyNbt.Source.BLOCK_ENTITY);
            for (String tag : tags) {
                func = func.replaceOperation(tag, "BlockEntityTag." + tag);
            }
            return entry.acceptFunction(func);
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
            CopyBlockState.Builder func = CopyBlockState.builder(b);
            for (Property<?> property : properties) {
                func = func.with(property);
            }
            return entry.acceptFunction(func);
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
    public void drops(Block b, boolean silk, LootEntry.Builder<?>... loot) {
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
        LootEntry.Builder<?> entry = this.combine(LootFactory.resolve(b, loot));
        if (silk.modifier != null) {
            LootEntry.Builder<?> silkBuilder = silk.modifier.apply(b, ItemLootEntry.builder(b)
                    .acceptCondition(this.silkCondition()));
            entry = AlternativesLootEntry.builder(silkBuilder, entry);
        }
        LootPool.Builder pool = LootPool.builder().name("main")
                .rolls(ConstantRange.of(1)).addEntry(entry)
                .acceptCondition(SurvivesExplosion.builder());
        this.customLootTable(b, LootTable.builder().addLootPool(pool));
    }

    /**
     * Gets a standalone loot factory to always drop the block as item.
     */
    public StandaloneLootFactory item() {
        return StandaloneLootFactory.item();
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
     * Turns a standalone loot entry into a standalone loot factory.
     */
    public WrappedLootEntry from(StandaloneLootEntry.Builder<?> entry) {
        return new WrappedLootEntry(entry);
    }
    
    /**
     * Turns a loot entry into a loot factory.
     */
    public LootFactory from(LootEntry.Builder<?> entry) {
        return LootFactory.from(entry);
    }
    
    /**
     * Turns a loot function into a loot modifier.
     */
    public LootModifier from(LootFunction.Builder<?> function) {
        return (b, e) -> e.acceptFunction(function);
    }

    /**
     * A loot modifier to apply fortune based on the formula used for ores.
     */
    public LootModifier fortuneOres() {
        return (b, e) -> e.acceptFunction(ApplyBonus.oreDrops(Enchantments.FORTUNE));
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
        return (b, e) -> e.acceptFunction(ApplyBonus.uniformBonusCount(Enchantments.FORTUNE, multiplier));
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
        return (b, e) -> e.acceptFunction(ApplyBonus.binomialWithBonusCount(Enchantments.FORTUNE, probability, bonus));
    }

    /**
     * A condition that is random with a chance.
     */
    public ILootCondition.IBuilder random(float chance) {
        return RandomChance.builder(chance);
    }

    /**
     * A condition that is random with a chance and optionally different chances for
     * different fortune levels. Chances for different levels are computed automatically.
     */
    public ILootCondition.IBuilder randomFortune(float baseChance) {
        return this.randomFortune(baseChance, baseChance * (10/9f), baseChance * 1.25f, baseChance * (5/3f), baseChance * 5);
    }
    
    /**
     * A condition that is random with a chance and optionally different chances for
     * different fortune levels.
     * 
     * @param baseChance The chance without fortune.
     * @param levelChances the chances with fortune.
     */
    public ILootCondition.IBuilder randomFortune(float baseChance, float... levelChances) {
        float[] chances = new float[levelChances.length + 1];
        chances[0] = baseChance;
        System.arraycopy(levelChances, 0, chances, 1, levelChances.length);
        return TableBonus.builder(Enchantments.FORTUNE, chances);
    }

    /**
     * A loot modifier that sets the count of a stack.
     */
    public LootModifier count(int count) {
        return this.from(SetCount.builder(ConstantRange.of(count)));
    }
    
    /**
     * A loot modifier that uniformly sets the count of a stack between two values.
     */
    public LootModifier count(int min, int max) {
        if (min == max) {
            return this.from(SetCount.builder(ConstantRange.of(min)));
        } else {
            return this.from(SetCount.builder(RandomValueRange.of(min, max)));
        }
    }
    
    /**
     * A loot modifier that sets the count of a stack based on a binomial formula.
     */
    public LootModifier countBinomial(float chance, int num) {
        return this.from(SetCount.builder(BinomialRange.of(num, chance)));
    }
    
    /**
     * Inverts a loot condition
     */
    public ILootCondition.IBuilder not(ILootCondition.IBuilder condition) {
        return Inverted.builder(condition);
    }
    
    /**
     * Joins conditions with OR.
     */
    public ILootCondition.IBuilder or(ILootCondition.IBuilder... conditions) {
        return Alternative.builder(conditions);
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
    public LootEntry.Builder<?> combine(LootEntry.Builder<?>... loot) {
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
    public LootEntry.Builder<?> random(LootEntry.Builder<?>... loot) {
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
    public LootEntry.Builder<?> first(LootEntry.Builder<?>... loot) {
        return LootData.combineBy(AlternativesLootEntry::builder, loot);
    }

    /**
     * Combines the given loot factories into one. Only the first matching factory is applied.
     */
    public LootFactory first(LootFactory... loot) {
        return e -> LootData.combineBy(AlternativesLootEntry::builder, l -> l.build(e), loot);
    }
    
    /**
     * Combines the given loot builders into one.
     * From all the loot entries until the first one not matching, one is selected.
     */
    public LootEntry.Builder<?> whileMatch(LootEntry.Builder<?>... loot) {
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
    public WrappedLootEntry stack(IItemProvider item) {
        return new WrappedLootEntry(ItemLootEntry.builder(item));
    }
    
    /**
     * Gets a loot condition builder for a match tool condition.
     */
    public MatchToolBuilder matchTool(IItemProvider item) {
        return new MatchToolBuilder(ItemPredicate.Builder.create().item(item));
    }
    
    /**
     * Gets a loot condition builder for a match tool condition.
     */
    public MatchToolBuilder matchTool(ITag<Item> item) {
        return new MatchToolBuilder(ItemPredicate.Builder.create().tag(item));
    }

    /**
     * Gets a loot condition for silk touch tools.
     */
    public ILootCondition.IBuilder silkCondition() {
        ItemPredicate.Builder predicate = ItemPredicate.Builder.create()
                .enchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.IntBound.atLeast(1)));
        return MatchTool.builder(predicate);
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
        if (b.getStateContainer().getValidStates().stream().anyMatch(this::needsLootTable)) {
            LootEntry.Builder<?> entry = ItemLootEntry.builder(b);
            LootPool.Builder pool = LootPool.builder().name("main").rolls(ConstantRange.of(1)).addEntry(entry)
                    .acceptCondition(SurvivesExplosion.builder());
            return LootTable.builder().addLootPool(pool);
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
        return !state.isAir() && state.getFluidState().getBlockState().getBlock() != state.getBlock()
                && !LootTables.EMPTY.equals(state.getBlock().getLootTable());
    }

    /**
     * Interface to get a loot entry from a block.
     */
    @FunctionalInterface
    public interface LootFactory {

        /**
         * Gets a loot factory that will always return the given loot entry.
         */
        static LootFactory from(LootEntry.Builder<?> builder) {
            return b -> builder;
        }

        /**
         * Gets an array of loot factories that will always return the given loot entries.
         */
        static LootFactory[] from(LootEntry.Builder<?>[] builders) {
            LootFactory[] factories = new LootFactory[builders.length];
            for (int i = 0; i < builders.length; i++) {
                LootEntry.Builder<?> builder = builders[i];
                factories[i] = b -> builder;
            }
            return factories;
        }

        /**
         * Calls {@link #build(Block)} on all factories with the given block and returns a new array.
         */
        static LootEntry.Builder<?>[] resolve(Block b, LootFactory[] factories) {
            LootEntry.Builder<?>[] entries = new LootEntry.Builder<?>[factories.length];
            for (int i = 0; i < factories.length; i++) {
                entries[i] = factories[i].build(b);
            }
            return entries;
        }

        LootEntry.Builder<?> build(Block block);
        
        default LootFactory with(ILootCondition.IBuilder... conditions) {
            return b -> {
                LootEntry.Builder<?> entry = this.build(b);
                for (ILootCondition.IBuilder condition : conditions) {
                    entry.acceptCondition(condition);
                }
                return entry;
            };
        }
    }

    /**
     * Interface to get a standalone loot entry from a block.
     */
    @FunctionalInterface
    public interface StandaloneLootFactory extends LootFactory {

        /**
         * Gets a standalone loot factory that always returns the given block as item.
         */
        static StandaloneLootFactory item() {
            return ItemLootEntry::builder;
        }

        /**
         * Gets a standalone loot factory that will always return the given loot entry.
         */
        static StandaloneLootFactory from(StandaloneLootEntry.Builder<?> builder) {
            return b -> builder;
        }

        /**
         * Gets an array of standalone loot factories that will always return the given loot entries.
         */
        static StandaloneLootFactory[] from(StandaloneLootEntry.Builder<?>[] builders) {
            StandaloneLootFactory[] factories = new StandaloneLootFactory[builders.length];
            for (int i = 0; i < builders.length; i++) {
                StandaloneLootEntry.Builder<?> builder = builders[i];
                factories[i] = b -> builder;
            }
            return factories;
        }

        /**
         * Calls {@link #build(Block)} on all factories with the given block and returns a new array.
         */
        static StandaloneLootEntry.Builder<?>[] resolve(Block b, StandaloneLootFactory[] factories) {
            StandaloneLootEntry.Builder<?>[] entries = new StandaloneLootEntry.Builder<?>[factories.length];
            for (int i = 0; i < factories.length; i++) {
                entries[i] = factories[i].build(b);
            }
            return entries;
        }

        @Override
        StandaloneLootEntry.Builder<?> build(Block block);
        
        default LootFactory withFinal(GenericLootModifier finalModifier) {
            return b -> finalModifier.apply(b, this.build(b));
        }
        
        default StandaloneLootFactory with(LootModifier... modifiers) {
            LootModifier chained = LootModifier.chain(modifiers);
            return b -> chained.apply(b, this.build(b));
        }

        @Override
        default StandaloneLootFactory with(ILootCondition.IBuilder... conditions) {
            return b -> {
                StandaloneLootEntry.Builder<?> entry = this.build(b);
                for (ILootCondition.IBuilder condition : conditions) {
                    entry.acceptCondition(condition);
                }
                return entry;
            };
        }
        
        default StandaloneLootFactory with(LootFunction.Builder<?>... functions) {
            LootModifier[] modifiers = new LootModifier[functions.length];
            for (int i = 0; i < functions.length; i++) {
                LootFunction.Builder<?> function = functions[i];
                modifiers[i] = (b, e) -> e.acceptFunction(function);
            }
            LootModifier chained = LootModifier.chain(modifiers);
            return b -> chained.apply(b, this.build(b));
        }
    }
    
    public static class WrappedLootEntry implements StandaloneLootFactory {
        
        public final StandaloneLootEntry.Builder<?> entry;

        private WrappedLootEntry(StandaloneLootEntry.Builder<?> entry) {
            this.entry = entry;
        }

        @Override
        public StandaloneLootEntry.Builder<?> build(Block block) {
            return this.entry;
        }
    }

    /**
     * Interface to modify a standalone loot entry to a new loot entry. This can also be used
     * as a {@link LootFactory} in which case the default item entry obtained from
     * {@link StandaloneLootFactory#item()} is passed to the {@link #apply(Block, StandaloneLootEntry.Builder) apply} method.
     */
    @FunctionalInterface
    public interface GenericLootModifier extends LootFactory {

        /**
         * Gets a loot modifier that does nothing.
         */
        static GenericLootModifier identity() {
            return (b, e) -> e;
        }

        LootEntry.Builder<?> apply(Block block, StandaloneLootEntry.Builder<?> entry);

        @Override
        default LootEntry.Builder<?> build(Block block) {
            return this.apply(block, StandaloneLootFactory.item().build(block));
        }
    }

    /**
     * Interface to modify a standalone loot entry to a new standalone loot entry. This can also be
     * used as a {@link StandaloneLootFactory} in which case the default item entry obtained from
     * {@link StandaloneLootFactory#item()} is passed to the {@link #apply(Block, StandaloneLootEntry.Builder) apply} method.
     */
    @FunctionalInterface
    public interface LootModifier extends GenericLootModifier, StandaloneLootFactory {

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
                    StandaloneLootEntry.Builder<?> entry = e;
                    for (LootModifier modifier : children) {
                        entry = modifier.apply(b, entry);
                    }
                    return entry;
                };
            }
        }

        @Override
        StandaloneLootEntry.Builder<?> apply(Block block, StandaloneLootEntry.Builder<?> entry);

        @Override
        default StandaloneLootEntry.Builder<?> build(Block block) {
            return this.apply(block, StandaloneLootFactory.item().build(block));
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
    public static class MatchToolBuilder implements ILootCondition.IBuilder {
        
        private final ItemPredicate.Builder builder;

        private MatchToolBuilder(ItemPredicate.Builder builder) {
            this.builder = builder;
        }

        @Nonnull
        @Override
        public ILootCondition build() {
            return MatchTool.builder(this.builder).build();
        }

        /**
         * Adds a required enchantment to this builder.
         */
        public MatchToolBuilder ench(Enchantment ench) {
            return this.ench(ench, MinMaxBounds.IntBound.atLeast(1));
        }
        
        /**
         * Adds a required enchantment to this builder.
         * 
         * @param minLevel The minimum level of the enchantment that must be present.
         */
        public MatchToolBuilder ench(Enchantment ench, int minLevel) {
            return this.ench(ench, MinMaxBounds.IntBound.atLeast(minLevel));
        }
        
        /**
         * Adds a required enchantment to this builder.
         * 
         * @param level The exact level of the enchantment that must be present.
         */
        public MatchToolBuilder enchExact(Enchantment ench, int level) {
            return this.ench(ench, MinMaxBounds.IntBound.exactly(level));
        }
        
        private MatchToolBuilder ench(Enchantment ench, MinMaxBounds.IntBound bounds) {
            this.builder.enchantment(new EnchantmentPredicate(ench, bounds));
            return this;
        }

        /**
         * Adds required NBT data to this builder.
         */
        public MatchToolBuilder nbt(CompoundNBT nbt) {
            this.builder.nbt(nbt);
            return this;
        }
    }
}
