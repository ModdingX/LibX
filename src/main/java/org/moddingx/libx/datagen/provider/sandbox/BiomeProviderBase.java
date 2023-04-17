package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;

/**
 * SandBox provider for {@link Biome biomes}.
 *
 * This provider must run in the {@link DatagenStage#REGISTRY_SETUP registry setup} stage.
 */
public abstract class BiomeProviderBase extends SandBoxProviderBase {

    protected BiomeProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " biomes";
    }

    /**
     * Creates a new builder for a biome.
     */
    public BiomeBuilder biome(float temperature, float downfall) {
        return new BiomeBuilder(temperature, downfall);
    }

    /**
     * Creates a new builder for {@link BiomeSpecialEffects}. This method must be used instead of directly
     * instantiating a {@link BiomeSpecialEffects.Builder}.
     */
    public BiomeSpecialEffects.Builder effects() {
        return new BiomeEffectsBuilder();
    }

    /**
     * Creates a new builder for {@link MobSpawnSettings}. This method must be used instead of directly
     * instantiating a {@link MobSpawnSettings.Builder}.
     */
    public MobSpawnSettings.Builder spawns() {
        return new BiomeSpawnsBuilder();
    }
    
    /**
     * Creates a new builder for {@link BiomeGenerationSettings}. This method must be used instead of directly
     * instantiating a {@link BiomeGenerationSettings.Builder}.
     */
    public BiomeGenerationSettings.Builder generation() {
        return new BiomeGenerationBuilder();
    }
    
    public class BiomeBuilder {

        private final float temperature;
        private final Biome.BiomeBuilder builder;

        private BiomeBuilder(float temperature, float downfall) {
            this.temperature = temperature;
            this.builder = new Biome.BiomeBuilder();
            this.builder.temperature(temperature);
            this.builder.downfall(downfall);
            this.builder.temperatureAdjustment(Biome.TemperatureModifier.NONE);
            this.effects(BiomeProviderBase.this.effects());
        }

        /**
         * Marks this biome as frozen.
         */
        public BiomeBuilder frozen() {
            this.builder.temperatureAdjustment(Biome.TemperatureModifier.FROZEN);
            return this;
        }

        /**
         * Sets the special effects for this biome. {@link BiomeProviderBase#effects()} must be used to create
         * the {@link BiomeSpecialEffects.Builder}.
         */
        @SuppressWarnings("UnusedReturnValue")
        public BiomeBuilder effects(BiomeSpecialEffects.Builder builder) {
            if (!(builder instanceof BiomeEffectsBuilder effectBuilder)) {
                throw new IllegalArgumentException("Use BiomeData#effects to create a BiomeSpecialEffects.Builder instance.");
            }
            effectBuilder.setDefaultSkyColor(this.temperature);
            this.builder.specialEffects(builder.build());
            return this;
        }

        /**
         * Sets the mob spawns for this biome. {@link BiomeProviderBase#spawns()} must be used to create
         * the {@link MobSpawnSettings.Builder}.
         */
        public BiomeBuilder mobSpawns(MobSpawnSettings.Builder builder) {
            if (!(builder instanceof BiomeSpawnsBuilder)) {
                throw new IllegalArgumentException("Use BiomeData#spawns to create a MobSpawnSettings.Builder instance.");
            }
            this.builder.mobSpawnSettings(builder.build());
            return this;
        }

        /**
         * Sets the feature generation for this biome. {@link BiomeProviderBase#generation()} must be used to create
         * the {@link BiomeGenerationSettings.Builder}.
         */
        public BiomeBuilder generation(BiomeGenerationSettings.Builder builder) {
            if (!(builder instanceof BiomeGenerationBuilder)) {
                throw new IllegalArgumentException("Use BiomeData#generation to create a BiomeGenerationSettings.Builder instance.");
            }
            this.builder.generationSettings(builder.build());
            return this;
        }

        /**
         * Builds the {@link Biome}.
         *
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added the registry. {@link SandBoxProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<Biome> build() {
            return BiomeProviderBase.this.registries.writableRegistry(Registries.BIOME).createIntrusiveHolder(this.builder.build());
        }
    }
    
    private static class BiomeEffectsBuilder extends BiomeSpecialEffects.Builder {
        
        private BiomeEffectsBuilder() {
            this.fogColor(0xc0d8ff);
            this.waterColor(0x3f76e4);
            this.waterFogColor(0x050533);
        }
        
        private void setDefaultSkyColor(float temperature) {
            if (this.skyColor.isEmpty()) {
                this.skyColor(OverworldBiomes.calculateSkyColor(temperature));
            }
        }
    }

    private static class BiomeSpawnsBuilder extends MobSpawnSettings.Builder {
        
        private BiomeSpawnsBuilder() {
            
        }
    }

    private class BiomeGenerationBuilder extends BiomeGenerationSettings.Builder {
        
        private BiomeGenerationBuilder() {
            super(BiomeProviderBase.this.registries.registry(Registries.PLACED_FEATURE).asLookup(), BiomeProviderBase.this.registries.registry(Registries.CONFIGURED_CARVER).asLookup());
        }
    }
}
