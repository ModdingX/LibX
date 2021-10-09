package io.github.noeppi_noeppi.libx.impl.tags;

import io.github.noeppi_noeppi.libx.LibX;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Required, so there are no conflicts if copy methods are called with vanilla tags in datagen.
public class InternalTags {
    
    public static class Items {
        
        private static final Map<Tag.Named<Item>, Tag.Named<Item>> TAGS = new HashMap<>();
        private static final Map<Tag.Named<Block>, Tag.Named<Item>> COPIES = new HashMap<>();

        public static Tag.Named<Item> WOODEN_BUTTONS = wrap(ItemTags.WOODEN_BUTTONS, Blocks.WOODEN_BUTTONS);
        public static Tag.Named<Item> BUTTONS = wrap(ItemTags.BUTTONS, Blocks.BUTTONS);
        public static Tag.Named<Item> WOODEN_DOORS = wrap(ItemTags.WOODEN_DOORS, Blocks.WOODEN_DOORS);
        public static Tag.Named<Item> WOODEN_STAIRS = wrap(ItemTags.WOODEN_STAIRS, Blocks.WOODEN_STAIRS);
        public static Tag.Named<Item> WOODEN_SLABS = wrap(ItemTags.WOODEN_SLABS, Blocks.WOODEN_SLABS);
        public static Tag.Named<Item> WOODEN_FENCES = wrap(ItemTags.WOODEN_FENCES, Blocks.WOODEN_FENCES);
        public static Tag.Named<Item> WOODEN_PRESSURE_PLATES = wrap(ItemTags.WOODEN_PRESSURE_PLATES, Blocks.WOODEN_PRESSURE_PLATES);
        public static Tag.Named<Item> WOODEN_TRAPDOORS = wrap(ItemTags.WOODEN_TRAPDOORS, Blocks.WOODEN_TRAPDOORS);
        public static Tag.Named<Item> DOORS = wrap(ItemTags.DOORS, Blocks.DOORS);
        public static Tag.Named<Item> STAIRS = wrap(ItemTags.STAIRS, Blocks.STAIRS);
        public static Tag.Named<Item> SLABS = wrap(ItemTags.SLABS, Blocks.SLABS);
        public static Tag.Named<Item> WALLS = wrap(ItemTags.WALLS, Blocks.WALLS);
        public static Tag.Named<Item> TRAPDOORS = wrap(ItemTags.TRAPDOORS, Blocks.TRAPDOORS);
        public static Tag.Named<Item> FENCES = wrap(ItemTags.FENCES, Blocks.FENCES);
        public static Tag.Named<Item> SIGNS = wrap(ItemTags.SIGNS, Blocks.STANDING_SIGNS);
        
        private static Tag.Named<Item> wrap(Tag.Named<Item> tag, Tag.Named<Block> blockTag) {
            Tag.Named<Item> newTag = ItemTags.bind(LibX.getInstance().resource("impl_" + tag.getName().getPath()).toString());
            TAGS.put(tag, newTag);
            COPIES.put(blockTag, newTag);
            return newTag;
        }

        public static Map<Tag.Named<Item>, Tag.Named<Item>> getTags() {
            return Collections.unmodifiableMap(TAGS);
        }

        public static Map<Tag.Named<Block>, Tag.Named<Item>> getCopies() {
            return Collections.unmodifiableMap(COPIES);
        }
    }
    
    public static class Blocks {

        private static final Map<Tag.Named<Block>, Tag.Named<Block>> TAGS = new HashMap<>();

        public static Tag.Named<Block> WOODEN_BUTTONS = wrap(BlockTags.WOODEN_BUTTONS);
        public static Tag.Named<Block> BUTTONS = wrap(BlockTags.BUTTONS);
        public static Tag.Named<Block> WOODEN_DOORS = wrap(BlockTags.WOODEN_DOORS);
        public static Tag.Named<Block> WOODEN_STAIRS = wrap(BlockTags.WOODEN_STAIRS);
        public static Tag.Named<Block> WOODEN_SLABS = wrap(BlockTags.WOODEN_SLABS);
        public static Tag.Named<Block> WOODEN_FENCES = wrap(BlockTags.WOODEN_FENCES);
        public static Tag.Named<Block> PRESSURE_PLATES = wrap(BlockTags.PRESSURE_PLATES);
        public static Tag.Named<Block> WOODEN_PRESSURE_PLATES = wrap(BlockTags.WOODEN_PRESSURE_PLATES);
        public static Tag.Named<Block> STONE_PRESSURE_PLATES = wrap(BlockTags.STONE_PRESSURE_PLATES);
        public static Tag.Named<Block> WOODEN_TRAPDOORS = wrap(BlockTags.WOODEN_TRAPDOORS);
        public static Tag.Named<Block> DOORS = wrap(BlockTags.DOORS);
        public static Tag.Named<Block> STAIRS = wrap(BlockTags.STAIRS);
        public static Tag.Named<Block> SLABS = wrap(BlockTags.SLABS);
        public static Tag.Named<Block> WALLS = wrap(BlockTags.WALLS);
        public static Tag.Named<Block> TRAPDOORS = wrap(BlockTags.TRAPDOORS);
        public static Tag.Named<Block> FENCES = wrap(BlockTags.FENCES);
        public static Tag.Named<Block> STANDING_SIGNS = wrap(BlockTags.STANDING_SIGNS);
        public static Tag.Named<Block> WALL_SIGNS = wrap(BlockTags.WALL_SIGNS);
        public static Tag.Named<Block> SIGNS = wrap(BlockTags.SIGNS);
        public static Tag.Named<Block> FENCE_GATES = wrap(BlockTags.FENCE_GATES);

        private static Tag.Named<Block> wrap(Tag.Named<Block> tag) {
            Tag.Named<Block> newTag = BlockTags.bind(LibX.getInstance().resource("impl_" + tag.getName().getPath()).toString());
            TAGS.put(tag, newTag);
            return newTag;
        }

        public static Map<Tag.Named<Block>, Tag.Named<Block>> getTags() {
            return Collections.unmodifiableMap(TAGS);
        }
    }
}
