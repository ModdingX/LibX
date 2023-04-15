package org.moddingx.libx;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.moddingx.libx.command.EnumArgument2;
import org.moddingx.libx.crafting.ingredient.EffectIngredient;
import org.moddingx.libx.datagen.DatagenSystem;
import org.moddingx.libx.impl.BlockEntityUpdateQueue;
import org.moddingx.libx.impl.InternalDataGen;
import org.moddingx.libx.impl.commands.client.ClientCommandsImpl;
import org.moddingx.libx.impl.commands.common.CommandsImpl;
import org.moddingx.libx.impl.config.ConfigEvents;
import org.moddingx.libx.impl.crafting.recipe.EmptyRecipe;
import org.moddingx.libx.impl.datapack.DynamicPackLocator;
import org.moddingx.libx.impl.loot.AllLootEntry;
import org.moddingx.libx.impl.loot.modifier.AdditionLootModifier;
import org.moddingx.libx.impl.loot.modifier.RemovalLootModifier;
import org.moddingx.libx.impl.menu.screen.GenericScreen;
import org.moddingx.libx.impl.network.NetworkImpl;
import org.moddingx.libx.impl.render.BlockOverlayQuadCache;
import org.moddingx.libx.impl.sandbox.EmptySurfaceRule;
import org.moddingx.libx.menu.GenericMenu;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.render.ClientTickHandler;
import org.moddingx.libx.sandbox.SandBox;
import org.moddingx.libx.sandbox.generator.BiomeLayer;
import org.moddingx.libx.sandbox.generator.ExtendedNoiseChunkGenerator;
import org.moddingx.libx.sandbox.generator.LayeredBiomeSource;
import org.moddingx.libx.sandbox.structure.PoolExtension;
import org.moddingx.libx.sandbox.surface.BiomeSurface;
import org.moddingx.libx.sandbox.surface.SurfaceRuleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LibX instance class.
 */
@Mod("libx")
public final class LibX extends ModX {
    
    public static final Logger logger = LoggerFactory.getLogger("libx");
    
    private static LibX instance;
    private static CommonNetwork networkWrapper;

    public LibX() {
        instance = this;
        NetworkImpl network = new NetworkImpl(this);
        networkWrapper = new CommonNetwork(network);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(InternalDataGen::gatherData);
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.LOW, DynamicPackLocator::locatePacks);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::createRegistries);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerStuff);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(BlockOverlayQuadCache::resourcesReload));

        MinecraftForge.EVENT_BUS.addListener(ClientTickHandler::tick);
        MinecraftForge.EVENT_BUS.addListener(BlockEntityUpdateQueue::tick);
        MinecraftForge.EVENT_BUS.addListener(CommandsImpl::registerCommands);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.addListener(ClientCommandsImpl::registerClientCommands));
        MinecraftForge.EVENT_BUS.register(new ConfigEvents());

        CraftingHelper.register(this.resource("effect"), EffectIngredient.Serializer.INSTANCE);

        DatagenSystem.registerExtensionRegistry(SandBox.BIOME_SURFACE);
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        //noinspection unchecked,rawtypes
        ArgumentTypeInfos.registerByClass((Class) EnumArgument2.class, EnumArgument2.Info.INSTANCE);
    }

    @Override
    protected void clientSetup(FMLClientSetupEvent event) {
        //noinspection CodeBlock2Expr
        event.enqueueWork(() -> {
            MenuScreens.register(GenericMenu.TYPE, GenericScreen::new);
        });
    }

    /**
     * Gets the LibX instance.
     */
    public static LibX getInstance() {
        return instance;
    }

    /**
     * Gets the network implementation of LibX.
     */
    public static CommonNetwork getNetwork() {
        return networkWrapper;
    }

    private void createRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(SandBox.SURFACE_RULE_SET, SurfaceRuleSet.DIRECT_CODEC, SurfaceRuleSet.DIRECT_CODEC);
        event.dataPackRegistry(SandBox.BIOME_SURFACE, BiomeSurface.DIRECT_CODEC, BiomeSurface.DIRECT_CODEC);
        event.dataPackRegistry(SandBox.BIOME_LAYER, BiomeLayer.DIRECT_CODEC, BiomeLayer.DIRECT_CODEC);
        event.dataPackRegistry(SandBox.TEMPLATE_POOL_EXTENSION, PoolExtension.DIRECT_CODEC, PoolExtension.DIRECT_CODEC);
    }

    private void registerStuff(RegisterEvent event) {
        event.register(Registries.LOOT_POOL_ENTRY_TYPE, AllLootEntry.ID, () -> AllLootEntry.TYPE);
        event.register(Registries.MENU, this.resource("generic"), () -> GenericMenu.TYPE);
        event.register(Registries.RECIPE_TYPE, EmptyRecipe.ID, () -> EmptyRecipe.TYPE);
        event.register(Registries.RECIPE_SERIALIZER, EmptyRecipe.ID, () -> EmptyRecipe.Serializer.INSTANCE);
        event.register(Registries.COMMAND_ARGUMENT_TYPE, this.resource("enum"), () -> EnumArgument2.Info.INSTANCE);
        event.register(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, this.resource("addition"), () -> AdditionLootModifier.CODEC);
        event.register(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, this.resource("removal"), () -> RemovalLootModifier.CODEC);
        event.register(Registries.MATERIAL_RULE, this.resource("empty"), () -> EmptySurfaceRule.CODEC);
        event.register(Registries.CHUNK_GENERATOR, this.resource("noise"), () -> ExtendedNoiseChunkGenerator.CODEC);
        event.register(Registries.BIOME_SOURCE, this.resource("layered"), () -> LayeredBiomeSource.CODEC);
    }
}
