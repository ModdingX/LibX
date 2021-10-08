package io.github.noeppi_noeppi.libx.data.provider.recipe;

import io.github.noeppi_noeppi.libx.impl.data.recipe.DecorationRecipes;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

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
