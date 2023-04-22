package org.moddingx.libx.datagen.provider.sandbox;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.sandbox.SandBox;
import org.moddingx.libx.sandbox.surface.BiomeSurface;
import org.moddingx.libx.sandbox.surface.SurfaceRuleSet;

/**
 * SandBox provider for {@link SurfaceRuleSet surface rule sets} and {@link BiomeSurface biome surface}.
 *
 * This provider must run in the {@link DatagenStage#EXTENSION_SETUP extension setup} stage.
 */
public abstract class SurfaceProviderBase extends SandBoxProviderBase {

    protected SurfaceProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.EXTENSION_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " surface rules";
    }

    /**
     * Returns a new builder for a {@link SurfaceRuleSet}.
     */
    public RuleSetBuilder ruleSet() {
        return this.ruleSet(false);
    }

    /**
     * Returns a new builder for a {@link SurfaceRuleSet}.
     * 
     * @param defaultNoiseSurface Whether to include the default surface rules from {@link NoiseGeneratorSettings#surfaceRule()}.
     */
    public RuleSetBuilder ruleSet(boolean defaultNoiseSurface) {
        return new RuleSetBuilder(defaultNoiseSurface);
    }

    /**
     * Creates a {@link BiomeSurface} for the given {@link Biome}.
     *
     * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
     * added the registry. {@link SandBoxProviderBase} does this automatically if the result is stored in a
     * {@code public}, non-{@code static} field inside the provider.
     */
    public Holder<BiomeSurface> biome(Holder<Biome> biome, SurfaceRules.RuleSource... rules) {
        ResourceKey<Biome> key = biome.unwrapKey().orElseThrow(() -> new IllegalStateException("Can't make biome surface: unbound biome holder: " + biome));
        return this.biome(key, rules);
    }

    /**
     * Creates a {@link BiomeSurface} for the given {@link Biome}.
     *
     * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
     * added the registry. {@link SandBoxProviderBase} does this automatically if the result is stored in a
     * {@code public}, non-{@code static} field inside the provider.
     */
    public Holder<BiomeSurface> biome(ResourceKey<Biome> biome, SurfaceRules.RuleSource... rules) {
        BiomeSurface surface = new BiomeSurface(of(rules));
        return this.registries.writableRegistry(SandBox.BIOME_SURFACE).register(ResourceKey.create(SandBox.BIOME_SURFACE, biome.location()), surface, Lifecycle.stable());
    }
    
    private static SurfaceRules.RuleSource of(SurfaceRules.RuleSource[] rules) {
        if (rules.length == 0) {
            return SandBox.emptySurface();
        } else if (rules.length == 1) {
            return rules[0];
        } else {
            return SurfaceRules.sequence(rules);
        }
    }
    
    public class RuleSetBuilder {
        
        private final boolean defaultNoiseSurface;
        private SurfaceRules.RuleSource beforeBiomes;
        private SurfaceRules.RuleSource afterBiomes;
        private SurfaceRules.RuleSource defaultBiomeSurface;

        private RuleSetBuilder(boolean defaultNoiseSurface) {
            this.defaultNoiseSurface = defaultNoiseSurface;
            this.beforeBiomes = SandBox.emptySurface();
            this.afterBiomes = SandBox.emptySurface();
            this.defaultBiomeSurface = SandBox.emptySurface();
        }

        public RuleSetBuilder beforeBiomes(SurfaceRules.RuleSource... rules) {
            this.beforeBiomes = of(rules);
            return this;
        }

        public RuleSetBuilder afterBiomes(SurfaceRules.RuleSource... rules) {
            this.afterBiomes = of(rules);
            return this;
        }
        
        public RuleSetBuilder defaultBiomeSurface(SurfaceRules.RuleSource... rules) {
            this.defaultBiomeSurface = of(rules);
            return this;
        }

        /**
         * Builds the {@link SurfaceRuleSet}.
         *
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added the registry. {@link SandBoxProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<SurfaceRuleSet> build() {
            return SurfaceProviderBase.this.registries.writableRegistry(SandBox.SURFACE_RULE_SET).createIntrusiveHolder(new SurfaceRuleSet(this.defaultNoiseSurface, this.beforeBiomes, this.afterBiomes, this.defaultBiomeSurface));
        }
    }
}
