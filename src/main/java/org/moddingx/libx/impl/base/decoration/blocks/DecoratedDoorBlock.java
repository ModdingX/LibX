package org.moddingx.libx.impl.base.decoration.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;
import org.moddingx.libx.base.decoration.DecoratedBlock;
import org.moddingx.libx.base.decoration.DecorationMaterial;
import org.moddingx.libx.impl.base.decoration.DecorationBlockIdContext;
import org.moddingx.libx.registration.Registerable;

import javax.annotation.Nonnull;

public class DecoratedDoorBlock extends DoorBlock implements Registerable {

    public final DecoratedBlock parent;

    public DecoratedDoorBlock(DecoratedBlock parent) {
        super(parent.getMaterialProperties().blockSetType(), Util.make(() -> {
            Properties blockProperties = DecorationBlockIdContext.applyId(Properties.ofFullCopy(parent))
                            .noOcclusion()
                            .pushReaction(PushReaction.DESTROY);
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
