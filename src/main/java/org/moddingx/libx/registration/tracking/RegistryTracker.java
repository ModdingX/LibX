package org.moddingx.libx.registration.tracking;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolderRegistry;
import org.moddingx.libx.annotation.meta.Experimental;
import org.moddingx.libx.impl.registration.tracking.TrackingData;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

import java.lang.reflect.Field;
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
    
    private static boolean registeredToObjectHolders = false;
    private static final Map<ResourceLocation, TrackingData<?>> trackedRegistries = new HashMap<>();

    /**
     * Add a static field to the list of tracked fields. It will then be updated whenever the registry value changes.
     * This will not ensure the field holds the value matching the registry at the time, the method is called.
     * 
     * @param registry The registry used to track the field.
     * @param field The field to track.
     * @param id The {@link ResourceLocation} used for registered the object
     */
    public static <T> void track(IForgeRegistry<T> registry, Field field, ResourceLocation id) {
        synchronized (LOCK) {
            trackingData(registry).addStatic(id, field);
        }
    }
    
    /**
     * Add an instance field to the list of tracked fields. It will then be updated whenever the registry value changes.
     * This will not ensure the field holds the value matching the registry at the time, the method is called. If the
     * object instance is garbage collected, the tracking will be removed.
     * 
     * If a field is tracked in {@link Registerable#initTracking(RegistrationContext, Registerable.TrackingCollector)}
     * and the tracking of the {@link Registerable} is initialised because the parent object changed due to registry
     * tracking, it will be ensured, that the field is updated as soon as possible to reflect the current registry
     * change. This does not hold true if the tracking is initialised during first registering.
     * 
     * @param registry The registry used to track the field.
     * @param field The field to track.
     * @param instance The object instance on which the field is updated.
     * @param id The {@link ResourceLocation} used for registered the object
     */
    public static <T> void track(IForgeRegistry<T> registry, Field field, Object instance, ResourceLocation id) {
        synchronized (LOCK) {
            trackingData(registry).addInstance(id, field, instance);
        }
    }
    
    /**
     * Add an action that is invoked whenever the object with the given id changes in the registry. The action is tied
     * to an instance object and won't be called any longer if the instance object is garbage collected.
     * hold true if the tracking is initialised during first registering.
     * 
     * @param registry The registry used to track the field.
     * @param action The action to run when the object updates in the registry.
     * @param instance The object instance to which the action is tied.
     * @param id The {@link ResourceLocation} used for registered the object
     */
    public static <T> void run(IForgeRegistry<T> registry, Consumer<T> action, Object instance, ResourceLocation id) {
        synchronized (LOCK) {
            trackingData(registry).addAction(id, instance, action);
        }
    }
    
    private static <T> TrackingData<T> trackingData(IForgeRegistry<T> registry) {
        synchronized (LOCK) {
            if (!registeredToObjectHolders) {
                ObjectHolderRegistry.addHandler(new UpdateConsumer());
                registeredToObjectHolders = true;
            }
            //noinspection unchecked
            return (TrackingData<T>) trackedRegistries.computeIfAbsent(registry.getRegistryName(), k -> new TrackingData<>(registry));
        }
    }

    private static class UpdateConsumer implements Consumer<Predicate<ResourceLocation>> {

        private final List<Runnable> enqueuedTasks = new ArrayList<>();
        private final Set<Object> objectsToUpdate = new HashSet<>();
        
        @Override
        public void accept(Predicate<ResourceLocation> changed) {
            Predicate<Object> instanceChanged = null;
            this.enqueuedTasks.clear();
            this.objectsToUpdate.clear();
            do {
                // enqueued tasks must run without lock as it allows objects to register new registry tracing fields
                this.enqueuedTasks.forEach(Runnable::run);
                this.enqueuedTasks.clear();
                this.objectsToUpdate.clear();
                synchronized (LOCK) {
                    for (Map.Entry<ResourceLocation, TrackingData<?>> entry : trackedRegistries.entrySet()) {
                        entry.getValue().apply(changed, instanceChanged, this.enqueuedTasks::add, this.objectsToUpdate::add);
                    }
                    Set<Object> objectsNextRound = Set.copyOf(this.objectsToUpdate);
                    instanceChanged = objectsNextRound::contains;
                }
            } while (!this.enqueuedTasks.isEmpty() || !this.objectsToUpdate.isEmpty());
        }
    }
}
