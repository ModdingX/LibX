package io.github.noeppi_noeppi.libx.data.provider;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.block.Block;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A base class for block tag provider
 */
public abstract class BlockTagProviderBase extends BlockTagsProvider {

    protected final ModX mod;

    public BlockTagProviderBase(ModX mod, DataGenerator generatorIn, ExistingFileHelper fileHelper) {
        super(generatorIn, mod.modid, fileHelper);
        this.mod = mod;
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " block tags";
    }

    @Override
    protected final void registerTags() {
        this.setup();

        ForgeRegistries.BLOCKS.getValues().stream()
                .filter(i -> this.mod.modid.equals(Objects.requireNonNull(i.getRegistryName()).getNamespace()))
                .forEach(this::defaultBlockTags);
    }

    /**
     * A method to add your custom tags to blocks.
     */
    protected abstract void setup();

    /**
     * Called for every block from your mod. You should add block tags here,
     * that can be inferred from the block.
     */
    public void defaultBlockTags(Block block) {

    }
}
