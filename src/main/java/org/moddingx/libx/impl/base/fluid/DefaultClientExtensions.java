package org.moddingx.libx.impl.base.fluid;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import javax.annotation.Nonnull;

public class DefaultClientExtensions implements IClientFluidTypeExtensions {

    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;

    public DefaultClientExtensions(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
    }

    @Nonnull
    @Override
    public ResourceLocation getStillTexture() {
        return this.stillTexture;
    }

    @Nonnull
    @Override
    public ResourceLocation getFlowingTexture() {
        return this.flowingTexture;
    }
}
