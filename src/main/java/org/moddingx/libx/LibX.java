package org.moddingx.libx;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.moddingx.libx.command.CommandUtil;
import org.moddingx.libx.command.EnumArgument2;
import org.moddingx.libx.crafting.ingredient.EffectIngredient;
import org.moddingx.libx.crafting.ingredient.PotionIngredient;
import org.moddingx.libx.impl.BlockEntityUpdateQueue;
import org.moddingx.libx.impl.InternalDataGen;
import org.moddingx.libx.impl.commands.client.ClientCommandsImpl;
import org.moddingx.libx.impl.commands.common.CommandsImpl;
import org.moddingx.libx.impl.config.ConfigEvents;
import org.moddingx.libx.impl.crafting.recipe.EmptyRecipe;
import org.moddingx.libx.impl.datapack.DynamicDatapackLocator;
import org.moddingx.libx.impl.loot.AllLootEntry;
import org.moddingx.libx.impl.menu.screen.GenericScreen;
import org.moddingx.libx.impl.network.NetworkImpl;
import org.moddingx.libx.impl.render.BlockOverlayQuadCache;
import org.moddingx.libx.menu.GenericMenu;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.render.ClientTickHandler;
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
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.LOW, DynamicDatapackLocator::locatePacks);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(GlobalLootModifierSerializer.class, this::registerLootData);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(MenuType.class, this::registerContainers);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(RecipeSerializer.class, this::registerRecipes);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(BlockOverlayQuadCache::resourcesReload));

        MinecraftForge.EVENT_BUS.addListener(ClientTickHandler::tick);
        MinecraftForge.EVENT_BUS.addListener(BlockEntityUpdateQueue::tick);
        MinecraftForge.EVENT_BUS.addListener(CommandsImpl::registerCommands);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.addListener(ClientCommandsImpl::registerClientCommands));
        MinecraftForge.EVENT_BUS.register(new ConfigEvents());

        CraftingHelper.register(this.resource("effect"), EffectIngredient.Serializer.INSTANCE);
        CraftingHelper.register(this.resource("potion"), PotionIngredient.Serializer.INSTANCE);
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        //noinspection CodeBlock2Expr
        event.enqueueWork(() -> {
            CommandUtil.registerGenericCommandArgument(this.modid + "_enum", EnumArgument2.class, new EnumArgument2.Serializer());
        });
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

    // We can't do this in setup as it would not be available for `runData`
    // Must be one of the RegistryEvent.Register events as in that time registries are not frozen
    private void registerLootData(RegistryEvent.Register<GlobalLootModifierSerializer<?>> event) {
        Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, AllLootEntry.ID, AllLootEntry.TYPE);
    }
    
    private void registerContainers(RegistryEvent.Register<MenuType<?>> event) {
        GenericMenu.TYPE.setRegistryName(this.resource("generic"));
        event.getRegistry().register(GenericMenu.TYPE);
    }

    private void registerRecipes(RegistryEvent.Register<RecipeSerializer<?>> event) {
        EmptyRecipe.Serializer.INSTANCE.setRegistryName(EmptyRecipe.ID);
        event.getRegistry().register(EmptyRecipe.Serializer.INSTANCE);
    }
}