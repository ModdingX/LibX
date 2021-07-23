package io.github.noeppi_noeppi.libx.annotation;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import io.github.noeppi_noeppi.libx.util.LazyImmutableMap;
import io.github.noeppi_noeppi.libx.util.LazyValue;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Contains methods the annotation processor code generator uses that rely on minecraft
 * classes. This is here to ensure compatibility between different mappings as these methods
 * are not remapped.
 * <b>THIS IS NOT MEANT TO BE USED BY ANYTHING ELSE BUT THE ANNOTATION PROCESSOR AND THEREFORE MARKED DEPRECATED.</b>
 */
// TODO move in extra package that can be excluded from javadoc
@Deprecated
public class ProcessorInterface {

    public static ResourceLocation newRL(String rl) {
        return new ResourceLocation(rl);
    }
    
    public static ResourceLocation newRL(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }
    
    public static <T> ResourceKey<Registry<T>> rootKey(ResourceLocation id) {
        return ResourceKey.createRegistryKey(id);
    }
    
    public static <T> MapCodec<Registry<T>> registryCodec(ResourceKey<Registry<T>> registry) {
        return RegistryLookupCodec.create(registry);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> ResourceKey<Registry<T>> getCodecDefaultRegistryKey(Class<T> clazz) {
        if (clazz.equals(Biome.class)) {
            return (ResourceKey<Registry<T>>) (ResourceKey) Registry.BIOME_REGISTRY;
        } else if (clazz.equals(NoiseGeneratorSettings.class)) {
            return (ResourceKey<Registry<T>>) (ResourceKey) Registry.NOISE_GENERATOR_SETTINGS_REGISTRY;
        } else {
            throw new IllegalStateException("Failed to get registry codec key for type: " + clazz);
        }
    }
    
    public static <T extends Event> void addModListener(Class<T> event, Consumer<T> listener) {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.NORMAL, false, event, listener);
    }
    
    public static <T extends Event> void addForgeListener(Class<T> event, Consumer<T> listener) {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, event, listener);
    }
    
    public static <K, V> LazyMapBuilder<K, V> lazyMapBuilder() {
        return new LazyMapBuilder<>();
    }
    
    public static class LazyMapBuilder<K, V> {
        
        private final ImmutableMap.Builder<K, LazyValue<V>> builder = ImmutableMap.builder();
        
        public void put(K k, Supplier<V> v) {
            this.builder.put(k, new LazyValue<>(v));
        }
        
        public Map<K, V> build() {
            return new LazyImmutableMap<>(this.builder.build());
        }
    }
}
