package org.moddingx.libx.datagen.provider.patchouli;

import org.jetbrains.annotations.Nullable;

/**
 * Basic properties for a patchouli book.
 * 
 * @param namespace The namespace of the book if it differs from the mods namespace.
 * @param bookName  The book name.
 * @param translate Whether the book is translatable. If this is {@code true}, a language ile for {@code en_us} is generated
 *                  in a different namespace ({@code modid_bookname}), so it does not clash with the main language file when the
 *                  jar is built.
 */
public record BookProperties(@Nullable String namespace, String bookName, boolean translate) {

    public BookProperties(String bookName) {
        this(null, bookName, false);
    }
    
    public BookProperties(String bookName, boolean translate) {
        this(null, bookName, translate);
    }
    
    public BookProperties(@Nullable String namespace, String bookName) {
        this(namespace, bookName, false);
    }
}
