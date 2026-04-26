package org.moddingx.libx.datagen.provider.recipe;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.DataComponentMatchers;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import org.moddingx.libx.mod.ModX;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A recipe extension is an interface that provides logic for a {@link RecipeProviderBase}. Custom recipe
 * extension should extend this interface and then add default methods to be used in {@link RecipeProviderBase#setup()}.
 * As {@link RecipeProviderBase} implements this interface as well, the abstract methods are then filled with logic.
 * <p>
 * Additionally, a recipe extension class can define a {@code public} {@code static} method named {@code setup} that
 * takes a {@link ModX} and an extension with the same type as the class that defines the method. When
 * a {@link RecipeProviderBase} implements that extension, it'll call that setup method during setup.
 */
public interface RecipeExtension {

    /**
     * Gets the {@link HolderGetter} for items.
     */
    HolderGetter<Item> items();

    /**
     * Gets the {@link RecipeProviderBase} for this extension.
     */
    RecipeProviderBase provider();

    /**
     * Gets the {@link RecipeOutput} to add recipes to.
     */
    RecipeOutput output();

    /**
     * Builds an {@link Criterion advancement criterion} for the given {@link ItemLike item}.
     */
    Criterion<?> criterion(ItemLike item);

    /**
     * Builds an {@link Criterion advancement criterion} for the given {@link TagKey tag}.
     */
    Criterion<?> criterion(TagKey<Item> item);

    /**
     * Builds an {@link Criterion advancement criterion} that requires all of the given
     * {@link ItemPredicate items}.
     */
    Criterion<?> criterion(ItemPredicate... items);

    /**
     * Builds an item predicate for a specific item.
     */
    default ItemPredicate item(ItemLike item) {
        return new ItemPredicate(Optional.of(HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(item.asItem()))), MinMaxBounds.Ints.ANY, DataComponentMatchers.ANY);
    }

    /**
     * Builds an item predicate for a specific item tag.
     */
    default ItemPredicate item(TagKey<Item> item) {
        return new ItemPredicate(Optional.of(this.items().getOrThrow(item)), MinMaxBounds.Ints.ANY, DataComponentMatchers.ANY);
    }

    /**
     * Gets a list of criteria that should be ORed, meaning that the recipe should unlock when one of
     * them is completed instead of all of them.
     */
    default List<Criterion<?>> criteria(Ingredient item) {
        List<Criterion<?>> instances = new ArrayList<>();
        if (item.getCustomIngredient() instanceof CompoundIngredient(List<Ingredient> children)) {
            for (Ingredient i : children) {
                instances.addAll(this.criteria(i));
            }
        } else {
            //noinspection deprecation
            for (Holder<Item> stack : item.items().toList()) {
                instances.add(this.criterion(this.item(stack.value())));
            }
        }
        return instances;
    }
}
