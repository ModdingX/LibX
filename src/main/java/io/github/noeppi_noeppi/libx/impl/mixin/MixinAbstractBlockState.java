package io.github.noeppi_noeppi.libx.impl.mixin;

import io.github.noeppi_noeppi.libx.event.RandomTickEvent;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class MixinAbstractBlockState {

    @Inject(
            method = "Lnet/minecraft/block/AbstractBlock$AbstractBlockState;randomTick(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void randomTick(ServerWorld world, BlockPos pos, Random rand, CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((AbstractBlock.AbstractBlockState) (Object) this instanceof BlockState) {
            if (MinecraftForge.EVENT_BUS.post(new RandomTickEvent.Block((BlockState) (Object) this, world, pos, rand))) {
                ci.cancel();
            }
        }
    }
}
