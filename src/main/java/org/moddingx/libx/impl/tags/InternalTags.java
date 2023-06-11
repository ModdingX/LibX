package org.moddingx.libx.impl.tags;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.moddingx.libx.LibX;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Required, so there are no conflicts if copy methods are called with vanilla tags in datagen.
public class InternalTags {
    
    public static class Items {
        
        private static final Map<TagKey<Item>, TagKey<Item>> TAGS = new HashMap<>();
        private static final Map<TagKey<Block>, TagKey<Item>> COPIES = new HashMap<>();

        public static final TagKey<Item> LOGS = wrap(ItemTags.LOGS, Blocks.LOGS);
        public static final TagKey<Item> LOGS_THAT_BURN = wrap(ItemTags.LOGS_THAT_BURN, Blocks.LOGS_THAT_BURN);
        public static final TagKey<Item> WOODEN_BUTTONS = wrap(ItemTags.WOODEN_BUTTONS, Blocks.WOODEN_BUTTONS);
        public static final TagKey<Item> STONE_BUTTONS = wrap(ItemTags.STONE_BUTTONS, Blocks.STONE_BUTTONS);
        public static final TagKey<Item> BUTTONS = wrap(ItemTags.BUTTONS, Blocks.BUTTONS);
        public static final TagKey<Item> WOODEN_DOORS = wrap(ItemTags.WOODEN_DOORS, Blocks.WOODEN_DOORS);
        public static final TagKey<Item> WOODEN_STAIRS = wrap(ItemTags.WOODEN_STAIRS, Blocks.WOODEN_STAIRS);
        public static final TagKey<Item> WOODEN_SLABS = wrap(ItemTags.WOODEN_SLABS, Blocks.WOODEN_SLABS);
        public static final TagKey<Item> WOODEN_FENCES = wrap(ItemTags.WOODEN_FENCES, Blocks.WOODEN_FENCES);
        public static final TagKey<Item> WOODEN_PRESSURE_PLATES = wrap(ItemTags.WOODEN_PRESSURE_PLATES, Blocks.WOODEN_PRESSURE_PLATES);
        public static final TagKey<Item> WOODEN_TRAPDOORS = wrap(ItemTags.WOODEN_TRAPDOORS, Blocks.WOODEN_TRAPDOORS);
        public static final TagKey<Item> DOORS = wrap(ItemTags.DOORS, Blocks.DOORS);
        public static final TagKey<Item> STAIRS = wrap(ItemTags.STAIRS, Blocks.STAIRS);
        public static final TagKey<Item> SLABS = wrap(ItemTags.SLABS, Blocks.SLABS);
        public static final TagKey<Item> WALLS = wrap(ItemTags.WALLS, Blocks.WALLS);
        public static final TagKey<Item> TRAPDOORS = wrap(ItemTags.TRAPDOORS, Blocks.TRAPDOORS);
        public static final TagKey<Item> FENCES = wrap(ItemTags.FENCES, Blocks.FENCES);
        public static final TagKey<Item> FENCE_GATES = wrap(ItemTags.FENCE_GATES, Blocks.FENCE_GATES);
        public static final TagKey<Item> SIGNS = wrap(ItemTags.SIGNS, Blocks.STANDING_SIGNS);
        public static final TagKey<Item> HANGING_SIGNS = wrap(ItemTags.HANGING_SIGNS, Blocks.CEILING_HANGING_SIGNS);
        
        private static TagKey<Item> wrap(TagKey<Item> tag, TagKey<Block> blockTag) {
            TagKey<Item> newTag = ItemTags.create(LibX.getInstance().resource("impl_" + tag.location().getPath()));
            TAGS.put(tag, newTag);
            COPIES.put(blockTag, newTag);
            return newTag;
        }

        public static Map<TagKey<Item>, TagKey<Item>> getTags() {
            return Collections.unmodifiableMap(TAGS);
        }

        public static Map<TagKey<Block>, TagKey<Item>> getCopies() {
            return Collections.unmodifiableMap(COPIES);
        }
    }
    
    public static class Blocks {

        private static final Map<TagKey<Block>, TagKey<Block>> TAGS = new HashMap<>();

        public static final TagKey<Block> LOGS = wrap(BlockTags.LOGS);
        public static final TagKey<Block> LOGS_THAT_BURN = wrap(BlockTags.LOGS_THAT_BURN);
        public static final TagKey<Block> WOODEN_BUTTONS = wrap(BlockTags.WOODEN_BUTTONS);
        public static final TagKey<Block> STONE_BUTTONS = wrap(BlockTags.STONE_BUTTONS);
        public static final TagKey<Block> BUTTONS = wrap(BlockTags.BUTTONS);
        public static final TagKey<Block> WOODEN_DOORS = wrap(BlockTags.WOODEN_DOORS);
        public static final TagKey<Block> WOODEN_STAIRS = wrap(BlockTags.WOODEN_STAIRS);
        public static final TagKey<Block> WOODEN_SLABS = wrap(BlockTags.WOODEN_SLABS);
        public static final TagKey<Block> WOODEN_FENCES = wrap(BlockTags.WOODEN_FENCES);
        public static final TagKey<Block> PRESSURE_PLATES = wrap(BlockTags.PRESSURE_PLATES);
        public static final TagKey<Block> WOODEN_PRESSURE_PLATES = wrap(BlockTags.WOODEN_PRESSURE_PLATES);
        public static final TagKey<Block> STONE_PRESSURE_PLATES = wrap(BlockTags.STONE_PRESSURE_PLATES);
        public static final TagKey<Block> WOODEN_TRAPDOORS = wrap(BlockTags.WOODEN_TRAPDOORS);
        public static final TagKey<Block> DOORS = wrap(BlockTags.DOORS);
        public static final TagKey<Block> STAIRS = wrap(BlockTags.STAIRS);
        public static final TagKey<Block> SLABS = wrap(BlockTags.SLABS);
        public static final TagKey<Block> WALLS = wrap(BlockTags.WALLS);
        public static final TagKey<Block> TRAPDOORS = wrap(BlockTags.TRAPDOORS);
        public static final TagKey<Block> FENCES = wrap(BlockTags.FENCES);
        public static final TagKey<Block> FENCE_GATES = wrap(BlockTags.FENCE_GATES);
        public static final TagKey<Block> STANDING_SIGNS = wrap(BlockTags.STANDING_SIGNS);
        public static final TagKey<Block> WALL_SIGNS = wrap(BlockTags.WALL_SIGNS);
        public static final TagKey<Block> SIGNS = wrap(BlockTags.SIGNS);
        public static final TagKey<Block> CEILING_HANGING_SIGNS = wrap(BlockTags.CEILING_HANGING_SIGNS);
        public static final TagKey<Block> WALL_HANGING_SIGNS = wrap(BlockTags.WALL_HANGING_SIGNS);
        public static final TagKey<Block> HANGING_SIGNS = wrap(BlockTags.ALL_HANGING_SIGNS);

        private static TagKey<Block> wrap(TagKey<Block> tag) {
            TagKey<Block> newTag = BlockTags.create(LibX.getInstance().resource("impl_" + tag.location().getPath()));
            TAGS.put(tag, newTag);
            return newTag;
        }

        public static Map<TagKey<Block>, TagKey<Block>> getTags() {
            return Collections.unmodifiableMap(TAGS);
        }
    }
}
