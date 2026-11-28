package org.moddingx.libx.impl.base.decoration;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import org.moddingx.libx.impl.base.decoration.blocks.DecoratedWoodBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// Since 26.3, axe stripping is done through block transformers. LibX appends one entry using this provider to the
// vanilla axe transformer, so all decorated logs and woods can be stripped without any data from the mods using them.
public class DecorationStrippingProvider implements BlockStateProvider {

    public static final DecorationStrippingProvider INSTANCE = new DecorationStrippingProvider();
    public static final MapCodec<DecorationStrippingProvider> CODEC = MapCodec.unit(INSTANCE);

    private DecorationStrippingProvider() {

    }

    @Nonnull
    @Override
    public MapCodec<DecorationStrippingProvider> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    public BlockState getState(@Nonnull LevelAccessor level, @Nonnull RandomSource random, @Nonnull BlockPos pos) {
        BlockState state = this.getOptionalState(level, random, pos);
        return state != null ? state : level.getBlockState(pos);
    }

    @Nullable
    @Override
    public BlockState getOptionalState(@Nonnull LevelAccessor level, @Nonnull RandomSource random, @Nonnull BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof DecoratedWoodBlock wood) {
            return wood.getStrippedState(state);
        } else {
            return null;
        }
    }
}
