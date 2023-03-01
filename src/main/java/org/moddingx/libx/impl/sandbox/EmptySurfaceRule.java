package org.moddingx.libx.impl.sandbox;

import com.mojang.serialization.Codec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

import javax.annotation.Nonnull;

public class EmptySurfaceRule implements SurfaceRules.RuleSource {

    public static final EmptySurfaceRule INSTANCE = new EmptySurfaceRule();
    public static final Codec<EmptySurfaceRule> CODEC = Codec.unit(INSTANCE);

    private EmptySurfaceRule() {

    }

    @Nonnull
    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
        return KeyDispatchDataCodec.of(CODEC);
    }

    @Nonnull
    @Override
    public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
        return (x, y, z) -> null;
    }
}

