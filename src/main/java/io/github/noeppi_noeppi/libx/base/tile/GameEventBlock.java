package io.github.noeppi_noeppi.libx.base.tile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;

// TODO find better name
public interface GameEventBlock {

    int gameEventRange();
    
    // true = event handled
    boolean notifyGameEvent(GameEvent event, @Nullable Entity source);
}
