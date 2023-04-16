package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class StructureProviderBase extends SandBoxProviderBase {

    protected StructureProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " structures";
    }
    
    public StructureSettingsBuilder forFactory(Function<Structure.StructureSettings, Structure> factory) {
        return new StructureSettingsBuilder(factory);
    }
    
    public JigsawBuilder jigsaw(Holder<StructureTemplatePool> startPool) {
        return new JigsawBuilder(startPool);
    }
    
    public class JigsawBuilder {
        
        private final Holder<StructureTemplatePool> startPool;
        private ResourceLocation centerJigsawBlockNameInStartPool;
        private int maxNestingDepth;
        private int maxDistanceFromCenter;
        private HeightProvider startHeight;
        private Heightmap.Types heightRelativeTo;
        private boolean expansionHack;

        private JigsawBuilder(Holder<StructureTemplatePool> startPool) {
            this.startPool = startPool;
            this.centerJigsawBlockNameInStartPool = null;
            this.maxNestingDepth = 1;
            this.maxDistanceFromCenter = 80;
            this.startHeight = ConstantHeight.of(VerticalAnchor.absolute(0));
            this.heightRelativeTo = Heightmap.Types.WORLD_SURFACE_WG;
            this.expansionHack = false;
        }
        
        public JigsawBuilder centerPositionInStartPool(ResourceLocation id)  {
            this.centerJigsawBlockNameInStartPool = id;
            return this;
        }
        
        public JigsawBuilder nestingDepth(int maxNestingDepth) {
            if (maxNestingDepth <= 0) throw new IllegalArgumentException("Depth must be positive");
            this.maxNestingDepth = maxNestingDepth;
            return this;
        }
        
        public JigsawBuilder centerDistance(int maxDistanceFromCenter) {
            this.maxDistanceFromCenter = maxDistanceFromCenter;
            return this;
        }

        public JigsawBuilder height(Heightmap.Types heightmap) {
            return this.height(heightmap, 0);
        }
        
        public JigsawBuilder height(Heightmap.Types heightmap, int relativeHeight) {
            return this.height(heightmap, ConstantHeight.of(VerticalAnchor.absolute(relativeHeight)));
        }
        
        public JigsawBuilder height(Heightmap.Types heightmap, HeightProvider relativeHeight) {
            this.heightRelativeTo = heightmap;
            this.startHeight = relativeHeight;
            return this;
        }
        
        public JigsawBuilder height(HeightProvider absoluteHeight) {
            this.heightRelativeTo = null;
            this.startHeight = absoluteHeight;
            return this;
        }
        
        public JigsawBuilder expansionHack() {
            this.expansionHack = true;
            return this;
        }
        
        public StructureSettingsBuilder structure() {
            return StructureProviderBase.this.forFactory(settings -> new JigsawStructure(settings, this.startPool, Optional.ofNullable(this.centerJigsawBlockNameInStartPool), this.maxNestingDepth, this.startHeight, this.expansionHack, Optional.ofNullable(this.heightRelativeTo), this.maxDistanceFromCenter));
        }
    }
    
    public class StructureSettingsBuilder {
        
        private final Function<Structure.StructureSettings, Structure> factory;
        private HolderSet<Biome> biomes;
        private final Map<MobCategory, StructureSpawnOverride> spawnOverrides;
        private GenerationStep.Decoration step;
        private TerrainAdjustment terrain;

        private StructureSettingsBuilder(Function<Structure.StructureSettings, Structure> factory) {
            this.factory = factory;
            this.biomes = null;
            this.spawnOverrides = new HashMap<>();
            this.step = GenerationStep.Decoration.SURFACE_STRUCTURES;
            this.terrain = TerrainAdjustment.NONE;
        }
        
        public StructureSettingsBuilder biomes(TagKey<Biome> biomes) {
            return this.biomes(StructureProviderBase.this.set(biomes));
        }

        public StructureSettingsBuilder biomes(HolderSet<Biome> biomes) {
            this.biomes = biomes;
            return this;
        }
        
        public StructureSettingsBuilder spawn(MobCategory category, StructureSpawnOverride spawns) {
            this.spawnOverrides.put(category, spawns);
            return this;
        }
        
        public StructureSettingsBuilder step(GenerationStep.Decoration step) {
            this.step = step;
            return this;
        }
        
        public StructureSettingsBuilder terrain(TerrainAdjustment terrain) {
            this.terrain = terrain;
            return this;
        }
        
        public Holder<Structure> build() {
            if (this.biomes == null) {
                throw new IllegalStateException("No biomes for structure");
            }
            Structure.StructureSettings settings = new Structure.StructureSettings(this.biomes, Map.copyOf(this.spawnOverrides), this.step, this.terrain);
            return StructureProviderBase.this.registries.writableRegistry(Registries.STRUCTURE).createIntrusiveHolder(this.factory.apply(settings));
        }
    }
}
