package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class StructureSetProviderBase extends SandBoxProviderBase {

    private long nextSeed = 7;

    protected StructureSetProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " structure sets";
    }
    
    public StructureEntryBuilder structureSet() {
        return new StructureEntryBuilder();
    }
    
    public class StructureEntryBuilder {
        
        private final List<StructureSet.StructureSelectionEntry> entries;
        
        private StructureEntryBuilder() {
            this.entries = new ArrayList<>();
        }
        
        public StructureEntryBuilder entry(Holder<Structure> structure) {
            return this.entry(1, structure);
        }
        
        public StructureEntryBuilder entry(int weight, Holder<Structure> structure) {
            this.entries.add(new StructureSet.StructureSelectionEntry(structure, weight));
            return this;
        }
        
        public RandomPlacementBuilder placeRandom(int spacing, int separation) {
            return new RandomPlacementBuilder(List.copyOf(this.entries), spacing, separation);
        }
        
        public RingPlacementBuilder placeRings(int distance, int spread, int count) {
            return new RingPlacementBuilder(List.copyOf(this.entries), distance, spread, count);
        }
        
        public Holder<StructureSet> place(StructurePlacement placement) {
            return StructureSetProviderBase.this.registries.writableRegistry(Registries.STRUCTURE_SET).createIntrusiveHolder(new StructureSet(List.copyOf(this.entries), placement));
        }
    }
    
    public abstract class BasePlacementBuilder<T extends BasePlacementBuilder<T>> {
        
        protected final List<StructureSet.StructureSelectionEntry> entries;
        protected float frequency;
        protected StructurePlacement.FrequencyReductionMethod frequencyReduction;
        protected Vec3i locateOffset;
        protected int salt;
        
        private BasePlacementBuilder(List<StructureSet.StructureSelectionEntry> entries) {
            this.entries = List.copyOf(entries);
            this.frequency = -1;
            this.frequencyReduction = null;
            this.locateOffset = Vec3i.ZERO;
            int salt = new Random(StructureSetProviderBase.this.nextSeed * StructureSetProviderBase.this.mod.modid.hashCode()).nextInt();
            this.salt = salt == Integer.MIN_VALUE ? 0 : Math.abs(salt);
            StructureSetProviderBase.this.nextSeed += 142;
        }
        
        protected abstract T self();
        
        public T frequency(float frequency) {
            return this.frequency(frequency, StructurePlacement.FrequencyReductionMethod.DEFAULT);
        }
        
        public T frequency(float frequency, StructurePlacement.FrequencyReductionMethod frequencyReduction) {
            this.frequency = frequency;
            this.frequencyReduction = frequencyReduction;
            return this.self();
        }
        
        public T locateOffset(int x, int y, int z) {
            this.locateOffset = new Vec3i(x, y, z);
            return this.self();
        }
        
        public T locateOffset(Vec3i offset) {
            this.locateOffset = new Vec3i(offset.getX(), offset.getY(), offset.getZ());
            return this.self();
        }
        
        public T salt(int salt) {
            this.salt = salt;
            return this.self();
        }
        
        protected void ensureFrequency() {
            if (this.frequency < 0 || this.frequencyReduction == null) {
                throw new IllegalStateException("Structure placement has no frequency set.");
            }
        }
    }
    
    public class RandomPlacementBuilder extends BasePlacementBuilder<RandomPlacementBuilder> {
        
        private final int spacing;
        private final int separation;
        private RandomSpreadType spreadType;
        
        private RandomPlacementBuilder(List<StructureSet.StructureSelectionEntry> entries, int spacing, int separation) {
            super(entries);
            this.spacing = spacing;
            this.separation = separation;
            this.spreadType = RandomSpreadType.LINEAR;
        }

        @Override
        protected RandomPlacementBuilder self() {
            return this;
        }
        
        public RandomPlacementBuilder spreadType(RandomSpreadType type) {
            this.spreadType = type;
            return this;
        }
        
        public Holder<StructureSet> build() {
            this.ensureFrequency();
            return StructureSetProviderBase.this.registries.writableRegistry(Registries.STRUCTURE_SET).createIntrusiveHolder(new StructureSet(this.entries, new RandomSpreadStructurePlacement(this.locateOffset, this.frequencyReduction, this.frequency, this.salt, Optional.empty(), this.spacing, this.separation, this.spreadType)));
        }
    }
    
    public class RingPlacementBuilder extends BasePlacementBuilder<RingPlacementBuilder> {

        private final int distance;
        private final int spread;
        private final int count;
        private HolderSet<Biome> preferredBiomes;

        public RingPlacementBuilder(List<StructureSet.StructureSelectionEntry> entries, int distance, int spread, int count) {
            super(entries);
            this.distance = distance;
            this.spread = spread;
            this.count = count;
            this.preferredBiomes = null;
        }

        @Override
        protected RingPlacementBuilder self() {
            return this;
        }
        
        public RingPlacementBuilder preferredBiomes(TagKey<Biome> biomes) {
            return this.preferredBiomes(StructureSetProviderBase.this.set(biomes));
        }
        
        public RingPlacementBuilder preferredBiomes(HolderSet<Biome> biomes) {
            this.preferredBiomes = biomes;
            return this;
        }
        
        public Holder<StructureSet> build() {
            this.ensureFrequency();
            if (this.preferredBiomes == null) {
                throw new IllegalStateException("Concentric ring placement has no preferred biomes");
            }
            return StructureSetProviderBase.this.registries.writableRegistry(Registries.STRUCTURE_SET).createIntrusiveHolder(new StructureSet(this.entries, new ConcentricRingsStructurePlacement(this.locateOffset, this.frequencyReduction, this.frequency, this.salt, Optional.empty(), this.distance, this.spread, this.count, this.preferredBiomes)));
        }
    }
}
