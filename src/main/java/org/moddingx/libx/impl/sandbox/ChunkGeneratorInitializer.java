package org.moddingx.libx.impl.sandbox;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.moddingx.libx.sandbox.generator.ExtendedNoiseChunkGenerator;
import org.moddingx.libx.sandbox.generator.LayeredBiomeSource;

public class ChunkGeneratorInitializer {
    
    public static void initChunkGenerator(ChunkGenerator generator, MinecraftServer server) {
        if (generator.getBiomeSource() instanceof LayeredBiomeSource layered) {
            layered.init(server.getWorldGenSettings().options().seed());
        }
        if (generator instanceof ExtendedNoiseChunkGenerator gen) {
            gen.init(server.registryAccess());
        }
    }
}
