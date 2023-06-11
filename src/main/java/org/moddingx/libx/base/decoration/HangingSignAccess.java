package org.moddingx.libx.base.decoration;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;

import javax.annotation.Nonnull;

/**
 * Interface to access different registered parts of a hanging sign.
 */
public interface HangingSignAccess extends ItemLike {

    /**
     * Gets the sign item.
     */
    @Nonnull
    @Override
    Item asItem();

    /**
     * Gets the ceiling sign block.
     */
    CeilingHangingSignBlock getCeilingBlock();
    
    /**
     * Gets the wall sign block.
     */
    WallHangingSignBlock getWallBlock();
}
