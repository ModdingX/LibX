package org.moddingx.libx.impl.datagen.model;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class TypedBlockModelProvider extends BlockModelProvider {

    private final ResourceLocation renderTypes;
    
    public TypedBlockModelProvider(PackOutput packOutput, String modid, ExistingFileHelper fileHelper, ResourceLocation renderTypes) {
        super(packOutput, modid, fileHelper);
        this.renderTypes = renderTypes;
    }

    @Nonnull
    @Override
    public BlockModelBuilder getBuilder(@Nonnull String path) {
        return super.getBuilder(path).renderType(this.renderTypes);
    }
    
    @Nonnull
    @Override // Method is protected in superclass
    public CompletableFuture<?> generateAll(@Nonnull CachedOutput cache) {
        return super.generateAll(cache);
    }

    @Nonnull
    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput cache) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected void registerModels() {
        //
    }
}
