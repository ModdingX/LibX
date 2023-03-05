package org.moddingx.libx.impl.base.decoration.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.moddingx.libx.base.decoration.DecoratedBlock;
import org.moddingx.libx.base.decoration.DecorationContext;

import javax.annotation.Nonnull;

public class DecoratedPressurePlate extends PressurePlateBlock {

    public final DecoratedBlock parent;
    public final PressurePlateBlock.Sensitivity sensitivity;

    public DecoratedPressurePlate(PressurePlateBlock.Sensitivity sensitivity, DecoratedBlock parent) {
        super(sensitivity, Properties.copy(parent), getOffSound(parent.getContext().baseMaterial()), getOnSound(parent.getContext().baseMaterial()));
        this.parent = parent;
        this.sensitivity = sensitivity;
    }
    
    private static SoundEvent getOnSound(DecorationContext.BaseMaterial material) {
        return switch (material) {
            case WOOD -> SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON;
            case METAL -> SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON;
            default -> SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON;
        };
    }
    
    private static SoundEvent getOffSound(DecorationContext.BaseMaterial material) {
        return switch (material) {
            case WOOD -> SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF;
            case METAL -> SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF;
            default -> SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF;
        };
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
