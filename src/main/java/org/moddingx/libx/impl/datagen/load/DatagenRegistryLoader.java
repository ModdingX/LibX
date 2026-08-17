package org.moddingx.libx.impl.datagen.load;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.Util;
import net.neoforged.neoforge.registries.DataPackRegistriesHooks;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.libxcore.CoreRegistryLoad;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

public class DatagenRegistryLoader {

    // Hacky code to load a registry access during datagen from a resource manager
    // See WorldLoader#load
    public static RegistryAccess.Frozen loadRegistries(ResourceManager mgr, RegistrySelector selector) {
        LibX.logger.info("Start loading registries for datagen");
        LayeredRegistryAccess<RegistryLayer> access = RegistryLayer.createRegistryAccess();
        access = loadLayer(mgr, access, RegistryLayer.WORLDGEN, getDataPackRegistries(RegistryLayer.WORLDGEN, selector));
        // Invoke our coremod patch here
        CoreRegistryLoad.afterWorldGenLayerLoad(access);
        access = loadLayer(mgr, access, RegistryLayer.DIMENSIONS, getDataPackRegistries(RegistryLayer.DIMENSIONS, selector));
        LibX.logger.info("Finished loading registries for datagen");
        return access.compositeAccess();
    }

    private static LayeredRegistryAccess<RegistryLayer> loadLayer(ResourceManager mgr, LayeredRegistryAccess<RegistryLayer> access, RegistryLayer layer, List<RegistryDataLoader.RegistryData<?>> registries) {
        // Only the load of the registries themselves is marked as bootstrap, so the TagLoad coremod patch may allow
        // their tags to reference elements that only a RegistryProvider creates. Tags of the static registries are
        // loaded from the unmarked manager and keep their regular behavior.
        ResourceManager registryManager = DatagenResourceManager.markRegistryBootstrap(mgr);
        // The registry loader is asynchronous now. Each layer must still be loaded and awaited on its own,
        // as the RegistryLoad coremod patch hooks in between the layers.
        return access.replaceFrom(layer, RegistryDataLoader.load(registryManager, TagLoader.buildUpdatedLookups(access.getAccessForLoading(layer), TagLoader.loadTagsForExistingRegistries(mgr, access.getLayer(RegistryLayer.STATIC))), registries, Util.backgroundExecutor()).join());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static List<RegistryDataLoader.RegistryData<?>> getDataPackRegistries(@Nullable RegistryLayer layer, @Nullable RegistrySelector selector) {
        if (layer == null) {
            return Stream.concat(
                    Stream.concat(
                            getDataPackRegistries(RegistryLayer.STATIC, selector).stream(),
                            getDataPackRegistries(RegistryLayer.WORLDGEN, selector).stream()
                    ),
                    Stream.concat(
                            getDataPackRegistries(RegistryLayer.DIMENSIONS, selector).stream(),
                            getDataPackRegistries(RegistryLayer.RELOADABLE, selector).stream()
                    )
            ).toList();
        }
        List<RegistryDataLoader.RegistryData<?>> defaultRegistries = switch(layer) {
            case STATIC, RELOADABLE -> List.of();
            case WORLDGEN -> List.copyOf(DataPackRegistriesHooks.getDataPackRegistries());
            case DIMENSIONS -> List.copyOf(RegistryDataLoader.DIMENSION_REGISTRIES);
        };
        return selector == null ? defaultRegistries : List.copyOf(selector.selectRegistries(layer, defaultRegistries));
    }

    @FunctionalInterface
    public interface RegistrySelector {
        List<RegistryDataLoader.RegistryData<?>> selectRegistries(RegistryLayer layer, List<RegistryDataLoader.RegistryData<?>> registries);
    }
}
