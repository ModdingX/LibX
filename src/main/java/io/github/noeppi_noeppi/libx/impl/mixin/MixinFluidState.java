package io.github.noeppi_noeppi.libx.impl.mixin;

import io.github.noeppi_noeppi.libx.event.RandomTickEvent;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(FluidState.class)
public class MixinFluidState {

    @Inject(
            method = "Lnet/minecraft/fluid/FluidState;randomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void randomTick(World world, BlockPos pos, Random rand, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new RandomTickEvent.Fluid((FluidState) (Object) this, world, pos, rand))) {
            ci.cancel();
        }
    }
}
