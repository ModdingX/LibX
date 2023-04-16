package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class BiomeModifierProviderBase extends SandBoxProviderBase {

    protected BiomeModifierProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.EXTENSION_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " biome modifiers";
    }
    
    public Holder<BiomeModifier> modifier(BiomeModifier modifier) {
        return this.registries.writableRegistry(ForgeRegistries.Keys.BIOME_MODIFIERS).createIntrusiveHolder(modifier);
    }
    
    public FeaturesBuilder addFeatures(TagKey<Biome> biomes, GenerationStep.Decoration step) {
        return this.addFeatures(this.set(biomes), step);
    }
    
    public FeaturesBuilder addFeatures(HolderSet<Biome> biomes, GenerationStep.Decoration step) {
        return new FeaturesBuilder(biomes, Set.of(step), false);
    }
    
    public FeaturesBuilder removeFeatures(TagKey<Biome> biomes, GenerationStep.Decoration... steps) {
        return this.removeFeatures(this.set(biomes), steps);
    }
    
    public FeaturesBuilder removeFeatures(HolderSet<Biome> biomes, GenerationStep.Decoration... steps) {
        return new FeaturesBuilder(biomes, Set.of(steps), false);
    }
    
    public AddMobSpawnsBuilder addSpawns(TagKey<Biome> biomes) {
        return this.addSpawns(this.set(biomes));
    }
    
    public AddMobSpawnsBuilder addSpawns(HolderSet<Biome> biomes) {
        return new AddMobSpawnsBuilder(biomes);
    }
    
    public RemoveMobSpawnsBuilder removeSpawns(TagKey<Biome> biomes) {
        return this.removeSpawns(this.set(biomes));
    }
    
    public RemoveMobSpawnsBuilder removeSpawns(HolderSet<Biome> biomes) {
        return new RemoveMobSpawnsBuilder(biomes);
    }
    
    public class FeaturesBuilder {
        
        private final HolderSet<Biome> biomes;
        private final Set<GenerationStep.Decoration> steps;
        private final boolean remove;
        private final List<Holder<PlacedFeature>> features;

        private FeaturesBuilder(HolderSet<Biome> biomes, Set<GenerationStep.Decoration> steps, boolean remove) {
            this.biomes = biomes;
            this.steps = steps;
            this.remove = remove;
            this.features = new ArrayList<>();
        }
        
        public FeaturesBuilder feature(Holder<PlacedFeature> feature) {
            this.features.add(feature);
            return this;
        }
        
        public Holder<BiomeModifier> build() {
            BiomeModifier modifier;
            if (this.remove) {
                modifier = new ForgeBiomeModifiers.RemoveFeaturesBiomeModifier(this.biomes, HolderSet.direct(List.copyOf(this.features)), Set.copyOf(this.steps));
            } else {
                modifier = new ForgeBiomeModifiers.AddFeaturesBiomeModifier(this.biomes, HolderSet.direct(List.copyOf(this.features)), this.steps.iterator().next());
            }
            return BiomeModifierProviderBase.this.registries.writableRegistry(ForgeRegistries.Keys.BIOME_MODIFIERS).createIntrusiveHolder(modifier);
        }
    }

    public class AddMobSpawnsBuilder {

        private final HolderSet<Biome> biomes;
        private final List<MobSpawnSettings.SpawnerData> spawns;

        private AddMobSpawnsBuilder(HolderSet<Biome> biomes) {
            this.biomes = biomes;
            this.spawns = new ArrayList<>();
        }

        public AddMobSpawnsBuilder spawn(EntityType<?> type, int weight, int min, int max) {
            return this.spawn(new MobSpawnSettings.SpawnerData(type, weight, min, max));
        }
        
        public AddMobSpawnsBuilder spawn(MobSpawnSettings.SpawnerData spawn) {
            this.spawns.add(spawn);
            return this;
        }

        public Holder<BiomeModifier> build() {
            BiomeModifier modifier = new ForgeBiomeModifiers.AddSpawnsBiomeModifier(this.biomes, List.copyOf(this.spawns));
            return BiomeModifierProviderBase.this.registries.writableRegistry(ForgeRegistries.Keys.BIOME_MODIFIERS).createIntrusiveHolder(modifier);
        }
    }

    public class RemoveMobSpawnsBuilder {

        private final HolderSet<Biome> biomes;
        private final List<Holder<EntityType<?>>> entities;

        private RemoveMobSpawnsBuilder(HolderSet<Biome> biomes) {
            this.biomes = biomes;
            this.entities = new ArrayList<>();
        }

        public RemoveMobSpawnsBuilder entity(EntityType<?> type) {
            this.entities.add(BiomeModifierProviderBase.this.holder(ForgeRegistries.ENTITY_TYPES.getResourceKey(type).orElseThrow()));
            return this;
        }

        public Holder<BiomeModifier> build() {
            BiomeModifier modifier = new ForgeBiomeModifiers.RemoveSpawnsBiomeModifier(this.biomes, HolderSet.direct(List.copyOf(this.entities)));
            return BiomeModifierProviderBase.this.registries.writableRegistry(ForgeRegistries.Keys.BIOME_MODIFIERS).createIntrusiveHolder(modifier);
        }
    }
}
