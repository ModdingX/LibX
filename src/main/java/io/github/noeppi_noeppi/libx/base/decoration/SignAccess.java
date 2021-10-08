package io.github.noeppi_noeppi.libx.base.decoration;

import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;

public interface SignAccess {
    
    StandingSignBlock getStandingBlock();
    WallSignBlock getWallBlock();
}
