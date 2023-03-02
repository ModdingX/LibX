package org.moddingx.libx.sandbox;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.sandbox.EmptySurfaceRule;
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
     * The {@link PoolExtension} registry.
     */
    public static final ResourceKey<Registry<PoolExtension>> TEMPLATE_POOL_EXTENSION = ResourceKey.createRegistryKey(LibX.getInstance().resource("template_pool_extension"));

    /**
     * Gets a surface rule that leaves every block unchanged.
     */
    public static SurfaceRules.RuleSource emptySurface() {
        return EmptySurfaceRule.INSTANCE;
    }
}
