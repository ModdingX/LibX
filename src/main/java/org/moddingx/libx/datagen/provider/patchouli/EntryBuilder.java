package org.moddingx.libx.datagen.provider.patchouli;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.moddingx.libx.annotation.meta.Experimental;
import org.moddingx.libx.datagen.provider.patchouli.content.CaptionContent;
import org.moddingx.libx.datagen.provider.patchouli.content.TextContent;
import org.moddingx.libx.datagen.provider.patchouli.page.Content;
import org.moddingx.libx.datagen.provider.patchouli.page.PageBuilder;
import org.moddingx.libx.datagen.provider.patchouli.page.PageJson;
import org.moddingx.libx.impl.datagen_old.patchouli.content.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Builder for a patchouli book entry.
 */
@Experimental
public class EntryBuilder {
    
    public final String id;
    public final ResourceLocation category;
    private String name;
    private ItemStack icon;
    private ResourceLocation advancement;
    private Content content;
    private final List<Consumer<JsonObject>> postProcessors;

    public EntryBuilder(String id, ResourceLocation category) {
        this.id = id;
        this.category = category;
        this.name = null;
        this.icon = null;
        this.advancement = null;
        this.content = Content.EMPTY;
        this.postProcessors = new ArrayList<>();
    }

    /**
     * Sets the entry name. This is required.
     */
    public EntryBuilder name(String name) {
        this.name = name;
        return this;
    }
    
    /**
     * Sets the entry icon. This is required.
     */
    public EntryBuilder icon(ItemLike icon) {
        return this.icon(new ItemStack(icon));
    }
    
    /**
     * Sets the entry icon. This is required.
     */
    public EntryBuilder icon(ItemStack icon) {
        this.icon = icon.copy();
        return this;
    }

    /**
     * Sets an advancement needed to unlock the entry. The namespace is set to the namespace of the category, this
     * entry belongs to.
     */
    public EntryBuilder advancement(String path) {
        return this.advancement(this.category.getNamespace(), path);
    }

    /**
     * Sets an advancement needed to unlock the entry.
     */
    public EntryBuilder advancement(String namespace, String path) {
        return this.advancement(new ResourceLocation(namespace, path));
    }

    /**
     * Sets an advancement needed to unlock the entry.
     */
    public EntryBuilder advancement(ResourceLocation advancement) {
        this.advancement = advancement;
        return this;
    }

    /**
     * Adds some text content to this entry.
     */
    public EntryBuilder text(String text) {
        return this.add(new TextContent(text, false));
    }

    /**
     * Adds some caption text content to the entry. Caption text behaves as regular text except when added right
     * after a {@link CaptionContent} in which case the caption text will merge onto the previous content.
     */
    public EntryBuilder caption(String text) {
        return this.add(new TextContent(text, true));
    }
    
    /**
     * Causes a page flip. That means no text after a flip will be put on a page that started before the flip.
     */
    public EntryBuilder flip() {
        return this.add(new FlipContent(null));
    }

    /**
     * Causes a page flip. That means no text after a flip will be put on a page that started before the flip.
     * Also adds an anchor to the next page that is built, that can then be referenced by links inside the book.
     */
    public EntryBuilder flip(String anchor) {
        return this.add(new FlipContent(anchor));
    }

    /**
     * Adds some images to the entry. The image namespaces are set to the namespace of the category, this entry belongs to.
     */
    public EntryBuilder image(String title, String... images) {
        return this.image(title, Arrays.stream(images).map(s -> new ResourceLocation(this.category.getNamespace(), s)).toArray(ResourceLocation[]::new));
    }
    
    /**
     * Adds some images to the entry.
     */
    public EntryBuilder image(String title, ResourceLocation... images) {
        return this.add(new ImageContent(title, List.of(images), null));
    }

    /**
     * Adds a crafting recipe to the entry. If the previous content is a crafting recipe as well, they'll merge together
     * and form a double recipe page.
     */
    public EntryBuilder crafting(String path) {
        return this.crafting(this.category.getNamespace(), path);
    }
    
    /**
     * Adds a crafting recipe to the entry. If the previous content is a crafting recipe as well, they'll merge together
     * and form a double recipe page.
     */
    public EntryBuilder crafting(String namespace, String path) {
        return this.crafting(new ResourceLocation(namespace, path));
    }
    
    /**
     * Adds a crafting recipe to the entry. If the previous content is a crafting recipe as well, they'll merge together
     * and form a double recipe page.
     */
    public EntryBuilder crafting(ResourceLocation id) {
        return this.add(new DoubleRecipePage("patchouli:crafting", 8, id));
    }

    /**
     * Adds a smelting recipe to the entry. If the previous content is a smelting recipe as well, they'll merge together
     * and form a double recipe page.
     */
    public EntryBuilder smelting(String path) {
        return this.smelting(this.category.getNamespace(), path);
    }

