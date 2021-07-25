package io.github.noeppi_noeppi.libx.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

/**
 * Utilities to deal with NBT.
 */
public class NBTX {

    /**
     * Store a {@link BlockPos} in a {@link CompoundTag} with a given key.
     */
    public static void putPos(CompoundTag nbt, String key, BlockPos pos) {
        nbt.put(key, new IntArrayTag(new int[]{ pos.getX(), pos.getY(), pos.getZ() }));
    }

    /**
     * Get a {@link BlockPos} from a {@link CompoundTag} stored with a given key or null if there's no such
     * block pos.
     */
    @Nullable
    public static BlockPos getPos(CompoundTag nbt, String key) {
        if (nbt.contains(key, Constants.NBT.TAG_INT_ARRAY)) {
            int[] list = nbt.getIntArray(key);
            if (list.length == 3) {
                return new BlockPos(list[0], list[1], list[2]);
            }
        }
        return null;
    }

    /**
     * Get a {@link BlockPos} from a {@link CompoundTag} stored with a given key or the default value if there's
     * no such block pos.
     */
    public static BlockPos getPos(CompoundTag nbt, String key, BlockPos defaultValue) {
        BlockPos pos = getPos(nbt, key);
        return pos == null ? defaultValue : pos;
    }

    /**
     * Stores a {@link ResourceLocation} in a {@link CompoundTag} with a given key.
     */
    public static void putRL(CompoundTag nbt, String key, ResourceLocation rl) {
        nbt.putString(key, rl.toString());
    }

    /**
     * Gets a {@link ResourceLocation} from a {@link CompoundTag} stored with a given key or null if there's no
     * such resource location.
     */
    @Nullable
    public static ResourceLocation getRL(CompoundTag nbt, String key) {
        if (nbt.contains(key, Constants.NBT.TAG_STRING)) {
            return ResourceLocation.tryParse(nbt.getString(key));
        } else {
            return null;
        }
    }
    
    /**
     * Gets a {@link ResourceLocation} from a {@link CompoundTag} stored with a given key or the default value if 
     * there's no such resource location.
     */
    public static ResourceLocation getRL(CompoundTag nbt, String key, ResourceLocation defaultValue) {
        ResourceLocation rl = getRL(nbt, key);
        return rl == null ? defaultValue : rl;
    }

    /**
     * Stores the location of a {@link ResourceKey}. This will <b>not</b> store the registry.
     * 
     * @see NBTX#putRL(CompoundTag, String, ResourceLocation)
     */
    public static void putKey(CompoundTag nbt, String key, ResourceKey<?> rl) {
        putRL(nbt, key, rl.location());
    }
    
    /**
     * Gets a {@link ResourceKey}. This will only load the location, the {@link Registry} must be provided by yourself.
     * 
     * @see NBTX#getRL(CompoundTag, String) 
     */
    @Nullable
    public static <T> ResourceKey<T> getKey(CompoundTag nbt, String key, ResourceKey<Registry<T>> registry) {
        ResourceLocation rl = getRL(nbt, key);
        if (rl != null) {
            return ResourceKey.create(registry, rl);
        } else {
            return null;
        }
    }
    
    /**
     * Gets a {@link ResourceKey}. This will only load the location, the {@link Registry} must be provided by yourself.
     * 
     * @see NBTX#getRL(CompoundTag, String, ResourceLocation)
     */
    public static <T> ResourceKey<T> getKey(CompoundTag nbt, String key, ResourceKey<Registry<T>> registry, ResourceKey<T> defaultValue) {
        ResourceKey<T> rl = getKey(nbt, key, registry);
        return rl == null ? defaultValue : rl;
    }
}
