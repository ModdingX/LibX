package org.moddingx.libx.impl.base.fluid;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.BucketResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;
import org.moddingx.libx.registration.util.CapabilityInfo;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

public class DefaultBucketItem extends BucketItem implements Registerable {
    
    public DefaultBucketItem(Fluid content, Properties properties) {
        super(content, properties);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        builder.register(null, new CapabilityInfo.Item<ResourceHandler<FluidResource>, ItemAccess>(this, Capabilities.Fluid.ITEM, (stack, itemAccess) -> new BucketResourceHandler(itemAccess)));
    }

    @Nonnull
    @Override
    public Component getName(@Nonnull ItemStack stack) {
        return Component.translatable("libx.tooltip.fluid_base.bucket", this.content.getFluidType().getDescription(new FluidStack(this.content, FluidType.BUCKET_VOLUME)));
    }
}
