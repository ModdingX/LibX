package org.moddingx.libx.impl.sandbox.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

import javax.annotation.Nonnull;

public record DensityDebug(Direction.Axis axis, double scale) implements DensityFunction.SimpleFunction {

    public static final KeyDispatchDataCodec<DensityDebug> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Direction.Axis.CODEC.fieldOf("axis").forGetter(DensityDebug::axis),
                    Codec.DOUBLE.fieldOf("scale").forGetter(DensityDebug::scale)
            ).apply(instance, DensityDebug::new))
    );

    @Nonnull
    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    @Override
    public double compute(@Nonnull FunctionContext context) {
        return this.scale * switch (this.axis) {
            case X -> context.blockX();
            case Y -> context.blockY();
            case Z -> context.blockZ();
        };
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
