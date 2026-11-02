package org.moddingx.libx.impl.sandbox.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

import javax.annotation.Nonnull;

public record DensityLerp(DensityFunction argument1, DensityFunction argument2, DensityFunction niveau, double mean, double deviation) implements DensityFunction {

    public static final KeyDispatchDataCodec<DensityLerp> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DensityFunction.CODEC.fieldOf("argument1").forGetter(DensityLerp::argument1),
                    DensityFunction.CODEC.fieldOf("argument2").forGetter(DensityLerp::argument2),
                    DensityFunction.CODEC.fieldOf("niveau").forGetter(DensityLerp::niveau),
                    Codec.DOUBLE.optionalFieldOf("mean", 0d).forGetter(DensityLerp::mean),
                    Codec.DOUBLE.optionalFieldOf("deviation", 1d).forGetter(DensityLerp::deviation)
            ).apply(instance, DensityLerp::new))
    );
    
    @Nonnull
    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    @Override
    public double compute(@Nonnull FunctionContext context) {
        double a = this.argument1().compute(context);
        double b = this.argument2().compute(context);
        double n = Mth.clamp(this.niveau().compute(context), this.mean() - this.deviation(), this.mean() + this.deviation());
        return Mth.lerp(((n - this.mean()) / (2 * this.deviation())) + 0.5, a, b);
    }

    @Override
    public void fillArray(@Nonnull double[] array, @Nonnull ContextProvider provider) {
        double[] a = new double[array.length];
        double[] b = new double[array.length];
        this.niveau().fillArray(array, provider);
        this.argument1().fillArray(a, provider);
        this.argument2().fillArray(b, provider);
        double n;
        for (int i = 0; i < array.length; i++) {
            n = Mth.clamp(array[i], this.mean() - this.deviation(), this.mean() + this.deviation());
            array[i] = Mth.lerp(((n - this.mean()) / (2 * this.deviation())) + 0.5, a[i], b[i]);
        }
    }

    @Nonnull
    @Override
    public DensityFunction mapChildren(@Nonnull Visitor visitor) {
        return new DensityLerp(visitor.apply(this.argument1()), visitor.apply(this.argument2()), visitor.apply(this.niveau()), this.mean(), this.deviation());
    }

    @Override
    public double minValue() {
        return Math.min(this.argument1().minValue(), this.argument2().minValue());
    }

    @Override
    public double maxValue() {
        return Math.max(this.argument1().maxValue(), this.argument2().maxValue());
    }
}
