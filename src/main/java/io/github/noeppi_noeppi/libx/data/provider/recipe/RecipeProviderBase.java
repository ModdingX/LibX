package io.github.noeppi_noeppi.libx.data.provider.recipe;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * A base class for recipe provider
 */
public abstract class RecipeProviderBase extends AnyRecipeProvider {
    
    public RecipeProviderBase(ModX mod, DataGenerator generator) {
        super(mod, generator);
    }

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " recipes";
    }

    /**
     * Creates four recipes like it's done with blocks, ingots and nuggets.
     */
    protected void makeBlockItemNugget(Consumer<IFinishedRecipe> consumer, IItemProvider block, IItemProvider ingot, IItemProvider nugget) {

        this.makeBlockItem(consumer, block, ingot);

        ShapedRecipeBuilder.shapedRecipe(ingot)
                .key('a', nugget)
                .patternLine("aaa")
                .patternLine("aaa")
                .patternLine("aaa")
                .setGroup(ingot.asItem().getRegistryName() + "_from_nuggets")
                .addCriterion("has_item", hasItem(nugget))
                .build(consumer, this.loc(ingot, "from_nuggets"));

        ShapelessRecipeBuilder.shapelessRecipe(nugget, 9)
                .addIngredient(ingot)
                .setGroup(nugget.asItem().getRegistryName() + "_from_ingot")
                .addCriterion("has_item", hasItem(ingot))
                .build(consumer, this.loc(nugget, "from_ingot"));
    }

    /**
     * Creates two recipes like it's done with blocks and ingots or ingots and nuggets
     */
    protected void makeBlockItem(Consumer<IFinishedRecipe> consumer, IItemProvider block, IItemProvider ingot) {

        ShapedRecipeBuilder.shapedRecipe(block)
                .key('a', ingot)
                .patternLine("aaa")
                .patternLine("aaa")
                .patternLine("aaa")
                .setGroup(block.asItem().getRegistryName() + "_from_ingots")
                .addCriterion("has_item", hasItem(ingot))
                .build(consumer, this.loc(block, "from_ingots"));

        ShapelessRecipeBuilder.shapelessRecipe(ingot, 9)
                .addIngredient(block)
                .setGroup(ingot.asItem().getRegistryName() + "_from_block")
                .addCriterion("has_item", hasItem(block))
                .build(consumer, this.loc(ingot, "from_block"));
    }

    /**
     * Calls {@link RecipeProviderBase#makeSmallBlockItem(Consumer, IItemProvider, IItemProvider, boolean)} with default value true for {@code revert}
     */
    protected void makeSmallBlockItem(Consumer<IFinishedRecipe> consumer, IItemProvider block, IItemProvider ingot) {

        this.makeSmallBlockItem(consumer, block, ingot, true);
    }

    /**
     * Creates one or two recipes like it's done with blocks with 2x2 ingots
     *
     * @param revert Whether the block can be crafted back or not
     */
    protected void makeSmallBlockItem(Consumer<IFinishedRecipe> consumer, IItemProvider block, IItemProvider ingot, boolean revert) {

        ShapedRecipeBuilder.shapedRecipe(block)
                .key('a', ingot)
                .patternLine("aa")
                .patternLine("aa")
                .setGroup(block.asItem().getRegistryName() + "_from_ingots")
                .addCriterion("has_item", hasItem(ingot))
                .build(consumer, this.loc(block, "from_ingots"));

        if (revert) {
            ShapelessRecipeBuilder.shapelessRecipe(ingot, 4)
                    .addIngredient(block)
                    .setGroup(ingot.asItem().getRegistryName() + "_from_block")
                    .addCriterion("has_item", hasItem(block))
                    .build(consumer, this.loc(ingot, "from_block"));
        }
    }

    /**
     * Creates tool recipes for a material. All the tool items may be null in which case the recipe is not created.
     */
    protected void makeTools(Consumer<IFinishedRecipe> consumer, IItemProvider material, @Nullable IItemProvider sword,
                          @Nullable IItemProvider axe, @Nullable IItemProvider pick, @Nullable IItemProvider shovel,
                          @Nullable IItemProvider hoe) {
        
        if (sword != null) {
            ShapedRecipeBuilder.shapedRecipe(sword)
                    .key('m', material)
                    .key('s', Tags.Items.RODS_WOODEN)
                    .patternLine("m")
                    .patternLine("m")
                    .patternLine("s")
                    .setGroup(material.asItem().getRegistryName() + "_sword")
                    .addCriterion("has_item0", hasItem(Tags.Items.RODS_WOODEN))
                    .addCriterion("has_item1", hasItem(material))
                    .build(consumer, this.loc(material, "sword"));
        }

        if (axe != null) {
            ShapedRecipeBuilder.shapedRecipe(axe)
                    .key('m', material)
                    .key('s', Tags.Items.RODS_WOODEN)
                    .patternLine("mm")
                    .patternLine("sm")
                    .patternLine("s ")
                    .setGroup(material.asItem().getRegistryName() + "_axe")
                    .addCriterion("has_item0", hasItem(Tags.Items.RODS_WOODEN))
                    .addCriterion("has_item1", hasItem(material))
                    .build(consumer, this.loc(material, "axe"));
        }

        if (pick != null) {
            ShapedRecipeBuilder.shapedRecipe(pick)
                    .key('m', material)
                    .key('s', Tags.Items.RODS_WOODEN)
                    .patternLine("mmm")
                    .patternLine(" s ")
                    .patternLine(" s ")
                    .setGroup(material.asItem().getRegistryName() + "_pick")
                    .addCriterion("has_item0", hasItem(Tags.Items.RODS_WOODEN))
                    .addCriterion("has_item1", hasItem(material))
                    .build(consumer, this.loc(material, "pick"));
        }

        if (shovel != null) {
            ShapedRecipeBuilder.shapedRecipe(shovel)
                    .key('m', material)
                    .key('s', Tags.Items.RODS_WOODEN)
                    .patternLine("m")
                    .patternLine("s")
                    .patternLine("s")
                    .setGroup(material.asItem().getRegistryName() + "_shovel")
                    .addCriterion("has_item0", hasItem(Tags.Items.RODS_WOODEN))
                    .addCriterion("has_item1", hasItem(material))
                    .build(consumer, this.loc(material, "shovel"));
        }

        if (hoe != null) {
            ShapedRecipeBuilder.shapedRecipe(hoe)
                    .key('m', material)
                    .key('s', Tags.Items.RODS_WOODEN)
                    .patternLine("mm")
                    .patternLine("s ")
                    .patternLine("s ")
                    .setGroup(material.asItem().getRegistryName() + "_hoe")
                    .addCriterion("has_item0", hasItem(Tags.Items.RODS_WOODEN))
                    .addCriterion("has_item1", hasItem(material))
                    .build(consumer, this.loc(material, "hoe"));
        }
    }

    /**
     * Creates armor recipes for a material. All the armor items may be null in which case the recipe is not created.
     */
    protected void makeArmor(Consumer<IFinishedRecipe> consumer, IItemProvider material, @Nullable IItemProvider helmet,
                          @Nullable IItemProvider chestplate, @Nullable IItemProvider leggings,
                          @Nullable IItemProvider boots) {
        
        if (helmet != null) {
            ShapedRecipeBuilder.shapedRecipe(helmet)
                    .key('m', material)
                    .patternLine("mmm")
                    .patternLine("m m")
                    .setGroup(material.asItem().getRegistryName() + "_helmet")
                    .addCriterion("has_item", hasItem(material))
                    .build(consumer, this.loc(material.asItem(), "helmet"));
        }

        if (chestplate != null) {
            ShapedRecipeBuilder.shapedRecipe(chestplate)
                    .key('m', material)
                    .patternLine("m m")
                    .patternLine("mmm")
                    .patternLine("mmm")
                    .setGroup(material.asItem().getRegistryName() + "_chestplate")
                    .addCriterion("has_item", hasItem(material))
                    .build(consumer, this.loc(material.asItem(), "chestplate"));
        }

        if (leggings != null) {
            ShapedRecipeBuilder.shapedRecipe(leggings)
                    .key('m', material)
                    .patternLine("mmm")
                    .patternLine("m m")
                    .patternLine("m m")
                    .setGroup(material.asItem().getRegistryName() + "_leggings")
                    .addCriterion("has_item", hasItem(material))
                    .build(consumer, this.loc(material.asItem(), "leggings"));
        }

        if (boots != null) {
            ShapedRecipeBuilder.shapedRecipe(boots)
                    .key('m', material)
                    .patternLine("m m")
                    .patternLine("m m")
                    .setGroup(material.asItem().getRegistryName() + "_boots")
                    .addCriterion("has_item", hasItem(material))
                    .build(consumer, this.loc(material.asItem(), "boots"));
        }
    }
}
