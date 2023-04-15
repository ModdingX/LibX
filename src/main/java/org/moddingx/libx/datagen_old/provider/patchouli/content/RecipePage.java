package org.moddingx.libx.datagen_old.provider.patchouli.content;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.annotation.meta.Experimental;
import org.moddingx.libx.datagen_old.provider.patchouli.page.PageBuilder;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Content for pages with a single recipe followed by some text.
 */
@Experimental
public abstract class RecipePage extends CaptionContent {

    protected final String pageType;
    protected final List<ResourceLocation> recipes;

    public RecipePage(String pageType, ResourceLocation... recipes) {
        this(pageType, List.of(recipes), null);
    }
    
    protected RecipePage(String pageType, List<ResourceLocation> recipes, @Nullable String caption) {
        super(caption);
        this.pageType = pageType;
        this.recipes = List.copyOf(recipes);
    }

    /**
     * Adds the recipe to the generated json. The default implementation will add the recipe id under a key named
     * {@code recipe} if there is exactly one recipe. If there are multiple recipes, an array of all keys is created
     * instead.
     */
    protected void addRecipeKey(JsonObject json) {
        if (this.recipes.size() != 1) {
            JsonArray array = new JsonArray();
            for (ResourceLocation recipe : this.recipes) {
                array.add(recipe.toString());
            }
            json.add("recipe", array);
        } else {
            json.addProperty("recipe", this.recipes.get(0).toString());
        }
    }
    
    @Override
    protected void specialPage(PageBuilder builder, @Nullable String caption) {
        JsonObject json = new JsonObject();
        json.addProperty("type", this.pageType);
        this.addRecipeKey(json);
        if (caption != null) {
            json.addProperty("text", builder.translate(caption));
        }
        builder.addPage(json);
    }
}
