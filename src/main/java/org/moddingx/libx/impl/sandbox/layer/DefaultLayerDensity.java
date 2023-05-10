package org.moddingx.libx.impl.sandbox.layer;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

public class DefaultLayerDensity implements DensityFunction.SimpleFunction {
    
    private final PerlinNoise noise;
    
    public DefaultLayerDensity(long seed) {
        this.noise = PerlinNoise.create(RandomSource.create(seed), IntStream.of(10, 0, 0, 0));
    }

    @Nonnull
    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double compute(FunctionContext context) {
        return this.noise.getValue(context.blockX() * 48, context.blockY() * 120, context.blockZ() * 48);
    }

    @Override
    public double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }
}
