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
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * SandBox provider for {@link BiomeModifier biome modifiers}.
 *
 * This provider must run in the {@link DatagenStage#EXTENSION_SETUP extension setup} stage.
 */
public abstract class BiomeModifierProviderBase extends RegistryProviderBase {

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

    /**
     * Returns a builder for a {@link BiomeModifier} that adds features to a biome.
     */
    public FeaturesBuilder addFeatures(TagKey<Biome> biomes, GenerationStep.Decoration step) {
        return this.addFeatures(this.set(biomes), step);
    }
    
    /**
     * Returns a builder for a {@link BiomeModifier} that adds features to a biome.
     */
    public FeaturesBuilder addFeatures(HolderSet<Biome> biomes, GenerationStep.Decoration step) {
        return new FeaturesBuilder(biomes, Set.of(step), false);
    }
    
    /**
     * Returns a builder for a {@link BiomeModifier} that removes features from a biome.
     */
    public FeaturesBuilder removeFeatures(TagKey<Biome> biomes, GenerationStep.Decoration... steps) {
        return this.removeFeatures(this.set(biomes), steps);
    }

    /**
     * Returns a builder for a {@link BiomeModifier} that removes features from a biome.
     */
    public FeaturesBuilder removeFeatures(HolderSet<Biome> biomes, GenerationStep.Decoration... steps) {
        return new FeaturesBuilder(biomes, Set.of(steps), false);
    }

    /**
     * Returns a builder for a {@link BiomeModifier} that adds spawns to a biome.
     */
    public AddMobSpawnsBuilder addSpawns(TagKey<Biome> biomes) {
        return this.addSpawns(this.set(biomes));
    }

    /**
     * Returns a builder for a {@link BiomeModifier} that adds spawns to a biome.
     */
    public AddMobSpawnsBuilder addSpawns(HolderSet<Biome> biomes) {
        return new AddMobSpawnsBuilder(biomes);
    }

    /**
     * Returns a builder for a {@link BiomeModifier} that removes spawns from a biome.
     */
    public RemoveMobSpawnsBuilder removeSpawns(TagKey<Biome> biomes) {
        return this.removeSpawns(this.set(biomes));
    }

    /**
     * Returns a builder for a {@link BiomeModifier} that removes spawns from a biome.
     */
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

        /**
         * Builds the {@link BiomeModifier}.
         *
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
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

        /**
         * Builds the {@link BiomeModifier}.
         *
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
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

        /**
         * Builds the {@link BiomeModifier}.
         *
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<BiomeModifier> build() {
            BiomeModifier modifier = new ForgeBiomeModifiers.RemoveSpawnsBiomeModifier(this.biomes, HolderSet.direct(List.copyOf(this.entities)));
            return BiomeModifierProviderBase.this.registries.writableRegistry(ForgeRegistries.Keys.BIOME_MODIFIERS).createIntrusiveHolder(modifier);
        }
    }
}
