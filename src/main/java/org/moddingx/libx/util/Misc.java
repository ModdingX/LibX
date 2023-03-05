package org.moddingx.libx.util;

import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/**
 * Some miscellaneous stuff that does not fit anywhere else.
 */
public class Misc {

    /**
     * This resource location should be used as a placeholder / invalid value
     * It's value is {@code minecraft:missigno}.
     * The reason for {@code minecraft:missigno} is that minecraft uses this resource location
     * on it's own. See for example {@link MissingTextureAtlasSprite}.
     */
    public static final ResourceLocation MISSIGNO = new ResourceLocation("minecraft", "missingno");
}
