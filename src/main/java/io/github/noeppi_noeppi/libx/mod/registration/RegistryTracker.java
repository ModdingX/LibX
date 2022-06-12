package io.github.noeppi_noeppi.libx.mod.registration;

import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.annotation.meta.Experimental;
import io.github.noeppi_noeppi.libx.impl.reflect.ReflectionHacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.ObjectHolderRegistry;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Provides a way to track the values of fields with a registry object. That means if the registry object is
 * replaced, the field is updated.
 */
@Experimental
public class RegistryTracker {

    private static final Object LOCK = new Object();
    
    private static final Map<ResourceLocation, RegistryData<?>> trackedRegistries = new HashMap<>();
    private static final Set<Field> trackedFields = new HashSet<>();

    /**
     * Add a field to the list of tracked fields. It will then be updated whenever the registry value changes.
     * This will not ensure the field holds the value matching the registry at the time, the method is called.
     * 
     * @param registry The registry used to track the field.
     * @param field The field to track.
     * @param id The {@link ResourceLocation} used for registering the object
     */
    public static <T extends IForgeRegistryEntry<T>> void trackField(IForgeRegistry<T> registry, Field field, ResourceLocation id) {
        synchronized (LOCK) {
            if (trackedFields.contains(field)) {
                throw new IllegalStateException("Can't track registry element field: Field already tracked: " + field);
            }
            trackedFields.add(field);
            trackedRegistries.computeIfAbsent(registry.getRegistryName(), key -> {
                RegistryData<T> data = new RegistryData<>(registry);
                ObjectHolderRegistry.addHandler(data);
                return data;
            }).add(id, field);
        }
    }

    private static final class RegistryData<T extends IForgeRegistryEntry<T>> implements Consumer<Predicate<ResourceLocation>> {

        public final ResourceLocation registryId;
        public final IForgeRegistry<T> registry;
        private final List<Pair<ResourceLocation, Field>> entries;

        private RegistryData(IForgeRegistry<T> registry) {
            this.registryId = registry.getRegistryName();
            this.registry = registry;
            this.entries = new ArrayList<>();
        }

        public void add(ResourceLocation id, Field field) {
            if (!Modifier.isStatic(field.getModifiers())) {
                throw new IllegalStateException("Can't track registry element field: Must be static: " + field);
            } if (!this.registry.getRegistrySuperType().isAssignableFrom(field.getType())) {
                throw new IllegalStateException("Can't track registry element field: Has type " + field.getType() + ", expected " + this.registry.getRegistrySuperType() + " for value of registry " + this.registryId);
            } else {
                this.entries.add(Pair.of(id, field));
            }
        }

        @Override
        public void accept(Predicate<ResourceLocation> changed) {
            if (changed.test(this.registryId)) {
                for (Pair<ResourceLocation, Field> entry : this.entries) {
                    try {
                        //noinspection unchecked
                        T oldValue = (T) entry.getValue().get(null);
                        T value = this.registry.getValue(entry.getKey());
                        if (value == null) {
                            throw new IllegalStateException("Tracked registry object not present: " + this.registryId + " / " + entry.getKey() + ", was " + oldValue + " before.");
                        } else if (entry.getValue().getType().isAssignableFrom(value.getClass())) {
                            throw new IllegalStateException("Tracked registry object has invalid type: " + this.registryId + " / " + entry.getKey() + ", was " + oldValue + " before. Probably a failed registry replacement.");
                        } else if (value != oldValue) {
                            if (Modifier.isFinal(entry.getValue().getModifiers())) {
                                try {
                                    ReflectionHacks.setFinalField(entry.getValue(), null, value);
                                } catch (Exception e) {
                                    throw new ReflectiveOperationException("Failed to set final tracked registry field " + entry.getValue(), e);
                                }
                            } else {
                                entry.getValue().setAccessible(true);
                                entry.getValue().set(null, value);
                            }
                        }
                    } catch (ReflectiveOperationException e) {
                        LibX.logger.error("Failed to update registry object: " + this.registryId + " / " + entry.getKey(), e);
                    }
                }
            }
        }

        @Override
        public int hashCode() {
            return this.registryId.hashCode();
        }
    }
}
