package io.github.noeppi_noeppi.libx.data.provider;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;

import javax.annotation.Nonnull;

public class ItemTagProviderBase extends ItemTagsProvider {

    protected final ModX mod;

    public ItemTagProviderBase(ModX mod, DataGenerator generatorIn, BlockTagProviderBase blockTags) {
        super(generatorIn, blockTags);
        this.mod = mod;
    }

    @Nonnull
    @Override
    public final String getName() {
        return mod.modid + " item tags";
    }
}
