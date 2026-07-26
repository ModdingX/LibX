package org.moddingx.libx.impl.base.decoration.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.moddingx.libx.base.decoration.DecoratedBlock;
import org.moddingx.libx.impl.base.decoration.DecorationBlockIdContext;

import javax.annotation.Nonnull;

public class DecoratedPressurePlate extends PressurePlateBlock {

    public final DecoratedBlock parent;

    public DecoratedPressurePlate(DecoratedBlock parent) {
        super(parent.getMaterialProperties().blockSetType(), DecorationBlockIdContext.applyId(Properties.ofFullCopy(parent)));
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
    protected int getLightDampening(@Nonnull BlockState state) {
        return this.parent.getLightDampening(state);
    }

    @Override
    public int getLightEmission(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        return this.parent.getLightEmission(state, world, pos);
    }

    @Override
    public boolean isEnabled(@Nonnull FeatureFlagSet enabledFeatures) {
        return this.parent.isEnabled(enabledFeatures);
    }
}
