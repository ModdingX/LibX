package io.github.noeppi_noeppi.libx.impl.datapack;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.fmllegacy.packs.ModFileResourcePack;
import net.minecraftforge.forgespi.locating.IModFile;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LibXDatapack extends ModFileResourcePack {

    public static final String PREFIX = "libxdata";
    public static final int PACK_VERSION = 7;
    
    private final IModFile modFile;
    private final String packId;
    private final byte[] packMcmeta;

    public LibXDatapack(IModFile modFile, String packId) {
        super(modFile);
        this.modFile = modFile;
        this.packId = packId;
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(bout);
            JsonObject packFile = new JsonObject();
            JsonObject packSection = new JsonObject();
            packSection.addProperty("description", "Dynamic Datapack: " + modFile.getFileName() + "/" + packId);
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

    @Nonnull
    @Override
    public String getName() {
        return this.modFile.getFileName() + "/" + this.packId;
    }

    @Nonnull
    @Override
    protected InputStream getResource(@Nonnull String name) throws IOException {
        if (name.equals("pack.mcmeta")) {
            return new ByteArrayInputStream(this.packMcmeta);
        } else {
            return super.getResource(PREFIX + "/" + this.packId + "/" + name);
        }
    }

    @Override
    protected boolean hasResource(@Nonnull String name) {
        if (name.equals("pack.mcmeta")) {
            return true;
        } else {
            return super.hasResource(PREFIX + "/" + this.packId + "/" + name);
        }
    }

    @Nonnull
    @Override
    public Collection<ResourceLocation> getResources(PackType type, @Nonnull String namespace, @Nonnull String path, int maxDepth, @Nonnull Predicate<String> filter) {
        try {
            Path root = this.modFile.findResource(PREFIX, this.packId, type.getDirectory()).toAbsolutePath();
            Path comparingPath = root.getFileSystem().getPath(path);
            return Files.walk(root)
                    .map(p -> root.relativize(p.toAbsolutePath()))
                    .filter(p -> p.getNameCount() > 1 && p.getNameCount() <= (maxDepth == Integer.MAX_VALUE ? maxDepth : maxDepth + 1))
                    .filter(p -> !p.getFileName().toString().endsWith(".mcmeta"))
                    .filter(p -> p.subpath(1, p.getNameCount()).startsWith(comparingPath))
                    .filter(p -> filter.test(p.getFileName().toString()))
                    .map(p -> {
                        Path rlPath = p.subpath(1, Math.min(maxDepth, p.getNameCount()));
                        String pathName = rlPath.toString().replace(File.separator, "/");
                        return new ResourceLocation(p.getName(0).toString(), pathName);
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    @Nonnull
    @Override
    public Set<String> getNamespaces(PackType type) {
        try {
            Path root = this.modFile.findResource(PREFIX, this.packId, type.getDirectory()).toAbsolutePath();
            return Files.walk(root,1)
                    .map(path -> root.relativize(path.toAbsolutePath()))
                    .filter(path -> path.getNameCount() > 0)
                    .map(Path::toString)
                    .map(p -> p.endsWith(File.separator) ? p.substring(0, p.length() - File.separator.length()) : p)
                    .map(p -> p.endsWith("/") ? p.substring(0, p.length() - 1) : p)
                    .filter(p -> !p.isEmpty())
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            return Collections.emptySet();
        }
    }
}
