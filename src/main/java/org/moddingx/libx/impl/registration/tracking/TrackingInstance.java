package org.moddingx.libx.impl.registration.tracking;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.tracking.RegistryTracker;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class TrackingInstance implements Registerable.TrackingCollector {

    private final ResourceLocation baseId;
    private final Object instance;

    public TrackingInstance(ResourceLocation baseId, Object instance) {
        this.baseId = baseId;
        this.instance = instance;
    }

    @Override
    public void track(IForgeRegistry<?> registry, Field field) {
        RegistryTracker.track(registry, field, this.instance, this.baseId);
    }

    @Override
    public void trackNamed(IForgeRegistry<?> registry, String name, Field field) {
        RegistryTracker.track(registry, field, this.instance, new ResourceLocation(this.baseId.getNamespace(), this.baseId.getPath() + "_" + name));
    }

    @Override
    public <T extends IForgeRegistryEntry<T>> void run(IForgeRegistry<T> registry, Consumer<T> action) {
        RegistryTracker.run(registry, action, this.instance, this.baseId);
    }

    @Override
    public <T extends IForgeRegistryEntry<T>> void runNamed(IForgeRegistry<T> registry, String name, Consumer<T> action) {
        RegistryTracker.run(registry, action, this.instance, new ResourceLocation(this.baseId.getNamespace(), this.baseId.getPath() + "_" + name));
    }
}
