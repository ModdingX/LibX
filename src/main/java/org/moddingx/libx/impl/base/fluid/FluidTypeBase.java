package org.moddingx.libx.impl.base.fluid;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.DistExecutor;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FluidTypeBase extends FluidType {

    private final Supplier<Supplier<IClientFluidTypeExtensions>> clientExtensions;

    public FluidTypeBase(Properties properties, Supplier<Supplier<IClientFluidTypeExtensions>> clientExtensions) {
        super(properties);
        this.clientExtensions = clientExtensions;
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> consumer.accept(this.clientExtensions.get().get()));
    }
}
