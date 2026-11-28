package org.moddingx.libx.impl.base.decoration.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.moddingx.libx.base.decoration.DecoratedBlock;
import org.moddingx.libx.base.decoration.DecorationType;
import org.moddingx.libx.impl.base.decoration.DecorationBlockIdContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DecoratedWoodBlock extends RotatedPillarBlock {

    public final DecoratedBlock parent;
    @Nullable public final DecorationType<? extends Block> log;
    @Nullable public final DecorationType<? extends Block> stripped;

    public DecoratedWoodBlock(DecoratedBlock parent, @Nullable DecorationType<? extends Block> log, @Nullable DecorationType<? extends Block> stripped) {
        super(DecorationBlockIdContext.applyId(Properties.ofFullCopy(parent)));
        this.parent = parent;
        this.log = log;
        this.stripped = stripped;
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

    public boolean isBurnable() {
        return this.parent.isBurnable();
    }

    @Override
    protected int getLightDampening(@Nonnull BlockState state) {
        return this.parent.getLightDampening(state);
    }

    @Override
    public int getLightEmission(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        return this.parent.getLightEmission(state, world, pos);
    }

    @Nullable
    public BlockState getStrippedState(BlockState state) {
        if (this.stripped == null || !this.parent.has(this.stripped)) {
            return null;
        }

        BlockState strippedState = this.parent.get(this.stripped).defaultBlockState();
        if (strippedState.hasProperty(BlockStateProperties.AXIS) && state.hasProperty(BlockStateProperties.AXIS)) {
            strippedState = strippedState.setValue(BlockStateProperties.AXIS, state.getValue(BlockStateProperties.AXIS));
        }

        return strippedState;
    }

    @Override
    public boolean isEnabled(@Nonnull FeatureFlagSet enabledFeatures) {
        return this.parent.isEnabled(enabledFeatures);
    }
}
