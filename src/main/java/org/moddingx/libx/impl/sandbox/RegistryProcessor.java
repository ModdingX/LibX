package org.moddingx.libx.impl.sandbox;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.moddingx.libx.sandbox.SandBox;
import org.moddingx.libx.sandbox.structure.PoolExtension;

import java.util.Map;

public class RegistryProcessor {
    
    public static void processWorldGenStage(LayeredRegistryAccess<RegistryLayer> access) {
        Registry<StructureTemplatePool> poolRegistry = access.getLayer(RegistryLayer.WORLDGEN).registry(Registries.TEMPLATE_POOL).orElse(null);
        Registry<PoolExtension> extRegistry = access.getLayer(RegistryLayer.WORLDGEN).registry(SandBox.TEMPLATE_POOL_EXTENSION).orElse(null);
        if (poolRegistry != null && extRegistry != null) {
            for (PoolExtension ext : extRegistry.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList()) {
                StructureTemplatePool pool = poolRegistry.getOptional(ext.pool()).orElse(null);
                if (pool != null) {
                    for (Pair<StructurePoolElement, Integer> entry : ext.elements()) {
                        for (int i = 0; i < entry.getSecond(); i++) {
                            pool.templates.add(entry.getFirst());
                        }
                    }
                } else if (ext.required()) {
                    throw new IllegalStateException("Failed to apply template pool extension: " + extRegistry.getKey(ext));
                }
            }
        }
    }
}
