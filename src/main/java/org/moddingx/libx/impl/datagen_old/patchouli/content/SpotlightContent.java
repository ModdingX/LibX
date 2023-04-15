package org.moddingx.libx.impl.datagen_old.patchouli.content;

import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import org.moddingx.libx.datagen.provider.patchouli.content.CaptionContent;
import org.moddingx.libx.datagen.provider.patchouli.page.PageBuilder;
import org.moddingx.libx.datagen.provider.patchouli.page.PageJson;

import javax.annotation.Nullable;

public class SpotlightContent extends CaptionContent {

    private final ItemStack stack;
    private final boolean recipe;
    
    public SpotlightContent(ItemStack stack, boolean recipe) {
        this(stack, recipe, null);
    }
    
    private SpotlightContent(ItemStack stack, boolean recipe, @Nullable String caption) {
        super(caption);
        this.stack = stack.copy();
        this.recipe = recipe;
    }

    @Override
    protected int lineSkip() {
        return 4;
    }

    @Override
    protected boolean canTakeRegularText() {
        return true;
    }

    @Override
    protected CaptionContent withCaption(String caption) {
        return new SpotlightContent(this.stack, this.recipe, caption);
    }

    @Override
    protected void specialPage(PageBuilder builder, @Nullable String caption) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "patchouli:spotlight");
        json.add("item", PageJson.stack(this.stack));
        json.addProperty("link_recipe", this.recipe);
        if (caption != null) {
            json.addProperty("text", builder.translate(caption));
        }
        builder.addPage(json);
    }
}
