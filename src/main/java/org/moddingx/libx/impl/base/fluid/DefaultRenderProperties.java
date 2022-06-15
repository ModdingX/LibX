package org.moddingx.libx.impl.base.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.IFluidTypeRenderProperties;

import javax.annotation.Nonnull;

public class DefaultRenderProperties implements IFluidTypeRenderProperties {

    private final ResourceLocation texture;

    public DefaultRenderProperties(ResourceLocation texture) {
        this.texture = texture;
    }

    @Nonnull
    @Override
    public ResourceLocation getStillTexture() {
        return this.texture;
    }

    @Nonnull
    @Override
    public ResourceLocation getFlowingTexture() {
        return this.texture;
    }
}
