package org.moddingx.libx.impl.datagen.load;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.DataPackRegistriesHooks;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.libxcore.CoreRegistryLoad;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

public class DatagenRegistryLoader {

    // Hacky code to load a registry access during datagen from a resource manager
    // See WorldLoader#load
    public static RegistryAccess.Frozen loadRegistries(ExistingFileHelper fileHelper) {
        LibX.logger.info("Start loading registries for datagen");
        ResourceManager mgr = DatagenLoader.resources(fileHelper, PackType.SERVER_DATA);
        LayeredRegistryAccess<RegistryLayer> access = RegistryLayer.createRegistryAccess();
        access = loadLayer(mgr, access, RegistryLayer.WORLDGEN, getDataPackRegistries(RegistryLayer.WORLDGEN));
        // Invoke our coremod patch here
        CoreRegistryLoad.afterWorldGenLayerLoad(access);
        access = loadLayer(mgr, access, RegistryLayer.DIMENSIONS, getDataPackRegistries(RegistryLayer.DIMENSIONS));
        LibX.logger.info("Finished loading registries for datagen");
        return access.compositeAccess();
    }

    private static LayeredRegistryAccess<RegistryLayer> loadLayer(ResourceManager mgr, LayeredRegistryAccess<RegistryLayer> access, RegistryLayer layer, List<RegistryDataLoader.RegistryData<?>> registries) {
        return access.replaceFrom(layer, RegistryDataLoader.load(mgr, access.getAccessForLoading(layer), registries));
    }

    @SuppressWarnings("UnstableApiUsage")
    public static List<RegistryDataLoader.RegistryData<?>> getDataPackRegistries(@Nullable RegistryLayer layer) {
        if (layer == null) {
            return Stream.concat(
                    Stream.concat(getDataPackRegistries(RegistryLayer.STATIC).stream(), getDataPackRegistries(RegistryLayer.WORLDGEN).stream()),
                    Stream.concat(getDataPackRegistries(RegistryLayer.DIMENSIONS).stream(), getDataPackRegistries(RegistryLayer.RELOADABLE).stream())
            ).toList();
        }
        return switch (layer) {
            case STATIC, RELOADABLE -> List.of();
            case WORLDGEN -> DataPackRegistriesHooks.getDataPackRegistries();
            case DIMENSIONS -> RegistryDataLoader.DIMENSION_REGISTRIES;
        };
    }
}
