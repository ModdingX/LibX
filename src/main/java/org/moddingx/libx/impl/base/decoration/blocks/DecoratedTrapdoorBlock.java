package org.moddingx.libx.impl.base.decoration.blocks;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.moddingx.libx.base.decoration.DecoratedBlock;
import org.moddingx.libx.base.decoration.DecorationMaterial;
import org.moddingx.libx.registration.Registerable;

import javax.annotation.Nonnull;

public class DecoratedTrapdoorBlock extends TrapDoorBlock implements Registerable {

    public final DecoratedBlock parent;

    public DecoratedTrapdoorBlock(DecoratedBlock parent) {
        super(parent.getMaterialProperties().blockSetType(), Util.make(() -> {
                    Properties blockProperties = Properties.ofFullCopy(parent)
                            .noOcclusion()
                            .isValidSpawn(Blocks::never);
                    // we need to check for WOOD since isWood() is true for the non-burnable NETHER_WOOD, too
                    if (parent.getContext().material() == DecorationMaterial.WOOD) {
                        blockProperties.instrument(NoteBlockInstrument.BASS)
                                .strength(3.0f)
                                .ignitedByLava();
                    }
                    return blockProperties;
                })
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
    public int getLightBlock(@Nonnull BlockState state) {
        return this.parent.getLightBlock(state);
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
