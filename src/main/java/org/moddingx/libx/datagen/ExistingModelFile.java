package org.moddingx.libx.datagen;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelFile;

/**
 * A {@link ModelFile} that always returns {@code true} in {@link #exists()} for cases
 * where the model obviously exists but is not found.
 */
public class ExistingModelFile extends ModelFile {

    public ExistingModelFile(ResourceLocation rl) {
        super(rl);
    }

    @Override
    protected boolean exists() {
        return true;
    }
}
