package org.moddingx.libx.impl.libxcore;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;
import org.moddingx.libx.impl.sandbox.RegistryProcessor;

import java.util.concurrent.Executor;

public class CoreRegistryLoad {

    /**
     * Patched into {@link WorldLoader#load(WorldLoader.InitConfig, WorldLoader.WorldDataSupplier, WorldLoader.ResultFactory, Executor, Executor)}
     * after the call to {@link LayeredRegistryAccess#replaceFrom(Object, RegistryAccess.Frozen...)} for the
     * {@link RegistryLayer#WORLDGEN} layer.
     */
    public static void afterWorldGenLayerLoad(LayeredRegistryAccess<RegistryLayer> access) {
        RegistryProcessor.processWorldGenStage(access);
    }
}
