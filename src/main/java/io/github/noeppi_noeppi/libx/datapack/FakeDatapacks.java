package io.github.noeppi_noeppi.libx.datapack;

import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This allows you to have multiple datapacks in your mod file. While the main one is always loaded, you
 * can load the other ones via {@link FakeDatapacks#enablePack(String, String)}. This should be called in
 * your mods constructor.
 * A fake datapack must be located in {@code libxdata/[name]} inside your JAR file where {@code [name]} is
 * the name of your fake datapack. Inside this folder you can have an `assets` and / or `data` directory.
 * A {@code pack.mcmeta} file is not required and if you provide one it'll be ignored.
 */
public class FakeDatapacks {
    
    private static final Set<ResourceLocation> enabledPacks = new HashSet<>();

    /**
     * Enables a fake datapack.
     */
    public static void enablePack(String modId, String packName) {
        enabledPacks.add(new ResourceLocation(modId, packName));
    }

    /**
     * Gets all enabled fake datapacks.
     */
    public static Set<ResourceLocation> getEnabledPacks() {
        return Collections.unmodifiableSet(enabledPacks);
    }
}
