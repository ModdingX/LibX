package org.moddingx.libx.impl.sandbox.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.*;

import javax.annotation.Nonnull;

public record DensityInfluence(DensityFunction base, DensityFunction modifier, DensityFunction influence, double minInfluence, double maxInfluence) implements DensityFunction {

    public static final MapCodec<DensityInfluence> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DensityFunction.CODEC.fieldOf("base").forGetter(DensityInfluence::base),
            DensityFunction.CODEC.fieldOf("modifier").forGetter(DensityInfluence::modifier),
            DensityFunction.CODEC.fieldOf("influence").forGetter(DensityInfluence::influence),
            Codec.DOUBLE.optionalFieldOf("min_influence", -1d).forGetter(DensityInfluence::minInfluence),
            Codec.DOUBLE.optionalFieldOf("max_influence", -1d).forGetter(DensityInfluence::maxInfluence)
    ).apply(instance, DensityInfluence::new));

    @Nonnull
    @Override
    public MapCodec<DensityInfluence> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    public DensitySampler compileSampler(@Nonnull CompileContext context) {
        return new Sampler(this.base().compileSampler(context), this.modifier().compileSampler(context), this.influence().compileSampler(context), (float) this.minInfluence(), (float) this.maxInfluence());
    }

    @Nonnull
    @Override
    public DensityFunction rewriteChildren(@Nonnull DfRewriteRule rule) {
        DensityFunction base = rule.rewrite(this.base());
        DensityFunction modifier = rule.rewrite(this.modifier());
        DensityFunction influence = rule.rewrite(this.influence());
        if (base == this.base() && modifier == this.modifier() && influence == this.influence()) return this;
        return new DensityInfluence(base, modifier, influence, this.minInfluence(), this.maxInfluence());
    }

    @Nonnull
    @Override
    public Interval range() {
        // base + (minInfluence + (influence / (maxInfluence - minInfluence))) * modifier
        Interval factor = Interval.add(Interval.ofExact((float) this.minInfluence()), Interval.div(this.influence().range(), Interval.ofExact((float) (this.maxInfluence() - this.minInfluence()))));
        return Interval.add(this.base().range(), Interval.mul(factor, this.modifier().range()));
    }

    @Override
    public int domainAxes() {
        return this.base().domainAxes() | this.modifier().domainAxes() | this.influence().domainAxes();
    }

    private record Sampler(DensitySampler base, DensitySampler modifier, DensitySampler influence, float minInfluence, float maxInfluence) implements DensitySampler {

        private float apply(float base, float modifier, float influence) {
            return base + (this.minInfluence + (influence / (this.maxInfluence - this.minInfluence))) * modifier;
        }

        @Override
        public void sampleVolume(@Nonnull SamplerContext context, @Nonnull DensityBuffer outputBuffer, @Nonnull DensityVolume volume) {
            this.base.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer modifierBuffer = context.acquireBuffer(volume); ScopedDensityBuffer influenceBuffer = context.acquireBuffer(volume)) {
                this.modifier.sampleVolume(context, modifierBuffer, volume);
                this.influence.sampleVolume(context, influenceBuffer, volume);
                for (int i = 0; i < outputBuffer.size(); i++) {
                    outputBuffer.set(i, this.apply(outputBuffer.get(i), modifierBuffer.get(i), influenceBuffer.get(i)));
                }
            }
        }

        @Override
        public float sampleValue(@Nonnull SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.apply(
                    this.base.sampleValue(context, blockX, blockY, blockZ),
                    this.modifier.sampleValue(context, blockX, blockY, blockZ),
                    this.influence.sampleValue(context, blockX, blockY, blockZ)
            );
        }
    }
}
