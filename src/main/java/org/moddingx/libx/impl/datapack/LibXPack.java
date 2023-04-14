package org.moddingx.libx.impl.datapack;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathPackResources;
import org.moddingx.libx.datapack.DatapackHelper;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class LibXPack extends PathPackResources {

    public static final Map<PackType, PackTypeConfig> PACK_CONFIG = Map.of(
            PackType.CLIENT_RESOURCES, new PackTypeConfig(PackSource.BUILT_IN, "libxassets", 13),
            PackType.SERVER_DATA, new PackTypeConfig(PackSource.DEFAULT, "libxdata", 12)
    );
    
    private final String packId;
    private final PackType type;
    private final LazyValue<IoSupplier<InputStream>> packMcmeta;

    public LibXPack(IModFile mod, PackType type, String packId) {
        // Get the base part of the mod in there and the override resolve
        super(mod.getFileName() + "/" + packId, true, mod.findResource(PACK_CONFIG.get(type).prefix()));
        this.packId = packId;
        this.type = type;
        this.packMcmeta = new LazyValue<>(() -> {
            String description = "Dynamic " + type.getDirectory() + ": " + mod.getFileName() + "/" + packId;
            try {
                Path descPath = this.getSource().resolve(this.packId).resolve("description.txt");
                if (Files.isRegularFile(descPath)) description = Files.readString(descPath, StandardCharsets.UTF_8).strip();
            } catch (Exception e) {
                //
            }
            return DatapackHelper.generatePackMeta(mod, description, type);
        });
    }

    @Override
    public IoSupplier<InputStream> getRootResource(@Nonnull String... names) {
        return names[0].equals(PACK_META) ? this.packMcmeta.get() : super.getRootResource(names);
    }

    @Override
    public boolean isHidden() {
        return this.type == PackType.SERVER_DATA;
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
            case "pack.png" -> this.getSource().resolve(this.packId).resolve("pack.png");
            default -> {
                if (this.type.getDirectory().equals(paths[0])) {
                    yield this.getSource().resolve(this.packId);
                } else {
                    yield this.getSource().resolve(this.packId).resolve("SNOWBALL");
                }
            }
        };
        for (int i = 1; i < paths.length; i++) path = path.resolve(paths[i]);
        return path;
    }
    
    public record PackTypeConfig(PackSource source, String prefix, int version) {}
}
