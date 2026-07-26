package org.moddingx.libx.registration.util;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

import java.util.function.Supplier;

/**
 * Auxiliary classes to to register client extensions easily.
 */
public abstract class ClientExtensionInfo {
    
    private ClientExtensionInfo() {
        
    }

    /**
     * Wraps some {@link IClientItemExtensions client item extensions}. Using the LibX registration system, an
     * instance of this class can be registered on the client using the same id as the associated
     * {@link net.minecraft.world.item.Item} to set up the {@link IClientItemExtensions client item extensions}.
     * 
     * @see Registerable#registerClientAdditional(RegistrationContext, Registerable.EntryCollector)
     */
    public record Item(Supplier<IClientItemExtensions> extensions) {
        public Item(IClientItemExtensions extensions) { this(() -> extensions); }
    }

    /**
     * Wraps some {@link IClientBlockExtensions client block extensions}. Using the LibX registration system, an
     * instance of this class can be registered on the client using the same id as the associated
     * {@link net.minecraft.world.level.block.Block} to set up the {@link IClientBlockExtensions client block extensions}.
     *
     * @see Registerable#registerClientAdditional(RegistrationContext, Registerable.EntryCollector)
     */
    public record Block(Supplier<IClientBlockExtensions> extensions) {
        public Block(IClientBlockExtensions extensions) { this(() -> extensions); }
    }

    /**
     * Wraps some {@link IClientFluidTypeExtensions client fluid type extensions} together with a
     * {@link FluidModel.Unbaked fluid model}. Using the LibX registration system, an instance of this class can be
     * registered on the client using the same id as the associated {@link net.neoforged.neoforge.fluids.FluidType}
     * to set up the {@link IClientFluidTypeExtensions client fluid type extensions}. The
     * {@link FluidModel.Unbaked fluid model} is registered for every {@link net.minecraft.world.level.material.Fluid fluid}
     * belonging to that fluid type, so for both the source and the flowing fluid.
     *
     * @see Registerable#registerClientAdditional(RegistrationContext, Registerable.EntryCollector)
     */
    public record Fluid(Supplier<IClientFluidTypeExtensions> extensions, Supplier<FluidModel.Unbaked> model) {
        public Fluid(IClientFluidTypeExtensions extensions, FluidModel.Unbaked model) { this(() -> extensions, () -> model); }
    }

    /**
     * Wraps some {@link IClientMobEffectExtensions client mob effect extensions}. Using the LibX registration system, an
     * instance of this class can be registered on the client using the same id as the associated
     * {@link net.minecraft.world.effect.MobEffect} to set up the {@link IClientMobEffectExtensions client mob effect extensions}.
     *
     * @see Registerable#registerClientAdditional(RegistrationContext, Registerable.EntryCollector)
     */
    public record MobEffect(Supplier<IClientMobEffectExtensions> extensions) {
        public MobEffect(IClientMobEffectExtensions extensions) { this(() -> extensions); }
    }

    /**
     * Wraps a {@link MenuScreens.ScreenConstructor screen constructor}. Using the LibX registration system, an
     * instance of this class can be registered on the client using the same id as the associated
     * {@link MenuType} to set up the {@link MenuScreens.ScreenConstructor screen constructor} for that menu.
     *
     * @see Registerable#registerClientAdditional(RegistrationContext, Registerable.EntryCollector)
     */
    public record MenuScreen<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>>(MenuType<T> menuType, MenuScreens.ScreenConstructor<T, U> screenConstructor) {}
}
