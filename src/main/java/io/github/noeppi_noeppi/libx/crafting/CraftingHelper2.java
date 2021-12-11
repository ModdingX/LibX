package io.github.noeppi_noeppi.libx.crafting;

import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;

import java.util.Objects;

/**
 * Utility functions missing in {@link CraftingHelper}
 */
public class CraftingHelper2 {
    
    public static JsonObject serializeItemStack(ItemStack stack, boolean writeNBT) {
        JsonObject json = new JsonObject();
        json.addProperty("item", Objects.requireNonNull(stack.getItem().getRegistryName(), "Can't serialize ItemStack: Item has no registry name: " + stack.getItem()).toString());
        json.addProperty("count", stack.getCount());
        if (writeNBT && stack.hasTag()) {
            json.addProperty("nbt", stack.getOrCreateTag().toString());
        }
        return json;
    }
}
