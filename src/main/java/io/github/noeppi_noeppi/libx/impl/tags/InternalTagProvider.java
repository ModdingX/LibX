package io.github.noeppi_noeppi.libx.impl.tags;

import io.github.noeppi_noeppi.libx.data.provider.CommonTagsProviderBase;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Map;

public class InternalTagProvider extends CommonTagsProviderBase {
    
    public InternalTagProvider(ModX mod, DataGenerator generator, ExistingFileHelper fileHelper) {
        super(mod, generator, fileHelper);
    }

    @Override
    public void setup() {
        for (Map.Entry<Tag.Named<Item>, Tag.Named<Item>> entry : InternalTags.Items.getTags().entrySet()) {
            this.item(entry.getKey()).addTag(entry.getValue());
        }
        for (Map.Entry<Tag.Named<Block>, Tag.Named<Block>> entry : InternalTags.Blocks.getTags().entrySet()) {
            this.block(entry.getKey()).addTag(entry.getValue());
        }
    }
}