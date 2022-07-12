package org.moddingx.libx.annotation.impl;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.moddingx.libx.codec.MoreCodecs;
import org.moddingx.libx.impl.ModInternal;
import org.moddingx.libx.impl.reflect.ReflectionHacks;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.mod.ModXRegistration;
import org.moddingx.libx.registration.MultiRegisterable;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.function.Consumer;

public class ProcessorInterface {

    public static ResourceLocation newRL(String rl) {
        return new ResourceLocation(rl);
    }
    
    public static ResourceLocation newRL(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }
    
    public static void register(ModX mod, @Nullable ResourceKey<? extends Registry<?>> registryKey, String name, Object value, @Nullable FieldGetter field, boolean multi) throws ReflectiveOperationException {
        if (!(mod instanceof ModXRegistration reg)) throw new IllegalStateException("Can't register to a non-ModXRegistration mod.");
        if (multi) {
            if (!(value instanceof MultiRegisterable<?> multiReg)) throw new IllegalStateException("Can't multi-register a non-MultiRegisterable.");
            //noinspection unchecked
            reg.registerMulti((ResourceKey<? extends Registry<Object>>) registryKey, name, (MultiRegisterable<Object>) multiReg);
        } else {
            //noinspection unchecked
            reg.register((ResourceKey<? extends Registry<Object>>) registryKey, name, value);
            
            // Only directly add registry tracking for actually registered stuff, MultiRegisterable has no real registry
            if (registryKey != null && field != null) {
                IForgeRegistry<?> forgeRegistry = RegistryManager.ACTIVE.getRegistry(registryKey.location());
                if (forgeRegistry != null) {
                    ModInternal.get(mod).getRegistrationDispatcher().notifyRegisterField(forgeRegistry, name, field.get());
                }
            }
        }
    }
    
    // For code with checked exceptions that we know are never thrown
    public static void runUnchecked(ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            ReflectionHacks.throwUnchecked(e);
        }
    }
    
    public static <T extends Enum<T>> Codec<T> enumCodec(Class<T> clazz) {
        return MoreCodecs.enumCodec(clazz);
    }
    
    public static <T extends Event> void addModListener(Class<T> event, Consumer<T> listener) {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.NORMAL, false, event, listener);
    }
    
    public static <T extends Event> void addForgeListener(Class<T> event, Consumer<T> listener) {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, event, listener);
    }
    
    public static void addDataProvider(GatherDataEvent event, DataProvider provider) {
        event.getGenerator().addProvider(true, provider);
    }
    
    public static DataGenerator getDataGenerator(GatherDataEvent event) {
        return event.getGenerator();
    }
    
    public static ExistingFileHelper getDataFileHelper(GatherDataEvent event) {
        return event.getExistingFileHelper();
    }
    
    public static <K, V> LazyMapBuilder<K, V> lazyMapBuilder() {
        return new LazyMapBuilder<>();
    }

    public static boolean isModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }
    
    @FunctionalInterface
    public static interface ThrowingRunnable {
        
        public void run() throws Exception;
    }
    
    @FunctionalInterface
    public static interface FieldGetter {
        
        public Field get() throws ReflectiveOperationException;
    }
}
