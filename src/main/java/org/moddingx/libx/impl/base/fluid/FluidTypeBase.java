package org.moddingx.libx.impl.base.fluid;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.DistExecutor;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FluidTypeBase extends FluidType {

    private final Supplier<Supplier<IClientFluidTypeExtensions>> renderProperties;

    public FluidTypeBase(Properties properties, Supplier<Supplier<IClientFluidTypeExtensions>> renderProperties) {
        super(properties);
        this.renderProperties = renderProperties;
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> consumer.accept(this.renderProperties.get().get()));
    }
}
