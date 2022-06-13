package org.moddingx.libx.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.moddingx.libx.codec.CodecHelper;

import java.util.HashMap;
import java.util.Map;

// TODO still needed?
public class ForgeRegistryCodec<A> implements Codec<A> {
    
    private static final Map<IForgeRegistry<?>, ForgeRegistryCodec<?>> INSTANCES = new HashMap<>();
    
    public static synchronized <A> ForgeRegistryCodec<A> get(IForgeRegistry<A> registry) {
        //noinspection unchecked
        return (ForgeRegistryCodec<A>) INSTANCES.computeIfAbsent(registry, ForgeRegistryCodec::new);
    }
    
    private final IForgeRegistry<A> registry;

    private ForgeRegistryCodec(IForgeRegistry<A> registry) {
        this.registry = registry;
    }

    @Override
    public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
        ResourceLocation id = this.registry.getKey(input);
        if (id == null) return DataResult.error("Value not present in registry " + this.registry.getRegistryName() + ": " + input);
        return ops.mergeToPrimitive(prefix, ops.createString(id.toString()));
    }

    @Override
    public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getStringValue(input)
                .flatMap(str -> CodecHelper.nonNull(ResourceLocation.tryParse(str), "Invalid resource location: " + str))
                .flatMap(id -> CodecHelper.nonNull(this.registry.getValue(id), "Value not present in registry " + this.registry.getRegistryName() + ": " + id))
                .map(r -> Pair.of(r, ops.empty()));
    }
}
