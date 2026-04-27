package org.moddingx.libx.util.data;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Utilities to deal with NBT.
 */
public class NbtX {

    /**
     * Stores a {@link ResourceLocation} in a {@link CompoundTag} with a given key.
     */
    public static void putResource(CompoundTag nbt, String key, ResourceLocation rl) {
        nbt.putString(key, rl.toString());
    }

    /**
     * Stores a {@link ResourceLocation} in a {@link ValueOutput} with a given key.
     */
    public static void putResource(ValueOutput output, String key, ResourceLocation rl) {
        output.putString(key, rl.toString());
    }

    /**
     * Gets a {@link ResourceLocation} from a {@link CompoundTag} stored with a given key or null if there's no
     * such resource location.
     */
    @Nullable
    public static ResourceLocation getResource(CompoundTag nbt, String key) {
        if (nbt.contains(key)) {
            return nbt.getString(key).map(ResourceLocation::tryParse).orElse(null);
        } else {
            return null;
        }
    }

    /**
     * Gets a {@link ResourceLocation} from a {@link ValueInput} stored with a given key or null if there's no
     * such resource location.
     */
    @Nullable
    public static ResourceLocation getResource(ValueInput input, String key) {
        Optional<String> string = input.getString(key);

        return string.map(ResourceLocation::tryParse).orElse(null);
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
     * Gets a {@link ResourceLocation} from a {@link ValueInput} stored with a given key or the default value if
     * there's no such resource location.
     */
    public static ResourceLocation getResource(ValueInput input, String key, ResourceLocation defaultValue) {
        ResourceLocation rl = getResource(input, key);
        return rl == null ? defaultValue : rl;
    }

    /**
     * Stores the location of a {@link ResourceKey}. This will <b>not</b> store the registry.
     * 
     * @see NbtX#putResource(CompoundTag, String, ResourceLocation)
     */
    public static void putResourceKey(CompoundTag nbt, String key, ResourceKey<?> rl) {
        putResource(nbt, key, rl.location());
    }

    /**
     * Stores the location of a {@link ResourceKey}. This will <b>not</b> store the registry.
     *
     * @see NbtX#putResource(ValueOutput, String, ResourceLocation)
     */
    public static void putResourceKey(ValueOutput output, String key, ResourceKey<?> rl) {
        putResource(output, key, rl.location());
    }
    
    /**
     * Gets a {@link ResourceKey}. This will only load the location, the {@link Registry} must be provided by the caller.
     * 
     * @see NbtX#getResource(CompoundTag, String) 
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
     * @see NbtX#getResource(ValueInput, String)
     */
    @Nullable
    public static <T> ResourceKey<T> getResourceKey(ValueInput input, String key, ResourceKey<Registry<T>> registry) {
        ResourceLocation rl = getResource(input, key);
        if (rl != null) {
            return ResourceKey.create(registry, rl);
        } else {
            return null;
        }
    }
    
    /**
     * Gets a {@link ResourceKey}. This will only load the location, the {@link Registry} must be provided by the caller.
     * 
     * @see NbtX#getResource(CompoundTag, String, ResourceLocation)
     */
    public static <T> ResourceKey<T> getResourceKey(CompoundTag nbt, String key, ResourceKey<Registry<T>> registry, ResourceKey<T> defaultValue) {
        ResourceKey<T> rl = getResourceKey(nbt, key, registry);
        return rl == null ? defaultValue : rl;
    }

    /**
     * Gets a {@link ResourceKey}. This will only load the location, the {@link Registry} must be provided by the caller.
     *
     * @see NbtX#getResource(ValueInput, String, ResourceLocation)
     */
    public static <T> ResourceKey<T> getResourceKey(ValueInput input, String key, ResourceKey<Registry<T>> registry, ResourceKey<T> defaultValue) {
        ResourceKey<T> rl = getResourceKey(input, key, registry);
        return rl == null ? defaultValue : rl;
    }
}
