package org.moddingx.libx.impl.libxcore;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.MinecraftForge;
import org.moddingx.libx.event.RandomTickEvent;

import java.util.Random;

public class CoreRandomTick {

    /**
     * Patched into {@link BlockStateBase#randomTick(ServerLevel, BlockPos, Random)} at the start of the
     * method passing the {@code this} reference and all parameters. Return {@code true} to stop further
     * processing.
     */
    public static boolean processBlockTick(BlockStateBase stateBase, ServerLevel level, BlockPos pos, Random rand) {
        if (stateBase instanceof BlockState state) {
            return MinecraftForge.EVENT_BUS.post(new RandomTickEvent.Block(state, level, pos, rand));
        }
        return false;
    }
    
    /**
     * Patched into {@link FluidState#randomTick(Level, BlockPos, Random)} at the start of the method.
     * Return {@code true} to stop further processing.
     */
    public static boolean processFluidTick(FluidState state, Level level, BlockPos pos, Random rand) {
        return MinecraftForge.EVENT_BUS.post(new RandomTickEvent.Fluid(state, level, pos, rand));
    }
}
