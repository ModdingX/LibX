package org.moddingx.libx.impl.sandbox;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.rule.RuleEvaluator;

import javax.annotation.Nonnull;

public class EmptyMaterialRule implements MaterialRule {

    public static final EmptyMaterialRule INSTANCE = new EmptyMaterialRule();
    public static final MapCodec<EmptyMaterialRule> CODEC = MapCodec.unit(INSTANCE);

    private EmptyMaterialRule() {

    }

    @Nonnull
    @Override
    public MapCodec<? extends MaterialRule> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    public RuleEvaluator compile(@Nonnull MaterialRuleContext context) {
        return (x, y, z) -> null;
    }
}
