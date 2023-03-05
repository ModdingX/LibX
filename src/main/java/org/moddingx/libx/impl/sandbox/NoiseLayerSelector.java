package org.moddingx.libx.impl.sandbox;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

import java.util.stream.IntStream;

public class NoiseLayerSelector {
    
    private final double horizontalFactor;
    private final double verticalFactor;
    private final double[] weights;
    private final PerlinNoise[] noises;

    public NoiseLayerSelector(double horizontalScale, double verticalScale, double[] weights, RandomSource random) {
        this.horizontalFactor = 1 / horizontalScale;
        this.verticalFactor = 1 / verticalScale;
        if (weights.length == 0) {
            throw new IllegalArgumentException("No elements");
        }
        this.weights = new double[weights.length];
        this.noises = new PerlinNoise[weights.length];
        for (int i = 0; i < weights.length; i++) {
            this.weights[i] = weights[i];
            this.noises[i] = PerlinNoise.create(RandomSource.create(random.nextLong() ^ 0x5A7DB0817611B870L), IntStream.of(10, 0, 0, 0));
        }
    }

    public int sample(double x, double y, double z, boolean[] allow) {
        double sx = x * this.horizontalFactor;
        double sy = y * this.verticalFactor;
        double sz = z * this.horizontalFactor;
        double max = Double.NEGATIVE_INFINITY;
        int maxIdx = 0;
        for (int i = 0; i < this.weights.length; i++) {
            if (allow[i]) {
                double v = this.noises[i].getValue(sx, sy, sz) * this.weights[i];
                if (v > max) {
                    max = v;
                    maxIdx = i;
                }
            }
        }
        return maxIdx;
    }
}
