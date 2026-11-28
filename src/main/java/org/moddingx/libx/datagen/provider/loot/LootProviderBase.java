package org.moddingx.libx.datagen.provider.loot;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.moddingx.libx.LibX;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.RegistrySet;
import org.moddingx.libx.datagen.loot.LootBuilders;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;
import org.moddingx.libx.datagen.provider.loot.entry.GenericLootModifier;
import org.moddingx.libx.datagen.provider.loot.entry.LootFactory;
import org.moddingx.libx.datagen.provider.loot.entry.LootModifier;
import org.moddingx.libx.datagen.provider.loot.entry.SimpleLootFactory;
import org.moddingx.libx.impl.datagen.loot.LootData;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class LootProviderBase<T> extends RegistryProviderBase {

    protected final ModX mod;
    protected final RegistrySet registries;
    protected final String folder;
    protected final ContextKeySet contextKeySet;
    protected final Supplier<Stream<Map.Entry<Identifier, T>>> modElements;
    protected final Function<T, Identifier> idResolver;

    private final Set<T> ignored = new HashSet<>();
    private final Map<T, Function<T, LootTable.Builder>> functionMap = new HashMap<>();
    private final MappedRegistry<LootTable> pendingReferences = new MappedRegistry<>(Registries.LOOT_TABLE, Lifecycle.stable());

    protected LootProviderBase(DatagenContext ctx, String folder, ContextKeySet contextKeySet, ResourceKey<? extends Registry<T>> registryKey) {
        this(ctx, folder, contextKeySet,
                () -> ctx.registries().registry(registryKey).entrySet().stream()
                        .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceKey::identifier)))
                        .map(entry -> Map.entry(entry.getKey().identifier(), entry.getValue())),
                id -> ctx.registries().registry(registryKey).getKey(id)
        );
    }
    
    private LootProviderBase(DatagenContext ctx, String folder, ContextKeySet contextKeySet, Supplier<Stream<Map.Entry<Identifier, T>>> modElements, Function<T, Identifier> allElementIds) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
        this.mod = ctx.mod();
        this.registries = ctx.registries();
        this.folder = folder;
        this.contextKeySet = contextKeySet;
        this.modElements = modElements;
        this.idResolver = value -> {
            Identifier id = allElementIds.apply(value);
            if (id == null) throw new IllegalStateException("Unregistered value: " + value);
            return id;
        };
    }
    
    protected LootProviderBase(DatagenContext ctx, String folder, ContextKeySet contextKeySet, Function<T, Identifier> elementIds) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
        this.mod = ctx.mod();
        this.registries = ctx.registries();
        this.folder = folder;
        this.contextKeySet = contextKeySet;
        this.modElements = () -> this.functionMap.keySet().stream().map(element -> Map.entry(elementIds.apply(element), element));
        this.idResolver = value -> {
            Identifier id = elementIds.apply(value);
            if (id == null) throw new IllegalStateException("Unregistered value: " + value);
            return id;
        };
    }

    protected abstract void setup();

    /**
     * The given item will not be processed by this provider. Useful when you want to create the loot table manually.
     */
    protected void customLootTable(T item) {
        this.ignored.add(item);
    }

    /**
     * The given item will get the given loot table.
     */
    protected void customLootTable(T item, LootTable.Builder loot) {
        this.functionMap.put(item, b -> loot);
    }

    /**
     * The given item will get the given loot table function.
     */
    protected void customLootTable(T item, Function<T, LootTable.Builder> loot) {
        this.functionMap.put(item, loot);
    }

    /**
     * Gets the element factory. The element factory is a loot factory that assigns
     * each element a loot entry. The default just assigns an empty entry.
     */
    protected SimpleLootFactory<T> element() {
        return SimpleLootFactory.from(EmptyLootItem.emptyItem());
    }
    
    /**
     * Creates a default loot table for the given item. Can be overridden to alter
     * default behavior. Should return null if no loot table should be generated.
     */
    @Nullable
    protected abstract LootTable.Builder defaultBehavior(T item);

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " " + this.folder + " loot tables";
    }
    
    @Override
    public void run() {
        // We do not invoke super.run()
        // Because of the validation needed for loot tables, we don't support registering from fields.
        this.setup();

        Map<Identifier, LootTable> tables = this.modElements.get()
                .filter(entry -> this.mod.modid.equals(entry.getKey().getNamespace()))
                .filter(entry -> !this.ignored.contains(entry.getValue()))
                .flatMap(this::resolve)
                .map(entry -> Map.entry(Identifier.fromNamespaceAndPath(entry.getKey().getNamespace(), this.folder + "/" + entry.getKey().getPath()), entry.getValue()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        WritableRegistry<LootTable> registry = this.registries.writableRegistry(Registries.LOOT_TABLE);
        for (Map.Entry<Identifier, LootTable> entry : tables.entrySet()) {
            registry.register(ResourceKey.create(Registries.LOOT_TABLE, entry.getKey()), entry.getValue(), RegistrationInfo.BUILT_IN);
        }

        List<ResourceKey<LootTable>> missingReferences = new ArrayList<>();
        for (ResourceKey<LootTable> key : List.copyOf(this.pendingReferences.registryKeySet())) {
            Optional<LootTable> value = registry.getOptional(key);
            if (value.isPresent()) {
                // Binds the holder created in reference(ResourceKey)
                this.pendingReferences.register(key, value.get(), RegistrationInfo.BUILT_IN);
            } else {
                missingReferences.add(key);
            }
        }
        if (!missingReferences.isEmpty()) {
            missingReferences.forEach(key -> LibX.logger.warn("Missing loot table reference: {}", key.identifier()));
            throw new IllegalStateException("There were problems resolving loot table references.");
        }

        ProblemReporter.Collector problems = new ProblemReporter.Collector();
        HolderLookup.Provider lootDataProvider = HolderLookup.Provider.create(Stream.<HolderLookup.RegistryLookup<?>>of(
                this.registries.registry(Registries.LOOT_TABLE), this.registries.registry(Registries.PREDICATE),
                this.registries.registry(Registries.ITEM_MODIFIER), this.registries.registry(Registries.SLOT_SOURCE),
                this.registries.registry(Registries.CONTEXT_INT_PROVIDER), this.registries.registry(Registries.CONTEXT_FLOAT_PROVIDER)
        ));
        ValidationContext validationContext = new ValidationContext(problems, this.contextKeySet, lootDataProvider);

        for (Map.Entry<Identifier, LootTable> entry : tables.entrySet()) {
            entry.getValue().validate(validationContext.enterElement(() -> "{" + entry.getKey() + "}", ResourceKey.create(Registries.LOOT_TABLE, entry.getKey())));
        }

        if (!problems.isEmpty()) {
            problems.forEach((where, what) -> LibX.logger.warn("LootTable validation problem in {}: {}", where, what.description()));
            throw new IllegalStateException("There were problems validating the loot tables.");
        }
    }

    private Stream<Map.Entry<Identifier, LootTable>> resolve(Map.Entry<Identifier, T> entry) {
        Function<T, LootTable.Builder> loot;
        if (this.functionMap.containsKey(entry.getValue())) {
            loot = this.functionMap.get(entry.getValue());
        } else {
            LootTable.Builder builder = this.defaultBehavior(entry.getValue());
            loot = builder == null ? null : b -> builder;
        }
        if (loot == null) return Stream.empty();
        LootTable.Builder builder = loot.apply(entry.getValue());
        if (builder.randomSequence.isEmpty()) builder.setRandomSequence(entry.getKey());
        return Stream.of(Map.entry(entry.getKey(), loot.apply(entry.getValue()).setParamSet(this.contextKeySet).build()));
    }

    protected final LootModifier<T> modifier(BiFunction<T, UniformContainerBase.Builder<?>, UniformContainerBase.Builder<?>> function) {
        return LootModifier.of(this.element(), function);
    }

    protected final GenericLootModifier<T> genericModifier(BiFunction<T, UniformContainerBase.Builder<?>, LootPoolEntryContainer.Builder<?>> function) {
        return GenericLootModifier.of(this.element(), function);
    }
    
    protected final LootModifier<T> identity() {
        return LootModifier.identity(this.element());
    }

    /**
     * Method to add a custom loot table for an item.
     */
    public void drops(T item, ItemStackTemplate... drops) {
        this.drops(item, Arrays.stream(drops).<LootFactory<T>>map(this::stack).toList());
    }
    
    /**
     * Method to add a custom loot table for an item.
     */
    @SafeVarargs
    public final void drops(T item, LootFactory<T>... loot) {
        this.drops(item, Arrays.stream(loot).toList());
    }
    
    /**
     * Method to add a custom loot table for an item.
     */
    public void drops(T item, List<LootFactory<T>> loot) {
        this.generateBaseTable(item, this.combine(loot).build(item));
    }
    
    /**
     * Generate the base loot table.
     */
    public void generateBaseTable(T item, LootPoolEntryContainer.Builder<?> entry) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1)).add(entry);
        this.customLootTable(item, LootTable.lootTable().withPool(pool));
    }
    
    /**
     * Turns a singleton loot entry into a simple loot factory.
     */
    public SimpleLootFactory<T> from(UniformContainerBase.Builder<?> entry) {
        return SimpleLootFactory.from(entry);
    }

    /**
     * Turns a loot entry into a loot factory.
     */
    public LootFactory<T> from(LootPoolEntryContainer.Builder<?> entry) {
        return LootFactory.from(entry);
    }

    /**
     * Turns a loot function into a loot modifier.
     */
    public LootModifier<T> from(LootItemConditionalFunction.Builder<?> function) {
        return this.modifier((item, entry) -> entry.apply(function));
    }

    /**
     * Makes a reference to another loot table in this mod.
     */
    public SimpleLootFactory<T> reference(T value) {
        Identifier elementId = this.idResolver.apply(value);
        return this.reference(ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(elementId.getNamespace(), this.folder + "/" + elementId.getPath())));
    }

    /**
     * Makes a reference to another loot table.
     */
    public SimpleLootFactory<T> reference(ResourceKey<LootTable> lootTable) {
        Registry<LootTable> registry = this.registries.registry(Registries.LOOT_TABLE);
        Holder<LootTable> holder = registry.get(lootTable).<Holder<LootTable>>map(h -> h).orElseGet(
                () -> this.pendingReferences.createRegistrationLookup().getOrThrow(lootTable)
        );

        return SimpleLootFactory.from(NestedLootTable.lootTableReference(holder));
    }
    
    /**
     * A condition that is random with a chance.
     */
    public LootItemCondition.Builder random(float chance) {
        return LootItemRandomChanceCondition.randomChance(chance);
    }

    /**
     * A loot modifier that sets the count of a stack.
     */
    public LootModifier<T> count(int count) {
        return this.from(SetItemCountFunction.setCount(ContextIntProviders.exactly(count)));
    }

    /**
     * A loot modifier that uniformly sets the count of a stack between two values.
     */
    public LootModifier<T> count(int min, int max) {
        if (min == max) {
            return this.from(SetItemCountFunction.setCount(ContextIntProviders.exactly(min)));
        } else {
            return this.from(SetItemCountFunction.setCount(ContextIntProviders.between(min, max)));
        }
    }

    /**
     * A loot modifier that sets the count of a stack based on a binomial formula.
     */
    public LootModifier<T> countBinomial(float chance, int num) {
        return this.from(SetItemCountFunction.setCount(ContextIntProviders.binomial(num, chance)));
    }

    /**
     * Inverts a loot condition
     */
    public LootItemCondition.Builder not(LootItemCondition.Builder condition) {
        return InvertedLootItemCondition.invert(condition);
    }
    
    /**
     * Creates a condition that is met when all the given conditions are met.
     */
    public LootItemCondition.Builder and(LootItemCondition.Builder... conditions) {
        return AllOfCondition.allOf(conditions);
    }

    /**
     * Creates a condition that is met when at least one of the given conditions is met.
     */
    public LootItemCondition.Builder or(LootItemCondition.Builder... conditions) {
        return AnyOfCondition.anyOf(conditions);
    }

    /**
     * A loot factory for a specific item.
     */
    public SimpleLootFactory<T> stack(ItemLike item) {
        return this.from(LootItem.lootTableItem(item));
    }

    /**
     * Tries to create the best possible representation of the given stack template in a loot entry.
     */
    public SimpleLootFactory<T> stack(ItemStackTemplate template) {
        return this.from(LootData.stack(template));
    }
    
    /**
     * Combines the given loot factories into one. (All loot factories will be applied).
     */
    @SafeVarargs
    public final LootFactory<T> combine(LootFactory<T>... loot) {
        return this.combine(Arrays.stream(loot).toList());
    }
    
    /**
     * Combines the given loot factories into one. (All loot factories will be applied).
     */
    public final LootFactory<T> combine(List<LootFactory<T>> loot) {
        return e -> LootData.combineBy(LootBuilders::all, l -> l.build(e), loot);
    }

    /**
     * Combines the given loot factories into one. (One loot factory will be applied).
     */
    @SafeVarargs
    public final LootFactory<T> random(LootFactory<T>... loot) {
        return this.random(Arrays.stream(loot).toList());
    
    }
    
    /**
     * Combines the given loot factories into one. (One loot factory will be applied).
     */
    public final LootFactory<T> random(List<LootFactory<T>> loot) {
        return e -> LootData.combineBy(LootBuilders::group, l -> l.build(e), loot);
    }

    /**
     * Combines the given loot factories into one. Only the first matching factory is applied.
     */
    @SafeVarargs
    public final LootFactory<T> first(LootFactory<T>... loot) {
        return this.first(Arrays.stream(loot).toList());
    
    }
    
    /**
     * Combines the given loot factories into one. Only the first matching factory is applied.
     */
    public final LootFactory<T> first(List<LootFactory<T>> loot) {
        return e -> LootData.combineBy(LootBuilders::alternative, l -> l.build(e), loot);
    }
    
    /**
     * Combines the given loot factories into one.
     * From all the loot factories until the first one not matching, one is selected.
     */
    @SafeVarargs
    public final LootFactory<T> whileMatch(LootFactory<T>... loot) {
        return this.whileMatch(Arrays.stream(loot).toList());
    }
    
    /**
     * Combines the given loot factories into one.
     * From all the loot factories until the first one not matching, one is selected.
     */
    public final LootFactory<T> whileMatch(List<LootFactory<T>> loot) {
        return e -> LootData.combineBy(LootBuilders::sequence, l -> l.build(e), loot);
    }
}
