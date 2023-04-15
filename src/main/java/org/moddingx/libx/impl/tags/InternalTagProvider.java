package org.moddingx.libx.impl.tags;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.tags.CommonTagsProviderBase;

import java.util.Map;

public class InternalTagProvider extends CommonTagsProviderBase {
    
    public InternalTagProvider(DatagenContext ctx) {
        super(ctx);
    }

    @Override
    public void setup() {
        for (Map.Entry<TagKey<Item>, TagKey<Item>> entry : InternalTags.Items.getTags().entrySet()) {
            this.item(entry.getKey()).addTag(entry.getValue());
            this.item(entry.getValue());
        }
        for (Map.Entry<TagKey<Block>, TagKey<Block>> entry : InternalTags.Blocks.getTags().entrySet()) {
            this.block(entry.getKey()).addTag(entry.getValue());
            this.block(entry.getValue());
        }
    }
}
