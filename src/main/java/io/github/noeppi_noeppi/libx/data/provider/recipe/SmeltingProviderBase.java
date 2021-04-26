package io.github.noeppi_noeppi.libx.data.provider.recipe;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.data.CookingRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public abstract class SmeltingProviderBase extends AnyRecipeProvider {
    
    public SmeltingProviderBase(ModX mod, DataGenerator generator) {
        super(mod, generator);
    }

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " furnace recipes";
    }

    /**
     * Adds a smelting recipe.
     */
    protected void smelting(Consumer<IFinishedRecipe> consumer, IItemProvider in, IItemProvider out, float exp, int time) {
        this.smelting(consumer, this.loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} and {@code} exp should be the values for the normal furnace. They'll be
     * adjusted for the blast furnace automatically.
     */
    protected void blasting(Consumer<IFinishedRecipe> consumer, IItemProvider in, IItemProvider out, float exp, int time) {
        this.blasting(consumer, this.loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} and {@code} exp should be the values for the normal furnace. They'll be
     * adjusted for the smoker automatically.
     */
    protected void cooking(Consumer<IFinishedRecipe> consumer, IItemProvider in, IItemProvider out, float exp, int time) {
        this.cooking(consumer, this.loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} and {@code} exp should be the values for the normal furnace. They'll be
     * adjusted for the smoker and the campfire automatically.
     */
    protected void campfire(Consumer<IFinishedRecipe> consumer, IItemProvider in, IItemProvider out, float exp, int time) {
        this.campfire(consumer, this.loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    protected void smelting(Consumer<IFinishedRecipe> consumer, ITag<Item> in, IItemProvider out, float exp, int time) {
        this.smelting(consumer, this.loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} and {@code} exp should be the values for the normal furnace. They'll be
     * adjusted for the blast furnace automatically.
     */
    protected void blasting(Consumer<IFinishedRecipe> consumer, ITag<Item> in, IItemProvider out, float exp, int time) {
        this.blasting(consumer, this.loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} and {@code} exp should be the values for the normal furnace. They'll be
     * adjusted for the smoker automatically.
     */
    protected void cooking(Consumer<IFinishedRecipe> consumer, ITag<Item> in, IItemProvider out, float exp, int time) {
        this.cooking(consumer, this.loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} and {@code} exp should be the values for the normal furnace. They'll be
     * adjusted for the smoker and the campfire automatically.
     */
    protected void campfire(Consumer<IFinishedRecipe> consumer, ITag<Item> in, IItemProvider out, float exp, int time) {
        this.campfire(consumer, this.loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    protected void smelting(Consumer<IFinishedRecipe> consumer, ResourceLocation outputId, IItemProvider in, IItemProvider out, float exp, int time) {
        CookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(in), out, exp, time)
                .addCriterion("has_item", hasItem(in))
                .build(consumer, new ResourceLocation(outputId.getNamespace(), "smelting/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} and {@code} exp should be the values for the normal furnace. They'll be
     * adjusted for the blast furnace automatically.
     */
    protected void blasting(Consumer<IFinishedRecipe> consumer, ResourceLocation outputId, IItemProvider in, IItemProvider out, float exp, int time) {
        this.smelting(consumer, outputId, in, out, exp, time);
        CookingRecipeBuilder.blastingRecipe(Ingredient.fromItems(in), out, exp / 2, time / 2)
                .addCriterion("has_item", hasItem(in))
                .build(consumer, new ResourceLocation(outputId.getNamespace(), "blasting/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} and {@code} exp should be the values for the normal furnace. They'll be
     * adjusted for the smoker automatically.
     */
    protected void cooking(Consumer<IFinishedRecipe> consumer, ResourceLocation outputId, IItemProvider in, IItemProvider out, float exp, int time) {
        this.smelting(consumer, outputId, in, out, exp, time);
        CookingRecipeBuilder.cookingRecipe(Ingredient.fromItems(in), out, exp / 2, time / 2, IRecipeSerializer.SMOKING)
                .addCriterion("has_item", hasItem(in))
                .build(consumer, new ResourceLocation(outputId.getNamespace(), "cooking/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} and {@code} exp should be the values for the normal furnace. They'll be
     * adjusted for the smoker and the campfire automatically.
     */
    protected void campfire(Consumer<IFinishedRecipe> consumer, ResourceLocation outputId, IItemProvider in, IItemProvider out, float exp, int time) {
        this.cooking(consumer, outputId, in, out, exp, time);
        CookingRecipeBuilder.cookingRecipe(Ingredient.fromItems(in), out, exp / 2, time * 3, IRecipeSerializer.CAMPFIRE_COOKING)
                .addCriterion("has_item", hasItem(in))
                .build(consumer, new ResourceLocation(outputId.getNamespace(), "campfire/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe.
     */
    protected void smelting(Consumer<IFinishedRecipe> consumer, ResourceLocation outputId, ITag<Item> in, IItemProvider out, float exp, int time) {
        CookingRecipeBuilder.smeltingRecipe(Ingredient.fromTag(in), out, exp, time)
                .addCriterion("has_item", hasItem(in))
                .build(consumer, new ResourceLocation(outputId.getNamespace(), "smelting/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} and {@code} exp should be the values for the normal furnace. They'll be
     * adjusted for the blast furnace automatically.
     */
    protected void blasting(Consumer<IFinishedRecipe> consumer, ResourceLocation outputId, ITag<Item> in, IItemProvider out, float exp, int time) {
        this.smelting(consumer, outputId, in, out, exp, time);
        CookingRecipeBuilder.blastingRecipe(Ingredient.fromTag(in), out, exp / 2, time / 2)
                .addCriterion("has_item", hasItem(in))
                .build(consumer, new ResourceLocation(outputId.getNamespace(), "blasting/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} and {@code} exp should be the values for the normal furnace. They'll be
     * adjusted for the smoker automatically.
     */
    protected void cooking(Consumer<IFinishedRecipe> consumer, ResourceLocation outputId, ITag<Item> in, IItemProvider out, float exp, int time) {
        this.smelting(consumer, outputId, in, out, exp, time);
        CookingRecipeBuilder.cookingRecipe(Ingredient.fromTag(in), out, exp / 2, time / 2, IRecipeSerializer.SMOKING)
                .addCriterion("has_item", hasItem(in))
                .build(consumer, new ResourceLocation(outputId.getNamespace(), "cooking/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} and {@code} exp should be the values for the normal furnace. They'll be
     * adjusted for the smoker and the campfire automatically.
     */
    protected void campfire(Consumer<IFinishedRecipe> consumer, ResourceLocation outputId, ITag<Item> in, IItemProvider out, float exp, int time) {
        this.cooking(consumer, outputId, in, out, exp, time);
        CookingRecipeBuilder.cookingRecipe(Ingredient.fromTag(in), out, exp / 2, time * 3, IRecipeSerializer.CAMPFIRE_COOKING)
                .addCriterion("has_item", hasItem(in))
                .build(consumer, new ResourceLocation(outputId.getNamespace(), "campfire/" + outputId.getPath()));
    }
}
