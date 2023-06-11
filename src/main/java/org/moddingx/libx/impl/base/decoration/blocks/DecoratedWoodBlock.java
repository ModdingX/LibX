package org.moddingx.libx.impl.base.decoration.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.moddingx.libx.base.decoration.DecoratedBlock;
import org.moddingx.libx.base.decoration.DecorationType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DecoratedWoodBlock extends RotatedPillarBlock {

    public final DecoratedBlock parent;
    @Nullable public final DecorationType<? extends Block> log;
    @Nullable public final DecorationType<? extends Block> stripped;

    public DecoratedWoodBlock(DecoratedBlock parent, @Nullable DecorationType<? extends Block> log, @Nullable DecorationType<? extends Block> stripped) {
        super(Properties.copy(parent));
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

    @Override
    @SuppressWarnings("deprecation")
    public int getLightBlock(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos) {
        return this.parent.getLightBlock(state, level, pos);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return this.parent.getLightEmission(state, world, pos);
    }

    @Nullable
    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if (toolAction == ToolActions.AXE_STRIP && this.stripped != null && this.parent.has(this.stripped)) {
            Block strippedBlock = this.parent.get(this.stripped);
            return strippedBlock.defaultBlockState().setValue(BlockStateProperties.AXIS, state.getValue(BlockStateProperties.AXIS));
        } else {
            return super.getToolModifiedState(state, context, toolAction, simulate);
        }
    }
}
