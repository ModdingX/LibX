package org.moddingx.libx.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired on right click with an empty hand but after the block action was processed. If this
 * is not canceled, {@link InteractionResult#PASS} is returned from the click. If this is canceled,
 * the value of {@link #cancellationResult} is returned.
 * This is only fired on the server.
 * IMPORTANT: Canceling this with {@link InteractionResult#CONSUME} for the main hand does not mean
 * it won't be posted for the offhand. For this cancel {@link PlayerInteractEvent.RightClickBlock}
 * on the client.
 * Returning {@link InteractionResult#SUCCESS} will swing the arm. However, it will first try to
 * propagate to the offhand. So in order to swing the main hand, the event for the main hand has to
 * return {@link InteractionResult#SUCCESS} and for the offhand {@link InteractionResult#PASS}.
 */
public class InteractBlockEmptyHandEvent extends Event {

    private final ServerPlayer player;
    private final Level level;
    private final InteractionHand hand;
    private final BlockHitResult hit;
    
    private InteractionResult cancellationResult;

    public InteractBlockEmptyHandEvent(ServerPlayer player, Level level, InteractionHand hand, BlockHitResult hit) {
        this.player = player;
        this.level = level;
        this.hand = hand;
        this.hit = hit;
        this.cancellationResult = InteractionResult.PASS;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public Level getLevel() {
        return this.level;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public BlockHitResult getHit() {
        return this.hit;
    }

    public InteractionResult getCancellationResult() {
        return this.cancellationResult;
    }

    public void setCancellationResult(InteractionResult cancellationResult) {
        this.cancellationResult = cancellationResult;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
