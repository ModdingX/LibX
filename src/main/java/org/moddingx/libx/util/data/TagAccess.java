package org.moddingx.libx.util.data;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.tags.ITagManager;
import org.moddingx.libx.annotation.meta.RemoveIn;

import java.util.Objects;
import java.util.Optional;

/**
 * A class to help accessing {@link TagKey tags} in a more user friendly way.
 * A {@link TagAccess} can contain a {@link RegistryAccess} to allow accessing
 * dynamic registries.
 * 
 * @deprecated Use {@link HolderSet}s where possible in datapack registries and {@link ITagManager} for
 *             forge non-datapack registries.
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.20")
public class TagAccess {
    
    private final RegistryAccess registries;
    
    private TagAccess(RegistryAccess registries) {
        this.registries = registries;
    }

    /**
     * Creates a new {@link TagAccess} from a given {@link RegistryAccess}.
     */
    public static TagAccess create(RegistryAccess registries) {
        return new TagAccess(Objects.requireNonNull(registries));
    }
    
    /**
     * Creates a new {@link TagAccess} for a given {@link MinecraftServer}.
     */
    public static TagAccess create(MinecraftServer server) {
        return create(server.registryAccess());
    }
    
    /**
     * Creates a new {@link TagAccess} for a given {@link Level}.
     */
    public static TagAccess create(Level level) {
        return create(level.registryAccess());
    }

    /**
     * Gets the {@link HolderSet} associated with a tag.
     * If the tag does not yet exist, it is created.
     */
    public <T> HolderSet.Named<T> get(TagKey<T> key) {
        return this.resolve(key.registry()).getOrCreateTag(key);
    }
    
    /**
     * Gets the {@link HolderSet} associated with a tag.
     * If the tag does not yet exist, the returned {@link Optional} will be empty.
     */
    public <T> Optional<HolderSet.Named<T>> tryGet(TagKey<T> key) {
        return this.resolve(key.registry()).getTag(key);
    }

    /**
     * Checks whether a tag has a certain element.
     */
    public <T> boolean has(TagKey<T> key, T value) {
        Registry<T> registry = this.resolve(key.registry());
        Optional<Holder<T>> holder = registry.getResourceKey(value).flatMap(registry::getHolder);
        //noinspection OptionalIsPresent
        if (holder.isEmpty()) return false;
        return registry.getTag(key).map(tag -> tag.contains(holder.get())).orElse(false);
    }

    /**
     * Gets a random element from a tag.
     */
    public <T> Optional<T> random(TagKey<T> key, RandomSource random) {
        return this.resolve(key.registry()).getTag(key).flatMap(tag -> tag.getRandomElement(random)).map(Holder::value);
    }
    
    private <T> Registry<T> resolve(ResourceKey<? extends Registry<T>> key) {
        return this.registries.registry(key).orElseThrow(() -> new IllegalArgumentException("Registry " + key.location() + " not found in access: " + this.registries));
    }
}
