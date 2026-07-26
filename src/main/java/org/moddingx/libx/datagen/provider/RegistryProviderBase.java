package org.moddingx.libx.datagen.provider;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.holdersets.AndHolderSet;
import net.neoforged.neoforge.registries.holdersets.AnyHolderSet;
import net.neoforged.neoforge.registries.holdersets.NotHolderSet;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;
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
public abstract class RegistryProviderBase implements RegistryProvider {

    protected final ModX mod;
    protected final RegistrySet registries;
    protected final DatagenContext context;
    
    protected RegistryProviderBase(DatagenContext ctx, DatagenStage requiredStage) {
        this.mod = ctx.mod();
        this.registries = ctx.registries();
        this.context = ctx;
        if (ctx.stage() != requiredStage) {
            throw new IllegalStateException("Invalid stage: Provider '" + this.getName() + "' must run on " + requiredStage + " stage.");
        }
    }

    /**
     * Gets a holder from a registry key. The current registry set must contain a matching element.
     */
    public final <T> Holder.Reference<T> holder(ResourceKey<T> key) {
        Registry<T> registry = this.registries.registry(ResourceKey.createRegistryKey(key.registry()));
        return registry.get(key).orElseThrow(() -> new IllegalArgumentException("Unregistered element in registry " + key.registry() + ": " + key.identifier()));
    }
    
    /**
     * Gets a holder from a value. The current registry set must contain a matching element.
     */
    public final <T> Holder.Reference<T> holder(ResourceKey<? extends Registry<T>> registryKey, T value) {
        Registry<T> registry = this.registries.registry(registryKey);
        return registry.getResourceKey(value).map(this::holder).orElseThrow(() -> new IllegalArgumentException("Unregistered element in registry " + registryKey.identifier() + ": " + value));
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
        try {
            return registry.getOrThrow(tag);
        } catch (IllegalStateException e) {
            // Tags not available in this datagen stage (tags unbound or tag not yet defined).
            // Create an unbound named holder set — it serializes as "#namespace:path" via
            // unwrap(), so generated JSON correctly references the tag by key without requiring
            // the tag to be resolved. The tag is bound when data packs are loaded in-game.
            return new HolderSet.Named<>(registry, tag);
        }
    }
    
    /**
     * Gets a holder set matching any value from the given registry.
     */
    public final <T> HolderSet<T> any(ResourceKey<? extends Registry<T>> registryKey) {
        return new AnyHolderSet<>(this.registries.registry(registryKey));
    }
    
    /**
     * Gets a holder set matching any value not in the given tag.
     */
    public final <T> HolderSet<T> not(TagKey<T> tag) {
        return new NotHolderSet<>(this.registries.registry(tag.registry()), this.set(tag));
    }
    
    /**
     * Gets a holder set matching any value not in the given holder set.
     */
    public final <T> HolderSet<T> not(ResourceKey<? extends Registry<T>> registryKey, HolderSet<T> set) {
        return new NotHolderSet<>(this.registries.registry(registryKey), set);
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

    /**
     * Runs the provider. The default implementation just invokes {@link #registerFields()}.
     */
    @Override
    public void run() {
        this.registerFields();
    }

    /**
     * Registers all unbound intrusive holders stored in public {@link Holder} fields in this class to their respective
     * registries.
     */
    protected final void registerFields() {
        try {
            for (Field field : this.getClass().getFields()) {
                if (field.getDeclaringClass() != this.getClass()) continue; // Skip fields from superclasses
                if (!Modifier.isPublic(field.getModifiers())) continue;
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (!Holder.class.isAssignableFrom(field.getType())) continue;
                Holder<?> value = (Holder<?>) field.get(this);
                if (value instanceof Holder.Reference<?> ref) {
                    if (ref.type == Holder.Reference.Type.INTRUSIVE && !ref.isBound()) {
                        ResourceKey<? extends Registry<?>> registryKey = this.registries.findRegistryFor(ref);
                        if (registryKey == null) throw new IllegalStateException("Can't infer target registry for " + field.getName() + " in '" + this.getName() + "'. Was the holder created properly?");
                        Identifier id;
                        Id idObj = field.getAnnotation(Id.class);
                        if (idObj != null) {
                            id = Identifier.fromNamespaceAndPath(idObj.namespace().isEmpty() ? this.mod.modid : idObj.namespace(), idObj.value());
                        } else {
                            StringBuilder sb = new StringBuilder();
                            for (char chr : field.getName().toCharArray()) {
                                if (Character.isUpperCase(chr)) {
                                    sb.append('_');
                                }
                                sb.append(Character.toLowerCase(chr));
                            }
                            id = Identifier.fromNamespaceAndPath(this.mod.modid, sb.toString());
                        }
                        //noinspection unchecked
                        this.registries.writableRegistry((ResourceKey<? extends Registry<Object>>) registryKey).register(ResourceKey.create((ResourceKey<? extends Registry<Object>>) registryKey, id), ref.value(), RegistrationInfo.BUILT_IN);
                    } else if (field.getAnnotation(Id.class) != null) {
                        Id idObj = field.getAnnotation(Id.class);
                        String id = (idObj.namespace().isEmpty() ? this.mod.modid : idObj.namespace()) + ":" + idObj.value();
                        LibX.logger.warn("Skipping bound holder {} with explicit id {} in '{}'", field.getName(), id, this.getName());
                    }
                } else {
                    LibX.logger.warn("Skipping direct holder in '{}' (from {})", this.getName(), field.getName());
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to register element for provider '" + this.getName() + "'", e);
        }
    }
}
