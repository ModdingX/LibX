package org.moddingx.libx.datapack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.forgespi.locating.IModFile;
import org.moddingx.libx.impl.datapack.LibXDatapack;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

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
        return key.location().getNamespace() + "/" + registryPart + "/" + key.location().getPath() + ".json";
    }
    
    /**
     * Creates a supplier that can be repeatedly called to create new {@link InputStream}s for
     * a dynamically generated {@code pack.mcmeta} based on the given mod file.
     */
    public static IoSupplier<InputStream> generatePackMeta(IModFile file, String description, PackType packType) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(bout, StandardCharsets.UTF_8);
            JsonObject packFile = new JsonObject();
            JsonObject packSection = new JsonObject();
            packSection.addProperty("description", description);
            packSection.addProperty("pack_format", getPackFormat(file, packType));
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
    
    private static int getPackFormat(IModFile mod, PackType packType) {
        try {
            Path path = mod.findResource("pack.mcmeta");
            if (!Files.exists(path)) return LibXDatapack.PACK_VERSION;
            try (Reader in = Files.newBufferedReader(path)) {
                JsonObject packInfo = GSON.fromJson(in, JsonObject.class).get("pack").getAsJsonObject();
                String specificKey = "forge:" + packType.bridgeType.name().toLowerCase(Locale.ROOT) + "_pack_format";
                if (packInfo.has(specificKey)) return packInfo.get(specificKey).getAsInt();
                return packInfo.get("pack_format").getAsInt();
            }
        } catch (Exception e) {
            return LibXDatapack.PACK_VERSION;
        }
    }
}
