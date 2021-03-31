package io.github.noeppi_noeppi.libx.annotation;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Consumer;

/**
 * Contains methods the annotation processor code generator uses that rely on minecraft
 * classes. This is here to ensure compatibility between different mappings as these methods
 * are not remapped.
 * <b>THIS IS NOT MEANT TO BE USED BY ANYTHING ELSE BUT THE ANNOTATION PROCESSOR AND THEREFORE MARKED DEPRECATED.</b>
 */
@Deprecated
public class ProcessorInterface {
    
    public static ResourceLocation newRL(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }
    
    public static <T extends Event> void addModListener(Class<T> event, Consumer<T> listener) {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.NORMAL, false, event, listener);
    }
    
    public static <T extends Event> void addForgeListener(Class<T> event, Consumer<T> listener) {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, event, listener);
    }
}
