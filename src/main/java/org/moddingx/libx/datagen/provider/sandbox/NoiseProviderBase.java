package org.moddingx.libx.datagen.provider.sandbox;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.sandbox.SandBox;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class NoiseProviderBase extends SandBoxProviderBase {

    protected NoiseProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " noise";
    }
    
    public GeneratorSettingsBuilder generator() {
        return new GeneratorSettingsBuilder();
    }
    
    public Holder<NormalNoise.NoiseParameters> noise(int firstOctave, double... amplitudes) {
        return this.registries.writableRegistry(Registries.NOISE).createIntrusiveHolder(new NormalNoise.NoiseParameters(firstOctave, DoubleList.of(amplitudes)));
    }
    
    public Holder<DensityFunction> density(DensityFunction function) {
        return this.registries.writableRegistry(Registries.DENSITY_FUNCTION).createIntrusiveHolder(function);
    }
    
    public class GeneratorSettingsBuilder {
        
        private NoiseSettings noise;
        private BlockState defaultBlock;
        private BlockState defaultFluid;
        private final RouterBuilder router;
        private SurfaceRules.RuleSource surface;
        private final List<Climate.ParameterPoint> spawnTargets;
        private final List<Climate.ParameterPoint> defaultSpawnTargets;
        private int seaLevel;
        private boolean disableMobGeneration;
        private boolean aquifersEnabled;
        private boolean oreVeinsEnabled;
        private boolean useLegacyRandomSource;
        
        private GeneratorSettingsBuilder() {
            NoiseGeneratorSettings defaultSettings = NoiseGeneratorSettings.overworld(new Bootstrap<>(), false, false);
            this.noise = defaultSettings.noiseSettings();
            this.defaultBlock = Blocks.STONE.defaultBlockState();
            this.defaultFluid = Blocks.WATER.defaultBlockState();
            this.router = new RouterBuilder(defaultSettings.noiseRouter());
            this.surface = SandBox.emptySurface();
            this.spawnTargets = new ArrayList<>();
            this.defaultSpawnTargets = List.copyOf(defaultSettings.spawnTarget());
            this.seaLevel = 64;
            this.disableMobGeneration = false;
            this.aquifersEnabled = true;
            this.oreVeinsEnabled = true;
            this.useLegacyRandomSource = false;
        }
        
        public GeneratorSettingsBuilder noise(int minY, int height, int noiseSizeHorizontal, int noiseSizeVertical) {
            return this.noise(new NoiseSettings(minY, height, noiseSizeHorizontal, noiseSizeVertical));
        }
        
        public GeneratorSettingsBuilder noise(NoiseSettings noise) {
            this.noise = noise;
            return this;
        }

        public GeneratorSettingsBuilder defaultBlock(Block block) {
            return this.defaultBlock(block.defaultBlockState());
        }
        
        public GeneratorSettingsBuilder defaultBlock(BlockState state) {
            this.defaultBlock = state;
            return this;
        }

        public GeneratorSettingsBuilder defaultFluid(Block block) {
            return this.defaultFluid(block.defaultBlockState());
        }
        
        public GeneratorSettingsBuilder defaultFluid(BlockState state) {
            this.defaultFluid = state;
            return this;
        }
        
        public GeneratorSettingsBuilder router(NoiseRouter router) {
            this.router.fromRouter(router);
            return this;
        }
        
        public RouterBuilder router() {
            return this.router;
        }
        
        public GeneratorSettingsBuilder surface(SurfaceRules.RuleSource surface) {
            this.surface = surface;
            return this;
        }
        
        public GeneratorSettingsBuilder addSpawnTarget(Climate.ParameterPoint target) {
            this.spawnTargets.add(target);
            return this;
        }
        
        public GeneratorSettingsBuilder seaLevel(int seaLevel) {
            this.seaLevel = seaLevel;
            return this;
        }
        
        public GeneratorSettingsBuilder disableMobGeneration() {
            this.disableMobGeneration = true;
            return this;
        }
        
        public GeneratorSettingsBuilder disableAquifers() {
            this.aquifersEnabled = false;
            return this;
        }
        
        public GeneratorSettingsBuilder disableOreVeins() {
            this.oreVeinsEnabled = false;
            return this;
        }
        
        public GeneratorSettingsBuilder useLegacyRandomSource() {
            this.useLegacyRandomSource = true;
            return this;
        }
        
        public Holder<NoiseGeneratorSettings> build() {
            NoiseGeneratorSettings settings = new NoiseGeneratorSettings(
                    this.noise, this.defaultBlock, this.defaultFluid, this.router.build(), this.surface,
                    this.spawnTargets.isEmpty() ? this.defaultSpawnTargets : this.spawnTargets,
                    this.seaLevel, this.disableMobGeneration, this.aquifersEnabled, this.oreVeinsEnabled,
                    this.useLegacyRandomSource
            );
            return NoiseProviderBase.this.registries.writableRegistry(Registries.NOISE_SETTINGS).createIntrusiveHolder(settings);
        }

        public class RouterBuilder {

            private DensityFunction barrierNoise;
            private DensityFunction fluidLevelFloodednessNoise;
            private DensityFunction fluidLevelSpreadNoise;
            private DensityFunction lavaNoise;
            private DensityFunction temperature;
            private DensityFunction vegetation;
            private DensityFunction continents;
            private DensityFunction erosion;
            private DensityFunction depth;
            private DensityFunction ridges;
            private DensityFunction initialDensityWithoutJaggedness;
            private DensityFunction finalDensity;
            private DensityFunction veinToggle;
            private DensityFunction veinRidged;
            private DensityFunction veinGap;
            
            private RouterBuilder(NoiseRouter initial) {
                this.fromRouter(initial);
            }

            public GeneratorSettingsBuilder barrierNoise(Holder<DensityFunction> function) {
                return this.barrierNoise(new DensityFunctions.HolderHolder(function));
            }
            
            public GeneratorSettingsBuilder barrierNoise(DensityFunction function) {
                this.barrierNoise = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder fluidLevelFloodednessNoise(Holder<DensityFunction> function) {
                return this.fluidLevelFloodednessNoise(new DensityFunctions.HolderHolder(function));
            }
            
            public GeneratorSettingsBuilder fluidLevelFloodednessNoise(DensityFunction function) {
                this.fluidLevelFloodednessNoise = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder fluidLevelSpreadNoise(Holder<DensityFunction> function) {
                return this.fluidLevelSpreadNoise(new DensityFunctions.HolderHolder(function));
            }
            
            public GeneratorSettingsBuilder fluidLevelSpreadNoise(DensityFunction function) {
                this.fluidLevelSpreadNoise = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder lavaNoise(Holder<DensityFunction> function) {
                return this.lavaNoise(new DensityFunctions.HolderHolder(function));
            }
            
            public GeneratorSettingsBuilder lavaNoise(DensityFunction function) {
                this.lavaNoise = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder temperature(Holder<DensityFunction> function) {
                return this.temperature(new DensityFunctions.HolderHolder(function));
            }
            
            public GeneratorSettingsBuilder temperature(DensityFunction function) {
                this.temperature = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder vegetation(Holder<DensityFunction> function) {
                return this.vegetation(new DensityFunctions.HolderHolder(function));
            }
            
            public GeneratorSettingsBuilder vegetation(DensityFunction function) {
                this.vegetation = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder continents(Holder<DensityFunction> function) {
                return this.continents(new DensityFunctions.HolderHolder(function));
            }
            
            public GeneratorSettingsBuilder continents(DensityFunction function) {
                this.continents = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder erosion(Holder<DensityFunction> function) {
                return this.erosion(new DensityFunctions.HolderHolder(function));
            }
            
            public GeneratorSettingsBuilder erosion(DensityFunction function) {
                this.erosion = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder depth(Holder<DensityFunction> function) {
                return this.depth(new DensityFunctions.HolderHolder(function));
            }
            
            public GeneratorSettingsBuilder depth(DensityFunction function) {
                this.depth = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder ridges(Holder<DensityFunction> function) {
                return this.ridges(new DensityFunctions.HolderHolder(function));
            }
            
            public GeneratorSettingsBuilder ridges(DensityFunction function) {
                this.ridges = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder initialDensityWithoutJaggedness(Holder<DensityFunction> function) {
                return this.initialDensityWithoutJaggedness(new DensityFunctions.HolderHolder(function));
            }
            
            public GeneratorSettingsBuilder initialDensityWithoutJaggedness(DensityFunction function) {
                this.initialDensityWithoutJaggedness = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder finalDensity(Holder<DensityFunction> function) {
                return this.finalDensity(new DensityFunctions.HolderHolder(function));
            }
            
            public GeneratorSettingsBuilder finalDensity(DensityFunction function) {
                this.finalDensity = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder veinToggle(Holder<DensityFunction> function) {
                return this.veinToggle(new DensityFunctions.HolderHolder(function));
            }
            
            public GeneratorSettingsBuilder veinToggle(DensityFunction function) {
                this.veinToggle = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder veinRidged(Holder<DensityFunction> function) {
                return this.veinRidged(new DensityFunctions.HolderHolder(function));
            }
            
            public GeneratorSettingsBuilder veinRidged(DensityFunction function) {
                this.veinRidged = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder veinGap(Holder<DensityFunction> function) {
                return this.veinGap(new DensityFunctions.HolderHolder(function));
            }
            
            public GeneratorSettingsBuilder veinGap(DensityFunction function) {
                this.veinGap = function;
                return GeneratorSettingsBuilder.this;
            }

            private void fromRouter(NoiseRouter router) {
                this.barrierNoise = router.barrierNoise();
                this.fluidLevelFloodednessNoise = router.fluidLevelFloodednessNoise();
                this.fluidLevelSpreadNoise = router.fluidLevelSpreadNoise();
                this.lavaNoise = router.lavaNoise();
                this.temperature = router.temperature();
                this.vegetation = router.vegetation();
                this.continents = router.continents();
                this.erosion = router.erosion();
                this.depth = router.depth();
                this.ridges = router.ridges();
                this.initialDensityWithoutJaggedness = router.initialDensityWithoutJaggedness();
                this.finalDensity = router.finalDensity();
                this.veinToggle = router.veinToggle();
                this.veinRidged = router.veinRidged();
                this.veinGap = router.veinGap();
            }

            private NoiseRouter build() {
                return new NoiseRouter(
                        this.barrierNoise, this.fluidLevelFloodednessNoise, this.fluidLevelSpreadNoise, this.lavaNoise,
                        this.temperature, this.vegetation, this.continents, this.erosion, this.depth, this.ridges,
                        this.initialDensityWithoutJaggedness, this.finalDensity, this.veinToggle, this.veinRidged,
                        this.veinGap
                );
            }
        }
    }
    
    private class Bootstrap<T> implements BootstapContext<T> {
        
        @Nonnull
        @Override
        @SuppressWarnings({ "unchecked", "rawtypes", "RedundantCast" })
        public Holder.Reference<T> register(@Nonnull ResourceKey<T> key, @Nonnull T value, @Nonnull Lifecycle lifecycle) {
            return NoiseProviderBase.this.registries.writableRegistry((ResourceKey) ResourceKey.createRegistryKey(key.registry())).register(key, value, lifecycle);
        }

        @Nonnull
        @Override
        @SuppressWarnings({ "unchecked" })
        public <S> HolderGetter<S> lookup(@Nonnull ResourceKey<? extends Registry<? extends S>> registryKey) {
            return NoiseProviderBase.this.registries.registry((ResourceKey<? extends Registry<S>>) registryKey).asLookup();
        }
    }
}
