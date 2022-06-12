package io.github.noeppi_noeppi.libx.util;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Utilities to deal with NBT.
 */
public class NBTX {

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
    public static ResourceLocation getResource(CompoundTag nbt, String key) {
        if (nbt.contains(key, Tag.TAG_STRING)) {
            return ResourceLocation.tryParse(nbt.getString(key));
        } else {
            return null;
        }
    }
    
    /**
     * Gets a {@link ResourceLocation} from a {@link CompoundTag} stored with a given key or the default value if 
     * there's no such resource location.
     */
    public static ResourceLocation getResource(CompoundTag nbt, String key, ResourceLocation defaultValue) {
        ResourceLocation rl = getResource(nbt, key);
        return rl == null ? defaultValue : rl;
    }

    /**
     * Stores the location of a {@link ResourceKey}. This will <b>not</b> store the registry.
     * 
     * @see NBTX#putRL(CompoundTag, String, ResourceLocation)
     */
    public static void putResourceKey(CompoundTag nbt, String key, ResourceKey<?> rl) {
        putRL(nbt, key, rl.location());
    }
    
    /**
     * Gets a {@link ResourceKey}. This will only load the location, the {@link Registry} must be provided by the caller.
     * 
     * @see NBTX#getResource(CompoundTag, String) 
     */
    @Nullable
    public static <T> ResourceKey<T> getResourceKey(CompoundTag nbt, String key, ResourceKey<Registry<T>> registry) {
        ResourceLocation rl = getResource(nbt, key);
        if (rl != null) {
            return ResourceKey.create(registry, rl);
        } else {
            return null;
        }
    }
    
    /**
     * Gets a {@link ResourceKey}. This will only load the location, the {@link Registry} must be provided by the caller.
     * 
     * @see NBTX#getResource(CompoundTag, String, ResourceLocation)
     */
    public static <T> ResourceKey<T> getResourceKey(CompoundTag nbt, String key, ResourceKey<Registry<T>> registry, ResourceKey<T> defaultValue) {
        ResourceKey<T> rl = getResourceKey(nbt, key, registry);
        return rl == null ? defaultValue : rl;
    }
}
