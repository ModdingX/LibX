package org.moddingx.libx.impl.registration.tracking;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.tracking.RegistryTracker;

import java.lang.reflect.Field;

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
}
