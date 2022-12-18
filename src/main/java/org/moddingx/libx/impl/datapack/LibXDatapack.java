package org.moddingx.libx.impl.datapack;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathPackResources;
import org.moddingx.libx.datapack.DatapackHelper;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.nio.file.Path;

public class LibXDatapack extends PathPackResources {

    public static final int PACK_VERSION = 10;
    public static final String PREFIX = "libxdata";

    private final String packId;
    private final IoSupplier<InputStream> packMcmeta;

    public LibXDatapack(IModFile mod, String packId) {
        // Get the base part of the mod in there and the override resolve
        super(mod.getFileName() + "/" + packId, true, mod.findResource(PREFIX));
        this.packId = packId;
        this.packMcmeta = DatapackHelper.generatePackMeta(mod, "Dynamic Datapack: " + mod.getFileName() + "/" + packId, PackType.SERVER_DATA);
    }

    @Override
    public IoSupplier<InputStream> getRootResource(@Nonnull String... names) {
        return names[0].equals(PACK_META) ? this.packMcmeta : super.getRootResource(names);
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
