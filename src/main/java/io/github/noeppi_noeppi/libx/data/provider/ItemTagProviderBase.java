package io.github.noeppi_noeppi.libx.data.provider;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A base class for block tag provider
 */
public abstract class ItemTagProviderBase extends ItemTagsProvider {

    protected final ModX mod;

    public ItemTagProviderBase(ModX mod, DataGenerator generatorIn, ExistingFileHelper fileHelper, BlockTagProviderBase blockTags) {
        super(generatorIn, blockTags, mod.modid, fileHelper);
        this.mod = mod;
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " item tags";
    }

    @Override
    protected final void registerTags() {
        this.setup();

        ForgeRegistries.ITEMS.getValues().stream()
                .filter(i -> this.mod.modid.equals(Objects.requireNonNull(i.getRegistryName()).getNamespace()))
                .filter(i -> !(i instanceof BlockItem))
                .forEach(this::defaultItemTags);

        ForgeRegistries.ITEMS.getValues().stream()
                .filter(i -> this.mod.modid.equals(Objects.requireNonNull(i.getRegistryName()).getNamespace()))
                .filter(i -> i instanceof BlockItem)
                .map(i -> ((BlockItem) i).getBlock())
                .forEach(this::defaultBlockItemTags);
    }

    /**
     * A method to add your custom tags to items.
     */
    protected abstract void setup();

    /**
     * Called for every item from your mod that is not a {@code BlockItem}. You should
     * add item tags here, that can be inferred from the item.
     */
    public void defaultItemTags(Item item) {

    }

    /**
     * Called for every item from your mod that is a {@code BlockItem}. You should
     * add item tags here, that can be inferred from the block. However this is not
     * the correct place for block tags.
     */
    public void defaultBlockItemTags(Block block) {

    }
}
