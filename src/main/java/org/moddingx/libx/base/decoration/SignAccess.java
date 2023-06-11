package org.moddingx.libx.base.decoration;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;

import javax.annotation.Nonnull;

/**
 * Interface to access different registered parts of a standing sign.
 */
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
