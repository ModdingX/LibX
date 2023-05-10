package org.moddingx.libx.sandbox;

import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.sandbox.EmptySurfaceRule;
import org.moddingx.libx.impl.sandbox.density.*;
import org.moddingx.libx.sandbox.generator.BiomeLayer;
import org.moddingx.libx.sandbox.structure.PoolExtension;
import org.moddingx.libx.sandbox.surface.BiomeSurface;
import org.moddingx.libx.sandbox.surface.SurfaceRuleSet;

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
         * always uses x block position {@code 0} to calculate the density.
         */
        public static DensityFunction smashX(DensityFunction density) {
            return smash(density, Direction.Axis.X);
        }

        /**
         * Creates a new density function that uses the given density function to calculate density values, but
         * always uses y block position {@code 0} to calculate the density.
         */
        public static DensityFunction smashY(DensityFunction density) {
            return smash(density, Direction.Axis.Y);
        }

        /**
         * Creates a new density function that uses the given density function to calculate density values, but
         * always uses z block position {@code 0} to calculate the density.
         */
        public static DensityFunction smashZ(DensityFunction density) {
            return smash(density, Direction.Axis.Z);
        }

        /**
         * Creates a new density function that uses the given density function to calculate density values, but
         * always uses a block position of {@code 0} on the given {@link Direction.Axis axis} to calculate the density.
         */
        public static DensityFunction smash(DensityFunction density, Direction.Axis axis) {
            return new DensitySmash(density, axis);
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
            if (!Double.isFinite(minInfluence)) throw new IllegalArgumentException("Invalid minimum influence: " + minInfluence);
            if (!Double.isFinite(maxInfluence)) throw new IllegalArgumentException("Invalid maximum influence: " + maxInfluence);
            return new DensityInfluence(base, modifier, influence, minInfluence, maxInfluence);
        }
        
        /**
         * Creates a new density function that constantly increases along one coordinate axis. This is mainly useful for debugging purposes.
         * The density value is given by {@code coordinate * scale}.
         */
        public static DensityFunction debug(Direction.Axis axis, double scale) {
            return new DensityDebug(axis, scale);
        }

        /**
         * Creates a new density function that lerps between {@code a} and {@code b} with mean {@code 0} and deviation {@code 1}.
         * 
         * @see #lerp(DensityFunction, DensityFunction, DensityFunction, double, double)
         */
        public static DensityFunction lerp(DensityFunction a, DensityFunction b, DensityFunction niveau) {
            return lerp(a, b, niveau, 0, 1);
        }
        
        /**
         * Creates a new density function that lerps between {@code a} and {@code b} depending on the value of {@code niveau}.
         * For {@code niveau = mean}, the influence of {@code a} and {@code b} is equal, at {@code niveau = mean - deviation} and lower,
         * the result will be the value of {@code a}, at {@code niveau = mean + deviation} and higher the result will be the value
         * of {@code b}.
         */
        public static DensityFunction lerp(DensityFunction a, DensityFunction b, DensityFunction niveau, double mean, double deviation) {
            return new DensityLerp(a, b, niveau, mean, deviation);
        }
        
        /**
         * Clamps the given density function between the given values.
         */
        public static DensityFunction clamp(DensityFunction density, double min, double max) {
            return new DensityClamp(density, min, max);
        }
    }
}
