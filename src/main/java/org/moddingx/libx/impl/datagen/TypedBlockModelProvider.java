package org.moddingx.libx.impl.datagen;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class TypedBlockModelProvider extends BlockModelProvider {

    private final ResourceLocation renderTypes;
    
    public TypedBlockModelProvider(PackOutput packOutput, String modid, ExistingFileHelper fileHelper, ResourceLocation renderTypes) {
        super(packOutput, modid, fileHelper);
        this.renderTypes = renderTypes;
    }

    @Override
    public BlockModelBuilder getBuilder(String path) {
        return super.getBuilder(path).renderType(this.renderTypes);
    }
    
    @Override // Method is protected in superclass
    public CompletableFuture<?> generateAll(CachedOutput cache) {
        return super.generateAll(cache);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected void registerModels() {
        //
    }
}
