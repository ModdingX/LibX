package org.moddingx.libx.datagen.provider.patchouli.content;

import org.moddingx.libx.datagen.provider.patchouli.page.Content;
import org.moddingx.libx.datagen.provider.patchouli.page.PageBuilder;
import org.moddingx.libx.datagen.provider.patchouli.page.PageJson;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Base class for pages with some special content that can be followed by some text. This class will handle
 * merging with other caption content and splitting the text up on multiple pages if required.
 */
public abstract class CaptionContent implements Content {
    
    @Nullable
    protected final String caption;

    protected CaptionContent(@Nullable String caption) {
        this.caption = caption;
    }

    /**
     * Gets the amount of lines, that should be skipped for the special content.
     */
    protected abstract int lineSkip();

    /**
     * Creates a copy of this {@link CaptionContent} with a new caption set.
     */
    protected abstract CaptionContent withCaption(String caption);

    /**
     * Builds the special page for this {@link CaptionContent}.
     * 
     * @param caption The text for that special page. All other text is added to regular text pages after this one.
     */
    protected abstract void specialPage(PageBuilder builder, @Nullable String caption);

    /**
     * Gets whether regular (non-caption) text can merge to this {@link CaptionContent}. Default is {@code false}.
     */
    protected boolean canTakeRegularText() {
        return false;
    }
    
    @Override
    public void pages(PageBuilder builder) {
        List<String> pages = this.caption == null ? List.of() : PageJson.splitText(this.caption, this.lineSkip());
        if (pages.isEmpty()) {
            this.specialPage(builder, null);
        } else if (pages.size() == 1) {
            this.specialPage(builder, pages.get(0));
        } else {
            this.specialPage(builder, pages.get(0));
            TextContent.addTextPages(builder, pages, false);
        }
    }

    @Override
    public Content with(Content next) {
        if (next instanceof TextContent tc && (this.canTakeRegularText() || tc.caption())) {
            return this.withCaption(this.caption == null ? tc.text() : this.caption + " " + tc.text());
        } else {
            return Content.super.with(next);
        }
    }
}
