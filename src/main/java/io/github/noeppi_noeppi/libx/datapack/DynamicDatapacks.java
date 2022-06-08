package io.github.noeppi_noeppi.libx.datapack;

import io.github.noeppi_noeppi.libx.impl.datapack.DynamicDatapackLocator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraftforge.forgespi.locating.IModFile;

/**
 * This allows you to have multiple datapacks in your mod file. While the main one is always loaded, you
 * can load the other ones via {@link DynamicDatapacks#enablePack(String, String)}. This should be called in
 * your mods constructor.
 * A dynamic datapack must be located in {@code libxdata/[name]} inside your JAR file where {@code [name]} is
 * the name of your dynamic datapack. Inside this folder you can put the content that normally is in the
 * {@code data} folder.
 * A {@code pack.mcmeta} file is not required and if you provide one it'll be ignored.
 */
public class DynamicDatapacks {

    /**
     * Enables a dynamic datapack.
     */
    public static void enablePack(String modId, String packName) {
        DynamicDatapackLocator.enablePack(new ResourceLocation(modId, packName), null);
    }
    
    /**
     * Enables a dynamic datapack. Here a custom {@link PackFactory} can be provided to
     * provide an entirely custom datapack
     */
    public static void enablePack(String modId, String packName, PackFactory factory) {
        DynamicDatapackLocator.enablePack(new ResourceLocation(modId, packName), factory);
    }

    /**
     * Gets tests whether a dynamic datapack is enabled.
     */
    public static boolean isEnabled(String modId, String packName) {
        return DynamicDatapackLocator.isEnabled(new ResourceLocation(modId, packName));
    }

    /**
     * Interface to create a dynamic datapack.
     */
    @FunctionalInterface
    public interface PackFactory {

        /**
         * Creates the {@link PackResources} from a given {@link IModFile mod file} and name.
         */
        PackResources create(IModFile modFile, String id);
    }
}
