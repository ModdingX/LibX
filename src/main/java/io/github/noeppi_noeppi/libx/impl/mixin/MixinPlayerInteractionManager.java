package io.github.noeppi_noeppi.libx.impl.mixin;

import io.github.noeppi_noeppi.libx.event.ClickBlockEmptyHandEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInteractionManager.class)
public class MixinPlayerInteractionManager {

    @Inject(
            method = "Lnet/minecraft/server/management/PlayerInteractionManager;processItemUsage(Lnet/minecraft/entity/player/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;Lnet/minecraft/util/math/BlockRayTraceResult;)Lnet/minecraft/util/ActionResultType;",
            at = @At(
                    value = "TAIL"
            ),
            cancellable = true
    )
    public void processItemUsage(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockRayTraceResult hit, CallbackInfoReturnable<ActionResultType> cir) {
        if (stack.isEmpty()) {
            ClickBlockEmptyHandEvent event = new ClickBlockEmptyHandEvent(player, world, hand, hit);
            if (MinecraftForge.EVENT_BUS.post(event)) {
                cir.setReturnValue(event.getCancellationResult());
                cir.cancel();
            }
        }
    }
}
