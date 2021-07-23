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
 * and things to be done in the client.
 */
public interface Registerable {

    /**
     * Gets additional items that should be registered. Those may be {@link Registerable},
     * Items, Block TileEntities, Biomes ...
     */
    default Set<Object> getAdditionalRegisters(ResourceLocation id) {
        return Collections.emptySet();
    }

    /**
     * Gets additional items that should be registered. Those may be {@link Registerable},
     * Items, Block TileEntities, Biomes ... The ones here may have a postfix.
     * That allows to register multiple thing to the same registry. The new id is created
     * from the id of this {@link Registerable}, an underscore and the key in the map.
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
