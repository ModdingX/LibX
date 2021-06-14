package io.github.noeppi_noeppi.libx.data.provider.recipe.crafting;

import io.github.noeppi_noeppi.libx.data.provider.recipe.RecipeExtension;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;

/**
 * A {@link RecipeExtension} that adds the ability to add common tool recipes easily.
 */
public interface ToolExtension extends RecipeExtension {

    /**
     * Creates tool recipes for a material. All the tool items may be null in which case the recipe is not created.
     */
    default void makeTools(IItemProvider material, @Nullable IItemProvider sword, @Nullable IItemProvider axe,
                             @Nullable IItemProvider pick, @Nullable IItemProvider shovel, @Nullable IItemProvider hoe) {

        if (sword != null) {
            ShapedRecipeBuilder.shapedRecipe(sword)
                    .key('m', material)
                    .key('s', Tags.Items.RODS_WOODEN)
                    .patternLine("m")
                    .patternLine("m")
                    .patternLine("s")
                    .setGroup(material.asItem().getRegistryName() + "_sword")
                    .addCriterion("has_item0", this.criterion(Tags.Items.RODS_WOODEN))
                    .addCriterion("has_item1", this.criterion(material))
                    .build(this.consumer(), this.provider().loc(material, "sword"));
        }

        if (axe != null) {
            ShapedRecipeBuilder.shapedRecipe(axe)
                    .key('m', material)
                    .key('s', Tags.Items.RODS_WOODEN)
                    .patternLine("mm")
                    .patternLine("sm")
                    .patternLine("s ")
                    .setGroup(material.asItem().getRegistryName() + "_axe")
                    .addCriterion("has_item0", this.criterion(Tags.Items.RODS_WOODEN))
                    .addCriterion("has_item1", this.criterion(material))
                    .build(this.consumer(), this.provider().loc(material, "axe"));
        }

        if (pick != null) {
            ShapedRecipeBuilder.shapedRecipe(pick)
                    .key('m', material)
                    .key('s', Tags.Items.RODS_WOODEN)
                    .patternLine("mmm")
                    .patternLine(" s ")
                    .patternLine(" s ")
                    .setGroup(material.asItem().getRegistryName() + "_pick")
                    .addCriterion("has_item0", this.criterion(Tags.Items.RODS_WOODEN))
                    .addCriterion("has_item1", this.criterion(material))
                    .build(this.consumer(), this.provider().loc(material, "pick"));
        }

        if (shovel != null) {
            ShapedRecipeBuilder.shapedRecipe(shovel)
                    .key('m', material)
                    .key('s', Tags.Items.RODS_WOODEN)
                    .patternLine("m")
                    .patternLine("s")
                    .patternLine("s")
                    .setGroup(material.asItem().getRegistryName() + "_shovel")
                    .addCriterion("has_item0", this.criterion(Tags.Items.RODS_WOODEN))
                    .addCriterion("has_item1", this.criterion(material))
                    .build(this.consumer(), this.provider().loc(material, "shovel"));
        }

        if (hoe != null) {
            ShapedRecipeBuilder.shapedRecipe(hoe)
                    .key('m', material)
                    .key('s', Tags.Items.RODS_WOODEN)
                    .patternLine("mm")
                    .patternLine("s ")
                    .patternLine("s ")
                    .setGroup(material.asItem().getRegistryName() + "_hoe")
                    .addCriterion("has_item0", this.criterion(Tags.Items.RODS_WOODEN))
                    .addCriterion("has_item1", this.criterion(material))
                    .build(this.consumer(), this.provider().loc(material, "hoe"));
        }
    }

    /**
     * Creates armor recipes for a material. All the armor items may be null in which case the recipe is not created.
     */
    default void makeArmor(IItemProvider material, @Nullable IItemProvider helmet, @Nullable IItemProvider chestplate,
                             @Nullable IItemProvider leggings, @Nullable IItemProvider boots) {

        if (helmet != null) {
            ShapedRecipeBuilder.shapedRecipe(helmet)
                    .key('m', material)
                    .patternLine("mmm")
                    .patternLine("m m")
                    .setGroup(material.asItem().getRegistryName() + "_helmet")
                    .addCriterion("has_item", this.criterion(material))
                    .build(this.consumer(), this.provider().loc(material.asItem(), "helmet"));
        }

        if (chestplate != null) {
            ShapedRecipeBuilder.shapedRecipe(chestplate)
                    .key('m', material)
                    .patternLine("m m")
                    .patternLine("mmm")
                    .patternLine("mmm")
                    .setGroup(material.asItem().getRegistryName() + "_chestplate")
                    .addCriterion("has_item", this.criterion(material))
                    .build(this.consumer(), this.provider().loc(material.asItem(), "chestplate"));
        }

        if (leggings != null) {
            ShapedRecipeBuilder.shapedRecipe(leggings)
                    .key('m', material)
                    .patternLine("mmm")
                    .patternLine("m m")
                    .patternLine("m m")
                    .setGroup(material.asItem().getRegistryName() + "_leggings")
                    .addCriterion("has_item", this.criterion(material))
                    .build(this.consumer(), this.provider().loc(material.asItem(), "leggings"));
        }

        if (boots != null) {
            ShapedRecipeBuilder.shapedRecipe(boots)
                    .key('m', material)
                    .patternLine("m m")
                    .patternLine("m m")
                    .setGroup(material.asItem().getRegistryName() + "_boots")
                    .addCriterion("has_item", this.criterion(material))
                    .build(this.consumer(), this.provider().loc(material.asItem(), "boots"));
        }
    }
}
