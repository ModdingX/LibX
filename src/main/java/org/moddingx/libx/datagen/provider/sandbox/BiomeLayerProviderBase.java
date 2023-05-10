package org.moddingx.libx.datagen.provider.sandbox;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;
import org.moddingx.libx.sandbox.SandBox;
import org.moddingx.libx.sandbox.generator.BiomeLayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * SandBox provider for {@link BiomeLayer biome layers}.
 * 
 * This provider must run in the {@link DatagenStage#REGISTRY_SETUP registry setup} stage.
 */
public abstract class BiomeLayerProviderBase extends RegistryProviderBase {

    protected BiomeLayerProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " biome layers";
    }
    
    public BiomeLayerBuilder layer() {
        return this.layer(1);
    }

    public BiomeLayerBuilder layer(int weight) {
        return new BiomeLayerBuilder(weight);
    }
    
    public class BiomeLayerBuilder {
        
        private final int weight;
        private ClimateRangeTarget range;
        private final List<Pair<Climate.ParameterPoint, Holder<Biome>>> biomes;
        
        private BiomeLayerBuilder(int weight) {
            this.weight = weight;
            this.range = ClimateRangeTarget.Special.FULL_RANGE;
            this.biomes = new ArrayList<>();
        }

        /**
         * Sets the noise range used for this {@link BiomeLayer} to the full noise range.
         */
        public BiomeLayerBuilder fullRange() {
            this.range = ClimateRangeTarget.Special.FULL_RANGE;
            return this;
        }

        /**
         * Sets the noise range used for this {@link BiomeLayer} to be automatically computed to cover the minimum
         * noise range that includes all contained biomes.
         */
        public BiomeLayerBuilder dynamicRange() {
            this.range = ClimateRangeTarget.Special.DYNAMIC;
            return this;
        }

        /**
         * Sets the noise range used for this {@link BiomeLayer} to the given range.
         */
        public BiomeLayerBuilder range(Climate.ParameterPoint range) {
            this.range = new ClimateRangeTarget.Value(range);
            return this;
        }

        /**
         * Adds a {@link Biome} to this {@link BiomeLayer} and returns a builder for this biomes climate settings.
         */
        public ClimateBuilder biome(ResourceKey<Biome> biome) {
            return this.biome(BiomeLayerProviderBase.this.holder(biome));
        }
        
        /**
         * Adds a {@link Biome} to this {@link BiomeLayer} and returns a builder for this biomes climate settings.
         */
        public ClimateBuilder biome(Holder<Biome> biome) {
            return new ClimateBuilder(this, biome);
        }

        /**
         * Builds the {@link BiomeLayer}.
         *
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<BiomeLayer> build() {
            if (this.biomes.isEmpty()) throw new IllegalStateException("Empty biome layer");
            Climate.ParameterList<Holder<Biome>> climateData = new Climate.ParameterList<>(List.copyOf(this.biomes));
            BiomeLayer layer = new BiomeLayer(this.weight, this.range.build(climateData), climateData);
            return BiomeLayerProviderBase.this.registries.writableRegistry(SandBox.BIOME_LAYER).createIntrusiveHolder(layer);
        }
    }
    
    public class ClimateBuilder {
        
        private final BiomeLayerBuilder target;
        private final Holder<Biome> biome;
        @Nullable private Climate.Parameter temperature;
        @Nullable private Climate.Parameter humidity;
        @Nullable private Climate.Parameter continentalness;
        @Nullable private Climate.Parameter erosion;
        @Nullable private Climate.Parameter depth;
        @Nullable private Climate.Parameter weirdness;
        private long offset;

        private ClimateBuilder(BiomeLayerBuilder target, Holder<Biome> biome) {
            this.target = target;
            this.biome = biome;
            this.temperature = null;
            this.humidity = null;
            this.continentalness = null;
            this.erosion = null;
            this.depth = null;
            this.weirdness = null;
            this.offset = 0;
        }

        /**
         * Sets all {@link Climate.ParameterPoint parameters} to cover the full noise range.
         */
        public ClimateBuilder fullRange() {
            this.temperature = Climate.Parameter.span(-1, 1);
            this.humidity = Climate.Parameter.span(-1, 1);
            this.continentalness = Climate.Parameter.span(-1, 1);
            this.erosion = Climate.Parameter.span(-1, 1);
            this.depth = Climate.Parameter.span(-1, 1);
            this.weirdness = Climate.Parameter.span(-1, 1);
            return this;
        }

        /**
         * Sets the {@link Climate.ParameterPoint#temperature() temperature} range for this {@link Biome} to a
         * single value.
         */
        public ClimateBuilder temperature(float temperature) {
            return this.temperature(temperature, temperature);
        }
        
        /**
         * Sets the {@link Climate.ParameterPoint#temperature() temperature} range for this {@link Biome} to the
         * given range.
         */
        public ClimateBuilder temperature(float min, float max) {
            return this.temperature(new Climate.Parameter(Climate.quantizeCoord(min), Climate.quantizeCoord(max)));
        }

        /**
         * Sets the {@link Climate.ParameterPoint#temperature() temperature} range for this {@link Biome} to the
         * given range.
         */
        public ClimateBuilder temperature(Climate.Parameter temperature) {
            this.temperature = temperature;
            return this;
        }

        /**
         * Sets the {@link Climate.ParameterPoint#humidity() humidity} range for this {@link Biome} to a
         * single value.
         */
        public ClimateBuilder humidity(float humidity) {
            return this.humidity(humidity, humidity);
        }

        /**
         * Sets the {@link Climate.ParameterPoint#humidity() humidity} range for this {@link Biome} to the
         * given range.
         */
        public ClimateBuilder humidity(float min, float max) {
            return this.humidity(new Climate.Parameter(Climate.quantizeCoord(min), Climate.quantizeCoord(max)));
        }

        /**
         * Sets the {@link Climate.ParameterPoint#humidity() humidity} range for this {@link Biome} to the
         * given range.
         */
        public ClimateBuilder humidity(Climate.Parameter humidity) {
            this.humidity = humidity;
            return this;
        }

        /**
         * Sets the {@link Climate.ParameterPoint#continentalness() continentalness} range for this {@link Biome} to a
         * single value.
         */
        public ClimateBuilder continentalness(float continentalness) {
            return this.continentalness(continentalness, continentalness);
        }

        /**
         * Sets the {@link Climate.ParameterPoint#continentalness() continentalness} range for this {@link Biome} to the
         * given range.
         */
        public ClimateBuilder continentalness(float min, float max) {
            return this.continentalness(new Climate.Parameter(Climate.quantizeCoord(min), Climate.quantizeCoord(max)));
        }

        /**
         * Sets the {@link Climate.ParameterPoint#continentalness() continentalness} range for this {@link Biome} to the
         * given range.
         */
        public ClimateBuilder continentalness(Climate.Parameter continentalness) {
            this.continentalness = continentalness;
            return this;
        }

        /**
         * Sets the {@link Climate.ParameterPoint#erosion() erosion} range for this {@link Biome} to a
         * single value.
         */
        public ClimateBuilder erosion(float erosion) {
            return this.erosion(erosion, erosion);
        }

        /**
         * Sets the {@link Climate.ParameterPoint#erosion() erosion} range for this {@link Biome} to the
         * given range.
         */
        public ClimateBuilder erosion(float min, float max) {
            return this.erosion(new Climate.Parameter(Climate.quantizeCoord(min), Climate.quantizeCoord(max)));
        }

        /**
         * Sets the {@link Climate.ParameterPoint#erosion() erosion} range for this {@link Biome} to the
         * given range.
         */
        public ClimateBuilder erosion(Climate.Parameter erosion) {
            this.erosion = erosion;
            return this;
        }

        /**
         * Sets the {@link Climate.ParameterPoint#depth() depth} range for this {@link Biome} to a
         * single value.
         */
        public ClimateBuilder depth(float depth) {
            return this.depth(depth, depth);
        }

        /**
         * Sets the {@link Climate.ParameterPoint#depth() depth} range for this {@link Biome} to the
         * given range.
         */
        public ClimateBuilder depth(float min, float max) {
            return this.depth(new Climate.Parameter(Climate.quantizeCoord(min), Climate.quantizeCoord(max)));
        }

        /**
         * Sets the {@link Climate.ParameterPoint#depth() depth} range for this {@link Biome} to the
         * given range.
         */
        public ClimateBuilder depth(Climate.Parameter depth) {
            this.depth = depth;
            return this;
        }

        /**
         * Sets the {@link Climate.ParameterPoint#weirdness() weirdness} range for this {@link Biome} to a
         * single value.
         */
        public ClimateBuilder weirdness(float weirdness) {
            return this.weirdness(weirdness, weirdness);
        }

        /**
         * Sets the {@link Climate.ParameterPoint#weirdness() weirdness} range for this {@link Biome} to the
         * given range.
         */
        public ClimateBuilder weirdness(float min, float max) {
            return this.weirdness(new Climate.Parameter(Climate.quantizeCoord(min), Climate.quantizeCoord(max)));
        }

        /**
         * Sets the {@link Climate.ParameterPoint#weirdness() weirdness} range for this {@link Biome} to the
         * given range.
         */
        public ClimateBuilder weirdness(Climate.Parameter weirdness) {
            this.weirdness = weirdness;
            return this;
        }

        /**
         * Sets the climate offset used for this biome.
         */
        public ClimateBuilder offset(long offset) {
            this.offset = offset;
            return this;
        }
        
        /**
         * Sets all climate parameters for this biome based on the given {@link Climate.ParameterPoint parameter point}.
         */
        public BiomeLayerBuilder from(Climate.ParameterPoint climate) {
            this.target.biomes.add(Pair.of(climate, this.biome));
            return this.target;
        }
        
        /**
         * Adds this biome to the {@link BiomeLayer} and returns to the layer builder.
         */
        public BiomeLayerBuilder add() {
            Objects.requireNonNull(this.temperature, "Climate parameter unset: temperature");
            Objects.requireNonNull(this.humidity, "Climate parameter unset: humidity");
            Objects.requireNonNull(this.continentalness, "Climate parameter unset: continentalness");
            Objects.requireNonNull(this.erosion, "Climate parameter unset: erosion");
            Objects.requireNonNull(this.depth, "Climate parameter unset: depth");
            Objects.requireNonNull(this.weirdness, "Climate parameter unset: weirdness");
            this.target.biomes.add(Pair.of(new Climate.ParameterPoint(this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, this.offset), this.biome));
            return this.target;
        }
    }
    
    private sealed interface ClimateRangeTarget {

        Climate.ParameterPoint build(Climate.ParameterList<Holder<Biome>> climateData);

        enum Special implements ClimateRangeTarget {

            FULL_RANGE, DYNAMIC;

            @Override
            public Climate.ParameterPoint build(Climate.ParameterList<Holder<Biome>> climateData) {
                return switch (this) {
                    case FULL_RANGE -> BiomeLayer.FULL_RANGE;
                    case DYNAMIC -> new Climate.ParameterPoint(
                            ofAll(climateData, Climate.ParameterPoint::temperature),
                            ofAll(climateData, Climate.ParameterPoint::humidity),
                            ofAll(climateData, Climate.ParameterPoint::continentalness),
                            ofAll(climateData, Climate.ParameterPoint::erosion),
                            ofAll(climateData, Climate.ParameterPoint::depth),
                            ofAll(climateData, Climate.ParameterPoint::weirdness),
                            0
                    );
                };
            }

            private static Climate.Parameter ofAll(Climate.ParameterList<Holder<Biome>> climateData, Function<Climate.ParameterPoint, Climate.Parameter> extractor) {
                List<Climate.Parameter> params = climateData.values().stream().map(Pair::getFirst).map(extractor).toList();
                long min = params.stream().mapToLong(Climate.Parameter::min).min().orElse(0);
                long max = params.stream().mapToLong(Climate.Parameter::max).max().orElse(0);
                return new Climate.Parameter(Math.min(min, max), Math.max(min, max));
            }
        }

        record Value(Climate.ParameterPoint value) implements ClimateRangeTarget {

            @Override
            public Climate.ParameterPoint build(Climate.ParameterList<Holder<Biome>> climateData) {
                return this.value();
            }
        }
    }
}
