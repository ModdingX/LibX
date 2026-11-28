package org.moddingx.libx.sandbox.generator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.moddingx.libx.impl.sandbox.FakeHolder;
import org.moddingx.libx.sandbox.SandBox;
import org.moddingx.libx.sandbox.material.BiomeMaterial;
import org.moddingx.libx.sandbox.material.MaterialRuleSet;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

/**
 * A version of {@link NoiseBasedChunkGenerator} that allows overriding the surface rules, taking {@link BiomeMaterial}
 * into account.
 */
public class ExtendedNoiseChunkGenerator extends NoiseBasedChunkGenerator {

    public static final MapCodec<ExtendedNoiseChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(gen -> gen.biomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(gen -> gen.actualSettings),
            MaterialRuleSet.CODEC.optionalFieldOf("material_override").forGetter(gen -> gen.materialOverride)
    ).apply(instance, ExtendedNoiseChunkGenerator::new));

    private final Optional<Holder<MaterialRuleSet>> materialOverride;
    private final Holder<NoiseGeneratorSettings> actualSettings;
    private final FakeHolder<NoiseGeneratorSettings> fakeSettings;

    public ExtendedNoiseChunkGenerator(BiomeSource biomes, Holder<NoiseGeneratorSettings> settings, Optional<Holder<MaterialRuleSet>> materialOverride) {
        this(biomes, settings, new FakeHolder<>(settings), materialOverride);
    }

    private ExtendedNoiseChunkGenerator(BiomeSource biomes, Holder<NoiseGeneratorSettings> settings, FakeHolder<NoiseGeneratorSettings> delegate, Optional<Holder<MaterialRuleSet>> materialOverride) {
        super(biomes, delegate);
        this.materialOverride = materialOverride;
        this.actualSettings = settings;
        this.fakeSettings = delegate;
    }

    public void init(RegistryAccess access) {
        if (this.materialOverride.isPresent()) {
            NoiseGeneratorSettings settings = this.actualSettings.value();
            MaterialRuleSet set = this.materialOverride.get().value();
            Set<Holder<Biome>> biomes = this.biomeSource.possibleBiomes();
            MaterialRule materialRule = set.build(access.lookupOrThrow(Registries.BIOME), access.lookupOrThrow(SandBox.BIOME_MATERIAL), biomes, this.actualSettings.value());
            this.fakeSettings.set(Holder.direct(materialRule(settings, materialRule)));
        }
    }

    @Nonnull
    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    public Holder<NoiseGeneratorSettings> generatorSettings() {
        return this.actualSettings;
    }

    @Override
    public boolean stable(@Nonnull ResourceKey<NoiseGeneratorSettings> settings) {
        return this.actualSettings.is(settings);
    }

    @SuppressWarnings("deprecation")
    private static NoiseGeneratorSettings materialRule(NoiseGeneratorSettings settings, MaterialRule materialRule) {
        return new NoiseGeneratorSettings(
                settings.noiseSettings(),
                settings.defaultBlock(),
                settings.defaultFluid(),
                settings.noiseRouter(),
                Holder.direct(materialRule),
                settings.spawnTarget(),
                settings.seaLevel(),
                settings.disableMobGeneration(),
                settings.aquifers(),
                settings.useLegacyRandomSource(),
                settings.debugFunctions()
        );
    }
}
