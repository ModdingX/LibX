package org.moddingx.libx.impl.sandbox.layer;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NoiseLayerSelector {
    
    private final DensityFunction[] densities;

    public NoiseLayerSelector(List<Optional<DensityFunction>> densities, RandomSource random) {
        if (densities.isEmpty()) {
            throw new IllegalArgumentException("No elements");
        }
        this.densities = new DensityFunction[densities.size()];
        for (int i = 0; i < this.densities.length; i++) {
            DensityFunction density = densities.get(i).orElse(null);
            this.densities[i] = this.createDensity(density, random);
        }
    }

    public int sample(int x, int y, int z, boolean[] allow) {
        DensityFunction.FunctionContext context = new DensityFunction.SinglePointContext(x, y, z);
        double max = Double.NEGATIVE_INFINITY;
        int maxIdx = 0;
        for (int i = 0; i < this.densities.length; i++) {
            if (allow[i]) {
                double v = this.densities[i].compute(context);
                if (v > max) {
                    max = v;
                    maxIdx = i;
                }
            }
        }
        return maxIdx;
    }
    
    private DensityFunction createDensity(@Nullable DensityFunction density, RandomSource random) {
        long seed = random.nextLong() ^ 0x1E6AC71A7E85E1ECL;
        if (density == null) return new DefaultLayerDensity(seed);
        
        Map<DensityFunction, DensityFunction> wrappedDensities = new HashMap<>();
        return density.mapAll(new DensityFunction.Visitor() {
            
            @Nonnull
            @Override
            public DensityFunction apply(@Nonnull DensityFunction density) {
                return wrappedDensities.computeIfAbsent(density, k -> {
                    if (density instanceof DensityFunctions.HolderHolder holder) {
                        return holder.function().value();
                    } else if (density instanceof DensityFunctions.MarkerOrMarked marker) {
                        return marker.wrapped();
                    } else {
                        return density;
                    }
                });
            }

            @Nonnull
            @Override
            public DensityFunction.NoiseHolder visitNoise(@Nonnull DensityFunction.NoiseHolder noise) {
                long xor = noise.noiseData().unwrapKey().map(key -> (((long) key.location().getNamespace().hashCode()) << 32) | key.location().getPath().hashCode()).orElse(42L);
                return new DensityFunction.NoiseHolder(noise.noiseData(), NormalNoise.create(RandomSource.create(seed ^ xor), noise.noiseData().get()));
            }
        });
    }
}
