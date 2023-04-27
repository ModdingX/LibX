package org.moddingx.libx.datagen.provider.patchouli.content;

import com.google.gson.JsonObject;
import org.moddingx.libx.datagen.provider.patchouli.page.Content;
import org.moddingx.libx.datagen.provider.patchouli.page.PageBuilder;
import org.moddingx.libx.datagen.provider.patchouli.page.PageJson;

import java.util.List;

/**
 * Content that generates one or multiple text pages and automatically wraps the text. Multiple text
 * contents that are added after each other will merge to a single one.
 * 
 * @param caption Whether this {@link TextContent} is a caption content.
 */
public record TextContent(String text, boolean caption) implements Content {

    @Override
    public void pages(PageBuilder builder) {
        List<String> pages = PageJson.splitText(this.text(), builder.isFirst());
        addTextPages(builder, pages, true);
    }

    @Override
    public Content with(Content next) {
        if (next instanceof TextContent tc && (!this.caption() || tc.caption())) {
            return new TextContent(this.text() + " " + tc.text(), this.caption());
        } else {
            return Content.super.with(next);
        }
    }

    public static void addTextPages(PageBuilder builder, List<String> pages, boolean includeFirst) {
        for (String page : (includeFirst ? pages : pages.stream().skip(1).toList())) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "patchouli:text");
            json.addProperty("text", builder.translate(page));
            builder.addPage(json);
        }
    }
}
