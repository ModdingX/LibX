package io.github.noeppi_noeppi.libx.datapack;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    
    private static final Set<ResourceLocation> enabledPacks = new HashSet<>();

    /**
     * Enables a dynamic datapack.
     */
    public static void enablePack(String modId, String packName) {
        throw new IllegalStateException("Dynamic datapacks are currenty broken. See https://github.com/MinecraftForge/securejarhandler/pull/4");
        // enabledPacks.add(new ResourceLocation(modId, packName));
    }

    /**
     * Gets all enabled dynamic datapacks.
     */
    // TODO add datapack finder for this
    public static Set<ResourceLocation> getEnabledPacks() {
        return Collections.unmodifiableSet(enabledPacks);
    }
}
