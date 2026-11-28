package org.moddingx.libx.impl.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.component.BlockTransformers;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.BlockTransformAppender;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.impl.base.decoration.DecorationStrippingProvider;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class InternalDataMapProvider extends DataMapProvider {

    public InternalDataMapProvider(DatagenContext ctx) {
        super(ctx.output(), CompletableFuture.completedFuture(ctx.registries().registryAccess()));
    }

    @Override
    protected void gather(@Nonnull HolderLookup.Provider provider) {
        // Strips all decorated logs and woods from all mods
        this.builder(NeoForgeDataMaps.BLOCK_TRANSFORM_APPENDERS).add(BlockTransformers.AXE, new BlockTransformAppender(List.of(
                BlockTransformer.BlockTransformData.builder(DecorationStrippingProvider.INSTANCE)
                        .sound(SoundEvents.AXE_STRIP)
                        .build()
        )), false);
    }

    @Nonnull
    @Override
    public String getName() {
        return "libx data maps";
    }
}
