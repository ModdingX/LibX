package io.github.noeppi_noeppi.libx.event;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.Event;

import java.util.Random;

/**
 * Called when a block or a fluid receives a random tick. Canceling the event will stop the
 * random tick. See subclasses.
 */
public abstract class RandomTickEvent extends Event {

    private final World world;
    private final BlockPos pos;
    private final Random rand;

    private RandomTickEvent(World world, BlockPos pos, Random rand) {
        this.world = world;
        this.pos = pos;
        this.rand = rand;
    }

    /**
     * Gets the world where the block / fluid is about to be ticked randomly.
     */
    public World getWorld() {
        return this.world;
    }

    /**
     * Gets the position of the block being ticked.
     */
    public BlockPos getPos() {
        return this.pos;
    }

    /**
     * Gets the Random passed to the randomTick method.
     */
    public Random getRand() {
        return this.rand;
    }

    @Override
    public final boolean isCancelable() {
        return true;
    }

    /**
     * Subclass for block ticks
     */
    public static class Block extends RandomTickEvent {

        private final BlockState state;
        private final ServerWorld serverWorld;
        
        public Block(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
            super(world, pos, rand);
            this.state = state;
            this.serverWorld = world;
        }

        /**
         * Gets the block state that is being ticked.
         */
        public BlockState getState() {
            return this.state;
        }

        /**
         * Gets the world as a server world.
         */
        public ServerWorld getServerWorld() {
            return this.serverWorld;
        }
    }
    
    /**
     * Subclass for fluid ticks
     */
    public static class Fluid extends RandomTickEvent {

        private final FluidState state;

        public Fluid(FluidState state, World world, BlockPos pos, Random rand) {
            super(world, pos, rand);
            this.state = state;
        }
        
        /**
         * Gets the fluid state that is being ticked.
         */
        public FluidState getState() {
            return this.state;
        }
    }
}
