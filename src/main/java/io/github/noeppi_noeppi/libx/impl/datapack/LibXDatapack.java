package io.github.noeppi_noeppi.libx.impl.datapack;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.forgespi.locating.IModFile;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LibXDatapack extends AbstractPackResources {

    public static final String PREFIX = "libxdata";
    public static final int PACK_VERSION = 7;
    
    private final IModFile mod;
    private final String packId;
    private final byte[] packMcmeta;

    public LibXDatapack(IModFile mod, String packId) {
        super(new File("dummy"));
        this.mod = mod;
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

    @Nonnull
    @Override
    public String getName() {
        return this.mod.getFileName() + "/" + this.packId;
    }

    @Nonnull
    @Override
    public Set<String> getNamespaces(@Nonnull PackType type) {
        return switch (type) {
            case CLIENT_RESOURCES -> Set.of();
            case SERVER_DATA -> {
                Path root = this.mod.findResource(PREFIX, this.packId);
                if (!Files.exists(root)) yield Set.of();
                try {
                    yield Set.copyOf(Files.list(root)
                            .filter(Files::isDirectory)
                            .map(root::relativize)
                            .filter(p -> p.getNameCount() == 1)
                            .map(Path::getFileName)
                            .map(Path::toString)
                            .toList());
                } catch (IOException e) {
                    yield Set.of();
                }
            }
        };
    }

    @Override
    protected boolean hasResource(@Nonnull String name) {
        return name.equals(PACK_META) || Files.exists(this.mod.findResource(PREFIX, this.packId, name));
    }
    
    @Override
    public boolean hasResource(@Nonnull PackType type, @Nonnull ResourceLocation location) {
        return switch (type) {
            case CLIENT_RESOURCES -> false;
            case SERVER_DATA -> this.hasResource(location.getNamespace() + "/" + location.getPath());
        };
    }

    @Nonnull
    @Override
    protected InputStream getResource(@Nonnull String name) throws IOException {
        if (name.equals(PACK_META)) {
            return new ByteArrayInputStream(this.packMcmeta);
        } else {
            Path path = this.mod.findResource(PREFIX, this.packId, name);
            if (!Files.exists(path)) throw new FileNotFoundException("Resource " + name + " not found in dynamic datapack " + this.getName());
            return Files.newInputStream(path);
        }
    }

    @Nonnull
    @Override
    public InputStream getResource(@Nonnull PackType type, @Nonnull ResourceLocation location) throws IOException {
        return switch (type) {
            case CLIENT_RESOURCES -> throw new FileNotFoundException("Dynamic datapack can't contain client resources: " + location + " not found in " + this.getName());
            case SERVER_DATA -> this.getResource(location.getNamespace() + "/" + location.getPath());
        };
    }

    @Nonnull
    @Override
    public Collection<ResourceLocation> getResources(@Nonnull PackType type, @Nonnull String namespace, @Nonnull String path, int maxDepth, @Nonnull Predicate<String> filter) {
        return switch (type) {
            case CLIENT_RESOURCES -> Set.of();
            case SERVER_DATA -> {
                Path root = this.mod.findResource(PREFIX, this.packId);
                try {
                    yield Set.copyOf(Files.walk(root, maxDepth + 1)
                            .map(root::relativize)
                            .map(Path::normalize)
                            .filter(p -> p.getNameCount() > 1 && p.getNameCount() + 1 <= maxDepth)
                            .filter(p -> p.getName(0).toString().equals(namespace))
                            .filter(p -> p.startsWith(path))
                            .flatMap(p -> this.resourcePath(namespace, p))
                            .toList());
                } catch (IOException e) {
                    yield Set.of();
                }
            }
        };
    }

    private Stream<ResourceLocation> resourcePath(String namespace, Path path) {
        Path p = path.subpath(1, path.getNameCount());
        String pathStr = IntStream.range(0, p.getNameCount())
                .mapToObj(idx -> p.getName(idx).toString())
                .collect(Collectors.joining("/"));
        if (ResourceLocation.isValidPath(pathStr)) {
            return Stream.of(new ResourceLocation(namespace, pathStr));
        } else {
            return Stream.empty();
        }
    }
    
    @Override
    public void close() {
        //
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}
