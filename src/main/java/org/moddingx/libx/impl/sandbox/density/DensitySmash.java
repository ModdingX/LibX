package org.moddingx.libx.impl.sandbox.density;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.*;

import javax.annotation.Nonnull;

// Evaluates the wrapped density function with one axis always set to 0.
public record DensitySmash(DensityFunction wrapped, Direction.Axis axis) implements DensityFunction {

    public static final MapCodec<DensitySmash> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DensityFunction.CODEC.fieldOf("density").forGetter(DensitySmash::wrapped),
            Direction.Axis.CODEC.fieldOf("axis").forGetter(DensitySmash::axis)
    ).apply(instance, DensitySmash::new));

    @Nonnull
    @Override
    public MapCodec<DensitySmash> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    public DensitySampler compileSampler(@Nonnull CompileContext context) {
        return new Sampler(this.wrapped().compileSampler(context), this.axis());
    }

    @Nonnull
    @Override
    public DensityFunction rewriteChildren(@Nonnull DfRewriteRule rule) {
        DensityFunction wrapped = rule.rewrite(this.wrapped());
        return wrapped == this.wrapped() ? this : new DensitySmash(wrapped, this.axis());
    }

    @Nonnull
    @Override
    public Interval range() {
        return this.wrapped().range();
    }

    @Override
    public int domainAxes() {
        // The smashed axis is constant for the wrapped function
        return this.wrapped().domainAxes() & ~DensityFunction.axesFrom(this.axis());
    }

    private record Sampler(DensitySampler wrapped, Direction.Axis axis) implements DensitySampler {

        @Override
        public void sampleVolume(@Nonnull SamplerContext context, @Nonnull DensityBuffer outputBuffer, @Nonnull DensityVolume volume) {
            DensitySampler.sampleVolumeNaive(context, outputBuffer, volume, this);
        }

        @Override
        public float sampleValue(@Nonnull SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.wrapped.sampleValue(context,
                    this.axis == Direction.Axis.X ? 0 : blockX,
                    this.axis == Direction.Axis.Y ? 0 : blockY,
                    this.axis == Direction.Axis.Z ? 0 : blockZ
            );
        }
    }
}
