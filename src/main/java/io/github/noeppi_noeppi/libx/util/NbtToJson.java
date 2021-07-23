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
        } else if (nbt instanceof CompoundTag) {
            JsonObject obj = new JsonObject();
            for (String key : ((CompoundTag) nbt).getAllKeys())
                obj.add(key, getJson(((CompoundTag) nbt).get(key), byteToBool));
            return obj;
        } else if (nbt instanceof ListTag) {
            JsonArray arr = new JsonArray();
            for (int i = 0; i < ((ListTag) nbt).size(); i++)
                arr.add(getJson(((ListTag) nbt).get(i), byteToBool));
            return arr;
        } else if (nbt instanceof ByteTag) {
            if (byteToBool && (((ByteTag) nbt).getAsByte() == 0 || ((ByteTag) nbt).getAsByte() == 1)) {
                return ((ByteTag) nbt).getAsByte() == 0 ? new JsonPrimitive(false) : new JsonPrimitive(true);
            } else {
                return new JsonPrimitive(((ByteTag) nbt).getAsByte());
            }
        } else if (nbt instanceof DoubleTag) {
            return new JsonPrimitive(((DoubleTag) nbt).getAsDouble());
        } else if (nbt instanceof FloatTag) {
            return new JsonPrimitive(((FloatTag) nbt).getAsFloat());
        } else if (nbt instanceof IntTag) {
            return new JsonPrimitive(((IntTag) nbt).getAsInt());
        } else if (nbt instanceof LongTag) {
            return new JsonPrimitive(((LongTag) nbt).getAsLong());
        } else if (nbt instanceof ShortTag) {
            return new JsonPrimitive(((ShortTag) nbt).getAsShort());
        } else if (nbt instanceof StringTag) {
            return new JsonPrimitive(nbt.getAsString());
        } else if (nbt instanceof ByteArrayTag) {
            JsonArray arr = new JsonArray();
            for (byte b : ((ByteArrayTag) nbt).getAsByteArray())
                arr.add(b);
            return arr;
        } else if (nbt instanceof IntArrayTag) {
            JsonArray arr = new JsonArray();
            for (int i : ((IntArrayTag) nbt).getAsIntArray())
                arr.add(i);
            return arr;
        } else if (nbt instanceof LongArrayTag) {
            JsonArray arr = new JsonArray();
            for (long l : ((LongArrayTag) nbt).getAsLongArray())
                arr.add(l);
            return arr;
        } else  {
            throw new IllegalArgumentException("NBT type unknown: " + nbt.getClass());
        }
    }
}
