package io.github.noeppi_noeppi.libx.impl.libxcore;

import io.github.noeppi_noeppi.libx.event.ClickBlockEmptyHandEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.world.InteractionResult;
import net.minecraft.util.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

public class CoreInteract {

    /**
     * Patched into {@link PlayerInteractionManager#processItemUsage(ServerPlayerEntity, World, ItemStack, Hand, BlockRayTraceResult)}
     * before the last return and getstatic. Passing all the arguments from the source method. Returning null
     * will trigger default behaviour. Returning anything else will replace the return value.
     */
    @Nullable
    public static InteractionResult processItemUsage(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) {
            ClickBlockEmptyHandEvent event = new ClickBlockEmptyHandEvent(player, level, hand, hit);
            if (MinecraftForge.EVENT_BUS.post(event)) {
                return event.getCancellationResult() == null ? InteractionResult.PASS : event.getCancellationResult();
            }
        }
        return null;
    }
}
