package org.moddingx.libx.impl.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.moddingx.libx.datagen_old.provider.CommonTagsProviderBase;
import org.moddingx.libx.mod.ModX;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class InternalTagProvider extends CommonTagsProviderBase {
    
    public InternalTagProvider(ModX mod, DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper fileHelper) {
        super(mod, generator, lookupProvider, fileHelper);
    }

    @Override
    public void setup() {
        for (Map.Entry<TagKey<Item>, TagKey<Item>> entry : InternalTags.Items.getTags().entrySet()) {
            this.item(entry.getKey()).addTag(entry.getValue());
        }
        for (Map.Entry<TagKey<Block>, TagKey<Block>> entry : InternalTags.Blocks.getTags().entrySet()) {
            this.block(entry.getKey()).addTag(entry.getValue());
        }
    }
}
