package io.github.noeppi_noeppi.libx;

import io.github.noeppi_noeppi.libx.command.CommandUtil;
import io.github.noeppi_noeppi.libx.command.UppercaseEnumArgument;
import io.github.noeppi_noeppi.libx.crafting.ingredient.EffectIngredient;
import io.github.noeppi_noeppi.libx.crafting.ingredient.NbtIngredient;
import io.github.noeppi_noeppi.libx.crafting.ingredient.PotionIngredient;
import io.github.noeppi_noeppi.libx.impl.commands.CommandsImpl;
import io.github.noeppi_noeppi.libx.impl.network.NetworkImpl;
import io.github.noeppi_noeppi.libx.impl.TileEntityUpdateQueue;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.render.ClientTickHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod("libx")
public class LibX extends ModX {

    private static LibX instance;
    private static NetworkImpl network;

    public LibX() {
        super("libx", null);
        instance = this;
        network = new NetworkImpl(this);

        MinecraftForge.EVENT_BUS.addListener(ClientTickHandler::tick);
        MinecraftForge.EVENT_BUS.addListener(TileEntityUpdateQueue::tick);
        MinecraftForge.EVENT_BUS.addListener(CommandsImpl::registerCommands);

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
        //
    }

    public static LibX getInstance() {
        return instance;
    }

    public static NetworkImpl getNetwork() {
        return network;
    }
}
