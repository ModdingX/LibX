package org.moddingx.libx.impl.registration.handler;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.moddingx.libx.registration.util.ClientExtensionInfo;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ClientExtensionRegistrationHandler extends SpecialRegistrationHandler {
    
    private final Map<Identifier, ClientExtensionInfo.Item> items;
    private final Map<Identifier, ClientExtensionInfo.Block> blocks;
    private final Map<Identifier, ClientExtensionInfo.Fluid> fluids;
    private final Map<Identifier, ClientExtensionInfo.MobEffect> mobEffects;
    private final Map<Identifier, ClientExtensionInfo.MenuScreen<?, ?>> menuScreens;

    public ClientExtensionRegistrationHandler(Runnable runRegistration) {
        super(runRegistration);
        this.items = new HashMap<>();
        this.blocks = new HashMap<>();
        this.fluids = new HashMap<>();
        this.mobEffects = new HashMap<>();
        this.menuScreens = new HashMap<>();
    }

    @Override
    public void handle(Identifier id, Object object) {
        if (object instanceof ClientExtensionInfo.Item itemInfo) {
            this.addToMap("ClientExtensionInfo.Item", this.items, id, itemInfo);
        }
        if (object instanceof ClientExtensionInfo.Block blockInfo) {
            this.addToMap("ClientExtensionInfo.Block", this.blocks, id, blockInfo);
        }
        if (object instanceof ClientExtensionInfo.Fluid fluidInfo) {
            this.addToMap("ClientExtensionInfo.Fluid", this.fluids, id, fluidInfo);
        }
        if (object instanceof ClientExtensionInfo.MobEffect mobEffectInfo) {
            this.addToMap("ClientExtensionInfo.MobEffect", this.mobEffects, id, mobEffectInfo);
        }
        if (object instanceof ClientExtensionInfo.MenuScreen<?, ?> menuScreenInfo) {
            this.addToMap("ClientExtensionInfo.MenuScreen", this.menuScreens, id, menuScreenInfo);
        }
    }
    
    public void registerClientExtensions(RegisterClientExtensionsEvent event) {
        this.runRegistration();
        for (Map.Entry<Identifier, ClientExtensionInfo.Item> entry : this.items.entrySet()) {
            Item item = BuiltInRegistries.ITEM.getOptional(entry.getKey()).orElse(null);
            if (item == null) throw new IllegalStateException("ClientExtensionInfo.Item registered for unknown item: " + entry.getKey());
            event.registerItem(entry.getValue().extensions().get(), item);
        }
        for (Map.Entry<Identifier, ClientExtensionInfo.Block> entry : this.blocks.entrySet()) {
            Block block = BuiltInRegistries.BLOCK.getOptional(entry.getKey()).orElse(null);
            if (block == null) throw new IllegalStateException("ClientExtensionInfo.Block registered for unknown block: " + entry.getKey());
            event.registerBlock(entry.getValue().extensions().get(), block);
        }
        for (Map.Entry<Identifier, ClientExtensionInfo.Fluid> entry : this.fluids.entrySet()) {
            FluidType fluidType = NeoForgeRegistries.FLUID_TYPES.getOptional(entry.getKey()).orElse(null);
            if (fluidType == null) throw new IllegalStateException("ClientExtensionInfo.Fluid registered for unknown fluid type: " + entry.getKey());
            event.registerFluidType(entry.getValue().extensions().get(), fluidType);
        }
        for (Map.Entry<Identifier, ClientExtensionInfo.MobEffect> entry : this.mobEffects.entrySet()) {
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.getOptional(entry.getKey()).orElse(null);
            if (effect == null) throw new IllegalStateException("ClientExtensionInfo.MobEffect registered for unknown mob effect: " + entry.getKey());
            event.registerMobEffect(entry.getValue().extensions().get(), effect);
        }
    }
    
    public void registerFluidModels(RegisterFluidModelsEvent event) {
        this.runRegistration();
        if (this.fluids.isEmpty()) return;
        Map<FluidType, ClientExtensionInfo.Fluid> fluidsByType = new HashMap<>();
        for (Map.Entry<Identifier, ClientExtensionInfo.Fluid> entry : this.fluids.entrySet()) {
            FluidType fluidType = NeoForgeRegistries.FLUID_TYPES.getOptional(entry.getKey()).orElse(null);
            if (fluidType == null) throw new IllegalStateException("ClientExtensionInfo.Fluid registered for unknown fluid type: " + entry.getKey());
            fluidsByType.put(fluidType, entry.getValue());
        }
        // A fluid model must be registered for every fluid of the fluid type, so for both the source and the flowing fluid.
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            ClientExtensionInfo.Fluid fluidInfo = fluidsByType.get(fluid.getFluidType());
            if (fluidInfo != null) {
                event.register(fluidInfo.model().get(), fluid);
            }
        }
    }

    public void registerMenuScreens(RegisterMenuScreensEvent event) {
        this.runRegistration();
        for (Map.Entry<Identifier, ClientExtensionInfo.MenuScreen<?, ?>> entry : this.menuScreens.entrySet()) {
            MenuType<?> menuType = BuiltInRegistries.MENU.getOptional(entry.getKey()).orElse(null);
            if (menuType == null) throw new IllegalStateException("ClientExtensionInfo.MenuScreen registered for unknown menu type: " + entry.getKey());
            if (menuType != entry.getValue().menuType()) {
                @Nullable Identifier expectedId = BuiltInRegistries.MENU.getKey(entry.getValue().menuType());
                throw new IllegalStateException("ClientExtensionInfo.MenuScreen registered with wrong id, expected " + expectedId + ", got " + entry.getKey());
            }
            registerMenuScreenTo(event, entry.getValue());
        }
    }
    
    private static <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void registerMenuScreenTo(RegisterMenuScreensEvent event, ClientExtensionInfo.MenuScreen<T, U> menuScreenInfo) {
        event.register(menuScreenInfo.menuType(), menuScreenInfo.screenConstructor());
    }
}
