package io.github.noeppi_noeppi.libx.impl.recipe;

import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.libx.LibX;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EmptyRecipe implements IRecipe<IInventory> {
    
    public static final ResourceLocation ID = new ResourceLocation(LibX.getInstance().modid, "empty");
    public static final IRecipeType<EmptyRecipe> TYPE = IRecipeType.register(ID.toString());
    
    private final ResourceLocation id;

    public EmptyRecipe(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public boolean matches(@Nonnull IInventory inv, @Nonnull World world) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull IInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Nonnull
    @Override
    public IRecipeType<?> getType() {
        return TYPE;
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> getRemainingItems(@Nonnull IInventory inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Nonnull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    @Override
    public boolean isDynamic() {
        return IRecipe.super.isDynamic();
    }
    
    @Nonnull
    @Override
    public ItemStack getIcon() {
        return new ItemStack(Blocks.BARRIER);
    }
    
    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<EmptyRecipe> {

        public static final Serializer INSTANCE = new Serializer();
        
        private Serializer() {
            
        }
        
        @Nonnull
        @Override
        public EmptyRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
            return new EmptyRecipe(recipeId);
        }

        @Nullable
        @Override
        public EmptyRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
            return new EmptyRecipe(recipeId);
        }

        @Override
        public void write(@Nonnull PacketBuffer buffer, @Nonnull EmptyRecipe recipe) {
            //
        }
    }
}
