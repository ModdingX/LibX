package org.moddingx.libx.impl.datagen.recipe;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import org.moddingx.libx.base.decoration.DecorationContext;
import org.moddingx.libx.datagen.provider.recipe.RecipeExtension;
import org.moddingx.libx.datagen.provider.recipe.StoneCuttingExtension;
import org.moddingx.libx.impl.base.decoration.blocks.*;

import java.util.List;

public class DecorationRecipes {
    
    public static void defaultRecipes(Block block, RecipeExtension ext) {
        if (block instanceof DecoratedSlabBlock decorated) {
            if (ext instanceof StoneCuttingExtension cut && decorated.parent.getContext().baseMaterial() == DecorationContext.BaseMaterial.STONE) {
                stoneCutting(ext, Ingredient.of(decorated.parent), decorated, 2);
            }
            ObjectCraftingBuilder.buildShaped(ext, new Object[]{ decorated, 6, "###", '#', decorated.parent });
        } else if (block instanceof DecoratedStairBlock decorated) {
            if (ext instanceof StoneCuttingExtension cut && decorated.parent.getContext().baseMaterial() == DecorationContext.BaseMaterial.STONE) {
                stoneCutting(ext, Ingredient.of(decorated.parent), decorated, 1);
            }
            ObjectCraftingBuilder.buildShaped(ext, new Object[]{ decorated, 4, "#  ", "## ", "###", '#', decorated.parent });
        } else if (block instanceof DecoratedWallBlock decorated) {
            if (ext instanceof StoneCuttingExtension cut && decorated.parent.getContext().baseMaterial() == DecorationContext.BaseMaterial.STONE) {
                stoneCutting(ext, Ingredient.of(decorated.parent), decorated, 1);
            }
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
    
    private static void stoneCutting(RecipeExtension ext, Ingredient input, ItemLike output, int amount) {
        SingleItemRecipeBuilder builder = SingleItemRecipeBuilder.stonecutting(input, output, amount);
        List<CriterionTriggerInstance> criteria = ext.criteria(input);
        for (int i = 0; i < criteria.size(); i++) {
            builder.unlockedBy("has_item" + i, criteria.get(i));
        }
        builder.save(ext.consumer(), ext.provider().loc(output, "stonecutting"));
    }
}
