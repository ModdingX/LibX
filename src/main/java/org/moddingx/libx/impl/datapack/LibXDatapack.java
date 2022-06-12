package org.moddingx.libx.impl.datapack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathResourcePack;
import org.moddingx.libx.datapack.DatapackHelper;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Supplier;

public class LibXDatapack extends PathResourcePack {

    public static final int PACK_VERSION = 9;
    public static final String PREFIX = "libxdata";
    
    private final String packId;
    private final Supplier<InputStream> packMcmeta;

    public LibXDatapack(IModFile mod, String packId) {
        // Get the base part of the mod in there and the override resolve
        super(mod.getFileName() + "/" + packId, mod.findResource(PREFIX));
        this.packId = packId;
        this.packMcmeta = DatapackHelper.generatePackMeta(mod, "Dynamic Datapack: " + mod.getFileName() + "/" + packId);
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
        return name.equals(PACK_META) ? this.packMcmeta.get() : super.getResource(name);
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
