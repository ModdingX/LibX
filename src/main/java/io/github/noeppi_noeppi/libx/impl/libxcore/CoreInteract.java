package io.github.noeppi_noeppi.libx.impl.libxcore;

import io.github.noeppi_noeppi.libx.event.ClickBlockEmptyHandEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

public class CoreInteract {

    /**
     * Patched into {@link ServerPlayerGameMode#useItemOn(ServerPlayer, Level, ItemStack, InteractionHand, BlockHitResult)}
     * before the last return and getstatic. Passing all the arguments from the source method. Returning null
     * will trigger default behaviour. Returning anything else will replace the return value.
     */
    @Nullable
    public static InteractionResult useItemOn(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) {
            ClickBlockEmptyHandEvent event = new ClickBlockEmptyHandEvent(player, level, hand, hit);
            if (MinecraftForge.EVENT_BUS.post(event)) {
                return event.getCancellationResult() == null ? InteractionResult.PASS : event.getCancellationResult();
            }
        }
        return null;
    }
}
