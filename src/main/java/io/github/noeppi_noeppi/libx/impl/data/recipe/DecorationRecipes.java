package io.github.noeppi_noeppi.libx.impl.data.recipe;

import io.github.noeppi_noeppi.libx.data.provider.recipe.RecipeExtension;
import io.github.noeppi_noeppi.libx.impl.base.decoration.blocks.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;

public class DecorationRecipes {
    
    public static void defaultRecipes(Block block, RecipeExtension ext) {
        if (block instanceof DecoratedSlabBlock decorated) {
            ObjectCraftingBuilder.buildShaped(ext, new Object[]{ decorated, 6, "###", '#', decorated.parent });
        } else if (block instanceof DecoratedStairBlock decorated) {
            ObjectCraftingBuilder.buildShaped(ext, new Object[]{ decorated, 4, "#  ", "## ", "###", '#', decorated.parent });
        } else if (block instanceof DecoratedWallBlock decorated) {
            ObjectCraftingBuilder.buildShaped(ext, new Object[]{ decorated, 6, "###", "###", '#', decorated.parent });
        } else if (block instanceof DecoratedFenceBlock decorated) {
            ObjectCraftingBuilder.buildShaped(ext, new Object[]{ decorated, 3, "#s#", "#s#", '#', decorated.parent, 's', Tags.Items.RODS_WOODEN });
        } else if (block instanceof DecoratedFenceGateBlock decorated) {
            ObjectCraftingBuilder.buildShaped(ext, new Object[]{ decorated, "s#s", "s#s", '#', decorated.parent, 's', Tags.Items.RODS_WOODEN });
        } else if (block instanceof DecoratedDoorBlock decorated) {
            ObjectCraftingBuilder.buildShaped(ext, new Object[]{ decorated, 3, "##", "##", "##", '#', decorated.parent });
        } else if (block instanceof DecoratedTrapdoorBlock decorated) {
            ObjectCraftingBuilder.buildShaped(ext, new Object[]{ decorated, 2, "###", "###", '#', decorated.parent });
        } else if (block instanceof DecoratedWoodButton decorated) {
            ObjectCraftingBuilder.buildShapeless(ext, new Object[]{ decorated, decorated.parent });
        } else if (block instanceof DecoratedStoneButton decorated) {
            ObjectCraftingBuilder.buildShapeless(ext, new Object[]{ decorated, decorated.parent });
        } else if (block instanceof DecoratedPressurePlate decorated) {
            ObjectCraftingBuilder.buildShaped(ext, new Object[]{ decorated, "##", '#', decorated.parent });
        } else if (block instanceof DecoratedSign.Standing decorated) {
            ObjectCraftingBuilder.buildShaped(ext, new Object[]{ decorated, 3, "###", "###", " s ", '#', decorated.parent, 's', Tags.Items.RODS_WOODEN });
        }
    }
}
