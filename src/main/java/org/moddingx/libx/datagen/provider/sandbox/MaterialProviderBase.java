package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;
import org.moddingx.libx.sandbox.SandBox;
import org.moddingx.libx.sandbox.material.BiomeMaterial;
import org.moddingx.libx.sandbox.material.MaterialRuleSet;

/**
 * SandBox provider for {@link MaterialRuleSet material rule sets} and {@link BiomeMaterial biome materials}.
 *
 * This provider must run in the {@link DatagenStage#EXTENSION_SETUP extension setup} stage.
 */
public abstract class MaterialProviderBase extends RegistryProviderBase {

    protected MaterialProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.EXTENSION_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " material rules";
    }

    /**
     * Returns a new builder for a {@link MaterialRuleSet}.
     */
    public RuleSetBuilder ruleSet() {
        return this.ruleSet(false);
    }

    /**
     * Returns a new builder for a {@link MaterialRuleSet}.
     * 
     * @param defaultNoiseMaterial Whether to include the default material rule from {@link NoiseGeneratorSettings#materialRule()}.
     */
    public RuleSetBuilder ruleSet(boolean defaultNoiseMaterial) {
        return new RuleSetBuilder(defaultNoiseMaterial);
    }

    /**
     * Creates a {@link BiomeMaterial} for the given {@link Biome}.
     *
     * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
     * added to the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
     * {@code public}, non-{@code static} field inside the provider.
     */
    public Holder<BiomeMaterial> biome(Holder<Biome> biome, MaterialRule... rules) {
        ResourceKey<Biome> key = biome.unwrapKey().orElseThrow(() -> new IllegalStateException("Can't make biome material: unbound biome holder: " + biome));
        return this.biome(key, rules);
    }

    /**
     * Creates a {@link BiomeMaterial} for the given {@link Biome}.
     *
     * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
     * added to the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
     * {@code public}, non-{@code static} field inside the provider.
     */
    public Holder<BiomeMaterial> biome(ResourceKey<Biome> biome, MaterialRule... rules) {
        BiomeMaterial material = new BiomeMaterial(of(rules));
        return this.registries.writableRegistry(SandBox.BIOME_MATERIAL).register(ResourceKey.create(SandBox.BIOME_MATERIAL, biome.identifier()), material, RegistrationInfo.BUILT_IN);
    }
    
    private static MaterialRule of(MaterialRule[] rules) {
        if (rules.length == 0) {
            return SandBox.emptyMaterial();
        } else if (rules.length == 1) {
            return rules[0];
        } else {
            return MaterialRules.sequence(rules);
        }
    }
    
    public class RuleSetBuilder {
        
        private final boolean defaultNoiseMaterial;
        private MaterialRule beforeBiomes;
        private MaterialRule afterBiomes;
        private MaterialRule defaultBiomeMaterial;

        private RuleSetBuilder(boolean defaultNoiseMaterial) {
            this.defaultNoiseMaterial = defaultNoiseMaterial;
            this.beforeBiomes = SandBox.emptyMaterial();
            this.afterBiomes = SandBox.emptyMaterial();
            this.defaultBiomeMaterial = SandBox.emptyMaterial();
        }

        public RuleSetBuilder beforeBiomes(MaterialRule... rules) {
            this.beforeBiomes = of(rules);
            return this;
        }

        public RuleSetBuilder afterBiomes(MaterialRule... rules) {
            this.afterBiomes = of(rules);
            return this;
        }
        
        public RuleSetBuilder defaultBiomeMaterial(MaterialRule... rules) {
            this.defaultBiomeMaterial = of(rules);
            return this;
        }

        /**
         * Builds the {@link MaterialRuleSet}.
         *
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added to the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<MaterialRuleSet> build() {
            return MaterialProviderBase.this.registries.writableRegistry(SandBox.MATERIAL_RULE_SET).createIntrusiveHolder(new MaterialRuleSet(this.defaultNoiseMaterial, this.beforeBiomes, this.afterBiomes, this.defaultBiomeMaterial));
        }
    }
}
