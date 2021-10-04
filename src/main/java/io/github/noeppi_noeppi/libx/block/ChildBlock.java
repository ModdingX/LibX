package io.github.noeppi_noeppi.libx.block;

import net.minecraft.world.level.block.Block;

/**
 * An interface which allows to set a parent block.
 */
public interface ChildBlock {

    Block getParent();
}
