package org.moddingx.libx.impl.base.fluid;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import javax.annotation.Nonnull;

public class DefaultClientExtensions implements IClientFluidTypeExtensions {

    private final Identifier stillTexture;
    private final Identifier flowingTexture;

    public DefaultClientExtensions(Identifier stillTexture, Identifier flowingTexture) {
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
    }

    @Nonnull
    @Override
    public Identifier getStillTexture() {
        return this.stillTexture;
    }

    @Nonnull
    @Override
    public Identifier getFlowingTexture() {
        return this.flowingTexture;
    }
}
