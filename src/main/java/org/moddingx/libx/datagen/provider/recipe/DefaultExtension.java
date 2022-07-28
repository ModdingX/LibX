package org.moddingx.libx.datagen.provider.recipe;

import net.minecraft.world.item.BlockItem;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.base.decoration.DecorationType;
import org.moddingx.libx.impl.datagen.recipe.DecorationRecipes;
import org.moddingx.libx.mod.ModX;

import java.util.Map;

/**
 * Recipe extension to add some default recipes. Currently this adds recipes for all
 * builtin {@link DecorationType decoration types}.
 */
public interface DefaultExtension extends RecipeExtension {
    
    static void setup(ModX mod, DefaultExtension ext) {
        ForgeRegistries.ITEMS.getEntries().stream()
                .filter(e -> mod.modid.equals(e.getKey().location().getNamespace()))
                .map(Map.Entry::getValue)
                .filter(item -> item instanceof BlockItem)
                .map(item -> ((BlockItem) item).getBlock())
                .forEach(block -> DecorationRecipes.defaultRecipes(block, ext));
    }
}