    /**
     * Adds a smelting recipe to the entry. If the previous content is a smelting recipe as well, they'll merge together
     * and form a double recipe page.
     */
    public EntryBuilder smelting(String namespace, String path) {
        return this.smelting(new ResourceLocation(namespace, path));
    }

    /**
     * Adds a smelting recipe to the entry. If the previous content is a smelting recipe as well, they'll merge together
     * and form a double recipe page.
     */
    public EntryBuilder smelting(ResourceLocation id) {
        return this.add(new DoubleRecipePage("patchouli:smelting", 4, id));
    }

    /**
     * Adds some spotlight content that displays an item. It will also link the recipe for that item and cause a page flip.
     */
    public EntryBuilder item(ItemLike stack) {
        return this.item(new ItemStack(stack), true);
    }

    /**
     * Adds some spotlight content that displays an item. It will also cause a page flip.
     */
    public EntryBuilder item(ItemLike stack, boolean linkRecipe) {
        return this.item(new ItemStack(stack), linkRecipe);
    }
    
    /**
     * Adds some spotlight content that displays an item. It will also link the recipe for that item and cause a page flip.
     */
    public EntryBuilder item(ItemStack stack) {
        return this.item(stack, true);
    }
    
    /**
     * Adds some spotlight content that displays an item. It will also cause a page flip.
     */
    public EntryBuilder item(ItemStack stack, boolean linkRecipe) {
        return this.add(new SpotlightContent(stack, linkRecipe));
    }

    /**
     * Adds some content to the entry that display the given entity.
     */
    public EntryBuilder entity(EntityType<?> entity) {
        return this.add(new EntityContent(entity));
    }

    /**
     * Adds some content to the entry that display a multiblock.
     * 
     * @param data The multiblock data as recognised by patchouli.
     */
    public EntryBuilder multiblock(String title, String data) {
        return this.add(new MultiblockContent(title, data));
    }

    /**
     * Adds the given content to this entry.
     */
    public EntryBuilder add(Content content) {
        this.content = this.content.with(content);
        return this;
    }
    
    /**
     * Adds a {@link Consumer} that can modify the final json data after it is built.
     */
    public EntryBuilder postProcess(Consumer<JsonObject> postProcessor) {
        this.postProcessors.add(postProcessor);
        return this;
    }
    
    JsonObject build(BiFunction<String, List<String>, String> translations, ExistingFileHelper fileHelper) {
        if (this.name == null) throw new IllegalStateException("Entry name not set: " + this.category + "/" + this.id);
        if (this.icon == null) throw new IllegalStateException("Entry icon not set: " + this.category + "/" + this.id);
        JsonObject json = new JsonObject();
        json.addProperty("name", translations.apply(this.name, List.of("entry", this.category.getNamespace(), this.category.getPath(), this.id)));
        json.addProperty("category", this.category.toString());
        json.add("icon", PageJson.stack(this.icon));
        if (this.advancement != null) {
            json.addProperty("advancement", this.advancement.toString());
        }

        JsonArray pages = new JsonArray();
        PageBuilder builder = new PageBuilder() {
            
            private int page = 0;
            private int key = 0;
            private String anchor = null;

            @Override
            public boolean isFirst() {
                return this.page == 0;
            }

            @Override
            public void addPage(JsonObject page) {
                this.page += 1;
                this.key = 0;
                if (this.anchor != null) {
                    if (page.has("anchor")) {
                        throw new IllegalStateException("Can't add pending anchor '" + this.anchor + "', page already has an anchor set: '" + page.get("anchor").getAsString() + "'");
                    } else {
                        page.addProperty("anchor", this.anchor);
                        this.anchor = null;
                    }
                }
                pages.add(page);
            }

            @Override
            public void addAnchor(String name) {
                if (this.anchor != null) {
                    throw new IllegalStateException("Can't add anchor '" + name + "', already a pending anchor: '" + this.anchor + "'");
                } else {
                    this.anchor = name;
                }
            }

            @Override
            public String translate(String localized) {
                return translations.apply(localized, List.of("entry", EntryBuilder.this.category.getNamespace(), EntryBuilder.this.category.getPath(), EntryBuilder.this.id, "page" + this.page, "text" + (this.key++)));
            }

            @Override
            public void flipToEven() {
                if ((this.page % 2) != 0) {
                    JsonObject json = new JsonObject();
                    json.addProperty("type", "patchouli:empty");
                    this.addPage(json);
                }
            }

            @Override
            public void checkAssets(ResourceLocation path) {
                if (!fileHelper.exists(path, PackType.CLIENT_RESOURCES)) {
                    throw new IllegalStateException("Resource " + path + " does not exist.");
                }
            }
        };
        
        this.content.pages(builder);
        json.add("pages", pages);
        
        for (Consumer<JsonObject> postProcessor : this.postProcessors) {
            postProcessor.accept(json);
        }
        
        return json;
    }
}
