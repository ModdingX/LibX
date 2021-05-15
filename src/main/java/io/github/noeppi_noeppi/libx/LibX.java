package io.github.noeppi_noeppi.libx;

import io.github.noeppi_noeppi.libx.command.CommandUtil;
import io.github.noeppi_noeppi.libx.command.UppercaseEnumArgument;
import io.github.noeppi_noeppi.libx.crafting.ingredient.EffectIngredient;
import io.github.noeppi_noeppi.libx.crafting.ingredient.NbtIngredient;
import io.github.noeppi_noeppi.libx.crafting.ingredient.PotionIngredient;
import io.github.noeppi_noeppi.libx.impl.TileEntityUpdateQueue;
import io.github.noeppi_noeppi.libx.impl.commands.CommandsImpl;
import io.github.noeppi_noeppi.libx.impl.config.ConfigEvents;
import io.github.noeppi_noeppi.libx.impl.inventory.screen.GenericScreen;
import io.github.noeppi_noeppi.libx.impl.loot.AllLootEntry;
import io.github.noeppi_noeppi.libx.impl.loot.conditions.BlockTag;
import io.github.noeppi_noeppi.libx.impl.network.NetworkImpl;
import io.github.noeppi_noeppi.libx.inventory.container.GenericContainer;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.render.ClientTickHandler;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("libx")
public class LibX extends ModX {

    public static final Logger logger = LogManager.getLogger();
    
    private static LibX instance;
    private static NetworkImpl network;

    public LibX() {
        super("libx", null);
        instance = this;
        network = new NetworkImpl(this);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerMisc);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(ContainerType.class, this::registerContainers);

        MinecraftForge.EVENT_BUS.addListener(ClientTickHandler::tick);
        MinecraftForge.EVENT_BUS.addListener(TileEntityUpdateQueue::tick);
        MinecraftForge.EVENT_BUS.addListener(CommandsImpl::registerCommands);
        MinecraftForge.EVENT_BUS.register(new ConfigEvents());

        CraftingHelper.register(new ResourceLocation(this.modid, "effect"), EffectIngredient.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation(this.modid, "potion"), PotionIngredient.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation(this.modid, "nbt"), NbtIngredient.Serializer.INSTANCE);
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        CommandUtil.registerGenericCommandArgument(this.modid + "_upperenum", UppercaseEnumArgument.class, new UppercaseEnumArgument.Serializer());
    }

    @Override
    protected void clientSetup(FMLClientSetupEvent event) {
        ScreenManager.registerFactory(GenericContainer.TYPE, GenericScreen::new);
    }

    public static LibX getInstance() {
        return instance;
    }

    public static NetworkImpl getNetwork() {
        return network;
    }

    // We can not do this in setup as it would not be available for `runData`
    private void registerMisc(RegistryEvent.NewRegistry event) {
        Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, AllLootEntry.ID, AllLootEntry.TYPE);
        Registry.register(Registry.LOOT_CONDITION_TYPE, BlockTag.ID, BlockTag.TYPE);
    }
    
    private void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        GenericContainer.TYPE.setRegistryName(new ResourceLocation(this.modid, "generic"));
        event.getRegistry().register(GenericContainer.TYPE);
    }
}
