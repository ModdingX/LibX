package org.moddingx.libx.datagen.provider.patchouli;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.moddingx.libx.datagen.provider.patchouli.page.PageJson;
import org.moddingx.libx.mod.ModX;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Builder for a patchouli book category.
 */
public class CategoryBuilder {

    public final ModX mod;
    public final ResourceLocation id;
    private String name;
    private String description;
    private ItemStack icon;
    
    private int sort;
    private final List<Consumer<JsonObject>> postProcessors;

    public CategoryBuilder(ModX mod, ResourceLocation id) {
        this.mod = mod;
        this.id = id;
        this.sort = -1;
        this.postProcessors = new ArrayList<>();
    }

    /**
     * Sets the category name. This is required.
     */
    public CategoryBuilder name(String name) {
        this.name = name;
        return this;
    }
    
    /**
     * Sets the category description. This is required.
     */
    public CategoryBuilder description(String description) {
        this.description = description;
        return this;
    }
    
    /**
     * Sets the category icon. This is required.
     */
    public CategoryBuilder icon(ItemLike icon) {
        return this.icon(new ItemStack(icon));
    }
    
    /**
     * Sets the category icon. This is required.
     */
    public CategoryBuilder icon(ItemStack icon) {
        this.icon = icon.copy();
        return this;
    }

    /**
     * Sets a sort num directly. If this is not called, categories will be sorted in order they are created.
     */
    public CategoryBuilder sort(int sort) {
        this.sort = sort;
        return this;
    }

    /**
     * Adds a {@link Consumer} that can modify the final json data after it is built.
     */
    public CategoryBuilder postProcess(Consumer<JsonObject> postProcessor) {
        this.postProcessors.add(postProcessor);
        return this;
    }
    
    JsonObject build(BiFunction<String, List<String>, String> translations, int num) {
        if (this.name == null) throw new IllegalStateException("Category name not set: " + this.id);
        if (this.description == null) throw new IllegalStateException("Category description not set: " + this.id);
        if (this.icon == null) throw new IllegalStateException("Category icon not set: " + this.id);
        JsonObject json = new JsonObject();
        json.addProperty("name", translations.apply(this.name, List.of("category", this.mod.modid, this.id.getNamespace(), this.id.getPath(), "name")));
        json.addProperty("description", translations.apply(this.description, List.of("category", this.mod.modid, this.id.getNamespace(), this.id.getPath(), "description")));
        json.add("icon", PageJson.stack(this.icon));
        json.addProperty("sortnum", this.sort < 0 ? num : this.sort);
        
        for (Consumer<JsonObject> postProcessor : this.postProcessors) {
            postProcessor.accept(json);
        }
        
        return json;
    }
}
