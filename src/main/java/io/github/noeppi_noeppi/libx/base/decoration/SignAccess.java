package io.github.noeppi_noeppi.libx.base.decoration;

import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;

import javax.annotation.Nonnull;

/**
 * Interface to access different registered parts of a sign.
 *
 * @deprecated See https://gist.github.com/noeppi-noeppi/9de9b6af950ee02f2dee611742fe2d6d
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.19")
public interface SignAccess extends ItemLike {

    /**
     * Gets the sign item.
     */
    @Nonnull
    @Override
    Item asItem();

    /**
     * Gets the standing sign block.
     */
    StandingSignBlock getStandingBlock();
    
    /**
     * Gets the wall sign block.
     */
    WallSignBlock getWallBlock();
}
