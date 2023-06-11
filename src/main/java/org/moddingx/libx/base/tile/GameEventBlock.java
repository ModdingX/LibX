package org.moddingx.libx.base.tile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.Vec3;

/**
 * A {@link BlockEntity} used with {@link BlockBE} can implement this. Then a {@link GameEventListener} to
 * listen to {@link GameEvent game events} will be created.
 */
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
    boolean notifyGameEvent(ServerLevel level, GameEvent message, GameEvent.Context context, Vec3 pos);
    
    /**
     * Gets the delivery mode for this game event listener.
     */
    default GameEventListener.DeliveryMode gameEventDelivery() {
        return GameEventListener.DeliveryMode.UNSPECIFIED;
    }
}
