package io.github.noeppi_noeppi.libx.impl.libxcore;

import io.github.noeppi_noeppi.libx.event.ClickBlockEmptyHandEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

public class CoreInteract {

    /**
     * Patched into {@link PlayerInteractionManager#processItemUsage(ServerPlayerEntity, World, ItemStack, Hand, BlockRayTraceResult)}
     * before the last return and getstatic. Passing all the arguments from the source method. Returning null
     * will trigger default behaviour. Returning anything else will replace the return value.
     */
    @Nullable
    public static ActionResultType processItemUsage(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockRayTraceResult hit) {
        if (stack.isEmpty()) {
            ClickBlockEmptyHandEvent event = new ClickBlockEmptyHandEvent(player, world, hand, hit);
            if (MinecraftForge.EVENT_BUS.post(event)) {
                return event.getCancellationResult() == null ? ActionResultType.PASS : event.getCancellationResult();
            }
        }
        return null;
    }
}
