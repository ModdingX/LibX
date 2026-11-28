package org.moddingx.libx.impl.sandbox.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.*;

import javax.annotation.Nonnull;

public record DensityDebug(Direction.Axis axis, double scale) implements DensityFunction {

    public static final MapCodec<DensityDebug> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Direction.Axis.CODEC.fieldOf("axis").forGetter(DensityDebug::axis),
            Codec.DOUBLE.fieldOf("scale").forGetter(DensityDebug::scale)
    ).apply(instance, DensityDebug::new));

    @Nonnull
    @Override
    public MapCodec<DensityDebug> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    public DensitySampler compileSampler(@Nonnull CompileContext context) {
        return new Sampler(this.axis(), (float) this.scale());
    }

    @Nonnull
    @Override
    public DensityFunction rewriteChildren(@Nonnull DfRewriteRule rule) {
        return this;
    }

    @Nonnull
    @Override
    public Interval range() {
        return Interval.INFINITE;
    }

    @Override
    public int domainAxes() {
        return DensityFunction.axesFrom(this.axis());
    }

    private record Sampler(Direction.Axis axis, float scale) implements DensitySampler {

        @Override
        public void sampleVolume(@Nonnull SamplerContext context, @Nonnull DensityBuffer outputBuffer, @Nonnull DensityVolume volume) {
            DensitySampler.sampleVolumeNaive(context, outputBuffer, volume, this);
        }

        @Override
        public float sampleValue(@Nonnull SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.scale * switch (this.axis) {
                case X -> blockX;
                case Y -> blockY;
                case Z -> blockZ;
            };
        }
    }
}
