package org.moddingx.libx.sandbox;

import net.minecraft.world.level.levelgen.SurfaceRules;
import org.moddingx.libx.impl.sandbox.EmptySurfaceRule;

public class SandBox {
    
    public static SurfaceRules.RuleSource emptySurface() {
        return EmptySurfaceRule.INSTANCE;
    }
}
