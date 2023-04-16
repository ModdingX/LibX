package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.material.Fluids;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class FeatureProviderBase extends SandBoxProviderBase {

    protected FeatureProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " features";
    }
    
    public Holder<ConfiguredFeature<?, ?>> feature(Feature<NoneFeatureConfiguration> feature) {
        return this.feature(feature, NoneFeatureConfiguration.INSTANCE);
    }
    
    public <C extends FeatureConfiguration> Holder<ConfiguredFeature<?, ?>> feature(Feature<C> feature, C config) {
        return this.registries.writableRegistry(Registries.CONFIGURED_FEATURE).createIntrusiveHolder(new ConfiguredFeature<>(feature, config));
    }

    public <C extends CarverConfiguration> Holder<ConfiguredWorldCarver<?>> carver(WorldCarver<C> carver, C config) {
        return this.registries.writableRegistry(Registries.CONFIGURED_CARVER).createIntrusiveHolder(new ConfiguredWorldCarver<>(carver, config));
    }
    
    public PlacementBuilder placement(Holder<ConfiguredFeature<?, ?>> feature) {
        return new PlacementBuilder(feature);
    }
    
    public ModifierBuilder modifiers() {
        return new ModifierBuilder();
    }

    public abstract static class AnyPlacementBuilder<T> {
        
        protected final List<PlacementModifier> modifiers;

        private AnyPlacementBuilder() {
            this.modifiers = new ArrayList<>();
        }
        
        public AnyPlacementBuilder<T> count(int count) {
            return this.count(ConstantInt.of(count));
        }
        
        public AnyPlacementBuilder<T> count(int min, int max) {
            return this.count(UniformInt.of(min, max));
        }
        
        public AnyPlacementBuilder<T> count(IntProvider count) {
            return this.add(CountPlacement.of(count));
        }
        
        public AnyPlacementBuilder<T> countExtra(int base, float chance, int extra) {
            return this.add(PlacementUtils.countExtra(base, chance, extra));
        }

        public AnyPlacementBuilder<T> rarity(int avgOnceEveryChunk) {
            return this.add(RarityFilter.onAverageOnceEvery(avgOnceEveryChunk));
        }
        
        public AnyPlacementBuilder<T> noiseCount(int noiseToCount, double factor, double offset) {
            return this.add(NoiseBasedCountPlacement.of(noiseToCount, factor, offset));
        }

        public AnyPlacementBuilder<T> noiseThresholdCount(double noiseLevel, int above, int below) {
            return this.add(NoiseThresholdCountPlacement.of(noiseLevel, above, below));
        }
        
        public AnyPlacementBuilder<T> spread() {
            return this.add(InSquarePlacement.spread());
        }
        
        public AnyPlacementBuilder<T> height(VerticalAnchor bottom, VerticalAnchor top) {
            return this.add(HeightRangePlacement.uniform(bottom, top));
        }
        
        public AnyPlacementBuilder<T> heightTriangle(VerticalAnchor bottom, VerticalAnchor top) {
            return this.add(HeightRangePlacement.triangle(bottom, top));
        }
        
        public AnyPlacementBuilder<T> heightmap(Heightmap.Types type) {
            return this.add(HeightmapPlacement.onHeightmap(type));
        }
        
        public AnyPlacementBuilder<T> biomeFilter() {
            return this.add(BiomeFilter.biome());
        }

        public AnyPlacementBuilder<T> validGround(TagKey<Block> tag) {
            return this.add(BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
                    BlockPredicate.matchesTag(new Vec3i(0, -1, 0), tag),
                    BlockPredicate.matchesFluids(Fluids.EMPTY)
            )));
        }
        
        public AnyPlacementBuilder<T> validGround(Block block) {
            return this.validGround(block.defaultBlockState());
        }
        
        public AnyPlacementBuilder<T> validGround(BlockState state) {
            return this.add(BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
                    BlockPredicate.wouldSurvive(state, BlockPos.ZERO),
                    BlockPredicate.matchesFluids(Fluids.EMPTY)
            )));
        }
        
        public AnyPlacementBuilder<T> waterDepth(int maxDepth) {
            return this.add(SurfaceWaterDepthFilter.forMaxDepth(maxDepth));
        }
        
        public AnyPlacementBuilder<T> inAir() {
            return this.add(PlacementUtils.isEmpty());
        }
        
        public AnyPlacementBuilder<T> add(PlacementModifiers modifiers) {
            return this.addAll(modifiers.modifiers);
        }
        
        public AnyPlacementBuilder<T> add(PlacementModifier... modifiers) {
            this.modifiers.addAll(Arrays.asList(modifiers));
            return this;
        }
        
        public AnyPlacementBuilder<T> addAll(Collection<PlacementModifier> modifiers) {
            this.modifiers.addAll(modifiers);
            return this;
        }
        
        public abstract T build();
    }
    
    public class PlacementBuilder extends AnyPlacementBuilder<Holder<PlacedFeature>> {

        private final Holder<ConfiguredFeature<?, ?>> feature;

        private PlacementBuilder(Holder<ConfiguredFeature<?, ?>> feature) {
            this.feature = feature;
        }

        @Override
        public Holder<PlacedFeature> build() {
            return FeatureProviderBase.this.registries.writableRegistry(Registries.PLACED_FEATURE).createIntrusiveHolder(new PlacedFeature(this.feature, List.copyOf(this.modifiers)));
        }
    }
    
    public static class ModifierBuilder extends AnyPlacementBuilder<PlacementModifiers> {
        
        private ModifierBuilder() {
            
        }

        @Override
        public PlacementModifiers build() {
            return new PlacementModifiers(List.copyOf(this.modifiers));
        }
    }
    
    public static class PlacementModifiers {
        
        private final List<PlacementModifier> modifiers;

        public PlacementModifiers(List<PlacementModifier> modifiers) {
            this.modifiers = List.copyOf(modifiers);
        }
    }
}
