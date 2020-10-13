package io.github.noeppi_noeppi.libx;

import io.github.noeppi_noeppi.libx.crafting.ingredient.EffectIngredient;
import io.github.noeppi_noeppi.libx.crafting.ingredient.PotionIngredient;
import io.github.noeppi_noeppi.libx.impl.network.NetworkImpl;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.util.ResourceLocation;
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
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        CraftingHelper.register(new ResourceLocation(this.modid, "effect"), EffectIngredient.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation(this.modid, "potion"), PotionIngredient.Serializer.INSTANCE);
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
