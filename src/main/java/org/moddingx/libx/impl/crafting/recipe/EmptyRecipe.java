package org.moddingx.libx.impl.crafting.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.moddingx.libx.LibX;
import org.moddingx.libx.codec.MoreStreamCodecs;

import javax.annotation.Nonnull;

public class EmptyRecipe implements Recipe<RecipeInput> {
    
    public static final ResourceLocation ID = LibX.getInstance().resource("empty");
    public static final RecipeType<EmptyRecipe> TYPE = RecipeType.simple(ID);

    private EmptyRecipe() {}

    @Override
    public boolean matches(@Nonnull RecipeInput inv, @Nonnull Level level) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull RecipeInput input, @Nonnull HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem(@Nonnull HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> getRemainingItems(@Nonnull RecipeInput input) {
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
        
    }

    @Nonnull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    @Override
    public boolean isSpecial() {
        return false;
    }
    
    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.BARRIER);
    }
    
    public static EmptyRecipe empty() {
        return new EmptyRecipe();
    }
    
    public static class Serializer implements RecipeSerializer<EmptyRecipe> {

        public static final Serializer INSTANCE = new Serializer();
        
        private Serializer() {
            
        }

        @Nonnull
        @Override
        public MapCodec<EmptyRecipe> codec() {
            return MapCodec.unit(EmptyRecipe::new);
        }

        @Nonnull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EmptyRecipe> streamCodec() {
            return MoreStreamCodecs.unit(EmptyRecipe::new);
        }
    }
}
