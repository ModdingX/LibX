package org.moddingx.libx.util;

import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;

/**
 * Some miscellaneous stuff that does not fit anywhere else.
 */
public class Misc {

    /**
     * This identifier should be used as a placeholder / invalid value
     * Its value is {@code minecraft:missigno}.
     * The reason for {@code minecraft:missigno} is that minecraft uses this identifier
     * on its own. See for example {@link MissingTextureAtlasSprite}.
     */
    public static final Identifier MISSINGNO = Identifier.withDefaultNamespace("missingno");
}
