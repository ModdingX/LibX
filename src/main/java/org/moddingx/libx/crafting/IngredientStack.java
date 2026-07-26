package org.moddingx.libx.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.moddingx.libx.crafting.ingredient.EmptyIngredient;

import java.util.function.Predicate;

/**
 * An {@link Ingredient} with an amount.
 */
public record IngredientStack(Ingredient ingredient, int count) implements Predicate<ItemStack> {

    public static final IngredientStack EMPTY = new IngredientStack(new EmptyIngredient().toVanilla(), 0);

    public static final Codec<IngredientStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(IngredientStack::ingredient),
                    Codec.INT.fieldOf("count").forGetter(IngredientStack::count)
            ).apply(instance, IngredientStack::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, IngredientStack> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, IngredientStack::ingredient,
            ByteBufCodecs.VAR_INT, IngredientStack::count,
            IngredientStack::new
    );

    public IngredientStack(Ingredient ingredient, int count) {
        if (count <= 0 || ingredient.isEmpty()) {
            this.ingredient = new EmptyIngredient().toVanilla();
            this.count = 0;
        } else {
            this.ingredient = ingredient;
            this.count = count;
        }
    }

    /**
     * Returns whether the ingredient matches the stack and the count of the stack is greater or equal
     * to the count of the IngredientStack.
     */
    @Override
    public boolean test(ItemStack stack) {
        return stack.getCount() >= this.count && this.ingredient.test(stack);
    }

    /**
     * Returns whether the count is 0 or {@link Ingredient#isEmpty()} returns true.
     */
    public boolean isEmpty() {
        return this.count == 0 || this.ingredient.isEmpty();
    }
}
