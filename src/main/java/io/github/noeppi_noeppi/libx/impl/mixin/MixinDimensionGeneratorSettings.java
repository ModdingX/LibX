package io.github.noeppi_noeppi.libx.impl.mixin;

import io.github.noeppi_noeppi.libx.world.WorldSeedHolder;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(DimensionGeneratorSettings.class)
public class MixinDimensionGeneratorSettings {

    @Inject(
            method = "Lnet/minecraft/world/gen/settings/DimensionGeneratorSettings;<init>(JZZLnet/minecraft/util/registry/SimpleRegistry;Ljava/util/Optional;)V",
            at = @At("RETURN")
    )
    public void constructor(long seed, boolean generateFeatures, boolean bonusChest, SimpleRegistry<Dimension> p_i231915_5_, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<String> p_i231915_6_, CallbackInfo ci) {
        //noinspection deprecation
        WorldSeedHolder.setSeed(seed);
    }
}
