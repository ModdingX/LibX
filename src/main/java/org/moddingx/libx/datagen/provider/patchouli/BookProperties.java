package org.moddingx.libx.datagen.provider.patchouli;

import net.minecraft.server.packs.PackType;

/**
 * Basic properties for a patchouli book.
 * 
 * @param bookName The name of the book.
 * @param packTarget The target, where the book should be generated. If {@code use_resource_pack} is set in {@code book.json},
 *                   this should be {@link PackType#CLIENT_RESOURCES}, if not {@link PackType#SERVER_DATA}.
 * @param translate Whether the book is translatable. If this is {@code true}, a language ile for {@code en_us} is generated
 *                  in a different namespace ({@code modid_bookname}), so it does not clash with the main language file when the
 *                  jar is built.
 */
public record BookProperties(String bookName, PackType packTarget, boolean translate) {

    public BookProperties(String name) {
        this(name, PackType.SERVER_DATA, false);
    }
    
    public BookProperties(String name, PackType packTarget) {
        this(name, packTarget, false);
    }
    public BookProperties(String name, boolean translate) {
        this(name, PackType.SERVER_DATA, translate);
    }
}
