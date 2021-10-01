package io.github.noeppi_noeppi.libx;

import io.github.noeppi_noeppi.libx.command.CommandUtil;
import io.github.noeppi_noeppi.libx.command.EnumArgument2;
import io.github.noeppi_noeppi.libx.crafting.ingredient.EffectIngredient;
import io.github.noeppi_noeppi.libx.crafting.ingredient.MergedIngredient;
import io.github.noeppi_noeppi.libx.crafting.ingredient.NbtIngredient;
import io.github.noeppi_noeppi.libx.crafting.ingredient.PotionIngredient;
import io.github.noeppi_noeppi.libx.impl.BlockEntityUpdateQueue;
import io.github.noeppi_noeppi.libx.impl.commands.CommandsImpl;
import io.github.noeppi_noeppi.libx.impl.config.ConfigEvents;
import io.github.noeppi_noeppi.libx.impl.crafting.recipe.EmptyRecipe;
import io.github.noeppi_noeppi.libx.impl.loot.AllLootEntry;
import io.github.noeppi_noeppi.libx.impl.menu.screen.GenericScreen;
import io.github.noeppi_noeppi.libx.impl.network.NetworkImpl;
import io.github.noeppi_noeppi.libx.impl.render.BlockOverlayQuadCache;
import io.github.noeppi_noeppi.libx.menu.GenericMenu;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.render.ClientTickHandler;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * LibX instance class.
 */
@Mod("libx")
public final class LibX extends ModX {

    public static final Logger logger = LogManager.getLogger();
    
    private static LibX instance;
    private static CommonNetwork networkWrapper;

    public LibX() {
        super("libx", null);
        
        instance = this;
        NetworkImpl network = new NetworkImpl(this);
        networkWrapper = new CommonNetwork(network);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerMisc);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(MenuType.class, this::registerContainers);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(RecipeSerializer.class, this::registerRecipes);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(BlockOverlayQuadCache::resourcesReload));

        MinecraftForge.EVENT_BUS.addListener(ClientTickHandler::tick);
        MinecraftForge.EVENT_BUS.addListener(BlockEntityUpdateQueue::tick);
        MinecraftForge.EVENT_BUS.addListener(CommandsImpl::registerCommands);
        MinecraftForge.EVENT_BUS.register(new ConfigEvents());

        CraftingHelper.register(this.resource("effect"), EffectIngredient.Serializer.INSTANCE);
        CraftingHelper.register(this.resource("potion"), PotionIngredient.Serializer.INSTANCE);
        CraftingHelper.register(this.resource("nbt"), NbtIngredient.Serializer.INSTANCE);
        CraftingHelper.register(this.resource("merged"), MergedIngredient.Serializer.INSTANCE);
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
    private void registerMisc(RegistryEvent.NewRegistry event) {
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
