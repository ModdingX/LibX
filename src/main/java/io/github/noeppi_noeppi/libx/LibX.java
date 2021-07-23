package io.github.noeppi_noeppi.libx;

import io.github.noeppi_noeppi.libx.command.CommandUtil;
import io.github.noeppi_noeppi.libx.command.EnumArgument2;
import io.github.noeppi_noeppi.libx.crafting.ingredient.EffectIngredient;
import io.github.noeppi_noeppi.libx.crafting.ingredient.MergedIngredient;
import io.github.noeppi_noeppi.libx.crafting.ingredient.NbtIngredient;
import io.github.noeppi_noeppi.libx.crafting.ingredient.PotionIngredient;
import io.github.noeppi_noeppi.libx.impl.TileEntityUpdateQueue;
import io.github.noeppi_noeppi.libx.impl.commands.CommandsImpl;
import io.github.noeppi_noeppi.libx.impl.config.ConfigEvents;
import io.github.noeppi_noeppi.libx.impl.inventory.screen.GenericScreen;
import io.github.noeppi_noeppi.libx.impl.loot.AllLootEntry;
import io.github.noeppi_noeppi.libx.impl.network.NetworkImpl;
import io.github.noeppi_noeppi.libx.impl.recipe.EmptyRecipe;
import io.github.noeppi_noeppi.libx.inventory.container.GenericContainerMenu;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.render.ClientTickHandler;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * TODO
 * Things that really need to be tested before publishing a 1.17 version:
 *   - CraftingExtension
 *   - All mixins need to be tested again with 1.17
 *   - The new registration system
 *   - Dynamic datapacks need to be tested again
 */
@Mod("libx")
public class LibX extends ModX {

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

        MinecraftForge.EVENT_BUS.addListener(ClientTickHandler::tick);
        MinecraftForge.EVENT_BUS.addListener(TileEntityUpdateQueue::tick);
        MinecraftForge.EVENT_BUS.addListener(CommandsImpl::registerCommands);
        MinecraftForge.EVENT_BUS.register(new ConfigEvents());

        CraftingHelper.register(new ResourceLocation(this.modid, "effect"), EffectIngredient.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation(this.modid, "potion"), PotionIngredient.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation(this.modid, "nbt"), NbtIngredient.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation(this.modid, "merged"), MergedIngredient.Serializer.INSTANCE);
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
            MenuScreens.register(GenericContainerMenu.TYPE, GenericScreen::new);
        });
    }

    public static LibX getInstance() {
        return instance;
    }

    public static CommonNetwork getNetwork() {
        return networkWrapper;
    }

    // We can not do this in setup as it would not be available for `runData`
    private void registerMisc(RegistryEvent.NewRegistry event) {
        Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, AllLootEntry.ID, AllLootEntry.TYPE);
    }
    
    private void registerContainers(RegistryEvent.Register<MenuType<?>> event) {
        GenericContainerMenu.TYPE.setRegistryName(new ResourceLocation(this.modid, "generic"));
        event.getRegistry().register(GenericContainerMenu.TYPE);
    }

    private void registerRecipes(RegistryEvent.Register<RecipeSerializer<?>> event) {
        EmptyRecipe.Serializer.INSTANCE.setRegistryName(EmptyRecipe.ID);
        event.getRegistry().register(EmptyRecipe.Serializer.INSTANCE);
    }
}
