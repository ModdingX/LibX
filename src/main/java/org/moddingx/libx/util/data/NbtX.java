package org.moddingx.libx.util.data;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Utilities to deal with NBT.
 */
public class NbtX {

    /**
     * Stores a {@link Identifier} in a {@link CompoundTag} with a given key.
     */
    public static void putResource(CompoundTag nbt, String key, Identifier rl) {
        nbt.putString(key, rl.toString());
    }

    /**
     * Stores a {@link Identifier} in a {@link ValueOutput} with a given key.
     */
    public static void putResource(ValueOutput output, String key, Identifier rl) {
        output.putString(key, rl.toString());
    }

    /**
     * Gets a {@link Identifier} from a {@link CompoundTag} stored with a given key or null if there's no
     * such resource location.
     */
    @Nullable
    public static Identifier getResource(CompoundTag nbt, String key) {
        if (nbt.contains(key)) {
            return nbt.getString(key).map(Identifier::tryParse).orElse(null);
        } else {
            return null;
        }
    }

    /**
     * Gets a {@link Identifier} from a {@link ValueInput} stored with a given key or null if there's no
     * such resource location.
     */
    @Nullable
    public static Identifier getResource(ValueInput input, String key) {
        Optional<String> string = input.getString(key);

        return string.map(Identifier::tryParse).orElse(null);
    }
    
    /**
     * Gets a {@link Identifier} from a {@link CompoundTag} stored with a given key or the default value if 
     * there's no such resource location.
     */
    public static Identifier getResource(CompoundTag nbt, String key, Identifier defaultValue) {
        Identifier rl = getResource(nbt, key);
        return rl == null ? defaultValue : rl;
    }

    /**
     * Gets a {@link Identifier} from a {@link ValueInput} stored with a given key or the default value if
     * there's no such resource location.
     */
    public static Identifier getResource(ValueInput input, String key, Identifier defaultValue) {
        Identifier rl = getResource(input, key);
        return rl == null ? defaultValue : rl;
    }

    /**
     * Stores the location of a {@link ResourceKey}. This will <b>not</b> store the registry.
     * 
     * @see NbtX#putResource(CompoundTag, String, Identifier)
     */
    public static void putResourceKey(CompoundTag nbt, String key, ResourceKey<?> rl) {
        putResource(nbt, key, rl.identifier());
    }

    /**
     * Stores the location of a {@link ResourceKey}. This will <b>not</b> store the registry.
     *
     * @see NbtX#putResource(ValueOutput, String, Identifier)
     */
    public static void putResourceKey(ValueOutput output, String key, ResourceKey<?> rl) {
        putResource(output, key, rl.identifier());
    }
    
    /**
     * Gets a {@link ResourceKey}. This will only load the location, the {@link Registry} must be provided by the caller.
     * 
     * @see NbtX#getResource(CompoundTag, String) 
     */
    @Nullable
    public static <T> ResourceKey<T> getResourceKey(CompoundTag nbt, String key, ResourceKey<Registry<T>> registry) {
        Identifier rl = getResource(nbt, key);
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
        Identifier rl = getResource(input, key);
        if (rl != null) {
            return ResourceKey.create(registry, rl);
        } else {
            return null;
        }
    }
    
    /**
     * Gets a {@link ResourceKey}. This will only load the location, the {@link Registry} must be provided by the caller.
     * 
     * @see NbtX#getResource(CompoundTag, String, Identifier)
     */
    public static <T> ResourceKey<T> getResourceKey(CompoundTag nbt, String key, ResourceKey<Registry<T>> registry, ResourceKey<T> defaultValue) {
        ResourceKey<T> rl = getResourceKey(nbt, key, registry);
        return rl == null ? defaultValue : rl;
    }

    /**
     * Gets a {@link ResourceKey}. This will only load the location, the {@link Registry} must be provided by the caller.
     *
     * @see NbtX#getResource(ValueInput, String, Identifier)
     */
    public static <T> ResourceKey<T> getResourceKey(ValueInput input, String key, ResourceKey<Registry<T>> registry, ResourceKey<T> defaultValue) {
        ResourceKey<T> rl = getResourceKey(input, key, registry);
        return rl == null ? defaultValue : rl;
    }
}
