package org.moddingx.libx.sandbox.material;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.codec.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.moddingx.libx.sandbox.SandBox;
import org.moddingx.libx.sandbox.generator.ExtendedNoiseChunkGenerator;

/**
 * Defines material rules specific to a biome. Needs to be registered with the same {@link ResourceKey key} as
 * the biome. These are used by {@link ExtendedNoiseChunkGenerator} when the material rules are overridden.
 */
public record BiomeMaterial(MaterialRule rule) {
    
    public static final Codec<BiomeMaterial> DIRECT_CODEC = MaterialRule.CODEC.xmap(BiomeMaterial::new, BiomeMaterial::rule);

    public static final Codec<Holder<BiomeMaterial>> CODEC = RegistryFileCodec.create(SandBox.BIOME_MATERIAL, DIRECT_CODEC, true);

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
