package io.github.noeppi_noeppi.libx.util;

import net.minecraft.util.ResourceLocation;

/**
 * Some miscellaneous stuff that does not fit anywhere else.
 */
public class Misc {

    /**
     * This resource location should be used as a placeholder / invalid  value
     * This is {@code minecraft:missigno}
     * The reason for {@code minecraft:missigno} is that minecraft uses this resource location
     * on it's own. See for example MissingTextureSprite.
     */
    public static final ResourceLocation MISSIGNO = new ResourceLocation("minecraft", "missingno");
}
