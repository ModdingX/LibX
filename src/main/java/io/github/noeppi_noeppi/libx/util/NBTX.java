package io.github.noeppi_noeppi.libx.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

/**
 * Utilities to deal with NBT.
 */
public class NBTX {

    /**
     * Store a {@link BlockPos} in a {@link CompoundNBT} with a given key.
     */
    public static void putPos(CompoundNBT nbt, String key, BlockPos pos) {
        nbt.put(key, new IntArrayNBT(new int[]{ pos.getX(), pos.getY(), pos.getZ() }));
    }

    /**
     * Get a {@link BlockPos} from a {@link CompoundNBT} stored with a given key or null if there's no such
     * block pos.
     */
    @Nullable
    public static BlockPos getPos(CompoundNBT nbt, String key) {
        if (nbt.contains(key, Constants.NBT.TAG_INT_ARRAY)) {
            int[] list = nbt.getIntArray(key);
            if (list.length == 3) {
                return new BlockPos(list[0], list[1], list[2]);
            }
        }
        return null;
    }

    /**
     * Get a {@link BlockPos} from a {@link CompoundNBT} stored with a given key or the default value if there's
     * no such block pos.
     */
    public static BlockPos getPos(CompoundNBT nbt, String key, BlockPos defaultValue) {
        BlockPos pos = getPos(nbt, key);
        return pos == null ? defaultValue : pos;
    }

    /**
     * Stores a {@link ResourceLocation} in a {@link CompoundNBT} with a given key.
     */
    public static void putRL(CompoundNBT nbt, String key, ResourceLocation rl) {
        nbt.putString(key, rl.toString());
    }

    /**
     * Gets a {@link ResourceLocation} from a {@link CompoundNBT} stored with a given key or null if there's no
     * such resource location.
     */
    @Nullable
    public static ResourceLocation getRL(CompoundNBT nbt, String key) {
        if (nbt.contains(key, Constants.NBT.TAG_STRING)) {
            return ResourceLocation.tryCreate(nbt.getString(key));
        } else {
            return null;
        }
    }
    
    /**
     * Gets a {@link ResourceLocation} from a {@link CompoundNBT} stored with a given key or the default value if 
     * there's no such resource location.
     */
    public static ResourceLocation getRL(CompoundNBT nbt, String key, ResourceLocation defaultValue) {
        ResourceLocation rl = getRL(nbt, key);
        return rl == null ? defaultValue : rl;
    }

    /**
     * Stores the location of a {@link RegistryKey}. This will <b>not</b> store the registry.
     * 
     * @see NBTX#putRL(CompoundNBT, String, ResourceLocation)
     */
    public static void putKey(CompoundNBT nbt, String key, RegistryKey<?> rl) {
        putRL(nbt, key, rl.getLocation());
    }
    
    /**
     * Gets a {@link RegistryKey}. This will only load the location, the {@link Registry} must be provided by yourself.
     * 
     * @see NBTX#getRL(CompoundNBT, String) 
     */
    @Nullable
    public static <T> RegistryKey<T> getKey(CompoundNBT nbt, String key, RegistryKey<Registry<T>> registry) {
        ResourceLocation rl = getRL(nbt, key);
        if (rl != null) {
            return RegistryKey.getOrCreateKey(registry, rl);
        } else {
            return null;
        }
    }
    
    /**
     * Gets a {@link RegistryKey}. This will only load the location, the {@link Registry} must be provided by yourself.
     * 
     * @see NBTX#getRL(CompoundNBT, String, ResourceLocation)
     */
    public static <T> RegistryKey<T> getKey(CompoundNBT nbt, String key, RegistryKey<Registry<T>> registry, RegistryKey<T> defaultValue) {
        RegistryKey<T> rl = getKey(nbt, key, registry);
        return rl == null ? defaultValue : rl;
    }
}
