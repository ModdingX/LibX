package org.moddingx.libx.datapack;

import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.impl.datapack.DynamicPackLocator;

/**
 * This allows you to have multiple resource and datapacks in your mod file. While the main one is always loaded, you
 * can load the other ones via {@link DynamicPacks#enablePack(String, String)}. This should be called in your mods
 * constructor.
 * 
 * A dynamic resource pack must be located in {@code libxassets/[name]} inside your JAR file where {@code [name]} is
 * the name of your dynamic resource pack. Inside this folder you can put the content that normally is in the
 * {@code assets} folder. The resource pack is shown in the resource pack selection menu and can be activated based
 * on the users preference.
 * A {@code pack.mcmeta} file is not required and if you provide one it'll be ignored.
 * 
 * A dynamic datapack must be located in {@code libxdata/[name]} inside your JAR file where {@code [name]} is
 * the name of your dynamic datapack. Inside this folder you can put the content that normally is in the
 * {@code data} folder. The datapack is automatically loaded without the need for the user to select it.
 * A {@code pack.mcmeta} file is not required and if you provide one it'll be ignored.
 * 
 * A dynamic pack may contain a file named {@code description.txt} on the root level to set its description.
 */
public class DynamicPacks {

    public static final DynamicPacks RESOURCE_PACKS = new DynamicPacks(DynamicPackLocator.RESOURCE_PACKS);
    public static final DynamicPacks DATA_PACKS = new DynamicPacks(DynamicPackLocator.DATA_PACKS);
    
    private final DynamicPackLocator locator;

    private DynamicPacks(DynamicPackLocator locator) {
        this.locator = locator;
    }

    /**
     * Enables a dynamic pack.
     */
    public void enablePack(String modId, String packName) {
        this.locator.enablePack(new ResourceLocation(modId, packName));
    }

    /**
     * Gets tests whether a dynamic pack is enabled.
     */
    public boolean isEnabled(String modId, String packName) {
        return this.locator.isEnabled(new ResourceLocation(modId, packName));
    }
}
