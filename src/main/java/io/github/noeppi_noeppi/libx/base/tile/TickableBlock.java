package io.github.noeppi_noeppi.libx.base.tile;

import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

/**
 * A {@link BlockEntity} used with {@link BlockBE} can implement this. Then a {@link BlockEntityTicker} to
 * tick the block entity will be created.
 *
 * @deprecated See https://gist.github.com/noeppi-noeppi/9de9b6af950ee02f2dee611742fe2d6d
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.19")
public interface TickableBlock {

    /**
     * Ticks the {@link BlockEntity}.
     */
    void tick();
}
