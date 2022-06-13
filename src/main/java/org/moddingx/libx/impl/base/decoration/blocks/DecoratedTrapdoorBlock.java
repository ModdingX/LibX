package org.moddingx.libx.impl.base.decoration.blocks;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.moddingx.libx.base.decoration.DecoratedBlock;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.SetupContext;

import javax.annotation.Nonnull;

public class DecoratedTrapdoorBlock extends TrapDoorBlock implements Registerable {

    public final DecoratedBlock parent;

    public DecoratedTrapdoorBlock(DecoratedBlock parent) {
        super(Properties.copy(parent));
        this.parent = parent;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient(SetupContext ctx) {
        ctx.enqueue(() -> ItemBlockRenderTypes.setRenderLayer(this, RenderType.cutout()));
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
