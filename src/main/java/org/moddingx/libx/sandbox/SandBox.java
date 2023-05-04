package org.moddingx.libx.sandbox;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.sandbox.EmptySurfaceRule;
import org.moddingx.libx.impl.sandbox.density.DensityInfluence;
import org.moddingx.libx.impl.sandbox.density.DensitySmash;
import org.moddingx.libx.sandbox.generator.BiomeLayer;
import org.moddingx.libx.sandbox.structure.PoolExtension;
import org.moddingx.libx.sandbox.surface.BiomeSurface;
import org.moddingx.libx.sandbox.surface.SurfaceRuleSet;
import org.moddingx.libx.util.math.IntPolynomial;

/**
 * SandBox is the LibX worldgen library.
 */
public class SandBox {

    /**
     * The {@link SurfaceRuleSet} registry.
     */
    public static final ResourceKey<Registry<SurfaceRuleSet>> SURFACE_RULE_SET = ResourceKey.createRegistryKey(LibX.getInstance().resource("surface_rule_set"));
    
    /**
     * The {@link BiomeSurface} registry.
     */
    public static final ResourceKey<Registry<BiomeSurface>> BIOME_SURFACE = ResourceKey.createRegistryKey(LibX.getInstance().resource("biome_surface"));
    
    /**
     * The {@link BiomeLayer} registry.
     */
    public static final ResourceKey<Registry<BiomeLayer>> BIOME_LAYER = ResourceKey.createRegistryKey(LibX.getInstance().resource("biome_layer"));
    
    /**
     * The {@link PoolExtension} registry.
     */
    public static final ResourceKey<Registry<PoolExtension>> TEMPLATE_POOL_EXTENSION = ResourceKey.createRegistryKey(LibX.getInstance().resource("template_pool_extension"));

    /**
     * Gets a surface rule that leaves every block unchanged.
     */
    public static SurfaceRules.RuleSource emptySurface() {
        return EmptySurfaceRule.INSTANCE;
    }

    /**
     * Provides some more useful {@link DensityFunctions} density functions.
     */
    public static class Density {

        private Density() {
            
        }
        
        /**
         * Creates a new density function that uses the given density function to calculate density values, but
         * first transforms the {@code x} input coordinate according to the given polynomial.
         */
        public static DensityFunction smashX(DensityFunction density, IntPolynomial smashX) {
            return smash(density, smashX, IntPolynomial.IDENTITY, IntPolynomial.IDENTITY);
        }

        /**
         * Creates a new density function that uses the given density function to calculate density values, but
         * first transforms the {@code y} input coordinate according to the given polynomial.
         */
        public static DensityFunction smashY(DensityFunction density, IntPolynomial smashY) {
            return smash(density, IntPolynomial.IDENTITY, smashY, IntPolynomial.IDENTITY);
        }

        /**
         * Creates a new density function that uses the given density function to calculate density values, but
         * first transforms the {@code z} input coordinate according to the given polynomial.
         */
        public static DensityFunction smashZ(DensityFunction density, IntPolynomial smashZ) {
            return smash(density, IntPolynomial.IDENTITY, IntPolynomial.IDENTITY, smashZ);
        }

        /**
         * Creates a new density function that uses the given density function to calculate density values, but
         * first transforms the input coordinates according to the given polynomials.
         */
        public static DensityFunction smash(DensityFunction density, IntPolynomial smashX, IntPolynomial smashY, IntPolynomial smashZ) {
            return new DensitySmash(density, smashX, smashY, smashZ);
        }

        /**
         * Creates a new influence density function with automatically filled out values for {@code min_influence}
         * and {@code max_influence}.
         * 
         * @see #influence(DensityFunction, DensityFunction, DensityFunction, double, double)
         */
        public static DensityFunction influence(DensityFunction base, DensityFunction modifier, DensityFunction influence) {
            return new DensityInfluence(base, modifier, influence, influence.minValue(), influence.maxValue());
        }
        
        /**
         * Creates a new density function using the formula {@code base + (min_influence + influence/(max_influence - min_influence))*modifier}
         */
        public static DensityFunction influence(DensityFunction base, DensityFunction modifier, DensityFunction influence, double minInfluence, double maxInfluence) {
            return new DensityInfluence(base, modifier, influence, minInfluence, maxInfluence);
        }
    }
}
