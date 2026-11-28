package org.moddingx.libx.impl.sandbox.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.densityfunction.*;

import javax.annotation.Nonnull;

public record DensityLerp(DensityFunction argument1, DensityFunction argument2, DensityFunction niveau, double mean, double deviation) implements DensityFunction {

    public static final MapCodec<DensityLerp> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DensityFunction.CODEC.fieldOf("argument1").forGetter(DensityLerp::argument1),
            DensityFunction.CODEC.fieldOf("argument2").forGetter(DensityLerp::argument2),
            DensityFunction.CODEC.fieldOf("niveau").forGetter(DensityLerp::niveau),
            Codec.DOUBLE.optionalFieldOf("mean", 0d).forGetter(DensityLerp::mean),
            Codec.DOUBLE.optionalFieldOf("deviation", 1d).forGetter(DensityLerp::deviation)
    ).apply(instance, DensityLerp::new));

    @Nonnull
    @Override
    public MapCodec<DensityLerp> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    public DensitySampler compileSampler(@Nonnull CompileContext context) {
        return new Sampler(this.argument1().compileSampler(context), this.argument2().compileSampler(context), this.niveau().compileSampler(context), (float) this.mean(), (float) this.deviation());
    }

    @Nonnull
    @Override
    public DensityFunction rewriteChildren(@Nonnull DfRewriteRule rule) {
        DensityFunction argument1 = rule.rewrite(this.argument1());
        DensityFunction argument2 = rule.rewrite(this.argument2());
        DensityFunction niveau = rule.rewrite(this.niveau());
        if (argument1 == this.argument1() && argument2 == this.argument2() && niveau == this.niveau()) return this;
        return new DensityLerp(argument1, argument2, niveau, this.mean(), this.deviation());
    }

    @Nonnull
    @Override
    public Interval range() {
        return Interval.encapsulating(this.argument1().range(), this.argument2().range());
    }

    @Override
    public int domainAxes() {
        return this.argument1().domainAxes() | this.argument2().domainAxes() | this.niveau().domainAxes();
    }

    private record Sampler(DensitySampler argument1, DensitySampler argument2, DensitySampler niveau, float mean, float deviation) implements DensitySampler {

        private float apply(float a, float b, float niveau) {
            float n = Mth.clamp(niveau, this.mean - this.deviation, this.mean + this.deviation);
            return Mth.lerp(((n - this.mean) / (2 * this.deviation)) + 0.5f, a, b);
        }

        @Override
        public void sampleVolume(@Nonnull SamplerContext context, @Nonnull DensityBuffer outputBuffer, @Nonnull DensityVolume volume) {
            this.niveau.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer a = context.acquireBuffer(volume); ScopedDensityBuffer b = context.acquireBuffer(volume)) {
                this.argument1.sampleVolume(context, a, volume);
                this.argument2.sampleVolume(context, b, volume);
                for (int i = 0; i < outputBuffer.size(); i++) {
                    outputBuffer.set(i, this.apply(a.get(i), b.get(i), outputBuffer.get(i)));
                }
            }
        }

        @Override
        public float sampleValue(@Nonnull SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.apply(
                    this.argument1.sampleValue(context, blockX, blockY, blockZ),
                    this.argument2.sampleValue(context, blockX, blockY, blockZ),
                    this.niveau.sampleValue(context, blockX, blockY, blockZ)
            );
        }
    }
}
