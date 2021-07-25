package io.github.noeppi_noeppi.libx.util;

import com.google.gson.*;
import net.minecraft.nbt.*;

/**
 * Provides a way to convert NBT to json just as JsonToNBT does the other way round.
 */
public class NbtToJson {

    /**
     * Transforms the given nbt to a JsonElement.
     *
     * @param byteToBool Whether bytes with the value 0 and 1 should get booleans in
     *                   the resulting json. This is here because nbt has no boolean
     *                   type and uses byte instead.
     */
    public static JsonElement getJson(Tag nbt, boolean byteToBool) {
        if (nbt instanceof EndTag) {
            return JsonNull.INSTANCE;
        } else if (nbt instanceof CompoundTag tag) {
            JsonObject obj = new JsonObject();
            for (String key : tag.getAllKeys())
                obj.add(key, getJson(tag.get(key), byteToBool));
            return obj;
        } else if (nbt instanceof ListTag tag) {
            JsonArray arr = new JsonArray();
            for (Tag value : tag) arr.add(getJson(value, byteToBool));
            return arr;
        } else if (nbt instanceof ByteTag tag) {
            if (byteToBool && (tag.getAsByte() == 0 || tag.getAsByte() == 1)) {
                return tag.getAsByte() == 0 ? new JsonPrimitive(false) : new JsonPrimitive(true);
            } else {
                return new JsonPrimitive(((ByteTag) nbt).getAsByte());
            }
        } else if (nbt instanceof DoubleTag tag) {
            return new JsonPrimitive(tag.getAsDouble());
        } else if (nbt instanceof FloatTag tag) {
            return new JsonPrimitive(tag.getAsFloat());
        } else if (nbt instanceof IntTag tag) {
            return new JsonPrimitive(tag.getAsInt());
        } else if (nbt instanceof LongTag tag) {
            return new JsonPrimitive(tag.getAsLong());
        } else if (nbt instanceof ShortTag tag) {
            return new JsonPrimitive(tag.getAsShort());
        } else if (nbt instanceof StringTag tag) {
            return new JsonPrimitive(tag.getAsString());
        } else if (nbt instanceof ByteArrayTag tag) {
            JsonArray arr = new JsonArray();
            for (byte b : tag.getAsByteArray())
                arr.add(b);
            return arr;
        } else if (nbt instanceof IntArrayTag tag) {
            JsonArray arr = new JsonArray();
            for (int i : tag.getAsIntArray())
                arr.add(i);
            return arr;
        } else if (nbt instanceof LongArrayTag tag) {
            JsonArray arr = new JsonArray();
            for (long l : tag.getAsLongArray())
                arr.add(l);
            return arr;
        } else  {
            throw new IllegalArgumentException("NBT type unknown: " + nbt.getClass());
        }
    }
}
