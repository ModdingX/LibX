package io.github.noeppi_noeppi.libx.registration;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public interface Registerable {
    
    default void registerCommon(SetupContext ctx) {
        
    }
    
    @OnlyIn(Dist.CLIENT)
    default void registerClient(SetupContext ctx) {
        
    }
    
    default void buildAdditionalRegisters(EntryCollector builder) {
        
    } 
    
    interface EntryCollector {
        
        <T> void register(@Nullable ResourceKey<? extends Registry<T>> registry, T value);
        <T> void registerNamed(@Nullable ResourceKey<? extends Registry<T>> registry, String name, T value);
        
        <T> Holder<T> createHolder(@Nullable ResourceKey<? extends Registry<T>> registry, T value);
        <T> Holder<T> createNamedHolder(@Nullable ResourceKey<? extends Registry<T>> registry, String name, T value);
    }
}
