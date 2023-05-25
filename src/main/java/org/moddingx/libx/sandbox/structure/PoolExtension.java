package org.moddingx.libx.sandbox.structure;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.moddingx.libx.sandbox.SandBox;

import java.util.List;

/**
 * Specifies an extension to a {@link StructureTemplatePool template pool}.
 */
public class PoolExtension {
    
    public static final Codec<PoolExtension> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("pool").forGetter(p -> p.pool.location()),
            Codec.BOOL.optionalFieldOf("required", false).forGetter(p -> p.required),
            Codec.mapPair(
                    StructurePoolElement.CODEC.fieldOf("element"),
                    Codec.intRange(1, 150).fieldOf("weight")
            ).codec().listOf().fieldOf("elements").forGetter(p -> p.elements)
    ).apply(instance, PoolExtension::new));

    public static final Codec<Holder<PoolExtension>> CODEC = RegistryFileCodec.create(SandBox.TEMPLATE_POOL_EXTENSION, DIRECT_CODEC);
    
    private final ResourceKey<StructureTemplatePool> pool;
    private final boolean required;
    private final List<Pair<StructurePoolElement, Integer>> elements;

    private PoolExtension(ResourceLocation poolId, boolean required, List<Pair<StructurePoolElement, Integer>> elements) {
        this(ResourceKey.create(Registries.TEMPLATE_POOL, poolId), required, elements);
    }
    
    public PoolExtension(ResourceKey<StructureTemplatePool> pool, List<Pair<StructurePoolElement, Integer>> elements) {
        this(pool, false, elements);
    }
    
    public PoolExtension(ResourceKey<StructureTemplatePool> pool, boolean required, List<Pair<StructurePoolElement, Integer>> elements) {
        this.pool = pool;
        this.required = required;
        this.elements = List.copyOf(elements);
    }

    /**
     * Gets the pool id to extend.
     */
    public ResourceKey<StructureTemplatePool> pool() {
        return this.pool;
    }

    /**
     * Gets whether the extension is required. Required extensions will throw an exception if the
     * target pool does not exist.
     */
    public boolean required() {
        return this.required;
    }

    /**
     * Gets the list of {@link StructurePoolElement elements} and weights to extend
     * the {@link StructureTemplatePool template pool}.
     */
    public List<Pair<StructurePoolElement, Integer>> elements() {
        return this.elements;
    }
}
