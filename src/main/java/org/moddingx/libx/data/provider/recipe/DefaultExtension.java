package org.moddingx.libx.data.provider.recipe;

import net.minecraft.world.item.BlockItem;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.impl.data.recipe.DecorationRecipes;
import org.moddingx.libx.mod.ModX;

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
