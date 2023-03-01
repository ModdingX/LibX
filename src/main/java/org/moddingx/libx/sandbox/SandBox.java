package org.moddingx.libx.sandbox;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.sandbox.EmptySurfaceRule;

/**
 * SandBox is the LibX worldgen library.
 */
public class SandBox {

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
