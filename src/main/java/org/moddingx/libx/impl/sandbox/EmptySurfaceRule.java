package org.moddingx.libx.impl.sandbox;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

import javax.annotation.Nonnull;

public class EmptySurfaceRule implements SurfaceRules.RuleSource {

    public static final EmptySurfaceRule INSTANCE = new EmptySurfaceRule();
    public static final MapCodec<EmptySurfaceRule> CODEC = MapCodec.unit(INSTANCE);

    private EmptySurfaceRule() {

    }

    @Nonnull
    @Override
    public MapCodec<? extends SurfaceRules.RuleSource> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
        return (x, y, z) -> null;
    }
}
