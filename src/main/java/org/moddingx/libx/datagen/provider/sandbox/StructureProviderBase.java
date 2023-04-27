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
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * SandBox provider for {@link Structure structures}.
 *
 * This provider must run in the {@link DatagenStage#REGISTRY_SETUP registry setup} stage.
 */
public abstract class StructureProviderBase extends RegistryProviderBase {

    protected StructureProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " structures";
    }

    /**
     * Returns a new structure builder based on a function that creates a structure from
     * its {@link Structure.StructureSettings}.
     */
    public StructureSettingsBuilder forFactory(Function<Structure.StructureSettings, Structure> factory) {
        return new StructureSettingsBuilder(factory);
    }
    
    /**
     * Returns a new structure builder for jigsaw structures.
     */
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

        /**
         * Sets the name of the central block in the start pool.
         */
        public JigsawBuilder centerPositionInStartPool(ResourceLocation id)  {
            this.centerJigsawBlockNameInStartPool = id;
            return this;
        }
        
        /**
         * Sets the maximum nesting depth.
         */
        public JigsawBuilder nestingDepth(int maxNestingDepth) {
            if (maxNestingDepth <= 0) throw new IllegalArgumentException("Depth must be positive");
            this.maxNestingDepth = maxNestingDepth;
            return this;
        }
        
        /**
         * Sets the maximum distance from the center.
         */
        public JigsawBuilder centerDistance(int maxDistanceFromCenter) {
            this.maxDistanceFromCenter = maxDistanceFromCenter;
            return this;
        }
        
        /**
         * Sets the height for the structure to generate based on a {@link Heightmap}.
         */
        public JigsawBuilder height(Heightmap.Types heightmap) {
            return this.height(heightmap, 0);
        }

        /**
         * Sets the height for the structure to generate based on a {@link Heightmap} and an offset.
         */
        public JigsawBuilder height(Heightmap.Types heightmap, int relativeHeight) {
            return this.height(heightmap, ConstantHeight.of(VerticalAnchor.absolute(relativeHeight)));
        }

        /**
         * Sets the height for the structure to generate based on a {@link Heightmap} and an offset.
         */
        public JigsawBuilder height(Heightmap.Types heightmap, HeightProvider relativeHeight) {
            this.heightRelativeTo = heightmap;
            this.startHeight = relativeHeight;
            return this;
        }

        /**
         * Sets the absolute height for the structure to generate.
         */
        public JigsawBuilder height(HeightProvider absoluteHeight) {
            this.heightRelativeTo = null;
            this.startHeight = absoluteHeight;
            return this;
        }

        /**
         * Enables jigsaw expansion hack.
         */
        public JigsawBuilder expansionHack() {
            this.expansionHack = true;
            return this;
        }

        /**
         * Returns a builder for the structure settings.
         */
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

        /**
         * Sets the biomes, this structure can generate in.
         */
        public StructureSettingsBuilder biomes(TagKey<Biome> biomes) {
            return this.biomes(StructureProviderBase.this.set(biomes));
        }

        /**
         * Sets the biomes, this structure can generate in.
         */
        public StructureSettingsBuilder biomes(HolderSet<Biome> biomes) {
            this.biomes = biomes;
            return this;
        }
        
        /**
         * Sets structure spawn overrides.
         */
        public StructureSettingsBuilder spawn(MobCategory category, StructureSpawnOverride spawns) {
            this.spawnOverrides.put(category, spawns);
            return this;
        }

        /**
         * Sets the generation step, this structure should spawn in.
         */
        public StructureSettingsBuilder step(GenerationStep.Decoration step) {
            this.step = step;
            return this;
        }
        
        /**
         * Sets the terrain adjustment for this structure.
         */
        public StructureSettingsBuilder terrain(TerrainAdjustment terrain) {
            this.terrain = terrain;
            return this;
        }

        /**
         * Builds the {@link Structure}.
         *
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<Structure> build() {
            if (this.biomes == null) {
                throw new IllegalStateException("No biomes for structure");
            }
            Structure.StructureSettings settings = new Structure.StructureSettings(this.biomes, Map.copyOf(this.spawnOverrides), this.step, this.terrain);
            return StructureProviderBase.this.registries.writableRegistry(Registries.STRUCTURE).createIntrusiveHolder(this.factory.apply(settings));
        }
    }
}
