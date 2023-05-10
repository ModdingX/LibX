package org.moddingx.libx.impl.sandbox.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

import javax.annotation.Nonnull;

public record DensityInfluence(DensityFunction base, DensityFunction modifier, DensityFunction influence, double minInfluence, double maxInfluence) implements DensityFunction {

    public static final KeyDispatchDataCodec<DensityInfluence> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("base").forGetter(DensityInfluence::base),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("modifier").forGetter(DensityInfluence::modifier),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("influence").forGetter(DensityInfluence::influence),
                    Codec.DOUBLE.optionalFieldOf("min_influence", -1d).forGetter(DensityInfluence::minInfluence),
                    Codec.DOUBLE.optionalFieldOf("max_influence", -1d).forGetter(DensityInfluence::maxInfluence)
            ).apply(instance, DensityInfluence::new))
    );

    @Nonnull
    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    @Override
    public double compute(@Nonnull FunctionContext context) {
        double influence = this.minInfluence() + (this.influence().compute(context) / (this.maxInfluence() - this.minInfluence()));
        return this.base().compute(context) + influence * this.modifier().compute(context);
    }

    @Override
    public void fillArray(@Nonnull double[] array, @Nonnull ContextProvider provider) {
        double[] influence = new double[array.length];
        double[] modifier = new double[array.length];
        this.base().fillArray(array, provider);
        this.influence().fillArray(influence, provider);
        this.modifier().fillArray(modifier, provider);
        for (int i = 0; i < array.length; i++) {
            array[i] += (this.minInfluence() + (influence[i] / (this.maxInfluence() - this.minInfluence()))) * modifier[i];
        }
    }

    @Nonnull
    @Override
    public DensityFunction mapAll(@Nonnull Visitor visitor) {
        return visitor.apply(new DensityInfluence(this.base().mapAll(visitor), this.modifier().mapAll(visitor), this.influence().mapAll(visitor), this.minInfluence(), this.maxInfluence()));
    }

    @Override
    public double minValue() {
        return this.base().minValue() + (this.minInfluence() + (this.influence().minValue() / (this.maxInfluence() - this.minInfluence()))) * this.modifier().minValue();
    }

    @Override
    public double maxValue() {
        return this.base().maxValue() + (this.minInfluence() + (this.influence().maxValue() / (this.maxInfluence() - this.minInfluence()))) * this.modifier().maxValue();
    }
}
