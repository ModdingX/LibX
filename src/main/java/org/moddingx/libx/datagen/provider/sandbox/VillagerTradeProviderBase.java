package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * SandBox provider for {@link VillagerTrade villager trades}.
 *
 * A villager trade describes a single offer: what the merchant {@link VillagerTradeBuilder#wants() wants}
 * and what it gives in return. Trades are grouped into {@link net.minecraft.world.item.trading.TradeSet trade sets}
 * through tags, see {@link TradeSetProviderBase}.
 *
 * This provider must run in the {@link DatagenStage#REGISTRY_SETUP registry setup} stage.
 */
public abstract class VillagerTradeProviderBase extends RegistryProviderBase {

    protected VillagerTradeProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " villager trades";
    }

    /**
     * Returns a new builder for a trade where the merchant wants {@code count} items of the given type
     * and gives one item of the given result type in return.
     */
    public VillagerTradeBuilder trade(ItemLike wants, int count, ItemLike gives) {
        return this.trade(cost(wants, count), gives, 1);
    }

    /**
     * Returns a new builder for a trade where the merchant wants {@code count} items of the given type
     * and gives {@code resultCount} items of the given result type in return.
     */
    public VillagerTradeBuilder trade(ItemLike wants, int count, ItemLike gives, int resultCount) {
        return this.trade(cost(wants, count), gives, resultCount);
    }

    /**
     * Returns a new builder for a trade with the given cost that gives {@code resultCount} items of the
     * given result type in return.
     */
    public VillagerTradeBuilder trade(TradeCost wants, ItemLike gives, int resultCount) {
        return this.trade(wants, new ItemStackTemplate(gives.asItem(), resultCount));
    }

    /**
     * Returns a new builder for a trade with the given cost and result.
     */
    public VillagerTradeBuilder trade(TradeCost wants, ItemStackTemplate gives) {
        return new VillagerTradeBuilder(wants, gives);
    }

    /**
     * Creates a {@link TradeCost trade cost} for a fixed amount of items.
     */
    public static TradeCost cost(ItemLike item, int count) {
        return new TradeCost(item, count);
    }

    /**
     * Creates a {@link TradeCost trade cost} for a randomised amount of items.
     */
    public static TradeCost cost(ItemLike item, NumberProvider count) {
        return new TradeCost(item, count);
    }

    /**
     * Creates a {@link TradeCost trade cost} for a fixed amount of items that must match the given
     * data component predicate.
     */
    public static TradeCost cost(ItemLike item, NumberProvider count, DataComponentExactPredicate components) {
        return new TradeCost(item.asItem().builtInRegistryHolder(), count, components);
    }

    public class VillagerTradeBuilder {

        private final TradeCost wants;
        private final ItemStackTemplate gives;
        private final List<LootItemFunction> givenItemModifiers;
        @Nullable
        private TradeCost additionalWants;
        private int maxUses;
        private int xp;
        private float reputationDiscount;
        @Nullable
        private LootItemCondition merchantPredicate;
        @Nullable
        private HolderSet<Enchantment> doubleTradePriceEnchantments;

        private VillagerTradeBuilder(TradeCost wants, ItemStackTemplate gives) {
            this.wants = wants;
            this.gives = gives;
            this.givenItemModifiers = new ArrayList<>();
            this.additionalWants = null;
            this.maxUses = 4;
            this.xp = 1;
            this.reputationDiscount = 0;
            this.merchantPredicate = null;
            this.doubleTradePriceEnchantments = null;
        }

        /**
         * Returns the primary cost of this trade.
         */
        public TradeCost wants() {
            return this.wants;
        }

        /**
         * Sets a second item stack the merchant wants in addition to the primary cost.
         */
        public VillagerTradeBuilder additionalWants(ItemLike item, int count) {
            return this.additionalWants(cost(item, count));
        }

        /**
         * Sets a second item stack the merchant wants in addition to the primary cost.
         */
        public VillagerTradeBuilder additionalWants(TradeCost cost) {
            this.additionalWants = cost;
            return this;
        }

        /**
         * Sets how often this trade can be used before the merchant runs out of stock. Defaults to {@code 4}.
         */
        public VillagerTradeBuilder maxUses(int maxUses) {
            this.maxUses = maxUses;
            return this;
        }

        /**
         * Sets the merchant experience granted for using this trade. Defaults to {@code 1}.
         */
        public VillagerTradeBuilder xp(int xp) {
            this.xp = xp;
            return this;
        }

        /**
         * Sets the fraction of the price that is discounted per reputation point the player has with the
         * merchant. Defaults to {@code 0}, vanilla villager trades commonly use {@code 0.05}.
         */
        public VillagerTradeBuilder reputationDiscount(float reputationDiscount) {
            this.reputationDiscount = reputationDiscount;
            return this;
        }

        /**
         * Sets a condition that is tested against the merchant. If it does not match, the trade is not offered.
         */
        public VillagerTradeBuilder merchantPredicate(LootItemCondition condition) {
            this.merchantPredicate = condition;
            return this;
        }

        /**
         * Sets a condition that is tested against the merchant. If it does not match, the trade is not offered.
         */
        public VillagerTradeBuilder merchantPredicate(LootItemCondition.Builder condition) {
            return this.merchantPredicate(condition.build());
        }

        /**
         * Adds loot functions that are applied to the given item stack when the trade is created. This can
         * be used to randomise the result, for example to enchant it.
         */
        public VillagerTradeBuilder modifyResult(LootItemFunction... modifiers) {
            this.givenItemModifiers.addAll(Arrays.asList(modifiers));
            return this;
        }

        /**
         * Adds a loot function that is applied to the given item stack when the trade is created.
         */
        public VillagerTradeBuilder modifyResult(LootItemFunction.Builder modifier) {
            this.givenItemModifiers.add(modifier.build());
            return this;
        }

        /**
         * Adds loot functions that are applied to the given item stack when the trade is created.
         */
        public VillagerTradeBuilder modifyResult(Collection<LootItemFunction> modifiers) {
            this.givenItemModifiers.addAll(modifiers);
            return this;
        }

        /**
         * Sets a set of enchantments that double the price of this trade when the result carries any of them
         * as a stored enchantment. Used by vanilla for enchanted book trades.
         */
        public VillagerTradeBuilder doublePriceFor(TagKey<Enchantment> enchantments) {
            return this.doublePriceFor(VillagerTradeProviderBase.this.set(enchantments));
        }

        /**
         * Sets a set of enchantments that double the price of this trade when the result carries any of them
         * as a stored enchantment. Used by vanilla for enchanted book trades.
         */
        public VillagerTradeBuilder doublePriceFor(HolderSet<Enchantment> enchantments) {
            this.doubleTradePriceEnchantments = enchantments;
            return this;
        }

        /**
         * Builds the {@link VillagerTrade}.
         *
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added to the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<VillagerTrade> build() {
            VillagerTrade trade = new VillagerTrade(
                    this.wants,
                    Optional.ofNullable(this.additionalWants),
                    this.gives,
                    this.maxUses,
                    this.xp,
                    this.reputationDiscount,
                    Optional.ofNullable(this.merchantPredicate),
                    List.copyOf(this.givenItemModifiers),
                    Optional.ofNullable(this.doubleTradePriceEnchantments)
            );
            return VillagerTradeProviderBase.this.registries.writableRegistry(Registries.VILLAGER_TRADE).createIntrusiveHolder(trade);
        }
    }
}
