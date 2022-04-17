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
    private final List<RegistryCondition> conditions;
    private final List<RegistryTransformer> transformers;
    
    public RegistrationBuilder() {
        this.resolvers = new ArrayList<>();
        this.conditions = new ArrayList<>();
        this.transformers = new ArrayList<>();
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
    
    public RegistrationBuilder condition(RegistryCondition condition) {
        this.conditions.add(condition);
        return this;
    }

    public RegistrationBuilder transformer(RegistryTransformer transformer) {
        this.transformers.add(transformer);
        return this;
    }
    
    public Result build() {
        List<RegistryResolver> resolvers = Stream.concat(this.resolvers.stream(), Stream.of(
                ForgeRegistryResolver.INSTANCE,
                new VanillaRegistryResolver(Registry.REGISTRY),
                new VanillaRegistryResolver(BuiltinRegistries.REGISTRY)
        )).toList();
        return new Result(resolvers, List.copyOf(this.conditions), List.copyOf(this.transformers));
    }
    
    public static record Result(List<RegistryResolver> resolvers, List<RegistryCondition> conditions, List<RegistryTransformer> transformers) {}
}
