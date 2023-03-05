package org.moddingx.libx.impl.libxcore;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.resources.ResourceManager;
import org.moddingx.libx.impl.sandbox.RegistryProcessor;

import java.util.List;
import java.util.concurrent.Executor;

public class CoreRegistryLoad {

    /**
     * Patched into {@link WorldLoader#load(WorldLoader.InitConfig, WorldLoader.WorldDataSupplier, WorldLoader.ResultFactory, Executor, Executor)}
     * after the call to {@link WorldLoader#loadAndReplaceLayer(ResourceManager, LayeredRegistryAccess, RegistryLayer, List)} for the
     * {@link RegistryLayer#WORLDGEN} layer.
     */
    public static void afterWorldGenLayerLoad(LayeredRegistryAccess<RegistryLayer> access) {
        RegistryProcessor.processWorldGenStage(access);
    }
}
