package org.moddingx.libx.impl.datagen_old.patchouli.content;

import org.moddingx.libx.datagen.provider.patchouli.page.Content;
import org.moddingx.libx.datagen.provider.patchouli.page.PageBuilder;

import javax.annotation.Nullable;

// Adds nothing but does a page flip.
public record FlipContent(@Nullable String anchor) implements Content {

    @Override
    public void pages(PageBuilder builder) {
        if (this.anchor() != null) {
            builder.addAnchor(this.anchor());
        }
    }
}
