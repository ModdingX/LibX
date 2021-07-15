package io.github.noeppi_noeppi.libx.data.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.noeppi_noeppi.libx.impl.data.LootData;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.Alternative;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.conditions.Inverted;
import net.minecraft.loot.conditions.RandomChance;
import net.minecraft.loot.functions.LootingEnchantBonus;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A base class for entity loot providers. When overriding this you should call the
 * {@link #customLootTable(EntityType, LootTable.Builder) customLootTable} methods in
 * {@link #setup() setup} to adjust the loot tables.
 */
public abstract class EntityLootProviderBase implements IDataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    protected final ModX mod;
    protected final DataGenerator generator;

    private final Map<EntityType<?>, Function<EntityType<?>, LootTable.Builder>> functionMap = new HashMap<>();

    public EntityLootProviderBase(ModX mod, DataGenerator generator) {
        this.mod = mod;
        this.generator = generator;
    }

    /**
     * The given entity will get the given loot table.
     */
    protected void customLootTable(EntityType<?> entity, LootTable.Builder loot) {
        this.functionMap.put(entity, b -> loot);
    }

    /**
     * The given entity will get the given loot table function.
     */
    protected void customLootTable(EntityType<?> entity, Function<EntityType<?>, LootTable.Builder> loot) {
        this.functionMap.put(entity, loot);
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " entity loot tables";
    }

    @Override
    public void act(@Nonnull DirectoryCache cache) throws IOException {
        this.setup();

        Map<ResourceLocation, LootTable.Builder> tables = new HashMap<>();
        
        for (ResourceLocation id : ForgeRegistries.ENTITIES.getKeys()) {
            EntityType<?> entity = ForgeRegistries.ENTITIES.getValue(id);
            if (entity != null && this.mod.modid.equals(id.getNamespace())) {
                Function<EntityType<?>, LootTable.Builder> loot;
                if (this.functionMap.containsKey(entity)) {
                    loot = this.functionMap.get(entity);
                } else {
                    LootTable.Builder builder = this.defaultBehavior(entity);
                    loot = builder == null ? null : b -> builder;
                }
                if (loot != null) {
                    tables.put(id, loot.apply(entity));
                }
            }
        }

        for (Map.Entry<ResourceLocation, LootTable.Builder> e : tables.entrySet()) {
            Path path = getPath(this.generator.getOutputFolder(), e.getKey());
            IDataProvider.save(GSON, cache, LootTableManager.toJson(e.getValue().setParameterSet(LootParameterSets.ENTITY).build()), path);
        }
    }

    protected abstract void setup();

    private static Path getPath(Path root, ResourceLocation id) {
        return root.resolve("data/" + id.getNamespace() + "/loot_tables/entities/" + id.getPath() + ".json");
    }

    /**
     * Method to add a custom loot table for an entity.
     *
     * @param e     The entity to add the loot table to.
     * @param drops A list of stacks that are dropped.
     */
    public void drops(EntityType<?> e, ItemStack... drops) {
        LootFactory[] loot = new LootFactory[drops.length];
        for (int i = 0; i < drops.length; i++) {
            loot[i] = this.stack(drops[i]);
        }
        this.drops(e, loot);
    }

    /**
     * Method to add a custom loot table for an entity.
     *
     * @param e    The entity to add the loot table to.
     * @param loot A list of loot builders that will all be applied.
     */
    public void drops(EntityType<?> e, LootEntry.Builder<?>... loot) {
        this.drops(e, LootFactory.from(loot));
    }
    
    /**
     * Method to add a custom loot table for an entity.
     *
     * @param e    The entity to add the loot table to.
     * @param loot A list of loot factories that will all be applied.
     */
    public void drops(EntityType<?> e, LootFactory... loot) {
        LootEntry.Builder<?> entry = this.combine(LootFactory.resolve(e, loot));
        LootPool.Builder pool = LootPool.builder().name("main")
                .rolls(ConstantRange.of(1)).addEntry(entry);
        this.customLootTable(e, LootTable.builder().addLootPool(pool));
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
     * Gets a loot modifier for the looting enchantment.
     * 
     * @param max The maximum amount of additional drops.
     */
    public LootModifier looting(int max) {
        return this.looting(0, max);
    }
    
    /**
     * Gets a loot modifier for the looting enchantment.
     * 
     * @param min The minimum amount of additional drops.
     * @param max The maximum amount of additional drops.
     */
    public LootModifier looting(int min, int max) {
        return (b, e) -> e.acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(min, max)));
    }

    /**
     * A condition that is random with a chance.
     */
    public ILootCondition.IBuilder random(float chance) {
        return RandomChance.builder(chance);
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
     * Tries to create the best possible representation of stack in a loot entry.
     */
    public WrappedLootEntry stack(ItemStack stack) {
        return new WrappedLootEntry(LootData.stack(stack));
    }

    /**
     * Creates a default loot table for the given entity. Can be overridden to alter
     * default behaviour. Should return null if no loot table should be generated.
     */
    @Nullable
    protected LootTable.Builder defaultBehavior(EntityType<?> e) {
        return null;
    }

    /**
     * Interface to get a loot entry from an entity.
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
         * Calls {@link #build(EntityType)} on all factories with the given entity and returns a new array.
         */
        static LootEntry.Builder<?>[] resolve(EntityType<?> e, LootFactory[] factories) {
            LootEntry.Builder<?>[] entries = new LootEntry.Builder<?>[factories.length];
            for (int i = 0; i < factories.length; i++) {
                entries[i] = factories[i].build(e);
            }
            return entries;
        }

        LootEntry.Builder<?> build(EntityType<?> entity);
        
        default LootFactory with(ILootCondition.IBuilder... conditions) {
            return e -> {
                LootEntry.Builder<?> entry = this.build(e);
                for (ILootCondition.IBuilder condition : conditions) {
                    entry.acceptCondition(condition);
                }
                return entry;
            };
        }
    }

    /**
     * Interface to get a standalone loot entry from an entity.
     */
    @FunctionalInterface
    public interface StandaloneLootFactory extends LootFactory {

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
                factories[i] = e -> builder;
            }
            return factories;
        }

        /**
         * Calls {@link #build(EntityType)} on all factories with the given entity and returns a new array.
         */
        static StandaloneLootEntry.Builder<?>[] resolve(EntityType<?> e, StandaloneLootFactory[] factories) {
            StandaloneLootEntry.Builder<?>[] entries = new StandaloneLootEntry.Builder<?>[factories.length];
            for (int i = 0; i < factories.length; i++) {
                entries[i] = factories[i].build(e);
            }
            return entries;
        }

        @Override
        StandaloneLootEntry.Builder<?> build(EntityType<?> entity);
        
        default LootFactory withFinal(GenericLootModifier finalModifier) {
            return e -> finalModifier.apply(e, this.build(e));
        }
        
        default StandaloneLootFactory with(LootModifier... modifiers) {
            LootModifier chained = LootModifier.chain(modifiers);
            return e -> chained.apply(e, this.build(e));
        }

        @Override
        default StandaloneLootFactory with(ILootCondition.IBuilder... conditions) {
            return e -> {
                StandaloneLootEntry.Builder<?> entry = this.build(e);
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
                modifiers[i] = (e, b) -> b.acceptFunction(function);
            }
            LootModifier chained = LootModifier.chain(modifiers);
            return e -> chained.apply(e, this.build(e));
        }
    }
    
    public static class WrappedLootEntry implements StandaloneLootFactory {
        
        public final StandaloneLootEntry.Builder<?> entry;

        private WrappedLootEntry(StandaloneLootEntry.Builder<?> entry) {
            this.entry = entry;
        }

        @Override
        public StandaloneLootEntry.Builder<?> build(EntityType<?> entity) {
            return this.entry;
        }
    }

    /**
     * Interface to modify a standalone loot entry to a new loot entry.
     */
    @FunctionalInterface
    public interface GenericLootModifier {

        /**
         * Gets a loot modifier that does nothing.
         */
        static GenericLootModifier identity() {
            return (b, e) -> e;
        }

        LootEntry.Builder<?> apply(EntityType<?> entity, StandaloneLootEntry.Builder<?> entry);
    }

    /**
     * Interface to modify a standalone loot entry to a new standalone loot entry.
     */
    @FunctionalInterface
    public interface LootModifier extends GenericLootModifier {

        /**
         * Gets a loot modifier that does nothing.
         */
        static LootModifier identity() {
            return (e, b) -> b;
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
        StandaloneLootEntry.Builder<?> apply(EntityType<?> entity, StandaloneLootEntry.Builder<?> entry);

        /**
         * Same as {@link #chain(LootModifier...)}
         */
        default LootModifier andThen(LootModifier other) {
            return chain(this, other);
        }
    }
}
