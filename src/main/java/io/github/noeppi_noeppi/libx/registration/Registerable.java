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
    
    // TODO strict super
    default void buildAdditionalRegisters(RegistrationContext ctx, EntryCollector builder) {
        
    }
    
    interface EntryCollector {

        <T> void register(@Nullable ResourceKey<? extends Registry<T>> registry, T value);
        <T> void registerNamed(@Nullable ResourceKey<? extends Registry<T>> registry, String name, T value);
        
        <T> void registerMulti(@Nullable ResourceKey<? extends Registry<T>> registry, MultiRegisterable<T> value);
        <T> void registerMultiNamed(@Nullable ResourceKey<? extends Registry<T>> registry, String name, MultiRegisterable<T> value);
        
        <T> Holder<T> createHolder(@Nullable ResourceKey<? extends Registry<T>> registry, T value);
        <T> Holder<T> createNamedHolder(@Nullable ResourceKey<? extends Registry<T>> registry, String name, T value);
    }
}
