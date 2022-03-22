package io.github.noeppi_noeppi.libx.impl.datapack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathResourcePack;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class LibXDatapack extends PathResourcePack {

    public static final int PACK_VERSION = 9;
    public static final String PREFIX = "libxdata";

    private static final Gson GSON = Util.make(() -> {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        return builder.create();
    });

    private final String packId;
    private final byte[] packMcmeta;

    public LibXDatapack(IModFile mod, String packId) {
        // Get the base part of the mod in there and the override resolve
        super(mod.getFileName() + "/" + packId, mod.findResource(PREFIX));
        this.packId = packId;
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(bout);
            JsonObject packFile = new JsonObject();
            JsonObject packSection = new JsonObject();
            packSection.addProperty("description", "Dynamic Datapack: " + mod.getFileName() + "/" + packId);
            packSection.addProperty("pack_format", getPackFormat(mod));
            packFile.add("pack", packSection);
            writer.write(GSON.toJson(packFile) + "\n");
            writer.close();
            bout.close();
            this.packMcmeta = bout.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create dynamic datapack", e);
        }
    }
    
    private static int getPackFormat(IModFile mod) {
        try {
            Path path = mod.findResource("pack.mcmeta");
            if (!Files.exists(path)) return PACK_VERSION;
            try (Reader in = Files.newBufferedReader(path)) {
                JsonObject packInfo = GSON.fromJson(in, JsonObject.class);
                return packInfo.get("pack").getAsJsonObject().get("pack_format").getAsInt();
            }
        } catch (Exception e) {
            return PACK_VERSION;
        }
    }
    
    @Override
    protected boolean hasResource(@Nonnull String name) {
        return name.equals(PACK_META) || super.hasResource(name);
    }
    
    @Override
    public boolean hasResource(@Nonnull PackType type, @Nonnull ResourceLocation location) {
        return this.resourceValid(type, location) && super.hasResource(type, location);
    }

    @Nonnull
    @Override
    protected InputStream getResource(@Nonnull String name) throws IOException {
        return name.equals(PACK_META) ? new ByteArrayInputStream(this.packMcmeta) : super.getResource(name);
    }

    @Nonnull
    @Override
    public InputStream getResource(@Nonnull PackType type, @Nonnull ResourceLocation location) throws IOException {
        if (this.resourceValid(type, location)) {
            return super.getResource(type, location);
        } else {
            throw new FileNotFoundException(type.getDirectory() + "/" + location.getNamespace() + "/" + location.getPath() + "@" + this.getName());
        }
    }

    private boolean resourceValid(PackType type, ResourceLocation location) {
        return type == PackType.SERVER_DATA || location.getPath().startsWith("lang/");
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Nonnull
    @Override
    protected Path resolve(@Nonnull String... pathParts) {
        String pathStr = switch (pathParts.length) {
            case 0 -> "";
            case 1 -> pathParts[0];
            default -> {
                StringBuilder sb = new StringBuilder();
                for (String pathPart : pathParts) sb.append("/").append(pathPart);
                yield sb.toString();
            }
        };
        while (pathStr.contains("//")) pathStr = pathStr.replace("//", "/");
        if (pathStr.startsWith("/")) pathStr = pathStr.substring(1);
        if (pathStr.endsWith("/")) pathStr = pathStr.substring(0, pathStr.length() - 1);
        String[] paths = pathStr.split("/");
        if (paths.length == 0) return this.getSource().resolve(this.packId);
        Path path = switch (paths[0]) {
            case PACK_META -> this.getSource();
            case "data" -> this.getSource().resolve(this.packId);
            default -> this.getSource().resolve(this.packId).resolve("SNOWBALL");
        };
        for (int i = 1; i < paths.length; i++) path = path.resolve(paths[i]);
        return path;
    }
}
