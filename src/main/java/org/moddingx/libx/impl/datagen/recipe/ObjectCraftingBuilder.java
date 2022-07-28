package org.moddingx.libx.impl.datagen.recipe;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.CompoundIngredient;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.datagen.provider.recipe.RecipeExtension;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ObjectCraftingBuilder {

    public static void buildShaped(RecipeExtension ext, Object[] objects) {
        ObjectReader reader = new ObjectReader(objects);
        ResourceLocation id = getId(reader);
        Pair<ItemLike, Integer> output = getOutput(reader);
        if (id == null) id = ext.provider().loc(output.getLeft());
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(output.getLeft(), output.getRight());
        for (String line : reader.consumeWhile(String.class)) {
            builder.pattern(line);
        }
        addShapedIngredients(ext, builder, reader);
        builder.save(ext.consumer(), id);
    }

    public static void buildShapeless(RecipeExtension ext, Object[] objects) {
        ObjectReader reader = new ObjectReader(objects);
        ResourceLocation id = getId(reader);
        Pair<ItemLike, Integer> output = getOutput(reader);
        if (id == null) id = ext.provider().loc(output.getLeft());
        ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(output.getLeft(), output.getRight());
        addShapelessIngredients(ext, builder, reader);
        builder.save(ext.consumer(), id);
    }

    private static void addShapedIngredients(RecipeExtension ext, ShapedRecipeBuilder builder, ObjectReader reader) {
        int nextId = 0;
        while (true) {
            Optional<Character> value = reader.expectConsume(Character.class);
            if (value.isPresent()) {
                char key = value.get();
                Ingredient ingredient = getIngredient(reader);
                builder.define(key, ingredient);
                nextId = addCriteriaToBuilder(builder::unlockedBy, ext.criteria(ingredient), nextId);
            } else {
                break;
            }
        }
    }

    private static void addShapelessIngredients(RecipeExtension ext, ShapelessRecipeBuilder builder, ObjectReader reader) {
        int nextId = 0;
        while (reader.hasNext()) {
            Ingredient ingredient = getIngredient(reader);
            builder.requires(ingredient);
            nextId = addCriteriaToBuilder(builder::unlockedBy, ext.criteria(ingredient), nextId);
        }
    }

    private static int addCriteriaToBuilder(BiConsumer<String, CriterionTriggerInstance> triggers, List<CriterionTriggerInstance> criteria, int nextId) {
        for (CriterionTriggerInstance criterion : criteria) {
            triggers.accept("criterion" + (nextId++), criterion);
        }
        return nextId;
    }

    @Nonnull
    private static Ingredient getIngredient(ObjectReader reader) {
        return ObjectCraftingBuilder.first(
                () -> reader.optConsume(ItemLike.class).map(Ingredient::of),
                () -> reader.optConsume(TagKey.class).map(ObjectCraftingBuilder::createTagIngredient),
                () -> reader.optConsume(Ingredient.class),
                () -> reader.optConsume(List.class).map(list -> {
                    ObjectReader sub = new ObjectReader(list.toArray());
                    List<Ingredient> subList = new ArrayList<>();
                    while (sub.hasNext()) subList.add(getIngredient(sub));
                    return CompoundIngredient.of(subList.toArray(new Ingredient[]{}));
                })
        ).orElseThrow(() -> new IllegalStateException("Can't build recipe, invalid ingredient at position " + reader.pos()));
    }

    @Nullable
    private static ResourceLocation getId(ObjectReader reader) {
        return reader.optConsume(ResourceLocation.class).orElse(null);
    }

    @Nonnull
    private static Pair<ItemLike, Integer> getOutput(ObjectReader reader) {
        return ObjectCraftingBuilder.<Pair<ItemLike, Integer>>first(
                () -> reader.optConsume(ItemLike.class).map(item -> Pair.of(item, reader.optConsume(Integer.class).orElse(1))),
                () -> reader.optConsume(ItemStack.class).map(stack -> Pair.of(stack.getItem(), stack.getCount()))
        ).orElseThrow(() -> new IllegalStateException("Can't build recipe, invalid output at position " + reader.pos()));
    }

    private static Ingredient createTagIngredient(TagKey<?> key) {
        if (key.registry() != Registry.ITEM_REGISTRY) throw new IllegalArgumentException("Non-item tag in recipe: " + key);
        //noinspection unchecked
        return Ingredient.of((TagKey<Item>) key);
    }
    
    @SafeVarargs
    private static <T> Optional<T> first(Supplier<Optional<T>>... values) {
        for (Supplier<Optional<T>> value : values) {
            Optional<T> opt = value.get();
            if (opt.isPresent()) {
                return opt;
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unused")
    private static class ObjectReader {

        private final Object[] objects;
        private int idx;

        public ObjectReader(Object[] objects) {
            this.objects = objects;
            for (Object object : objects) {
                if (object == null) {
                    throw new IllegalStateException("Can't build recipe, null objects are not allowed.");
                }
            }
        }

        @Nonnull
        public Object peek() {
            if (this.idx >= this.objects.length) {
                throw new IllegalStateException("Can't build recipe, end of array.");
            }
            return this.objects[this.idx];
        }

        @Nonnull
        public <T> T peek(Class<T> cls) {
            if (this.idx >= this.objects.length) {
                throw new IllegalStateException("Can't build recipe, end of array, expected element of type " + cls);
            } else if (!cls.isAssignableFrom(this.objects[this.idx].getClass())) {
                throw new IllegalStateException("Can't build recipe, expected element of type " + cls + " at position " + this.idx);
            } else {
                //noinspection unchecked
                return (T) this.objects[this.idx];
            }
        }

        @Nonnull
        public <T> Optional<T> expect(Class<T> cls) {
            if (this.idx >= this.objects.length) {
                return Optional.empty();
            } else if (!cls.isAssignableFrom(this.objects[this.idx].getClass())) {
                throw new IllegalStateException("Can't build recipe, expected element of type " + cls + " at position " + this.idx);
            } else {
                //noinspection unchecked
                return Optional.of((T) this.objects[this.idx]);
            }
        }

        @Nonnull
        public <T> Optional<T> expectConsume(Class<T> cls) {
            Optional<T> value = this.expect(cls);
            if (value.isPresent()) this.consume();
            return value;
        }

        @Nonnull
        public <T> Optional<T> opt(Class<T> cls) {
            if (this.idx >= this.objects.length) {
                return Optional.empty();
            } else if (!cls.isAssignableFrom(this.objects[this.idx].getClass())) {
                return Optional.empty();
            } else {
                //noinspection unchecked
                return Optional.of((T) this.objects[this.idx]);
            }
        }

        @Nonnull
        public <T> Optional<T> optConsume(Class<T> cls) {
            Optional<T> value = this.opt(cls);
            if (value.isPresent()) this.consume();
            return value;
        }

        @Nonnull
        public <T> List<T> consumeWhile(Class<T> cls) {
            List<T> list = new ArrayList<>();
            while (true) {
                Optional<T> value = this.optConsume(cls);
                if (value.isPresent()) {
                    list.add(value.get());
                } else {
                    return list;
                }
            }
        }
        
        @Nonnull
        @SuppressWarnings("UnusedReturnValue")
        public Object consume() {
            if (this.idx >= this.objects.length) {
                throw new IllegalStateException("Can't build recipe, end of array.");
            }
            this.idx += 1;
            return this.objects[this.idx - 1];
        }

        public boolean hasNext() {
            return this.idx < this.objects.length;
        }

        public int pos() {
            return this.idx;
        }
    }
}
