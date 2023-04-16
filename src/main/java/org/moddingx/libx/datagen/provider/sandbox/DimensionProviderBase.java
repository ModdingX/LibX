package org.moddingx.libx.datagen.provider.sandbox;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.sandbox.generator.BiomeLayer;
import org.moddingx.libx.sandbox.generator.ExtendedNoiseChunkGenerator;
import org.moddingx.libx.sandbox.generator.LayeredBiomeSource;
import org.moddingx.libx.sandbox.surface.SurfaceRuleSet;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class DimensionProviderBase extends SandBoxProviderBase {

    protected DimensionProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public String getName() {
        return this.mod.modid + " dimensions";
    }
    
    public BiomeSourceBuilder dimension(Holder<DimensionType> dimensionType) {
        return new BiomeSourceBuilder(dimensionType);
    }
    
    public Holder<LevelStem> dimension(Holder<DimensionType> dimensionType, ChunkGenerator generator) {
        LevelStem stem = new LevelStem(dimensionType, generator);
        return this.registries.writableRegistry(Registries.LEVEL_STEM).createIntrusiveHolder(stem);
    }
    
    public class BiomeSourceBuilder {
        
        private final Holder<DimensionType> dimensionType;
        
        private BiomeSourceBuilder(Holder<DimensionType> dimensionType) {
            this.dimensionType = dimensionType;
        }
        
        public ChunkGeneratorBuilder fixedBiome(ResourceKey<Biome> biome) {
            return this.fixedBiome(DimensionProviderBase.this.holder(biome));
        }
        
        public ChunkGeneratorBuilder fixedBiome(Holder<Biome> biome) {
            return new ChunkGeneratorBuilder(this.dimensionType, new FixedBiomeSource(biome));
        }
        
        public ChunkGeneratorBuilder multiNoiseBiome(Climate.ParameterList<Holder<Biome>> climate) {
            Climate.ParameterList<Holder<Biome>> cleanedClimate = new Climate.ParameterList<>(climate.values().stream()
                    .map(p -> Pair.of(p.getFirst(), p.getSecond()))
                    .toList()
            );
            return new ChunkGeneratorBuilder(this.dimensionType, MultiNoiseBiomeSource.createFromList(cleanedClimate));
        }
        
        public ChunkGeneratorBuilder layeredBiome(double horizontalScale, double verticalScale, TagKey<BiomeLayer> layers) {
            return this.layeredBiome(horizontalScale, verticalScale, DimensionProviderBase.this.set(layers));
        }
        
        public ChunkGeneratorBuilder layeredBiome(double horizontalScale, double verticalScale, HolderSet<BiomeLayer> layers) {
            return new ChunkGeneratorBuilder(this.dimensionType, new LayeredBiomeSource(horizontalScale, verticalScale, layers));
        }
    }
    
    public class ChunkGeneratorBuilder {
        
        private final Holder<DimensionType> dimensionType;
        private final BiomeSource biomes;

        private ChunkGeneratorBuilder(Holder<DimensionType> dimensionType, BiomeSource biomes) {
            this.dimensionType = dimensionType;
            this.biomes = biomes;
        }
        
        public FlatGeneratorBuilder flatGenerator() {
            return new FlatGeneratorBuilder(this.dimensionType, this.biomes);
        }
        
        public NoiseGeneratorBuilder noiseGenerator(Holder<NoiseGeneratorSettings> settings) {
            return new NoiseGeneratorBuilder(this.dimensionType, this.biomes, settings);
        }
    }
    
    public class FlatGeneratorBuilder {
        
        private final Holder<DimensionType> dimensionType;
        private final List<FlatLayerInfo> layers;
        private final Holder<Biome> biome;
        
        @Nullable
        private HolderSet<StructureSet> structures;
        private boolean lakes;
        private boolean decoration;

        private FlatGeneratorBuilder(Holder<DimensionType> dimensionType, BiomeSource biomes) {
            this.dimensionType = dimensionType;
            this.structures = null;
            if (biomes instanceof FixedBiomeSource source) {
                this.biome = source.biome;
            } else {
                throw new IllegalArgumentException("Flat generator can only be used with fixed biome source");
            }
            this.layers = new ArrayList<>();
            this.decoration = false;
            this.lakes = false;
        }

        @SafeVarargs
        public final FlatGeneratorBuilder structures(Holder<StructureSet>... structures) {
            return this.structures(DimensionProviderBase.this.set(structures));
        }

        public FlatGeneratorBuilder structures(HolderSet<StructureSet> structures) {
            this.structures = structures;
            return this;
        }

        public FlatGeneratorBuilder layer(Block block, int height) {
            if (height > 0) this.layers.add(new FlatLayerInfo(height, block));
            return this;
        }
        
        public FlatGeneratorBuilder withLakes() {
            this.lakes = true;
            return this;
        }
        
        public FlatGeneratorBuilder withDecoration() {
            this.decoration = true;
            return this;
        }

        public Holder<LevelStem> build() {
            if (this.layers.isEmpty()) this.layers.add(new FlatLayerInfo(1, Blocks.AIR));
            FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(Optional.ofNullable(this.structures), this.biome, List.of(
                    DimensionProviderBase.this.holder(MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND),
                    DimensionProviderBase.this.holder(MiscOverworldPlacements.LAKE_LAVA_SURFACE)
            ));
            settings = settings.withBiomeAndLayers(List.copyOf(this.layers), Optional.ofNullable(this.structures), this.biome);
            if (this.decoration) settings.setDecoration();
            if (this.lakes) settings.setAddLakes();
            FlatLevelSource generator = new FlatLevelSource(settings);
            return DimensionProviderBase.this.dimension(this.dimensionType, generator);
        }
    }
    
    public class NoiseGeneratorBuilder {
        
        private final Holder<DimensionType> dimensionType;
        private final BiomeSource biomes;
        private final Holder<NoiseGeneratorSettings> settings;
        
        @Nullable
        private Holder<SurfaceRuleSet> surfaceOverride;
        
        private NoiseGeneratorBuilder(Holder<DimensionType> dimensionType, BiomeSource biomes, Holder<NoiseGeneratorSettings> settings) {
            this.dimensionType = dimensionType;
            this.biomes = biomes;
            this.settings = settings;
            this.surfaceOverride = null;
        }
        
        public NoiseGeneratorBuilder surfaceOverride(Holder<SurfaceRuleSet> surface) {
            this.surfaceOverride = surface;
            return this;
        }
        
        public Holder<LevelStem> build() {
            NoiseBasedChunkGenerator generator;
            if (this.surfaceOverride != null) {
                generator = new ExtendedNoiseChunkGenerator(this.biomes, this.settings, Optional.of(this.surfaceOverride));
            } else {
                generator = new NoiseBasedChunkGenerator(this.biomes, this.settings);
            }
            return DimensionProviderBase.this.dimension(this.dimensionType, generator);
        }
    }
}
