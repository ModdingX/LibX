package org.moddingx.libx.impl.base.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import javax.annotation.Nonnull;

public class DefaultRenderProperties implements IClientFluidTypeExtensions {

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
