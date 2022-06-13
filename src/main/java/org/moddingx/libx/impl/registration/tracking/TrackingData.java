package org.moddingx.libx.impl.registration.tracking;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.reflect.ReflectionHacks;
import org.moddingx.libx.registration.MultiRegisterable;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class TrackingData<T> {

    public final ResourceLocation registryId;
    public final IForgeRegistry<T> registry;
    
    private final List<TrackedStaticField> staticFields;
    private final List<TrackedInstanceField> instanceFields;
    private final List<TrackedInstanceAction<T>> actions;
    private final Set<TrackedFieldKey> trackedFields;

    public TrackingData(IForgeRegistry<T> registry) {
        this.registryId = registry.getRegistryName();
        this.registry = registry;
        this.staticFields = new ArrayList<>();
        this.instanceFields = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.trackedFields = new HashSet<>();
    }

    public synchronized void addStatic(ResourceLocation id, Field field) {
        if (!Modifier.isStatic(field.getModifiers())) {
            throw new IllegalStateException("Can't track registry element field: Must be static: " + field);
        } else {
            TrackedFieldKey key = TrackedFieldKey.create(field, null);
            if (!this.trackedFields.contains(key)) {
                this.staticFields.add(new TrackedStaticField(id, field));
                this.trackedFields.add(key);
            }
        }
    }

    public synchronized void addInstance(ResourceLocation id, Field field, Object instance) {
        if (Modifier.isStatic(field.getModifiers())) {
            throw new IllegalStateException("Can't track registry instance field: Must not be static: " + field);
        } else if (!field.getDeclaringClass().isAssignableFrom(instance.getClass())) {
            throw new IllegalStateException("Can't track registry instance field: Instance object is of type " + instance.getClass() + ", expected " + field.getDeclaringClass() + ".");
        } else {
            TrackedFieldKey key = TrackedFieldKey.create(field, instance);
            if (!this.trackedFields.contains(key)) {
                this.instanceFields.add(new TrackedInstanceField(id, field, new WeakReference<>(instance)));
                this.trackedFields.add(key);
            }
        }
    }

    public synchronized void addAction(ResourceLocation id, Object instance, Consumer<T> action) {
        this.actions.add(new TrackedInstanceAction<>(id, new WeakReference<>(instance), action));
    }
    
    public synchronized void apply(Predicate<ResourceLocation> changed, @Nullable Predicate<Object> instanceChanged, Consumer<Runnable> enqueue, Consumer<Object> valueUpdate) {
        if (changed.test(this.registryId)) {
            if (instanceChanged == null) {
                for (TrackedStaticField field : this.staticFields) {
                    this.updateFrom(field.id(), field.field(), null, enqueue, valueUpdate);
                }
            }

            {
                Iterator<TrackedInstanceField> itr = this.instanceFields.iterator();
                while (itr.hasNext()) {
                    TrackedInstanceField field = itr.next();
                    Object instance = field.instance().get();
                    if (instance == null) {
                        itr.remove(); // Object has been cleared by garbage collector, don't track field any longer.
                    } else if (instanceChanged == null || instanceChanged.test(instance)) {
                        this.updateFrom(field.id(), field.field(), instance, enqueue, valueUpdate);
                    }
                }
            }

            {
                Iterator<TrackedInstanceAction<T>> itr = this.actions.iterator();
                while (itr.hasNext()) {
                    TrackedInstanceAction<T> action = itr.next();
                    Object instance = action.instance().get();
                    if (instance == null) {
                        itr.remove(); // Object has been cleared by garbage collector, don't track action any longer.
                    } else {
                        T value = this.registry.getValue(action.id());
                        if (value == null) {
                            throw new IllegalStateException("Tracked registry object not present for action: " + this.registryId + " / " + action.id() + ".");
                        } else if (instanceChanged == null || instanceChanged.test(instance)) {
                            action.action().accept(value);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return this.registryId.hashCode();
    }
    
    private void updateFrom(ResourceLocation id, Field field, Object instance, Consumer<Runnable> enqueue, Consumer<Object> valueUpdate) {
        try {
            //noinspection unchecked
            T oldValue = (T) field.get(instance);
            T value = this.registry.getValue(id);
            if (value == null) {
                throw new IllegalStateException("Tracked registry object not present: " + this.registryId + " / " + id + ", was " + oldValue + " before.");
            } else if (!field.getType().isAssignableFrom(value.getClass())) {
                throw new IllegalStateException("Tracked registry object has invalid type: " + this.registryId + " / " + id + ", was " + oldValue + " before. Probably a failed registry replacement. Expected: " + field.getType());
            } else if (value != oldValue) {
                if (Modifier.isFinal(field.getModifiers())) {
                    try {
                        ReflectionHacks.setFinalField(field, instance, value);
                    } catch (Exception e) {
                        throw new ReflectiveOperationException("Failed to set final tracked registry field " + field, e);
                    }
                } else {
                    field.setAccessible(true);
                    field.set(instance, value);
                }
                // A field was updated. It's instance fields need updating now as well.
                // First add it to the tracker as well.
                // Can't be done immediately or it would deadlock
                // Submit it to be done before the next round
                enqueue.accept(() -> {
                    RegistrationContext ctx = new RegistrationContext(id, this.registry.getRegistryKey());
                    try {
                        if (value instanceof Registerable registerable) {
                            registerable.initTracking(ctx, new TrackingInstance(id, value));
                        } else if (value instanceof MultiRegisterable<?> registerable) {
                            registerable.initTracking(ctx, new TrackingInstance(id, value));
                        }
                    } catch (ReflectiveOperationException e) {
                        LibX.logger.error("Failed to update instance tracking for " + value + " (" + id + "/" + this.registry.getRegistryName() + ")", e);
                    }
                });
                valueUpdate.accept(value);
            }
        } catch (ReflectiveOperationException e) {
            LibX.logger.error("Failed to update registry object: " + this.registryId + " / " + id, e);
        }
    }

    private static record TrackedStaticField(ResourceLocation id, Field field) {}
    private static record TrackedInstanceField(ResourceLocation id, Field field, WeakReference<Object> instance) {}
    private static record TrackedInstanceAction<T>(ResourceLocation id, WeakReference<Object> instance, Consumer<T> action) {}
    
    // Provide key with weak reference that allows hashing on fields and their instance
    private static record TrackedFieldKey(Field field, @Nullable WeakReference<Object> instance, int instanceHash) {
        
        public static TrackedFieldKey create(Field field, @Nullable Object instance) {
            return new TrackedFieldKey(field, instance == null ? null : new WeakReference<>(instance), System.identityHashCode(instance));
        }

        @Override
        public int hashCode() {
            return this.instanceHash ^ this.field.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TrackedFieldKey key)) return false;
            if (!Objects.equals(this.field(), key.field())) return false;
            if (this.instance() == null && key.instance() == null) return true;
            if (this.instance() == null || key.instance() == null) return false;
            Object instance = this.instance().get();
            return instance != null && key.instance().refersTo(instance);
        }
    }
}
