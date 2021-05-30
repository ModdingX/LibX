package io.github.noeppi_noeppi.libx.event;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired on right click with an empty hand but after the block action was processed. If this
 * is not canceled, {@code ActionResult.PASS} is returned from the click. If this is canceled,
 * the value of {@code cancellationResult} is returned.
 * This is only fired on the server.
 * IMPORTANT: Canceling this with {@code ActionResult.CONSUME} for the main hand does not mean
 * it won't be posted for the offhand. For this cancel {@code PlayerInteractEvent.RightClickBlock}
 * on the client.
 */
public class ClickBlockEmptyHandEvent extends Event {

    private final ServerPlayerEntity player;
    private final World world;
    private final Hand hand;
    private final BlockRayTraceResult hit;
    
    private ActionResultType cancellationResult;

    public ClickBlockEmptyHandEvent(ServerPlayerEntity player, World world, Hand hand, BlockRayTraceResult hit) {
        this.player = player;
        this.world = world;
        this.hand = hand;
        this.hit = hit;
        this.cancellationResult = ActionResultType.PASS;
    }

    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    public World getWorld() {
        return this.world;
    }

    public Hand getHand() {
        return this.hand;
    }

    public BlockRayTraceResult getHit() {
        return this.hit;
    }

    public ActionResultType getCancellationResult() {
        return this.cancellationResult;
    }

    public void setCancellationResult(ActionResultType cancellationResult) {
        this.cancellationResult = cancellationResult;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
