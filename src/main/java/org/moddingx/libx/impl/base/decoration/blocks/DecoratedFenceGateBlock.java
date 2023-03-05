package org.moddingx.libx.impl.base.decoration.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.moddingx.libx.base.decoration.DecoratedBlock;

import javax.annotation.Nonnull;

public class DecoratedFenceGateBlock extends FenceGateBlock {

    public final DecoratedBlock parent;

    public DecoratedFenceGateBlock(DecoratedBlock parent) {
        super(Properties.copy(parent), SoundEvents.FENCE_GATE_CLOSE, SoundEvents.FENCE_GATE_OPEN);
        this.parent = parent;
    }

    @Override
    public void animateTick(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull RandomSource random) {
        this.parent.animateTick(state, level, pos, random);
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getExplosionResistance() {
        return this.parent.getExplosionResistance();
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLightBlock(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos) {
        return this.parent.getLightBlock(state, level, pos);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return this.parent.getLightEmission(state, world, pos);
    }
}
