package io.github.noeppi_noeppi.libx.base.tile;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

/**
 * A {@link BlockEntity} used with {@link BlockBE} can implement this. Then a {@link BlockEntityTicker} to
 * tick the block entity will be created.
 */
public interface TickableBlock {

    /**
     * Ticks the {@link BlockEntity}.
     */
    void tick();
}
