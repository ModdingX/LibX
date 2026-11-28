package org.moddingx.libx.sandbox.placement;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import javax.annotation.Nonnull;

/**
 * A {@link PlacementFilter} for inverting other {@link PlacementFilter filters}.
 */
public class InvertPlacementFilter implements PlacementFilter {

    public static final MapCodec<InvertPlacementFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PlacementModifier.CODEC.fieldOf("filter").flatXmap(
                    modifier -> modifier instanceof PlacementFilter filter ? DataResult.success(filter) : DataResult.error(() -> "Can only invert placement filters, not modifiers. Invalid filter: " + modifier),
                    DataResult::success
            ).forGetter(filter -> filter.filter)
    ).apply(instance, InvertPlacementFilter::new));

    private final PlacementFilter filter;

    /**
     * Creates a new {@link InvertPlacementFilter}.
     */
    public InvertPlacementFilter(PlacementFilter filter) {
        this.filter = filter;
    }

    @Nonnull
    @Override
    public MapCodec<? extends PlacementFilter> codec() {
        return CODEC;
    }

    @Override
    public boolean shouldPlace(@Nonnull PlacementContext context, @Nonnull RandomSource random, @Nonnull BlockPos pos) {
        return !this.filter.shouldPlace(context, random, pos);
    }
}
