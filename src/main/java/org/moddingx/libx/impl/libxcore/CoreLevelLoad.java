package org.moddingx.libx.impl.libxcore;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.moddingx.libx.impl.sandbox.ChunkGeneratorInitializer;

import java.util.List;
import java.util.concurrent.Executor;

public class CoreLevelLoad {

    /**
     * Patched into {@link ServerLevel#ServerLevel(MinecraftServer, Executor, LevelStorageSource.LevelStorageAccess, ServerLevelData, ResourceKey, LevelStem, ChunkProgressListener, boolean, long, List, boolean)}
     * after the call to {@link LevelStem#generator()}.
     */
    public static void startLevelLoad(ChunkGenerator generator, MinecraftServer server) {
        ChunkGeneratorInitializer.initChunkGenerator(generator, server);
    }
}
