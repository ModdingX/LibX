package org.moddingx.libx.impl.base.fluid;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public class DefaultClientExtensions implements IClientFluidTypeExtensions {

    private final FluidModel.Unbaked model;

    public DefaultClientExtensions(Identifier stillTexture, Identifier flowingTexture) {
        this.model = new FluidModel.Unbaked(new Material(stillTexture), new Material(flowingTexture), null, null, null);
    }

    /**
     * Gets the default {@link FluidModel.Unbaked fluid model} that uses the still and flowing
     * texture this instance was created with and no tint.
     */
    public FluidModel.Unbaked model() {
        return this.model;
    }
}
