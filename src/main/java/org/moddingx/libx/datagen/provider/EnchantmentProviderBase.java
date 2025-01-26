package org.moddingx.libx.datagen.provider;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.*;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.EnchantmentsByCost;
import net.minecraft.world.item.enchantment.providers.EnchantmentsByCostWithDifficulty;
import net.minecraft.world.item.enchantment.providers.SingleEnchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Registry provider for {@link Enchantment enchantments} and {@link EnchantmentProvider enchantment providers}.
 * <p>
 * This provider must run in the {@link DatagenStage#REGISTRY_SETUP registry setup} stage.
 */
public abstract class EnchantmentProviderBase extends RegistryProviderBase {

    public EnchantmentProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " enchantments";
    }

    public EnchantmentBuilder enchantment(Component name) {
        return new EnchantmentBuilder(name);
    }

    public SingleEnchantmentProviderBuilder provider(Holder<Enchantment> enchantment) {
        return new SingleEnchantmentProviderBuilder(enchantment);
    }

    public MultiEnchantmentProviderBuilder provider(TagKey<Enchantment> enchantments) {
        return new MultiEnchantmentProviderBuilder(this.set(enchantments));
    }
    
    public MultiEnchantmentProviderBuilder provider(HolderSet<Enchantment> enchantments) {
        return new MultiEnchantmentProviderBuilder(enchantments);
    }

    public class EnchantmentBuilder {

        private final Component name;
        @Nullable private HolderSet<Item> supportedItems;
        @Nullable private HolderSet<Item> primaryItems;
        private int weight;
        private int maxLevel;
        @Nullable private Enchantment.Cost minCost;
        @Nullable private Enchantment.Cost maxCost;
        private int anvilCost;
        private final Set<EquipmentSlotGroup> slots;
        private HolderSet<Enchantment> exclusiveSet;
        private final DataComponentMap.Builder effects;
        private final Map<DataComponentType<List<?>>, List<?>> effectLists;

        private EnchantmentBuilder(Component name) {
            this.name = name;
            this.supportedItems = null;
            this.primaryItems = null;
            this.weight = 1;
            this.maxLevel = 1;
            this.minCost = null;
            this.maxCost = null;
            this.anvilCost = 1;
            this.slots = new HashSet<>();
            this.exclusiveSet = HolderSet.direct();
            this.effects = DataComponentMap.builder();
            this.effectLists = new HashMap<>();
        }
        
        public EnchantmentBuilder supportedItems(ItemLike... items) {
            List<Holder.Reference<Item>> holderList = Arrays.stream(items).map(item -> EnchantmentProviderBase.this.holder(Registries.ITEM, item.asItem())).toList();
            return this.supportedItems(HolderSet.direct(holderList));
        }
        
        public EnchantmentBuilder supportedItems(TagKey<Item> items) {
            return this.supportedItems(EnchantmentProviderBase.this.set(items));
        }
        
        public EnchantmentBuilder supportedItems(HolderSet<Item> items) {
            this.supportedItems = items;
            return this;
        }
        
        public EnchantmentBuilder primaryItems(ItemLike... items) {
            List<Holder.Reference<Item>> holderList = Arrays.stream(items).map(item -> EnchantmentProviderBase.this.holder(Registries.ITEM, item.asItem())).toList();
            return this.primaryItems(HolderSet.direct(holderList));
        }
        
        public EnchantmentBuilder primaryItems(TagKey<Item> items) {
            return this.primaryItems(EnchantmentProviderBase.this.set(items));
        }
        
        public EnchantmentBuilder primaryItems(HolderSet<Item> items) {
            this.primaryItems = items;
            return this;
        }
        
        public EnchantmentBuilder weight(int weight) {
            this.weight = weight;
            return this;
        }
        
        public EnchantmentBuilder maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }
        
        public EnchantmentBuilder minCost(int base, int extraPerAdditionalLevel) {
            this.minCost = new Enchantment.Cost(base, extraPerAdditionalLevel);
            return this;
        }
        
        public EnchantmentBuilder maxCost(int base, int extraPerAdditionalLevel) {
            this.maxCost = new Enchantment.Cost(base, extraPerAdditionalLevel);
            return this;
        }
        
        public EnchantmentBuilder cost(int base, int extraPerAdditionalLevel) {
            this.minCost = new Enchantment.Cost(base, extraPerAdditionalLevel);
            this.maxCost = new Enchantment.Cost(base, extraPerAdditionalLevel);
            return this;
        }
        
        public EnchantmentBuilder anvilCost(int anvilCost) {
            this.anvilCost = anvilCost;
            return this;
        }
        
        public EnchantmentBuilder slot(EquipmentSlotGroup slot) {
            this.slots.add(slot);
            return this;
        }
        
        @SafeVarargs
        public final EnchantmentBuilder exclusiveSet(Holder<Enchantment>... exclusiveSet) {
            return this.exclusiveSet(EnchantmentProviderBase.this.set(exclusiveSet));
        }
        
        public EnchantmentBuilder exclusiveSet(TagKey<Enchantment> exclusiveSet) {
            return this.exclusiveSet(EnchantmentProviderBase.this.set(exclusiveSet));
        }
        
        public EnchantmentBuilder exclusiveSet(HolderSet<Enchantment> exclusiveSet) {
            this.exclusiveSet = exclusiveSet;
            return this;
        }
        
        public <T> EnchantmentBuilder onlyEffect(DataComponentType<T> type, T value) {
            this.effects.set(type, value);
            return this;
        }
        
        public <T> EnchantmentBuilder effect(DataComponentType<List<T>> type, T value) {
            //noinspection unchecked
            List<T> effectList = ((List<T>) this.effectLists.computeIfAbsent((DataComponentType<List<?>>) (DataComponentType<?>) type, k -> new ArrayList<>()));
            effectList.add(value);
            return this;
        }
        
        public <T> EnchantmentBuilder conditionalEffect(DataComponentType<List<ConditionalEffect<T>>> type, T value) {
            return this.conditionalEffect(type, value, null);
        }
        
        public <T> EnchantmentBuilder conditionalEffect(DataComponentType<List<ConditionalEffect<T>>> type, T value, @Nullable LootItemCondition condition) {
            //noinspection unchecked
            List<ConditionalEffect<T>> effectList = ((List<ConditionalEffect<T>>) this.effectLists.computeIfAbsent((DataComponentType<List<?>>) (DataComponentType<?>) type, k -> new ArrayList<>()));
            effectList.add(new ConditionalEffect<>(value, Optional.ofNullable(condition)));
            return this;
        }

        public EnchantmentBuilder addValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, float value) {
            return this.addValue(type, value, null);
        }

        public EnchantmentBuilder multiplyValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, float value) {
            return this.multiplyValue(type, value, null);
        }

        public EnchantmentBuilder replaceValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, float value) {
            return this.replaceValue(type, value, null);
        }

        public EnchantmentBuilder removeBinomialValueValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, float value) {
            return this.removeBinomialValueValue(type, value, null);
        }

        public EnchantmentBuilder addValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, float value, @Nullable LootItemCondition condition) {
            return this.addValue(type, new LevelBasedValue.Constant(value), condition);
        }

        public EnchantmentBuilder multiplyValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, float value, @Nullable LootItemCondition condition) {
            return this.multiplyValue(type, new LevelBasedValue.Constant(value), condition);
        }

        public EnchantmentBuilder replaceValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, float value, @Nullable LootItemCondition condition) {
            return this.replaceValue(type, new LevelBasedValue.Constant(value), condition);
        }

        public EnchantmentBuilder removeBinomialValueValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, float value, @Nullable LootItemCondition condition) {
            return this.removeBinomialValueValue(type, new LevelBasedValue.Constant(value), condition);
        }

        public EnchantmentBuilder addValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, float base, float extraPerLevelAboveFirst) {
            return this.addValue(type, base, extraPerLevelAboveFirst, null);
        }

        public EnchantmentBuilder multiplyValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, float base, float extraPerLevelAboveFirst) {
            return this.multiplyValue(type, base, extraPerLevelAboveFirst, null);
        }

        public EnchantmentBuilder replaceValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, float base, float extraPerLevelAboveFirst) {
            return this.replaceValue(type, base, extraPerLevelAboveFirst, null);
        }

        public EnchantmentBuilder removeBinomialValueValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, float base, float extraPerLevelAboveFirst) {
            return this.removeBinomialValueValue(type, base, extraPerLevelAboveFirst, null);
        }

        public EnchantmentBuilder addValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, float base, float extraPerLevelAboveFirst, @Nullable LootItemCondition condition) {
            return this.addValue(type, new LevelBasedValue.Linear(base, extraPerLevelAboveFirst), condition);
        }

        public EnchantmentBuilder multiplyValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, float base, float extraPerLevelAboveFirst, @Nullable LootItemCondition condition) {
            return this.multiplyValue(type, new LevelBasedValue.Linear(base, extraPerLevelAboveFirst), condition);
        }

        public EnchantmentBuilder replaceValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, float base, float extraPerLevelAboveFirst, @Nullable LootItemCondition condition) {
            return this.replaceValue(type, new LevelBasedValue.Linear(base, extraPerLevelAboveFirst), condition);
        }

        public EnchantmentBuilder removeBinomialValueValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, float base, float extraPerLevelAboveFirst, @Nullable LootItemCondition condition) {
            return this.removeBinomialValueValue(type, new LevelBasedValue.Linear(base, extraPerLevelAboveFirst), condition);
        }

        public EnchantmentBuilder addValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, LevelBasedValue value) {
            return this.addValue(type, value, null);
        }

        public EnchantmentBuilder multiplyValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, LevelBasedValue value) {
            return this.multiplyValue(type, value, null);
        }

        public EnchantmentBuilder replaceValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, LevelBasedValue value) {
            return this.replaceValue(type, value, null);
        }

        public EnchantmentBuilder removeBinomialValueValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, LevelBasedValue value) {
            return this.removeBinomialValueValue(type, value, null);
        }
        
        public EnchantmentBuilder addValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, LevelBasedValue value, @Nullable LootItemCondition condition) {
            return this.conditionalEffect(type, new AddValue(value), condition);
        }
        
        public EnchantmentBuilder multiplyValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, LevelBasedValue value, @Nullable LootItemCondition condition) {
            return this.conditionalEffect(type, new MultiplyValue(value), condition);
        }
        
        public EnchantmentBuilder replaceValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, LevelBasedValue value, @Nullable LootItemCondition condition) {
            return this.conditionalEffect(type, new SetValue(value), condition);
        }

        public EnchantmentBuilder removeBinomialValueValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, LevelBasedValue value, @Nullable LootItemCondition condition) {
            return this.conditionalEffect(type, new RemoveBinomial(value), condition);
        }

        /**
         * Builds the {@link Enchantment}.
         * <p>
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<Enchantment> build() {
            if (this.supportedItems == null) throw new IllegalStateException("No supported items set for enchantment.");
            if (this.minCost == null) throw new IllegalStateException("No minimum cost set for enchantment.");
            if (this.maxCost == null) throw new IllegalStateException("No maximum cost set for enchantment.");
            if (this.slots.contains(EquipmentSlotGroup.HAND)) {
                this.slots.remove(EquipmentSlotGroup.MAINHAND);
                this.slots.remove(EquipmentSlotGroup.OFFHAND);
            }
            if (this.slots.contains(EquipmentSlotGroup.ARMOR)) {
                this.slots.remove(EquipmentSlotGroup.HEAD);
                this.slots.remove(EquipmentSlotGroup.CHEST);
                this.slots.remove(EquipmentSlotGroup.LEGS);
                this.slots.remove(EquipmentSlotGroup.FEET);
            }
            
            DataComponentMap.Builder finalEffects = DataComponentMap.builder();
            for (Map.Entry<DataComponentType<List<?>>, List<?>> entry : this.effectLists.entrySet()) {
                finalEffects.set(entry.getKey(), List.copyOf(entry.getValue()));
            }
            finalEffects.addAll(this.effects.build());
            List<EquipmentSlotGroup> slotList = this.slots.isEmpty() || this.slots.contains(EquipmentSlotGroup.ANY) ? List.of(EquipmentSlotGroup.ANY) : this.slots.stream().sorted().toList();
            Enchantment.EnchantmentDefinition definition = new Enchantment.EnchantmentDefinition(this.supportedItems, Optional.ofNullable(this.primaryItems), this.weight, this.maxLevel, this.minCost, this.maxCost, this.anvilCost, slotList);
            Enchantment enchantment = new Enchantment(this.name, definition, this.exclusiveSet, finalEffects.build());
            return EnchantmentProviderBase.this.registries.writableRegistry(Registries.ENCHANTMENT).createIntrusiveHolder(enchantment);
        }
    }

    public class SingleEnchantmentProviderBuilder {
        
        private final Holder<Enchantment> enchantment;
        private IntProvider level;

        private SingleEnchantmentProviderBuilder(Holder<Enchantment> enchantment) {
            this.enchantment = enchantment;
            this.level = ConstantInt.of(1);
        }
        
        public SingleEnchantmentProviderBuilder level(int level) {
            return this.level(ConstantInt.of(level));
        }
        
        public SingleEnchantmentProviderBuilder level(int minLevel, int maxLevel) {
            return this.level(UniformInt.of(minLevel, maxLevel));
        }
        
        public SingleEnchantmentProviderBuilder level(IntProvider level) {
            this.level = level;
            return this;
        }

        /**
         * Builds the {@link EnchantmentProvider}.
         * <p>
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<EnchantmentProvider> build() {
            EnchantmentProvider provider = new SingleEnchantment(this.enchantment, this.level);
            return EnchantmentProviderBase.this.registries.writableRegistry(Registries.ENCHANTMENT_PROVIDER).createIntrusiveHolder(provider);
        }
    }

    public class MultiEnchantmentProviderBuilder {
        
        private final HolderSet<Enchantment> enchantments;
        private Cost cost;

        private MultiEnchantmentProviderBuilder(HolderSet<Enchantment> enchantments) {
            this.enchantments = enchantments;
            this.cost = new Cost.Single(ConstantInt.of(1));
        }
        
        public MultiEnchantmentProviderBuilder cost(int cost) {
            return this.cost(ConstantInt.of(cost));
        }
        
        public MultiEnchantmentProviderBuilder cost(int minCost, int maxCost) {
            return this.cost(UniformInt.of(minCost, maxCost));
        }
        
        public MultiEnchantmentProviderBuilder cost(IntProvider cost) {
            this.cost = new Cost.Single(cost);
            return this;
        }
        
        public MultiEnchantmentProviderBuilder difficultyBasedCost(int minCost, int maxCostSpan) {
            this.cost = new Cost.DifficultyBased(minCost, maxCostSpan);
            return this;
        }

        /**
         * Builds the {@link EnchantmentProvider}.
         * <p>
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<EnchantmentProvider> build() {
            EnchantmentProvider provider = switch (this.cost) {
                case Cost.Single single -> new EnchantmentsByCost(this.enchantments, single.value());
                case Cost.DifficultyBased difficultyBased -> new EnchantmentsByCostWithDifficulty(this.enchantments, difficultyBased.minCost(), difficultyBased.maxCostSpan());
            };
            return EnchantmentProviderBase.this.registries.writableRegistry(Registries.ENCHANTMENT_PROVIDER).createIntrusiveHolder(provider);
        }
        
        private sealed interface Cost {
            record Single(IntProvider value) implements Cost {}
            record DifficultyBased(int minCost, int maxCostSpan) implements Cost {}
        }
    }
}
