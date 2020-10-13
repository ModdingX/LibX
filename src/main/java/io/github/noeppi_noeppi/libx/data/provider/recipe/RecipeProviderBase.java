package io.github.noeppi_noeppi.libx.data.provider.recipe;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.data.*;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * A base class for recipe provider
 */
public abstract class RecipeProviderBase extends RecipeProvider {

    protected final ModX mod;

    public RecipeProviderBase(ModX mod, DataGenerator generator) {
        super(generator);
        this.mod = mod;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " recipes";
    }

    protected abstract void registerRecipes(@Nonnull Consumer<IFinishedRecipe> consumer);

    /**
     * Creates four recipes like it's done with blocks, ingots and nuggets.
     */
    @SuppressWarnings("ConstantConditions")
    private void makeBlockItemNugget(Consumer<IFinishedRecipe> consumer, IItemProvider block, IItemProvider ingot, IItemProvider nugget) {

        this.makeBlockItem(consumer, block, ingot);

        ShapedRecipeBuilder.shapedRecipe(ingot)
                .key('a', nugget)
                .patternLine("aaa")
                .patternLine("aaa")
                .patternLine("aaa")
                .setGroup(ingot.asItem().getRegistryName() + "_from_nuggets")
                .addCriterion("has_item", hasItem(nugget))
                .build(consumer, new ResourceLocation(this.mod.modid, ingot.asItem().getRegistryName().getPath() + "_from_nuggets"));

        ShapelessRecipeBuilder.shapelessRecipe(nugget, 9)
                .addIngredient(ingot)
                .setGroup(nugget.asItem().getRegistryName() + "_from_ingot")
                .addCriterion("has_item", hasItem(ingot))
                .build(consumer, new ResourceLocation(this.mod.modid, nugget.asItem().getRegistryName().getPath() + "_from_ingot"));
    }

    /**
     * Creates two recipes like it's done with blocks and ingots or ingots and nuggets
     */
    @SuppressWarnings("ConstantConditions")
    private void makeBlockItem(Consumer<IFinishedRecipe> consumer, IItemProvider block, IItemProvider ingot) {

        ShapedRecipeBuilder.shapedRecipe(block)
                .key('a', ingot)
                .patternLine("aaa")
                .patternLine("aaa")
                .patternLine("aaa")
                .setGroup(block.asItem().getRegistryName() + "_from_ingots")
                .addCriterion("has_item", hasItem(ingot))
                .build(consumer, new ResourceLocation(this.mod.modid, block.asItem().getRegistryName().getPath() + "_from_ingots"));

        ShapelessRecipeBuilder.shapelessRecipe(ingot, 9)
                .addIngredient(block)
                .setGroup(ingot.asItem().getRegistryName() + "_from_block")
                .addCriterion("has_item", hasItem(block))
                .build(consumer, new ResourceLocation(this.mod.modid, ingot.asItem().getRegistryName().getPath() + "_from_block"));
    }
}
