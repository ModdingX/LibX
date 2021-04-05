package io.github.noeppi_noeppi.libx.impl.mixin;

import io.github.noeppi_noeppi.libx.impl.datapack.LibXDatapackFinder;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResourcePackList.class)
public class MixinResourcePackList {

    @Inject(
            method = "Lnet/minecraft/resources/ResourcePackList;<init>(Lnet/minecraft/resources/ResourcePackInfo$IFactory;[Lnet/minecraft/resources/IPackFinder;)V",
            at = @At("RETURN")
    )
    public void constructor(ResourcePackInfo.IFactory p_i231423_1_, IPackFinder[] p_i231423_2_, CallbackInfo ci) {
        ((ResourcePackList) (Object) this).addPackFinder(LibXDatapackFinder.INSTANCE);
    }
}
