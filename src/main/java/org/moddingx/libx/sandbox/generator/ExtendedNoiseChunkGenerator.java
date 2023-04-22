package org.moddingx.libx.sandbox.generator;

import com.mojang.serialization.Codec;
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
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.moddingx.libx.impl.sandbox.FakeHolder;
import org.moddingx.libx.sandbox.SandBox;
import org.moddingx.libx.sandbox.surface.BiomeSurface;
import org.moddingx.libx.sandbox.surface.SurfaceRuleSet;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

/**
 * A version of {@link NoiseBasedChunkGenerator} that allows overriding the surface rules, taking {@link BiomeSurface}
 * into account.
 */
public class ExtendedNoiseChunkGenerator extends NoiseBasedChunkGenerator {

    public static final Codec<ExtendedNoiseChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(gen -> gen.biomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(gen -> gen.actualSettings),
            SurfaceRuleSet.CODEC.optionalFieldOf("surface_override").forGetter(gen -> gen.surfaceOverride)
    ).apply(instance, ExtendedNoiseChunkGenerator::new));
    
    private final Optional<Holder<SurfaceRuleSet>> surfaceOverride;
    private final Holder<NoiseGeneratorSettings> actualSettings;
    private final FakeHolder<NoiseGeneratorSettings> fakeSettings;

    public ExtendedNoiseChunkGenerator(BiomeSource biomes, Holder<NoiseGeneratorSettings> settings, Optional<Holder<SurfaceRuleSet>> surfaceOverride) {
        this(biomes, settings, new FakeHolder<>(settings), surfaceOverride);
    }
    
    private ExtendedNoiseChunkGenerator(BiomeSource biomes, Holder<NoiseGeneratorSettings> settings, FakeHolder<NoiseGeneratorSettings> delegate, Optional<Holder<SurfaceRuleSet>> surfaceOverride) {
        super(biomes, delegate);
        this.surfaceOverride = surfaceOverride;
        this.actualSettings = settings;
        this.fakeSettings = delegate;
    }

    public void init(RegistryAccess access) {
        if (this.surfaceOverride.isPresent()) {
            NoiseGeneratorSettings settings = this.actualSettings.get();
            SurfaceRuleSet set = this.surfaceOverride.get().get();
            Set<Holder<Biome>> biomes = this.biomeSource.possibleBiomes();
            SurfaceRules.RuleSource surfaceRule = set.build(access.registryOrThrow(Registries.BIOME), access.registryOrThrow(SandBox.BIOME_SURFACE), biomes, this.actualSettings.get());
            this.fakeSettings.set(Holder.direct(withSurface(settings, surfaceRule)));
        }
    }

    @Nonnull
    @Override
    protected Codec<? extends ChunkGenerator> codec() {
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
    
    private static NoiseGeneratorSettings withSurface(NoiseGeneratorSettings settings, SurfaceRules.RuleSource surfaceRule) {
        //noinspection deprecation
        return new NoiseGeneratorSettings(
                 settings.noiseSettings(),
                 settings.defaultBlock(),
                 settings.defaultFluid(),
                 settings.noiseRouter(),
                 surfaceRule,
                 settings.spawnTarget(),
                 settings.seaLevel(),
                 settings.disableMobGeneration(),
                 settings.aquifersEnabled(),
                 settings.oreVeinsEnabled(),
                 settings.useLegacyRandomSource()
        );
    }
}
