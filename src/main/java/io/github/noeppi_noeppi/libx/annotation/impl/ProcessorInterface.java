package io.github.noeppi_noeppi.libx.annotation.impl;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import io.github.noeppi_noeppi.libx.util.LazyImmutableMap;
import io.github.noeppi_noeppi.libx.util.LazyValue;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        if (clazz.equals(DimensionType.class)) return (ResourceKey<Registry<T>>) (ResourceKey) Registry.DIMENSION_TYPE_REGISTRY;
        if (clazz.equals(Biome.class)) return (ResourceKey<Registry<T>>) (ResourceKey) Registry.BIOME_REGISTRY;
        if (clazz.equals(ConfiguredSurfaceBuilder.class)) return (ResourceKey<Registry<T>>) (ResourceKey) Registry.CONFIGURED_SURFACE_BUILDER_REGISTRY;
        if (clazz.equals(ConfiguredWorldCarver.class)) return (ResourceKey<Registry<T>>) (ResourceKey) Registry.CONFIGURED_CARVER_REGISTRY;
        if (clazz.equals(ConfiguredFeature.class)) return (ResourceKey<Registry<T>>) (ResourceKey) Registry.CONFIGURED_FEATURE_REGISTRY;
        if (clazz.equals(ConfiguredStructureFeature.class)) return (ResourceKey<Registry<T>>) (ResourceKey) Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY;
        if (clazz.equals(StructureProcessorList.class)) return (ResourceKey<Registry<T>>) (ResourceKey) Registry.PROCESSOR_LIST_REGISTRY;
        if (clazz.equals(StructureTemplatePool.class)) return (ResourceKey<Registry<T>>) (ResourceKey) Registry.TEMPLATE_POOL_REGISTRY;
        if (clazz.equals(NoiseGeneratorSettings.class)) return (ResourceKey<Registry<T>>) (ResourceKey) Registry.NOISE_GENERATOR_SETTINGS_REGISTRY;
        throw new IllegalStateException("Failed to get registry codec key for type: " + clazz);
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
