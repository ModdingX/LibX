package org.moddingx.libx.impl.sandbox.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.densityfunction.*;

import javax.annotation.Nonnull;

public record DensityClamp(DensityFunction density, double min, double max) implements DensityFunction {

    public static final MapCodec<DensityClamp> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DensityFunction.CODEC.fieldOf("density").forGetter(DensityClamp::density),
            Codec.DOUBLE.fieldOf("min").forGetter(DensityClamp::min),
            Codec.DOUBLE.fieldOf("max").forGetter(DensityClamp::max)
    ).apply(instance, DensityClamp::new));

    @Nonnull
    @Override
    public MapCodec<DensityClamp> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    public DensitySampler compileSampler(@Nonnull CompileContext context) {
        return new Sampler(this.density().compileSampler(context), (float) this.min(), (float) this.max());
    }

    @Nonnull
    @Override
    public DensityFunction rewriteChildren(@Nonnull DfRewriteRule rule) {
        DensityFunction density = rule.rewrite(this.density());
        return density == this.density() ? this : new DensityClamp(density, this.min(), this.max());
    }

    @Nonnull
    @Override
    public Interval range() {
        Interval range = this.density().range();
        if (range.isNaI()) return range;
        float min = Math.max(range.min(), (float) this.min());
        float max = Math.min(range.max(), (float) this.max());
        return max < min ? Interval.ofExact(Mth.clamp(range.min(), (float) this.min(), (float) this.max())) : Interval.of(min, max);
    }

    @Override
    public int domainAxes() {
        return this.density().domainAxes();
    }

    private record Sampler(DensitySampler density, float min, float max) implements DensitySampler {

        @Override
        public void sampleVolume(@Nonnull SamplerContext context, @Nonnull DensityBuffer outputBuffer, @Nonnull DensityVolume volume) {
            this.density.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); i++) {
                outputBuffer.set(i, Mth.clamp(outputBuffer.get(i), this.min, this.max));
            }
        }

        @Override
        public float sampleValue(@Nonnull SamplerContext context, int blockX, int blockY, int blockZ) {
            return Mth.clamp(this.density.sampleValue(context, blockX, blockY, blockZ), this.min, this.max);
        }
    }
}
