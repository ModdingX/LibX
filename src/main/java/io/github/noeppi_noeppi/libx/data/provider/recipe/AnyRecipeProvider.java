package io.github.noeppi_noeppi.libx.data.provider.recipe;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AnyRecipeProvider extends RecipeProvider {

    protected final ModX mod;

    public AnyRecipeProvider(ModX mod, DataGenerator generator) {
        super(generator);
        this.mod = mod;
    }

    @Nonnull
    @Override
    public abstract String getName();

    @Override
    protected abstract void registerRecipes(@Nonnull Consumer<IFinishedRecipe> consumer);

    /**
     * Gets a {@link ResourceLocation} with the namespace being the modid of the mod given in constructor
     * and the path being the registry path of the given item.
     */
    protected ResourceLocation loc(IItemProvider item) {
        return new ResourceLocation(this.mod.modid, Objects.requireNonNull(item.asItem().getRegistryName()).getPath());
    }

    /**
     * Gets a {@link ResourceLocation} with the namespace being the modid of the mod given in constructor
     * and the path being the registry path of the given item followed by an underscore and the
     * given suffix.
     */
    protected ResourceLocation loc(IItemProvider item, String suffix) {
        return new ResourceLocation(this.mod.modid, Objects.requireNonNull(item.asItem().getRegistryName()).getPath() + "_" + suffix);
    }
}
