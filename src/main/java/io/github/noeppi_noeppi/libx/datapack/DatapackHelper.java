package io.github.noeppi_noeppi.libx.datapack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.libx.impl.datapack.LibXDatapack;
import net.minecraft.Util;
import net.minecraftforge.forgespi.locating.IModFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

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
     * Creates a supplier that can be repeatedly called to create new {@link InputStream}s for
     * a dynamically generated {@code pack.mcmeta} based on the given mod file.
     */
    public static Supplier<InputStream> generatePackMeta(IModFile file, String description) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(bout, StandardCharsets.UTF_8);
            JsonObject packFile = new JsonObject();
            JsonObject packSection = new JsonObject();
            packSection.addProperty("description", description);
            packSection.addProperty("pack_format", getPackFormat(file));
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
    
    private static int getPackFormat(IModFile mod) {
        try {
            Path path = mod.findResource("pack.mcmeta");
            if (!Files.exists(path)) return LibXDatapack.PACK_VERSION;
            try (Reader in = Files.newBufferedReader(path)) {
                JsonObject packInfo = GSON.fromJson(in, JsonObject.class);
                return packInfo.get("pack").getAsJsonObject().get("pack_format").getAsInt();
            }
        } catch (Exception e) {
            return LibXDatapack.PACK_VERSION;
        }
    }
}
