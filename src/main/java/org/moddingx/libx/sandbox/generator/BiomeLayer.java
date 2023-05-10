package org.moddingx.libx.sandbox.generator;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.moddingx.libx.sandbox.SandBox;

import java.util.Optional;

/**
 * A biome layer is a set of biomes and climate parameters that can generate in a noise range.
 * @param range A climate range that the layer can generate in.
 * @param density A custom density function that is sampled on every block position. Out of all biome layers the one
 *                with the highest density will be used to sample the biome at that position. These density functions
 *                don't support caching optimisations or interpolation, so they should be kept simple. 
 * @param biomes A climate parameter list mapping climate points to biomes.
 */
public record BiomeLayer(Climate.ParameterPoint range, Optional<DensityFunction> density, Climate.ParameterList<Holder<Biome>> biomes) {

    public static final Climate.ParameterPoint FULL_RANGE = new Climate.ParameterPoint(
            Climate.Parameter.span(-1, 1),
            Climate.Parameter.span(-1, 1),
            Climate.Parameter.span(-1, 1),
            Climate.Parameter.span(-1, 1),
            Climate.Parameter.span(-1, 1),
            Climate.Parameter.span(-1, 1),
            0
    );
    
    public BiomeLayer(Climate.ParameterList<Holder<Biome>> biomes) {
        this(FULL_RANGE, Optional.empty(), biomes);
    }

    public BiomeLayer(DensityFunction density, Climate.ParameterList<Holder<Biome>> biomes) {
        this(FULL_RANGE, Optional.of(density), biomes);
    }

    public BiomeLayer(Climate.ParameterPoint range, Climate.ParameterList<Holder<Biome>> biomes) {
        this(range, Optional.empty(), biomes);
    }
    
    public BiomeLayer(Climate.ParameterPoint range, DensityFunction density, Climate.ParameterList<Holder<Biome>> biomes) {
        this(range, Optional.of(density), biomes);
    }
    
    private static final Codec<Pair<Climate.ParameterPoint, Holder<Biome>>> ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst),
            Biome.CODEC.fieldOf("biome").forGetter(Pair::getSecond)
    ).apply(instance, Pair::of));
            
    public static final Codec<BiomeLayer> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Climate.ParameterPoint.CODEC.fieldOf("range").forGetter(BiomeLayer::range),
            DensityFunction.HOLDER_HELPER_CODEC.optionalFieldOf("density").forGetter(BiomeLayer::density),
            ExtraCodecs.nonEmptyList(ENTRY_CODEC.listOf()).xmap(Climate.ParameterList::new, Climate.ParameterList::values).fieldOf("biomes").forGetter(BiomeLayer::biomes)
    ).apply(instance, BiomeLayer::new));
    
    public static final Codec<Holder<BiomeLayer>> CODEC = RegistryFileCodec.create(SandBox.BIOME_LAYER, DIRECT_CODEC);

    /**
     * The overworld biome layer. It covers the full noise range and has a weight of 1.
     */
    public static final ResourceKey<BiomeLayer> OVERWORLD = ResourceKey.create(SandBox.BIOME_LAYER, new ResourceLocation("minecraft", "overworld"));
    
    /**
     * The nether biome layer. It covers the full noise range and has a weight of 1.
     */
    public static final ResourceKey<BiomeLayer> NETHER = ResourceKey.create(SandBox.BIOME_LAYER, new ResourceLocation("minecraft", "nether"));

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
