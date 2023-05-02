package org.moddingx.libx.sandbox.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import javax.annotation.Nonnull;

/**
 * A {@link PlacementFilter} for inverting other {@link PlacementFilter filters}.
 */
public class InvertPlacementFilter extends PlacementFilter {

    public static final Codec<InvertPlacementFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlacementModifier.CODEC.fieldOf("filter").flatXmap(
                    modifier -> modifier instanceof PlacementFilter filter ? DataResult.success(filter) : DataResult.error(() -> "Can only invert placement filters, not modifiers. Invalid filter: " + modifier),
                    DataResult::success
            ).forGetter(filter -> filter.filter)
    ).apply(instance, InvertPlacementFilter::new));
    
    public static final PlacementModifierType<InvertPlacementFilter> TYPE = () -> CODEC;

    private final PlacementFilter filter;

    /**
     * Creates a new {@link InvertPlacementFilter}.
     */
    public InvertPlacementFilter(PlacementFilter filter) {
        this.filter = filter;
    }
    
    @Nonnull
    @Override
    public PlacementModifierType<?> type() {
        return TYPE;
    }

    @Override
    protected boolean shouldPlace(@Nonnull PlacementContext context, @Nonnull RandomSource random, @Nonnull BlockPos pos) {
        return this.filter.getPositions(context, random, pos).findAny().isEmpty();
    }
}
