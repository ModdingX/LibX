package io.github.noeppi_noeppi.libx.crafting;

import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.libx.util.NbtToJson;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;

/**
 * Contains some methods that are missing in Forges' {@link CraftingHelper}
 */
public class CraftingHelper2 {

    /**
     * Writes an {@link ItemStack} the way it's expected in a recipe json.
     *
     * @param writeNbt Whether the stacks nbt tag should be written.
     */
    // TODO
    //  delete. Need to find out for which use it was made to find a good replacement
    //  but the json to nbt conversion used here is not really safe and should not be used
    //  if not really required. This should definitely be deleted.
    public static JsonObject serializeItemStack(ItemStack stack, boolean writeNbt) {
        JsonObject json = new JsonObject();
        //noinspection ConstantConditions
        json.addProperty("item", stack.getItem().getRegistryName().toString());

        if (stack.getCount() != 1) {
            json.addProperty("count", stack.getCount());
        }
        if (writeNbt && stack.hasTag()) {
            json.add("nbt", NbtToJson.getJson(stack.getOrCreateTag(), false));
        }
        return json;
    }
}
