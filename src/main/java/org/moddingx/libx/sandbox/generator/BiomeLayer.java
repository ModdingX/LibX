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
import org.moddingx.libx.sandbox.SandBox;

/**
 * A biome layer is a set of biomes and climate parameters that can generate in a noise range.
 * @param weight The weight for this layer to generate.
 * @param range A climate range that the layer can generate in.
 * @param biomes A climate parameter list mapping climate points to biomes.
 */
public record BiomeLayer(double weight, Climate.ParameterPoint range, Climate.ParameterList<Holder<Biome>> biomes) {

    public static final Climate.ParameterPoint FULL_RANGE = new Climate.ParameterPoint(
            Climate.Parameter.span(-1, 1),
            Climate.Parameter.span(-1, 1),
            Climate.Parameter.span(-1, 1),
            Climate.Parameter.span(-1, 1),
            Climate.Parameter.span(-1, 1),
            Climate.Parameter.span(-1, 1),
            0
    );
    
    public BiomeLayer(double weight, Climate.ParameterList<Holder<Biome>> biomes) {
        this(weight, FULL_RANGE, biomes);
    }
    
    public BiomeLayer(Climate.ParameterList<Holder<Biome>> biomes) {
        this(1, biomes);
    }
    
    private static final Codec<Pair<Climate.ParameterPoint, Holder<Biome>>> ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst),
            Biome.CODEC.fieldOf("biome").forGetter(Pair::getSecond)
    ).apply(instance, Pair::of));
            
    public static final Codec<BiomeLayer> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("weight").forGetter(BiomeLayer::weight),
            Climate.ParameterPoint.CODEC.fieldOf("range").forGetter(BiomeLayer::range),
            ExtraCodecs.nonEmptyList(ENTRY_CODEC.listOf()).xmap(Climate.ParameterList::new, Climate.ParameterList::values).fieldOf("biomes").forGetter(BiomeLayer::biomes)
    ).apply(instance, BiomeLayer::new));
    
    public static final Codec<Holder<BiomeLayer>> CODEC = RegistryFileCodec.create(SandBox.BIOME_LAYER, DIRECT_CODEC);
    
    // TODO datagen these
    public static final ResourceKey<BiomeLayer> OVERWORLD = ResourceKey.create(SandBox.BIOME_LAYER, new ResourceLocation("minecraft", "overworld"));
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
