package org.moddingx.libx.annotation.impl;

import com.mojang.serialization.Codec;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.moddingx.libx.codec.MoreCodecs;
import org.moddingx.libx.config.ConfigManager;
import org.moddingx.libx.config.mapper.GenericValueMapper;
import org.moddingx.libx.config.mapper.MapperFactory;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.impl.ModInternal;
import org.moddingx.libx.impl.reflect.ReflectionHacks;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.mod.ModXRegistration;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ProcessorInterface {

    public static boolean isDistClient() {
        return FMLLoader.getDist() == Dist.CLIENT;
    }
    
    public static ResourceLocation newRL(String rl) {
        return ResourceLocation.parse(rl);
    }

    public static ResourceLocation newRL(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static void registerConfig(ModX mod, String name, Class<?> configClass, boolean client) {
        ConfigManager.registerConfig(mod.resource(name), configClass, client);
    }

    public static void registerConfigMapper(ModX mod, ValueMapper<?, ?> mapper) {
        ConfigManager.registerValueMapper(mod.modid, mapper);
    }

    public static void registerConfigMapper(ModX mod, GenericValueMapper<?, ?, ?> mapper) {
        ConfigManager.registerValueMapper(mod.modid, mapper);
    }

    public static void registerConfigMapper(ModX mod, MapperFactory<?> mapper) {
        ConfigManager.registerValueMapperFactory(mod.modid, mapper);
    }

    public static void register(ModX mod, @Nullable ResourceKey<? extends Registry<?>> registryKey, String name, Object value) throws ReflectiveOperationException {
        if (!(mod instanceof ModXRegistration reg)) throw new IllegalStateException("Can't register to a non-ModXRegistration mod.");
        //noinspection unchecked
        reg.register((ResourceKey<? extends Registry<Object>>) registryKey, name, value);
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

    public static <T extends Event> void addModListener(ModX mod, Class<T> event, Consumer<T> listener) {
        ModInternal.get(mod).modEventBus().addListener(EventPriority.NORMAL, false, event, listener);
    }

    public static <T extends Event> void addLowModListener(ModX mod, Class<T> event, Consumer<T> listener) {
        ModInternal.get(mod).modEventBus().addListener(EventPriority.LOW, false, event, listener);
    }

    public static <K, V> LazyMapBuilder<K, V> lazyMapBuilder() {
        return new LazyMapBuilder<>();
    }

    public static boolean isModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }

    public static void addSpecialModel(ModelEvent.RegisterAdditional event, ResourceLocation id) {
        event.register(new ModelResourceLocation(id, ModelResourceLocation.STANDALONE_VARIANT));
    }

    public static BakedModel getSpecialModel(ModelEvent.BakingCompleted event, ResourceLocation id) {
        ModelResourceLocation modelId = new ModelResourceLocation(id, ModelResourceLocation.STANDALONE_VARIANT);
        if (event.getModels().containsKey(modelId)) {
            return event.getModels().get(modelId);
        } else {
            throw new IllegalStateException("Model not loaded: " + id);
        }
    }

    @FunctionalInterface
    public interface ThrowingRunnable {

        void run() throws Exception;
    }
}
