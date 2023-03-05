package org.moddingx.libx.impl.base.decoration.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.moddingx.libx.base.decoration.DecoratedBlock;

import javax.annotation.Nonnull;

public class DecoratedButton extends ButtonBlock {

    public final DecoratedBlock parent;

    public DecoratedButton(DecoratedBlock parent, boolean wooden) {
        super(Properties.copy(parent), wooden ? 30 : 20, wooden,
                wooden ? SoundEvents.WOODEN_BUTTON_CLICK_OFF : SoundEvents.STONE_BUTTON_CLICK_OFF,
                wooden ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_ON
        );
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
