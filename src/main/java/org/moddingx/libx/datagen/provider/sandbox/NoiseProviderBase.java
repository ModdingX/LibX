package org.moddingx.libx.datagen.provider.sandbox;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;
import org.moddingx.libx.sandbox.SandBox;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SandBox provider for noise related data.
 * <p>
 * This provider must run in the {@link DatagenStage#REGISTRY_SETUP registry setup} stage.
 */
public abstract class NoiseProviderBase extends RegistryProviderBase {

    protected NoiseProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " noise";
    }

    /**
     * Returns a new builder for noise generator settings.
     */
    public GeneratorSettingsBuilder generator() {
        return new GeneratorSettingsBuilder();
    }

    /**
     * Creates new {@link NormalNoise noise parameters}.
     * <p>
     * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
     * added to the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
     * {@code public}, non-{@code static} field inside the provider.
     */
    public Holder<NormalNoise> noise(int firstOctave, double... amplitudes) {
        return this.noise(NormalNoise.createParity(firstOctave, DoubleList.of(amplitudes)));
    }

    /**
     * Wraps new {@link NormalNoise noise} for registration.
     * <p>
     * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
     * added to the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
     * {@code public}, non-{@code static} field inside the provider.
     */
    public Holder<NormalNoise> noise(NormalNoise noise) {
        return this.registries.writableRegistry(Registries.NOISE).createIntrusiveHolder(noise);
    }

    /**
     * Wraps a new {@link DensityFunction} for registration.
     * <p>
     * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
     * added to the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
     * {@code public}, non-{@code static} field inside the provider.
     */
    public Holder<DensityFunction> density(DensityFunction function) {
        return this.registries.writableRegistry(Registries.DENSITY_FUNCTION).createIntrusiveHolder(function);
    }

    /**
     * Wrap the given density function holder into a density function that references the registry entry
     * represented by the holder.
     */
    public DensityFunction wrap(Holder<DensityFunction> holder) {
        return new DensityFunctions.HolderHolder(holder);
    }

    public class GeneratorSettingsBuilder {

        private NoiseSettings noise;
        private BlockState defaultBlock;
        private BlockState defaultFluid;
        private final RouterBuilder router;
        private MaterialRule materialRule;
        private final List<SpawnTargetPoint> spawnTargets;
        private final List<SpawnTargetPoint> defaultSpawnTargets;
        private int seaLevel;
        private boolean disableMobGeneration;
        @Nullable private Aquifer.Config aquifers;
        private boolean useLegacyRandomSource;

        private GeneratorSettingsBuilder() {
            NoiseGeneratorSettings defaultSettings = NoiseProviderBase.this.registries.registry(Registries.NOISE_SETTINGS).getValueOrThrow(NoiseGeneratorSettings.OVERWORLD);
            this.noise = defaultSettings.noiseSettings();
            this.defaultBlock = Blocks.STONE.defaultBlockState();
            this.defaultFluid = Blocks.WATER.defaultBlockState();
            this.router = new RouterBuilder(defaultSettings.noiseRouter());
            this.materialRule = SandBox.emptyMaterial();
            this.spawnTargets = new ArrayList<>();
            this.defaultSpawnTargets = List.copyOf(defaultSettings.spawnTarget());
            this.seaLevel = 64;
            this.disableMobGeneration = false;
            this.aquifers = defaultSettings.aquifers().orElse(null);
            this.useLegacyRandomSource = false;
        }

        public GeneratorSettingsBuilder noise(int minY, int height) {
            return this.noise(new NoiseSettings(minY, height));
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

        public GeneratorSettingsBuilder materialRule(MaterialRule materialRule) {
            this.materialRule = materialRule;
            return this;
        }

        public GeneratorSettingsBuilder addSpawnTarget(SpawnTargetPoint target) {
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

        public GeneratorSettingsBuilder aquifers(Aquifer.Config aquifers) {
            this.aquifers = aquifers;
            return this;
        }

        public GeneratorSettingsBuilder disableAquifers() {
            this.aquifers = null;
            return this;
        }

        public GeneratorSettingsBuilder useLegacyRandomSource() {
            this.useLegacyRandomSource = true;
            return this;
        }

        /**
         * Builds the {@link NoiseGeneratorSettings}.
         * <p>
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added to the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<NoiseGeneratorSettings> build() {
            Holder<MaterialRule> materialRule = this.materialRule instanceof MaterialRule.HolderHolder(Holder<MaterialRule> holder) ? holder : Holder.direct(this.materialRule);
            NoiseGeneratorSettings settings = new NoiseGeneratorSettings(
                    this.noise, this.defaultBlock, this.defaultFluid, this.router.build(), materialRule,
                    this.spawnTargets.isEmpty() ? this.defaultSpawnTargets : List.copyOf(this.spawnTargets),
                    this.seaLevel, this.disableMobGeneration, Optional.ofNullable(this.aquifers),
                    this.useLegacyRandomSource, NoiseGeneratorSettings.DebugFunctions.EMPTY
            );
            return NoiseProviderBase.this.registries.writableRegistry(Registries.NOISE_SETTINGS).createIntrusiveHolder(settings);
        }

        public class RouterBuilder {

            private DensityFunction temperature;
            private DensityFunction vegetation;
            private DensityFunction continents;
            private DensityFunction erosion;
            private DensityFunction depth;
            private DensityFunction ridges;
            private DensityFunction chunkSurfaceLevel;
            private DensityFunction finalDensity;

            private RouterBuilder(NoiseRouter initial) {
                this.fromRouter(initial);
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

            public GeneratorSettingsBuilder chunkSurfaceLevel(Holder<DensityFunction> function) {
                return this.chunkSurfaceLevel(new DensityFunctions.HolderHolder(function));
            }

            public GeneratorSettingsBuilder chunkSurfaceLevel(DensityFunction function) {
                this.chunkSurfaceLevel = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder finalDensity(Holder<DensityFunction> function) {
                return this.finalDensity(new DensityFunctions.HolderHolder(function));
            }

            public GeneratorSettingsBuilder finalDensity(DensityFunction function) {
                this.finalDensity = function;
                return GeneratorSettingsBuilder.this;
            }

            private void fromRouter(NoiseRouter router) {
                this.temperature = router.temperature();
                this.vegetation = router.vegetation();
                this.continents = router.continents();
                this.erosion = router.erosion();
                this.depth = router.depth();
                this.ridges = router.ridges();
                this.chunkSurfaceLevel = router.chunkSurfaceLevel();
                this.finalDensity = router.finalDensity();
            }

            private NoiseRouter build() {
                return new NoiseRouter(
                        this.temperature, this.vegetation, this.continents, this.erosion, this.depth, this.ridges,
                        this.chunkSurfaceLevel, this.finalDensity
                );
            }
        }
    }
}
