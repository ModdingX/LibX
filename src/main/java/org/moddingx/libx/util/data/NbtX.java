package org.moddingx.libx.util.data;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
    public static void putIdentifier(CompoundTag nbt, String key, Identifier id) {
        nbt.putString(key, id.toString());
    }

    /**
     * Stores a {@link Identifier} in a {@link ValueOutput} with a given key.
     */
    public static void putIdentifier(ValueOutput output, String key, Identifier id) {
        output.putString(key, id.toString());
    }

    /**
     * Gets a {@link Identifier} from a {@link CompoundTag} stored with a given key or null if there's no
     * such identifier.
     */
    @Nullable
    public static Identifier getIdentifier(CompoundTag nbt, String key) {
        if (nbt.contains(key)) {
            return nbt.getString(key).map(Identifier::tryParse).orElse(null);
        } else {
            return null;
        }
    }

    /**
     * Gets a {@link Identifier} from a {@link ValueInput} stored with a given key or null if there's no
     * such identifier.
     */
    @Nullable
    public static Identifier getIdentifier(ValueInput input, String key) {
        Optional<String> string = input.getString(key);

        return string.map(Identifier::tryParse).orElse(null);
    }
    
    /**
     * Gets a {@link Identifier} from a {@link CompoundTag} stored with a given key or the default value if 
     * there's no such identifier.
     */
    public static Identifier getIdentifier(CompoundTag nbt, String key, Identifier defaultValue) {
        Identifier id = getIdentifier(nbt, key);
        return id == null ? defaultValue : id;
    }

    /**
     * Gets a {@link Identifier} from a {@link ValueInput} stored with a given key or the default value if
     * there's no such identifier.
     */
    public static Identifier getIdentifier(ValueInput input, String key, Identifier defaultValue) {
        Identifier id = getIdentifier(input, key);
        return id == null ? defaultValue : id;
    }

    /**
     * Stores the location of a {@link ResourceKey}. This will <b>not</b> store the registry.
     * 
     * @see NbtX#putIdentifier(CompoundTag, String, Identifier)
     */
    public static void putResourceKey(CompoundTag nbt, String key, ResourceKey<?> id) {
        putIdentifier(nbt, key, id.identifier());
    }

    /**
     * Stores the location of a {@link ResourceKey}. This will <b>not</b> store the registry.
     *
     * @see NbtX#putIdentifier(ValueOutput, String, Identifier)
     */
    public static void putResourceKey(ValueOutput output, String key, ResourceKey<?> id) {
        putIdentifier(output, key, id.identifier());
    }
    
    /**
     * Gets a {@link ResourceKey}. This will only load the location, the {@link Registry} must be provided by the caller.
     * 
     * @see NbtX#getIdentifier(CompoundTag, String)
     */
    @Nullable
    public static <T> ResourceKey<T> getResourceKey(CompoundTag nbt, String key, ResourceKey<Registry<T>> registry) {
        Identifier id = getIdentifier(nbt, key);
        if (id != null) {
            return ResourceKey.create(registry, id);
        } else {
            return null;
        }
    }

    /**
     * Gets a {@link ResourceKey}. This will only load the location, the {@link Registry} must be provided by the caller.
     *
     * @see NbtX#getIdentifier(ValueInput, String)
     */
    @Nullable
    public static <T> ResourceKey<T> getResourceKey(ValueInput input, String key, ResourceKey<Registry<T>> registry) {
        Identifier id = getIdentifier(input, key);
        if (id != null) {
            return ResourceKey.create(registry, id);
        } else {
            return null;
        }
    }
    
    /**
     * Gets a {@link ResourceKey}. This will only load the location, the {@link Registry} must be provided by the caller.
     * 
     * @see NbtX#getIdentifier(CompoundTag, String, Identifier)
     */
    public static <T> ResourceKey<T> getResourceKey(CompoundTag nbt, String key, ResourceKey<Registry<T>> registry, ResourceKey<T> defaultValue) {
        ResourceKey<T> id = getResourceKey(nbt, key, registry);
        return id == null ? defaultValue : id;
    }

    /**
     * Gets a {@link ResourceKey}. This will only load the location, the {@link Registry} must be provided by the caller.
     *
     * @see NbtX#getIdentifier(ValueInput, String, Identifier)
     */
    public static <T> ResourceKey<T> getResourceKey(ValueInput input, String key, ResourceKey<Registry<T>> registry, ResourceKey<T> defaultValue) {
        ResourceKey<T> id = getResourceKey(input, key, registry);
        return id == null ? defaultValue : id;
    }
}
