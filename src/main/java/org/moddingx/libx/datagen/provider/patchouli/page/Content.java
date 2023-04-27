package org.moddingx.libx.datagen.provider.patchouli.page;

import org.moddingx.libx.datagen.provider.patchouli.EntryBuilder;
import org.moddingx.libx.impl.datagen.patchouli.content.CompositeContent;

import java.util.List;

/**
 * Defines some content for a {@link EntryBuilder patchouli book entry}.
 * A content is something that can create zero, one or multiple actual book pages, when built.
 * Each book entry will only ever have one single {@link Content}. When new content is added to the entry,
 * it is chained to the previous content using {@link #with(Content)}.
 * Subclasses may override this to allow for content to merge on the same page. For example, when a recipe
 * content is merged with some caption content, both of them will result in a single content that adds a
 * recipe page with text.
 */
public interface Content {

    /**
     * An empty content that does nothing.
     */
    Content EMPTY = new CompositeContent(List.of());

    /**
     * Generates some pages for this content.
     */
    void pages(PageBuilder builder);

    /**
     * Chains a new content with this one. The default implementation returns a content, that just calls
     * both {@link #pages(PageBuilder)} methods after each other.
     */
    default Content with(Content next) {
        return new CompositeContent(List.of(this, next));
    }
}
