package org.moddingx.libx.impl.datapack;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import org.moddingx.libx.LibX;
import org.moddingx.libx.datapack.DatapackHelper;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class LibXPack extends PathPackResources {

    @SuppressWarnings("deprecation")
    public static final Map<PackType, PackTypeConfig> PACK_CONFIG = Map.of(
            PackType.CLIENT_RESOURCES, new PackTypeConfig(PackSource.FEATURE, new PackSelectionConfig(false, Pack.Position.BOTTOM, false), "libxassets", SharedConstants.RESOURCE_PACK_FORMAT),
            PackType.SERVER_DATA, new PackTypeConfig(PackSource.DEFAULT, new PackSelectionConfig(true, Pack.Position.BOTTOM, true), "libxdata", SharedConstants.DATA_PACK_FORMAT)
    );
    
    private final PackType type;
    private final IoSupplier<InputStream> packMcmeta;

    public LibXPack(PackLocationInfo location, PackType type, IModInfo mod, IModFile modFile, String packId) {
        // Get the base part of the mod in there and the override resolve
        super(location, modFile.findResource(PACK_CONFIG.get(type).prefix()).resolve(packId));
        this.type = type;
        
        String description = mod.getDisplayName() + " (" + packId + ")";
        try {
            Path descPath = modFile.findResource(PACK_CONFIG.get(type).prefix()).resolve(packId).resolve("description.txt");
            if (Files.isRegularFile(descPath)) description = Files.readString(descPath, StandardCharsets.UTF_8).strip();
        } catch (Exception e) {
            //
        }
        this.packMcmeta = DatapackHelper.generatePackMeta(description, type);
    }
    
    public static PackLocationInfo generateLocationInfo(IModInfo mod, PackType type, String packId) {
        PackTypeConfig config = PACK_CONFIG.get(type);
        return new PackLocationInfo(
                mod.getModId() + "/" + packId,
                Component.literal(mod.getDisplayName() + " (" + packId + ")"),
                PACK_CONFIG.get(type).source(),
                Optional.of(new KnownPack(LibX.getInstance().modid, config.prefix() + "/" + mod.getModId() + "/" + packId, mod.getVersion().toString()))
        );
    }

    @Override
    public IoSupplier<InputStream> getRootResource(@Nonnull String... names) {
        return names[0].equals(PACK_META) ? this.packMcmeta : super.getRootResource(names);
    }

    @Override
    public boolean isHidden() {
        return this.type == PackType.SERVER_DATA;
    }
    
    public record PackTypeConfig(PackSource source, PackSelectionConfig selection, String prefix, int version) {}
}
