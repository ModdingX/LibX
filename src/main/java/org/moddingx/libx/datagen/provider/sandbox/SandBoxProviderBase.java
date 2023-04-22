package org.moddingx.libx.datagen.provider.sandbox;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.holdersets.AndHolderSet;
import net.minecraftforge.registries.holdersets.AnyHolderSet;
import net.minecraftforge.registries.holdersets.NotHolderSet;
import net.minecraftforge.registries.holdersets.OrHolderSet;
import org.moddingx.libx.LibX;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.RegistryProvider;
import org.moddingx.libx.datagen.RegistrySet;
import org.moddingx.libx.mod.ModX;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Base provider for all SandBox data providers. Handles registering values from public {@link Holder} fields
 * inside the provider to the registries. These fields should contain intrusive holders created through the
 * current {@link RegistrySet}.
 */
public abstract class SandBoxProviderBase implements RegistryProvider {

    protected final ModX mod;
    protected final RegistrySet registries;
    
    protected SandBoxProviderBase(DatagenContext ctx, DatagenStage requiredStage) {
        this.mod = ctx.mod();
        this.registries = ctx.registries();
        if (ctx.stage() != requiredStage) {
            throw new IllegalStateException("Invalid stage: Provider '" + this.getName() + "' must run on " + requiredStage + " stage.");
        }
    }

    /**
     * Gets a holder from a registry key. The current registry set must contain a matching element.
     */
    public final <T> Holder.Reference<T> holder(ResourceKey<T> key) {
        Registry<T> registry = this.registries.registry(ResourceKey.createRegistryKey(key.registry()));
        return registry.getHolder(key).orElseThrow(() -> new IllegalArgumentException("Unregistered element in registry " + key.registry() + ": " + key.location()));
    }
    
    /**
     * Gets a holder from a value. The current registry set must contain a matching element.
     */
    public final <T> Holder.Reference<T> holder(ResourceKey<? extends Registry<T>> registryKey, T value) {
        Registry<T> registry = this.registries.registry(registryKey);
        return registry.getResourceKey(value).map(this::holder).orElseThrow(() -> new IllegalArgumentException("Unregistered element in registry " + registryKey.location() + ": " + value));
    }

    /**
     * Gets a direct holder set from the given elements.
     */
    @SafeVarargs
    public final <T> HolderSet<T> set(Holder<T>... elements) {
        return HolderSet.direct(elements);
    }
    
    /**
     * Gets a holder set matching a tag.
     */
    public final <T> HolderSet<T> set(TagKey<T> tag) {
        Registry<T> registry = this.registries.registry(tag.registry());
        return registry.getOrCreateTag(tag);
    }
    
    /**
     * Gets a holder set matching any value from the given registry.
     */
    public final <T> HolderSet<T> any(ResourceKey<? extends Registry<T>> registryKey) {
        return new AnyHolderSet<>(this.registries.registry(registryKey).asLookup());
    }
    
    /**
     * Gets a holder set matching any value not in the given tag.
     */
    public final <T> HolderSet<T> not(TagKey<T> tag) {
        return new NotHolderSet<>(this.registries.registry(tag.registry()).asLookup(), this.set(tag));
    }
    
    /**
     * Gets a holder set matching any value not in the given holder set.
     */
    public final <T> HolderSet<T> not(ResourceKey<? extends Registry<T>> registryKey, HolderSet<T> set) {
        return new NotHolderSet<>(this.registries.registry(registryKey).asLookup(), set);
    }

    /**
     * Gets a holder set that matches the intersection of the given sets.
     */
    public final <T> HolderSet<T> and(TagKey<T> a, TagKey<T> b) {
        return this.and(this.set(a), this.set(b));
    }

    /**
     * Gets a holder set that matches the intersection of the given sets.
     */
    public final <T> HolderSet<T> and(TagKey<T> a, HolderSet<T> b) {
        return this.and(this.set(a), b);
    }

    /**
     * Gets a holder set that matches the intersection of the given sets.
     */
    public final <T> HolderSet<T> and(HolderSet<T> a, TagKey<T> b) {
        return this.and(a, this.set(b));
    }

    /**
     * Gets a holder set that matches the intersection of the given sets.
     */
    @SafeVarargs
    public final <T> HolderSet<T> and(HolderSet<T>... sets) {
        return new AndHolderSet<>(List.of(sets));
    }

    /**
     * Gets a holder set that matches the union of the given sets.
     */
    public final <T> HolderSet<T> or(TagKey<T> a, TagKey<T> b) {
        return this.and(this.set(a), this.set(b));
    }

    /**
     * Gets a holder set that matches the union of the given sets.
     */
    public final <T> HolderSet<T> or(TagKey<T> a, HolderSet<T> b) {
        return this.and(this.set(a), b);
    }

    /**
     * Gets a holder set that matches the union of the given sets.
     */
    public final <T> HolderSet<T> or(HolderSet<T> a, TagKey<T> b) {
        return this.and(a, this.set(b));
    }

    /**
     * Gets a holder set that matches the union of the given sets.
     */
    @SafeVarargs
    public final <T> HolderSet<T> or(HolderSet<T>... sets) {
        return new OrHolderSet<>(List.of(sets));
    }
    
    @Override
    public void run() {
        try {
            for (Field field : this.getClass().getFields()) {
                if (field.getDeclaringClass() != this.getClass()) continue; // Skip fields from superclasses
                if (!Modifier.isPublic(field.getModifiers())) continue;
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (!Holder.class.isAssignableFrom(field.getType())) continue;
                Holder<?> value = (Holder<?>) field.get(this);
                if (value instanceof Holder.Reference<?> ref) {
                    if (ref.getType() == Holder.Reference.Type.INTRUSIVE && !ref.isBound()) {
                        ResourceKey<? extends Registry<?>> registryKey = this.registries.findRegistryFor(ref);
                        if (registryKey == null) throw new IllegalStateException("Can't infer target registry for " + field.getName() + " in '" + this.getName() + "'. Was the holder created properly?");
                        ResourceLocation id;
                        Id idObj = field.getAnnotation(Id.class);
                        if (idObj != null) {
                            id = new ResourceLocation(idObj.namespace().isEmpty() ? this.mod.modid : idObj.namespace(), idObj.value());
                        } else {
                            StringBuilder sb = new StringBuilder();
                            for (char chr : field.getName().toCharArray()) {
                                if (Character.isUpperCase(chr)) {
                                    sb.append('_');
                                }
                                sb.append(Character.toLowerCase(chr));
                            }
                            id = new ResourceLocation(this.mod.modid, sb.toString());
                        }
                        //noinspection unchecked
                        this.registries.writableRegistry((ResourceKey<? extends Registry<Object>>) registryKey).register(ResourceKey.create((ResourceKey<? extends Registry<Object>>) registryKey, id), ref.value(), Lifecycle.stable());
                    } else if (field.getAnnotation(Id.class) != null) {
                        Id idObj = field.getAnnotation(Id.class);
                        String id = (idObj.namespace().isEmpty() ? this.mod.modid : idObj.namespace()) + ":" + idObj.value();
                        LibX.logger.warn("Skipping bound holder " + field.getName() + " with explicit id " + id + " in '" + this.getName() + "'");
                    }
                } else {
                    LibX.logger.warn("Skipping direct holder in '" + this.getName() + "' (from " + field.getName() + ")");
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to register element for provider '" + this.getName() + "'", e);
        }
    }
}
