package org.moddingx.libx.datagen.provider.patchouli.page;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.datagen.provider.patchouli.BookProperties;

/**
 * An interface that defines methods, instances of {@link Content} can use to build book pages.
 */
public interface PageBuilder {

    /**
     * Gets whether the next added page will be the first page of an entry.
     */
    boolean isFirst();

    /**
     * Adds a new page from the given json data.
     */
    void addPage(JsonObject page);

    /**
     * Adds a new floating anchor. A floating anchor is added to the next page that is added via {@link #addPage(JsonObject)}.
     * There may only ever be one floating anchor at a time and the next page after a floating anchor may not add an anchor itself.
     */
    void addAnchor(String name);

    /**
     * Gets a string that should be used in the book for the given text. Depending on the {@link BookProperties properties}, this
     * will either register a translation and return the translation key or will return the text itself.
     */
    String translate(String localized);

    /**
     * Inserts an empty page, if necessary, so the next added page will have an even page number.
     */
    void flipToEven();

    /**
     * Checks that a given asset exists, Throws an exception if not.
     */
    void checkAssets(ResourceLocation path);
}
