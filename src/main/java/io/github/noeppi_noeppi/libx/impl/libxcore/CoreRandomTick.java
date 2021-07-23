package io.github.noeppi_noeppi.libx.impl.libxcore;

import io.github.noeppi_noeppi.libx.event.RandomTickEvent;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;

import java.util.Random;

public class CoreRandomTick {

    /**
     * Patched into {@link AbstractBlockState#randomTick(ServerWorld, BlockPos, Random)} at the start of the method.
     * Return {@code true} to stop further processing.
     */
    public static boolean processBlockTick(BlockStateBase state, ServerLevel level, BlockPos pos, Random rand) {
        if (state instanceof BlockState) {
            return MinecraftForge.EVENT_BUS.post(new RandomTickEvent.Block((BlockState) state, level, pos, rand));
        }
        return false;
    }
    
    /**
     * Patched into {@link FluidState#randomTick(World, BlockPos, Random)} at the start of the method.
     * Return {@code true} to stop further processing.
     */
    public static boolean processFluidTick(FluidState state, Level level, BlockPos pos, Random rand) {
        return MinecraftForge.EVENT_BUS.post(new RandomTickEvent.Fluid(state, level, pos, rand));
    }
}
