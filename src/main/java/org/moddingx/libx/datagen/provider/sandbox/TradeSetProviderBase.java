package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * SandBox provider for {@link TradeSet trade sets}.
 *
 * This provider must run in the {@link DatagenStage#REGISTRY_SETUP registry setup} stage.
 */
public abstract class TradeSetProviderBase extends RegistryProviderBase {

    protected TradeSetProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " trade sets";
    }

    /**
     * Returns a new builder for a trade set that picks its trades from the given tag.
     */
    public TradeSetBuilder tradeSet(TagKey<VillagerTrade> trades) {
        return this.tradeSet(this.set(trades));
    }

    /**
     * Returns a new builder for a trade set that picks its trades from the given trades.
     */
    @SafeVarargs
    public final TradeSetBuilder tradeSet(Holder<VillagerTrade>... trades) {
        return this.tradeSet(this.set(trades));
    }

    /**
     * Returns a new builder for a trade set that picks its trades from the given holder set.
     */
    public TradeSetBuilder tradeSet(HolderSet<VillagerTrade> trades) {
        return new TradeSetBuilder(trades);
    }

    public class TradeSetBuilder {

        private final HolderSet<VillagerTrade> trades;
        private NumberProvider amount;
        private boolean allowDuplicates;
        @Nullable
        private Identifier randomSequence;

        private TradeSetBuilder(HolderSet<VillagerTrade> trades) {
            this.trades = trades;
            this.amount = ConstantValue.exactly(2);
            this.allowDuplicates = false;
            this.randomSequence = null;
        }

        /**
         * Sets the exact amount of trades to pick from the pool. Defaults to {@code 2}.
         */
        public TradeSetBuilder amount(int amount) {
            return this.amount(ConstantValue.exactly(amount));
        }

        /**
         * Sets the amount of trades to pick from the pool to a uniformly distributed random value.
         */
        public TradeSetBuilder amount(int min, int max) {
            return this.amount(UniformGenerator.between(min, max));
        }

        /**
         * Sets the amount of trades to pick from the pool.
         */
        public TradeSetBuilder amount(NumberProvider amount) {
            this.amount = amount;
            return this;
        }

        /**
         * Allows the same trade to be picked multiple times. By default, each trade is picked at most once,
         * which also caps the effective amount at the size of the pool.
         */
        public TradeSetBuilder allowDuplicates() {
            this.allowDuplicates = true;
            return this;
        }

        /**
         * Sets the random sequence used to pick the trades. Trade sets sharing a sequence produce the same
         * results for the same world seed. Vanilla uses the id of the trade set prefixed with
         * {@code trade_set/} here.
         */
        public TradeSetBuilder randomSequence(Identifier randomSequence) {
            this.randomSequence = randomSequence;
            return this;
        }

        /**
         * Builds the {@link TradeSet}.
         *
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added to the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<TradeSet> build() {
            TradeSet set = new TradeSet(this.trades, this.amount, this.allowDuplicates, Optional.ofNullable(this.randomSequence));
            return TradeSetProviderBase.this.registries.writableRegistry(Registries.TRADE_SET).createIntrusiveHolder(set);
        }
    }
}
