package io.github.noeppi_noeppi.libx.annotation.impl;

import com.mojang.serialization.Codec;
import io.github.noeppi_noeppi.libx.codec.MoreCodecs;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.util.function.Consumer;

public class ProcessorInterface {

    public static ResourceLocation newRL(String rl) {
        return new ResourceLocation(rl);
    }
    
    public static ResourceLocation newRL(String namespace, String path) {
        return new ResourceLocation(namespace, path);
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
        event.getGenerator().addProvider(provider);
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
}
