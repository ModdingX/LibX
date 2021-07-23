package io.github.noeppi_noeppi.libx.impl.libxcore;

import io.github.noeppi_noeppi.libx.event.RandomTickEvent;
import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;

import java.util.Random;

public class CoreRandomTick {

    /**
     * Patched into {@link AbstractBlockState#randomTick(ServerWorld, BlockPos, Random)} at the start of the method.
     * Return {@code true} to stop further processing.
     */
    public static boolean processBlockTick(AbstractBlockState state, ServerWorld world, BlockPos pos, Random rand) {
        if (state instanceof BlockState) {
            return MinecraftForge.EVENT_BUS.post(new RandomTickEvent.Block((BlockState) state, world, pos, rand));
        }
        return false;
    }
    
    /**
     * Patched into {@link FluidState#randomTick(World, BlockPos, Random)} at the start of the method.
     * Return {@code true} to stop further processing.
     */
    public static boolean processFluidTick(FluidState state, World world, BlockPos pos, Random rand) {
        return MinecraftForge.EVENT_BUS.post(new RandomTickEvent.Fluid(state, world, pos, rand));
    }
}
