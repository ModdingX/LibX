package org.moddingx.libx.datagen.provider.sandbox;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.sandbox.SandBox;
import org.moddingx.libx.sandbox.generator.BiomeLayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public abstract class BiomeLayerProviderBase extends SandBoxProviderBase {

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

        public BiomeLayerBuilder fullRange() {
            this.range = ClimateRangeTarget.Special.FULL_RANGE;
            return this;
        }

        public BiomeLayerBuilder dynamicRange() {
            this.range = ClimateRangeTarget.Special.DYNAMIC;
            return this;
        }

        public BiomeLayerBuilder range(Climate.ParameterPoint range) {
            this.range = new ClimateRangeTarget.Value(range);
            return this;
        }
        
        public ClimateBuilder biome(ResourceKey<Biome> biome) {
            return this.biome(BiomeLayerProviderBase.this.holder(biome));
        }
        
        public ClimateBuilder biome(Holder<Biome> biome) {
            return new ClimateBuilder(this, biome);
        }
        
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
        
        public ClimateBuilder temperature(float temperature) {
            return this.temperature(temperature, temperature);
        }
        
        public ClimateBuilder temperature(float min, float max) {
            return this.temperature(new Climate.Parameter(Climate.quantizeCoord(min), Climate.quantizeCoord(max)));
        }
        
        public ClimateBuilder temperature(Climate.Parameter temperature) {
            this.temperature = temperature;
            return this;
        }

        public ClimateBuilder humidity(float humidity) {
            return this.humidity(humidity, humidity);
        }
        
        public ClimateBuilder humidity(float min, float max) {
            return this.humidity(new Climate.Parameter(Climate.quantizeCoord(min), Climate.quantizeCoord(max)));
        }
        
        public ClimateBuilder humidity(Climate.Parameter humidity) {
            this.humidity = humidity;
            return this;
        }

        public ClimateBuilder continentalness(float continentalness) {
            return this.continentalness(continentalness, continentalness);
        }
        
        public ClimateBuilder continentalness(float min, float max) {
            return this.continentalness(new Climate.Parameter(Climate.quantizeCoord(min), Climate.quantizeCoord(max)));
        }
        
        public ClimateBuilder continentalness(Climate.Parameter continentalness) {
            this.continentalness = continentalness;
            return this;
        }

        public ClimateBuilder erosion(float erosion) {
            return this.erosion(erosion, erosion);
        }
        
        public ClimateBuilder erosion(float min, float max) {
            return this.erosion(new Climate.Parameter(Climate.quantizeCoord(min), Climate.quantizeCoord(max)));
        }
        
        public ClimateBuilder erosion(Climate.Parameter erosion) {
            this.erosion = erosion;
            return this;
        }

        public ClimateBuilder depth(float depth) {
            return this.depth(depth, depth);
        }
        
        public ClimateBuilder depth(float min, float max) {
            return this.depth(new Climate.Parameter(Climate.quantizeCoord(min), Climate.quantizeCoord(max)));
        }
        
        public ClimateBuilder depth(Climate.Parameter depth) {
            this.depth = depth;
            return this;
        }

        public ClimateBuilder weirdness(float weirdness) {
            return this.weirdness(weirdness, weirdness);
        }
        
        public ClimateBuilder weirdness(float min, float max) {
            return this.weirdness(new Climate.Parameter(Climate.quantizeCoord(min), Climate.quantizeCoord(max)));
        }
        
        public ClimateBuilder weirdness(Climate.Parameter weirdness) {
            this.weirdness = weirdness;
            return this;
        }

        public ClimateBuilder offset(long offset) {
            this.offset = offset;
            return this;
        }
        
        public BiomeLayerBuilder from(Climate.ParameterPoint climate) {
            this.target.biomes.add(Pair.of(climate, this.biome));
            return this.target;
        }
        
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
