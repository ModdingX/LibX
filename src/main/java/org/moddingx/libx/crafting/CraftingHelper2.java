package org.moddingx.libx.crafting;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Utility functions missing in {@link CraftingHelper}
 */
public class CraftingHelper2 {
    
    public static JsonObject serializeItemStack(ItemStack stack, boolean writeNBT) {
        JsonObject json = new JsonObject();
        json.addProperty("item", Objects.requireNonNull(stack.getItem().getRegistryName(), "Can't serialize ItemStack: Item has no registry name: " + stack.getItem()).toString());
        json.addProperty("count", stack.getCount());
        if (writeNBT) {
            CompoundTag stackTag = stack.hasTag() ? stack.getTag() : null;
            Tag capsTag = forgeCaps(stack);
            if (stackTag != null || capsTag != null) {
                // Need to make a copy, so the stack is not modified.
                CompoundTag resultTag = stackTag == null ? new CompoundTag() : stackTag.copy();
                if (capsTag != null) resultTag.put("ForgeCaps", capsTag);
                json.addProperty("nbt", resultTag.toString());
            }
        }
        return json;
    }
    
    @Nullable
    private static Tag forgeCaps(ItemStack stack) {
        CompoundTag nbt = stack.serializeNBT();
        if (nbt.contains("ForgeCaps") && (!(nbt.get("ForgeCaps") instanceof CompoundTag cmp) || !cmp.isEmpty())) {
            return nbt.get("ForgeCaps");
        } else {
            return null;
        }
    }
}
