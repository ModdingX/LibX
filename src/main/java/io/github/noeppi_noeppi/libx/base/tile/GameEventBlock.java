package io.github.noeppi_noeppi.libx.base.tile;

import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;

import javax.annotation.Nullable;

/**
 * A {@link BlockEntity} used with {@link BlockBE} can implement this. Then a {@link GameEventListener} to
 * listen to {@link GameEvent game events} will be created.
 *
 * @deprecated See https://gist.github.com/noeppi-noeppi/9de9b6af950ee02f2dee611742fe2d6d
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.19")
public interface GameEventBlock {

    /**
     * Gets the range in which game events should be detected. Defaults to {@code 8}.
     */
    default int gameEventRange() {
        return 8;
    }

    /**
     * Notifies the block entity of game event.
     * 
     * @return {@code true} to indicate the event was handled, {@code false} otherwise.
     */
    boolean notifyGameEvent(GameEvent event, @Nullable Entity source);
}
