package io.github.noeppi_noeppi.libx.impl.base.decoration.blocks;

import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import io.github.noeppi_noeppi.libx.base.decoration.DecoratedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Random;

@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.19")
public class DecoratedFenceBlock extends FenceBlock {

    public final DecoratedBlock parent;

    public DecoratedFenceBlock(DecoratedBlock parent) {
        super(Properties.copy(parent));
        this.parent = parent;
    }

    @Override
    public void animateTick(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Random rand) {
        this.parent.animateTick(state, level, pos, rand);
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
