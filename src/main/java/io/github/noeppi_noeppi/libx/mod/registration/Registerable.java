package io.github.noeppi_noeppi.libx.mod.registration;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Everything that is registered to {@link ModXRegistration} that implements this can specify dependencies
 * and things to be done during setup phase.
 */
public interface Registerable {

    /**
     * Gets additional items that should be registered. Those may be {@link Registerable} as well.
     * These objects will be registered with the same id as this registerable.
     */
    default Set<Object> getAdditionalRegisters(ResourceLocation id) {
        return Collections.emptySet();
    }

    /**
     * Gets additional items that should be registered. Those may be {@link Registerable} as well.
     * These objects will be registered with the id of this registerable appended by their key in
     * the map. This allows to register multiple thing to the same registry.
     */
    default Map<String, Object> getNamedAdditionalRegisters(ResourceLocation id) {
        return Collections.emptyMap();
    }
    
    /**
     * Do stuff needed in the setup phase. This is called during parallel mod loading.
     * 
     * @param defer Pass a runnable to this to defer it to the synchronous work queue.
     */
    default void registerCommon(ResourceLocation id, Consumer<Runnable> defer) {
        
    }
    
    /**
     * Do stuff needed on the client. This is called during parallel mod loading.
     * 
     * @param defer Pass a runnable to this to defer it to the synchronous work queue.
     */
    @OnlyIn(Dist.CLIENT)
    default void registerClient(ResourceLocation id, Consumer<Runnable> defer) {
        
    }
}
