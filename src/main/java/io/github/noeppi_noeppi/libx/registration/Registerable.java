package io.github.noeppi_noeppi.libx.registration;

import io.github.noeppi_noeppi.libx.mod.ModXRegistration;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

public interface Registerable {
    
    default void registerCommon(Consumer<Runnable> defer) {
        
    }
    
    @OnlyIn(Dist.CLIENT)
    default void registerClient(Consumer<Runnable> defer) {
        
    }
    
    default void buildAdditionalRegisters(EntryCollector builder) {
        
    } 
    
    interface EntryCollector {
        
        <T> void register(ResourceKey<? extends Registry<T>> registry, T value);
        <T> void registerNamed(ResourceKey<? extends Registry<T>> registry, String name, T value);
        
        <T> Holder<T> createHolder(ResourceKey<? extends Registry<T>> registry, T value);
        <T> Holder<T> createNamedHolder(ResourceKey<? extends Registry<T>> registry, String name, T value);
        
        default void register(Registerable value) {
            this.register(ModXRegistration.ANY_REGISTRY, value);
        }
        
        default void registerNamed(String name, Registerable value) {
            this.registerNamed(ModXRegistration.ANY_REGISTRY, name, value);
        }
    }
}
