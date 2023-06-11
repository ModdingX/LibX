package org.moddingx.libx.base.decoration;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A context that defines, what {@link DecorationType elements} should be registered with a {@link DecoratedBlock}.
 */
public class DecorationContext {

    /**
     * Generic context. Registers {@link DecorationType#SLAB slabs} and {@link DecorationType#STAIRS stairs}.
     */
    public static final DecorationContext GENERIC = new DecorationContext("generic", DecorationMaterial.GENERIC,
            DecorationType.BASE, DecorationType.SLAB, DecorationType.STAIRS
    );

    /**
     * Decoration context for wooden planks. Registers {@link DecorationType#SLAB slabs},
     * {@link DecorationType#STAIRS stairs}, {@link DecorationType#FENCE fences},
     * {@link DecorationType#FENCE_GATE fence gates}, {@link DecorationType#WOOD_BUTTON buttons},
     * {@link DecorationType#WOOD_PRESSURE_PLATE pressure plates}, {@link DecorationType#DOOR doors},
     * {@link DecorationType#TRAPDOOR trapdoors} and {@link DecorationType#SIGN signs}.
     */
    public static final DecorationContext PLANKS = new DecorationContext("planks", DecorationMaterial.WOOD,
            DecorationType.BASE, DecorationType.SLAB, DecorationType.STAIRS, DecorationType.FENCE,
            DecorationType.FENCE_GATE, DecorationType.WOOD_BUTTON, DecorationType.WOOD_PRESSURE_PLATE,
            DecorationType.DOOR, DecorationType.TRAPDOOR, DecorationType.SIGN, DecorationType.HANGING_SIGN
    );

    /**
     * Decoration context for stone blocks. Registers {@link DecorationType#SLAB slabs},
     * {@link DecorationType#STAIRS stairs}, {@link DecorationType#WALL walls},
     * {@link DecorationType#STONE_BUTTON buttons} and {@link DecorationType#STONE_PRESSURE_PLATE pressure plates}.
     */
    public static final DecorationContext STONE = new DecorationContext("stone", DecorationMaterial.STONE,
            DecorationType.BASE, DecorationType.SLAB, DecorationType.STAIRS, DecorationType.WALL,
            DecorationType.STONE_BUTTON, DecorationType.STONE_PRESSURE_PLATE
    );
    
    private final String name;
    private final DecorationMaterial material;
    private final Map<String, DecorationType<?>> types;
    private final Set<DecorationType<?>> typeSet;

    /**
     * Creates a new decoration context.
     *
     * @param name The name of the context.
     * @param types The types to register. This must include {@link DecorationType#BASE}.
     */
    public DecorationContext(String name, DecorationMaterial material, DecorationType<?>... types) {
        this.name = name;
        this.material = material;
        Map<String, DecorationType<?>> typeMap = new HashMap<>();
        for (DecorationType<?> type : types) {
            String typeName = type.name();
            if (typeName.isEmpty() && type != DecorationType.BASE) {
                throw new IllegalArgumentException("Only the base decoration type may have an empty name.");
            } else if (typeMap.containsKey(typeName)) {
                throw new IllegalArgumentException("A decoration context can't have multiple types with the same name.");
            } else {
                typeMap.put(typeName, type);
            }
        }
        if (!typeMap.containsKey("") || typeMap.get("") != DecorationType.BASE) {
            throw new IllegalArgumentException("Can't create decoration context without the base type.");
        }
        this.types = ImmutableMap.copyOf(typeMap);
        this.typeSet = ImmutableSet.copyOf(this.types.values());
    }

    /**
     * Gets the base material of the {@link DecorationContext}.
     */
    public DecorationMaterial material() {
        return this.material;
    }

    /**
     * Gets whether this context has a given {@link DecorationType}.
     */
    public boolean has(DecorationType<?> type) {
        if (type == DecorationType.BASE) return true;
        String name = type.name();
        return this.types.containsKey(name) && this.types.get(name) == type;
    }
    
    /**
     * Gets all supported {@link DecorationType}s.
     */
    public Set<DecorationType<?>> types() {
        return this.typeSet;
    }
    
    public RegistrationInfo register(ModX mod, DecoratedBlock block) {
        ImmutableMap.Builder<DecorationType<?>, DecorationType.DecorationElement<?, ?>> elementMap = ImmutableMap.builder();
        ImmutableMap.Builder<String, Registerable> registerMap = ImmutableMap.builder();
        for (Map.Entry<String, DecorationType<?>> entry : this.types.entrySet()) {
            DecorationType.DecorationElement<?, ?> element = entry.getValue().element(mod, this, block);
            Objects.requireNonNull(element.element(), "DecorationType registered a null element: "+ entry.getKey() + " - " + entry.getValue());
            elementMap.put(entry.getValue(), element);
            // Don't add base type to register map, as it is registered through the DecoratedBock class itself.
            if (!entry.getKey().isEmpty()) {
                registerMap.put(entry.getKey(), new Registerable() {
                    
                    @Override
                    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
                        element.registerTo(builder);
                        //noinspection unchecked
                        ((DecorationType<Object>) entry.getValue()).registerAdditional(mod, DecorationContext.this, block, element.element(), ctx, builder);
                    }
                });
            }
        }
        return new RegistrationInfo(elementMap.build(), registerMap.build());
    }

    @Override
    public String toString() {
        return this.name + "[" + this.types.values().stream().map(DecorationType::name).filter(s -> !s.isEmpty()).sorted().collect(Collectors.joining(",")) + "]";
    }

    // registerMap entries are registered without a registry
    public record RegistrationInfo(Map<DecorationType<?>, DecorationType.DecorationElement<?, ?>> elementMap, Map<String, Registerable> registerMap) {}
}
