package org.moddingx.libx.impl.data;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.io.IOException;

public class TypedBlockModelProvider extends BlockModelProvider {

    private final ResourceLocation renderTypes;
    
    public TypedBlockModelProvider(DataGenerator generator, String modid, ExistingFileHelper fileHelper, ResourceLocation renderTypes) {
        super(generator, modid, fileHelper);
        this.renderTypes = renderTypes;
    }

    @Override
    public BlockModelBuilder getBuilder(String path) {
        return super.getBuilder(path).renderType(this.renderTypes);
    }
    
    @Override // Method is protected in superclass
    public void generateAll(CachedOutput cache) {
        super.generateAll(cache);
    }

    @Override
    public void run(CachedOutput cache) throws IOException {
        //
    }

    @Override
    protected void registerModels() {
        //
    }
}
