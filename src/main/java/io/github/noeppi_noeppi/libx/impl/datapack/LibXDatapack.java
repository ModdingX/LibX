package io.github.noeppi_noeppi.libx.impl.datapack;

import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathResourcePack;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Path;

public class LibXDatapack extends PathResourcePack {

    public static final int PACK_VERSION = 9;
    public static final String PREFIX = "libxdata";

    private final String packId;
    private final byte[] packMcmeta;

    public LibXDatapack(IModFile mod, String packId) {
        // Get the base part of the mod in there and the noverride resolve
        super(mod.getFileName() + "/" + packId, validatePath(mod.findResource(PREFIX)));
        this.packId = packId;
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(bout);
            JsonObject packFile = new JsonObject();
            JsonObject packSection = new JsonObject();
            packSection.addProperty("description", "Dynamic Datapack: " + mod.getFileName() + "/" + packId);
            packSection.addProperty("pack_format", PACK_VERSION);
            packFile.add("pack", packSection);
            //noinspection UnnecessaryToStringCall
            writer.write(packFile.toString() + "\n");
            writer.close();
            bout.close();
            this.packMcmeta = bout.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create dynamic datapack", e);
        }
    }
    
    // Deprecated, so it is removed in 1.19 when sjh is hopefully fixed.
    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.19")
    @SuppressWarnings("DeprecatedIsStillUsed")
    private static Path validatePath(Path path) {
        // cpw said everything would be open. Now sjh isn't.
        // Well reflection does the trick.
        if ("cpw.mods.niofs.union.UnionPath".equals(path.getClass().getName())) {
            LibX.logger.warn("A LibX datapack was created with a UnionPath. These are currently buggy. See https://github.com/MinecraftForge/securejarhandler/pull/4");
            // hacky workaround that should keep things working:
            String pathStr = path.toString();
            if (pathStr.startsWith("/") || pathStr.startsWith(File.separator)) {
                return path.getFileSystem().getPath(pathStr.substring(1));
            } else {
                return path;
            }
        } else {
            return path;
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
