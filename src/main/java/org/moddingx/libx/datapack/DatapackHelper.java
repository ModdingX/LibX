package org.moddingx.libx.datapack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.Util;
import org.moddingx.libx.impl.datapack.LibXPack;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Adds some utilities for creating custom dynamic datapacks.
 */
public class DatapackHelper {

    public static final Gson GSON = Util.make(() -> {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        return builder.create();
    });
    
    /**
     * Gets the path for a registry element inside a datapack. For example for the key
     * {@code minecraft:worldgen/biome libx:some_biome}, this would be {@code libx/worldgen/biome/some_biome.json}
     */
    public static String registryPath(ResourceKey<?> key) {
        String registryPart;
        if (key.registry().getNamespace().equals("minecraft")) {
            registryPart = key.registry().getPath();
        } else {
            registryPart = key.registry().getNamespace() + "/" + key.registry().getPath();
        }
        return key.identifier().getNamespace() + "/" + registryPart + "/" + key.identifier().getPath() + ".json";
    }
    
    /**
     * Creates a supplier that can be repeatedly called to create new {@link InputStream}s for
     * a dynamically generated {@code pack.mcmeta}.
     */
    public static IoSupplier<InputStream> generatePackMeta(String description, PackType packType) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(bout, StandardCharsets.UTF_8);
            JsonObject packFile = new JsonObject();
            JsonObject packSection = new JsonObject();
            packSection.addProperty("description", description);
            packSection.addProperty("pack_format", LibXPack.PACK_CONFIG.get(packType).version());
            packFile.add("pack", packSection);
            writer.write(GSON.toJson(packFile) + "\n");
            writer.close();
            bout.close();
            byte[] data = bout.toByteArray();
            return () -> new ByteArrayInputStream(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create dynamic pack.mcmeta", e);
        }
    }
}
