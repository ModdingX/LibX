package org.moddingx.libx.sandbox.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import javax.annotation.Nonnull;

/**
 * A {@link PlacementFilter} for filtering by the absolute placement position.
 */
public class HeightPlacementFilter extends PlacementFilter {

    public static final Codec<HeightPlacementFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter(filter -> filter.minInclusive),
            VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter(filter -> filter.maxInclusive)
    ).apply(instance, HeightPlacementFilter::new));
    
    public static final PlacementModifierType<HeightPlacementFilter> TYPE = () -> CODEC;

    private final VerticalAnchor minInclusive;
    private final VerticalAnchor maxInclusive;

    /**
     * Creates a new {@link HeightPlacementFilter}.
     * 
     * @param minInclusive The minimum height the {@link PlacedFeature} can generate on.
     * @param maxInclusive The maximum height the {@link PlacedFeature} can generate on.
     */
    public HeightPlacementFilter(VerticalAnchor minInclusive, VerticalAnchor maxInclusive) {
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }
    
    @Nonnull
    @Override
    public PlacementModifierType<?> type() {
        return TYPE;
    }

    @Override
    protected boolean shouldPlace(@Nonnull PlacementContext context, @Nonnull RandomSource random, @Nonnull BlockPos pos) {
        return this.minInclusive.resolveY(context) <= pos.getY() && this.maxInclusive.resolveY(context) >= pos.getY();
    }
}
