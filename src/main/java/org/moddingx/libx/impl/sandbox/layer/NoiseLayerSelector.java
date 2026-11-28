package org.moddingx.libx.impl.sandbox.layer;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctionCompiler;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nonnull;
import java.util.List;

public class NoiseLayerSelector {

    private final DensitySampler[] samplers;

    public NoiseLayerSelector(List<DensityFunction> densities, RandomSource random) {
        if (densities.isEmpty()) {
            throw new IllegalArgumentException("No elements");
        }
        this.samplers = new DensitySampler[densities.size()];
        for (int i = 0; i < this.samplers.length; i++) {
            this.samplers[i] = this.createSampler(densities.get(i), random);
        }
    }

    public int sample(int x, int y, int z, boolean[] allow) {
        float max = Float.NEGATIVE_INFINITY;
        int maxIdx = 0;
        for (int i = 0; i < this.samplers.length; i++) {
            if (allow[i]) {
                float v = this.samplers[i].sampleValue(SamplerContext.EMPTY_UNCACHED, x, y, z);
                if (v > max) {
                    max = v;
                    maxIdx = i;
                }
            }
        }
        return maxIdx;
    }

    private DensitySampler createSampler(DensityFunction density, RandomSource random) {
        long seed = random.nextLong() ^ 0x1E6AC71A7E85E1ECL;
        DensityFunctionCompiler compiler = new DensityFunctionCompiler(new DensityFunction.CompileContext() {

            @Nonnull
            @Override
            public Noise createNoiseSampler(@Nonnull Holder<NormalNoise> parameters) {
                long xor = parameters.unwrapKey().map(key -> (((long) key.identifier().getNamespace().hashCode()) << 32) | key.identifier().getPath().hashCode()).orElse(42L);
                return parameters.value().create(RandomSource.create(seed ^ xor));
            }

            @Nonnull
            @Override
            public RandomSource createRandom(@Nonnull Identifier randomSeed) {
                long xor = (((long) randomSeed.getNamespace().hashCode()) << 32) | randomSeed.getPath().hashCode();
                return RandomSource.create(seed ^ xor);
            }

            @Nonnull
            @Override
            @Deprecated
            public RandomSource createEndIslandRandom() {
                return RandomSource.create(seed);
            }
        });
        return compiler.getSampler(density);
    }
}
