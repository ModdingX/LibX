package io.github.noeppi_noeppi.libx.data.provider;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;

import javax.annotation.Nonnull;

public class BlockTagProviderBase extends BlockTagsProvider {

    protected final ModX mod;

    public BlockTagProviderBase(ModX mod, DataGenerator generatorIn) {
        super(generatorIn);
        this.mod = mod;
    }

    @Nonnull
    @Override
    public final String getName() {
        return mod.modid + " block tags";
    }
}
