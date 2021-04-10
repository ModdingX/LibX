package io.github.noeppi_noeppi.libx;

import io.github.noeppi_noeppi.libx.command.CommandUtil;
import io.github.noeppi_noeppi.libx.command.UppercaseEnumArgument;
import io.github.noeppi_noeppi.libx.crafting.ingredient.EffectIngredient;
import io.github.noeppi_noeppi.libx.crafting.ingredient.NbtIngredient;
import io.github.noeppi_noeppi.libx.crafting.ingredient.PotionIngredient;
import io.github.noeppi_noeppi.libx.event.PlayerFindAmmoEvent;
import io.github.noeppi_noeppi.libx.impl.TileEntityUpdateQueue;
import io.github.noeppi_noeppi.libx.impl.commands.CommandsImpl;
import io.github.noeppi_noeppi.libx.impl.config.ConfigEvents;
import io.github.noeppi_noeppi.libx.impl.inventory.screen.GenericScreen;
import io.github.noeppi_noeppi.libx.impl.network.NetworkImpl;
import io.github.noeppi_noeppi.libx.inventory.BaseItemStackHandler;
import io.github.noeppi_noeppi.libx.inventory.container.GenericContainer;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.render.ClientTickHandler;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.ServerChatEvent;
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

        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(ContainerType.class, this::registerContainers);

        MinecraftForge.EVENT_BUS.addListener(ClientTickHandler::tick);
        MinecraftForge.EVENT_BUS.addListener(TileEntityUpdateQueue::tick);
        MinecraftForge.EVENT_BUS.addListener(CommandsImpl::registerCommands);
        MinecraftForge.EVENT_BUS.register(new ConfigEvents());
        MinecraftForge.EVENT_BUS.addListener(this::serverChat);
        MinecraftForge.EVENT_BUS.addListener(this::ammo);

        CraftingHelper.register(new ResourceLocation(this.modid, "effect"), EffectIngredient.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation(this.modid, "potion"), PotionIngredient.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation(this.modid, "nbt"), NbtIngredient.Serializer.INSTANCE);
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        GenericContainer.registerSlotValidator(new ResourceLocation("libx", "test"), (s, stack) -> s >= 5 || ItemTags.ARROWS.contains(stack.getItem()));
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
    
    private void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        GenericContainer.TYPE.setRegistryName(new ResourceLocation(this.modid, "generic"));
        event.getRegistry().register(GenericContainer.TYPE);
    }
    
    public void serverChat(ServerChatEvent event) {
        int size;
        try {
            size = Integer.parseInt(event.getMessage());
        } catch (NumberFormatException e) {
            size = 12 * 8 + 4;
        }
        GenericContainer.open(event.getPlayer(), new BaseItemStackHandler(
                size, s -> System.out.println("Changed slot: " + s))
        , new StringTextComponent("Test Container"), new ResourceLocation("libx", "test"));
    }
    
    public void ammo(PlayerFindAmmoEvent event) {
        event.setAmmo(new ItemStack(Items.SPECTRAL_ARROW));
    }
}
