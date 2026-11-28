package org.moddingx.libx.sandbox.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryFileCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.moddingx.libx.sandbox.SandBox;
import org.moddingx.libx.sandbox.generator.ExtendedNoiseChunkGenerator;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Defines logic to override the {@link NoiseGeneratorSettings} for an {@link ExtendedNoiseChunkGenerator}.
 * 
 * @param useDefaultNoiseMaterial Whether the default material rule from the {@link NoiseGeneratorSettings} should be included.
 * @param beforeBiomes {@link MaterialRule Material rules} that are applied before biome-specific rules.
 * @param afterBiomes {@link MaterialRule Material rules} that are applied after biome-specific rules.
 * @param defaultBiomeMaterial {@link MaterialRule Material rules} used for biomes, where no {@link BiomeMaterial} is registered.
 */
public record MaterialRuleSet(boolean useDefaultNoiseMaterial, MaterialRule beforeBiomes, MaterialRule afterBiomes, MaterialRule defaultBiomeMaterial) {

    public static final Codec<MaterialRuleSet> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("use_default_noise_material").orElse(false).forGetter(MaterialRuleSet::useDefaultNoiseMaterial),
            MaterialRule.CODEC.fieldOf("before_biomes").forGetter(MaterialRuleSet::beforeBiomes),
            MaterialRule.CODEC.fieldOf("after_biomes").forGetter(MaterialRuleSet::afterBiomes),
            MaterialRule.CODEC.fieldOf("default_biome_material").forGetter(MaterialRuleSet::defaultBiomeMaterial)
    ).apply(instance, MaterialRuleSet::new));

    public static final Codec<Holder<MaterialRuleSet>> CODEC = RegistryFileCodec.create(SandBox.MATERIAL_RULE_SET, DIRECT_CODEC, true);

    /**
     * Builds the concrete material rule for this rule set.
     */
    public MaterialRule build(Registry<Biome> biomeRegistry, Registry<BiomeMaterial> materialRegistry, Set<Holder<Biome>> biomes, NoiseGeneratorSettings settings) {
        List<MaterialRule> rules = new ArrayList<>();
        rules.add(this.beforeBiomes());
        ArrayList<ResourceKey<Biome>> biomesWithRules = new ArrayList<>(); 
        for (Holder<Biome> biome : biomes) {
            @Nullable
            Identifier id = switch (biome.kind()) {
                case REFERENCE -> ((Holder.Reference<Biome>) biome).key().identifier();
                case DIRECT -> biomeRegistry.getKey(biome.value());
            };
            if (id != null) {
                Optional<Holder.Reference<BiomeMaterial>> biomeMaterial = materialRegistry.get(id);
                if (biomeMaterial.isPresent()) {
                    ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, id);
                    if (!biomesWithRules.contains(biomeKey)) {
                        biomesWithRules.add(biomeKey);
                        rules.add(MaterialRules.ifTrue(
                                MaterialRules.isBiome(biomeRegistry, biomeKey),
                                biomeMaterial.get().value().rule()
                        ));
                    }
                }
            }
        }
        if (biomesWithRules.isEmpty()) {
            rules.add(this.defaultBiomeMaterial());
        } else {
            //noinspection unchecked
            rules.add(MaterialRules.ifTrue(
                    MaterialRules.not(MaterialRules.isBiome(biomeRegistry, biomesWithRules.toArray(ResourceKey[]::new))),
                    this.defaultBiomeMaterial()
            ));
        }
        rules.add(this.afterBiomes());
        if (this.useDefaultNoiseMaterial()) {
            rules.add(settings.materialRule().value());
        }
        return MaterialRules.sequence(rules.toArray(MaterialRule[]::new));
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
