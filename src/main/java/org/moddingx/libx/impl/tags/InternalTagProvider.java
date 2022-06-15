package org.moddingx.libx.impl.tags;

import net.minecraft.data.DataGenerator;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.moddingx.libx.datagen.provider.CommonTagsProviderBase;
import org.moddingx.libx.mod.ModX;

import java.util.Map;

public class InternalTagProvider extends CommonTagsProviderBase {
    
    public InternalTagProvider(ModX mod, DataGenerator generator, ExistingFileHelper fileHelper) {
        super(mod, generator, fileHelper);
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
