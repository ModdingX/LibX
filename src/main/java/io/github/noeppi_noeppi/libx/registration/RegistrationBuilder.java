package io.github.noeppi_noeppi.libx.registration;

import io.github.noeppi_noeppi.libx.impl.registration.resolution.ForgeRegistryResolver;
import io.github.noeppi_noeppi.libx.impl.registration.resolution.VanillaRegistryResolver;
import io.github.noeppi_noeppi.libx.registration.resolution.RegistryResolver;
import io.github.noeppi_noeppi.libx.registration.resolution.ResolvedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RegistrationBuilder {
    
    private final List<RegistryResolver> resolvers;
    
    public RegistrationBuilder() {
        this.resolvers = new ArrayList<>();
    }
    
    public RegistrationBuilder resolve(RegistryResolver resolver) {
        this.resolvers.add(resolver);
        return this;
    }
    
    public <T> RegistrationBuilder resolve(ResourceKey<? extends Registry<T>> id, ResolvedRegistry<T> registry) {
        return this.resolve(new RegistryResolver() {
            
            @Override
            public <A> Optional<ResolvedRegistry<A>> resolve(ResourceKey<? extends Registry<A>> key) {
                //noinspection unchecked
                return id.equals(key) ? Optional.of((ResolvedRegistry<A>) registry) : Optional.empty();
            }
        });
    }
    
    public <T> RegistrationBuilder resolve(ResourceKey<? extends Registry<T>> id, Registry<T> registry) {
        return this.resolve(id, new ResolvedRegistry.Vanilla<>(registry));
    }
    
    public Result build() {
        List<RegistryResolver> resolvers = Stream.concat(this.resolvers.stream(), Stream.of(
                ForgeRegistryResolver.INSTANCE,
                new VanillaRegistryResolver(Registry.REGISTRY),
                new VanillaRegistryResolver(BuiltinRegistries.REGISTRY)
        )).toList();
        return new Result(resolvers);
    }
    
    public static record Result(List<RegistryResolver> resolvers) {}
}
