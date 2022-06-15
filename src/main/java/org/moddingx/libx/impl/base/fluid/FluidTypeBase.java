package org.moddingx.libx.impl.base.fluid;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.IFluidTypeRenderProperties;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.DistExecutor;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FluidTypeBase extends FluidType {

    private final Supplier<Supplier<IFluidTypeRenderProperties>> renderProperties;

    public FluidTypeBase(Properties properties, Supplier<Supplier<IFluidTypeRenderProperties>> renderProperties) {
        super(properties);
        this.renderProperties = renderProperties;
    }

    @Override
    public void initializeClient(Consumer<IFluidTypeRenderProperties> consumer) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> consumer.accept(this.renderProperties.get().get()));
    }
}
