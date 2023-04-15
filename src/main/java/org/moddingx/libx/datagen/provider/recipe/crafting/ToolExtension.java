package org.moddingx.libx.datagen.provider.recipe.crafting;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.datagen.provider.recipe.RecipeExtension;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A {@link RecipeExtension} that adds the ability to add common tool recipes easily.
 */
public interface ToolExtension extends RecipeExtension {

    /**
     * Creates tool recipes for a material. All the tool items may be null in which case the recipe is not created.
     */
    default void makeTools(ItemLike material, @Nullable ItemLike sword, @Nullable ItemLike axe,
                           @Nullable ItemLike pick, @Nullable ItemLike shovel, @Nullable ItemLike hoe) {

        if (sword != null) {
            ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, sword)
                    .define('m', material)
                    .define('s', Tags.Items.RODS_WOODEN)
                    .pattern("m")
                    .pattern("m")
                    .pattern("s")
                    .group(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(material.asItem())) + "_sword")
                    .unlockedBy("has_item0", this.criterion(Tags.Items.RODS_WOODEN))
                    .unlockedBy("has_item1", this.criterion(material))
                    .save(this.consumer(), this.provider().loc(material, "sword"));
        }

        if (axe != null) {
            ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, axe)
                    .define('m', material)
                    .define('s', Tags.Items.RODS_WOODEN)
                    .pattern("mm")
                    .pattern("sm")
                    .pattern("s ")
                    .group(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(material.asItem())) + "_axe")
                    .unlockedBy("has_item0", this.criterion(Tags.Items.RODS_WOODEN))
                    .unlockedBy("has_item1", this.criterion(material))
                    .save(this.consumer(), this.provider().loc(material, "axe"));
        }

        if (pick != null) {
            ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, pick)
                    .define('m', material)
                    .define('s', Tags.Items.RODS_WOODEN)
                    .pattern("mmm")
                    .pattern(" s ")
                    .pattern(" s ")
                    .group(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(material.asItem())) + "_pick")
                    .unlockedBy("has_item0", this.criterion(Tags.Items.RODS_WOODEN))
                    .unlockedBy("has_item1", this.criterion(material))
                    .save(this.consumer(), this.provider().loc(material, "pick"));
        }

        if (shovel != null) {
            ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, shovel)
                    .define('m', material)
                    .define('s', Tags.Items.RODS_WOODEN)
                    .pattern("m")
                    .pattern("s")
                    .pattern("s")
                    .group(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(material.asItem())) + "_shovel")
                    .unlockedBy("has_item0", this.criterion(Tags.Items.RODS_WOODEN))
                    .unlockedBy("has_item1", this.criterion(material))
                    .save(this.consumer(), this.provider().loc(material, "shovel"));
        }

        if (hoe != null) {
            ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, hoe)
                    .define('m', material)
                    .define('s', Tags.Items.RODS_WOODEN)
                    .pattern("mm")
                    .pattern("s ")
                    .pattern("s ")
                    .group(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(material.asItem())) + "_hoe")
                    .unlockedBy("has_item0", this.criterion(Tags.Items.RODS_WOODEN))
                    .unlockedBy("has_item1", this.criterion(material))
                    .save(this.consumer(), this.provider().loc(material, "hoe"));
        }
    }

    /**
     * Creates armor recipes for a material. All the armor items may be null in which case the recipe is not created.
     */
    default void makeArmor(ItemLike material, @Nullable ItemLike helmet, @Nullable ItemLike chestplate,
                           @Nullable ItemLike leggings, @Nullable ItemLike boots) {

        if (helmet != null) {
            ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, helmet)
                    .define('m', material)
                    .pattern("mmm")
                    .pattern("m m")
                    .group(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(material.asItem())) + "_helmet")
                    .unlockedBy("has_item", this.criterion(material))
                    .save(this.consumer(), this.provider().loc(material.asItem(), "helmet"));
        }

        if (chestplate != null) {
            ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, chestplate)
                    .define('m', material)
                    .pattern("m m")
                    .pattern("mmm")
                    .pattern("mmm")
                    .group(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(material.asItem())) + "_chestplate")
                    .unlockedBy("has_item", this.criterion(material))
                    .save(this.consumer(), this.provider().loc(material.asItem(), "chestplate"));
        }

        if (leggings != null) {
            ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, leggings)
                    .define('m', material)
                    .pattern("mmm")
                    .pattern("m m")
                    .pattern("m m")
                    .group(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(material.asItem())) + "_leggings")
                    .unlockedBy("has_item", this.criterion(material))
                    .save(this.consumer(), this.provider().loc(material.asItem(), "leggings"));
        }

        if (boots != null) {
            ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, boots)
                    .define('m', material)
                    .pattern("m m")
                    .pattern("m m")
                    .group(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(material.asItem())) + "_boots")
                    .unlockedBy("has_item", this.criterion(material))
                    .save(this.consumer(), this.provider().loc(material.asItem(), "boots"));
        }
    }
}
