package io.github.noeppi_noeppi.libx.event;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.Event;

import java.util.Random;

/**
 * Called when a block or a fluid receives a random tick. Canceling the event will stop the
 * random tick. See subclasses.
 */
public abstract class RandomTickEvent extends Event {

    private final Level level;
    private final BlockPos pos;
    private final Random rand;

    private RandomTickEvent(Level level, BlockPos pos, Random rand) {
        this.level = level;
        this.pos = pos;
        this.rand = rand;
    }

    /**
     * Gets the world where the block / fluid is about to be ticked randomly.
     */
    public Level getLevel() {
        return this.level;
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
        private final ServerLevel serverLevel;
        
        public Block(BlockState state, ServerLevel level, BlockPos pos, Random rand) {
            super(level, pos, rand);
            this.state = state;
            this.serverLevel = level;
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
        public ServerLevel getServerLevel() {
            return this.serverLevel;
        }
    }
    
    /**
     * Subclass for fluid ticks
     */
    public static class Fluid extends RandomTickEvent {

        private final FluidState state;

        public Fluid(FluidState state, Level level, BlockPos pos, Random rand) {
            super(level, pos, rand);
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
