package org.moddingx.libx.sandbox.surface;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.moddingx.libx.sandbox.SandBox;
import org.moddingx.libx.sandbox.generator.ExtendedNoiseChunkGenerator;

/**
 * Defines surface rules specific to a biome. Needs to be registered with the same {@link ResourceKey key} as
 * the biome. These are used by {@link ExtendedNoiseChunkGenerator} when the surface rules are overridden.
 */
public record BiomeSurface(SurfaceRules.RuleSource rule) {
    
    public static final Codec<BiomeSurface> DIRECT_CODEC = SurfaceRules.RuleSource.CODEC.xmap(BiomeSurface::new, BiomeSurface::rule);

    public static final Codec<Holder<BiomeSurface>> CODEC = RegistryFileCodec.create(SandBox.BIOME_SURFACE, DIRECT_CODEC);

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
