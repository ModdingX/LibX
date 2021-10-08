package io.github.noeppi_noeppi.libx.base.decoration;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.Registerable;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public class DecorationContext {

    public static final DecorationContext GENERIC = new DecorationContext("stone",
            DecorationType.BASE, DecorationType.SLAB, DecorationType.STAIR
    );
    
    public static final DecorationContext PLANKS = new DecorationContext("planks",
            DecorationType.BASE, DecorationType.SLAB, DecorationType.STAIR, DecorationType.FENCE,
            DecorationType.FENCE_GATE, DecorationType.WOOD_BUTTON, DecorationType.WOOD_PRESSURE_PLATE,
            DecorationType.DOOR, DecorationType.TRAPDOOR, DecorationType.SIGN
    );

    public static final DecorationContext STONE = new DecorationContext("stone",
            DecorationType.BASE, DecorationType.SLAB, DecorationType.STAIR, DecorationType.WALL,
            DecorationType.STONE_BUTTON, DecorationType.STONE_PRESSURE_PLATE
    );
    
    private final String name;
    private final Map<String, DecorationType<?>> types;
    private final Set<DecorationType<?>> typeSet;

    public DecorationContext(String name, DecorationType<?>... types) {
        this.name = name;
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

    public boolean has(DecorationType<?> type) {
        if (type == DecorationType.BASE) return true;
        String name = type.name();
        return this.types.containsKey(name) && this.types.get(name) == type;
    }
    
    public Set<DecorationType<?>> types() {
        return this.typeSet;
    }
    
    public RegistrationInfo register(ModX mod, DecoratedBlock block) {
        ImmutableMap.Builder<DecorationType<?>, Object> elementMap = ImmutableMap.builder();
        ImmutableMap.Builder<String, Object> registerMap = ImmutableMap.builder();
        for (Map.Entry<String, DecorationType<?>> entry : this.types.entrySet()) {
            Object element = entry.getValue().registration(mod, this, block);
            elementMap.put(entry.getValue(), element);
            if (!entry.getKey().isEmpty()) {
                //noinspection unchecked
                Set<Object> additional = ((DecorationType<Object>) entry.getValue()).additionalRegistration(mod, this, block, element);
                if (additional.isEmpty()) {
                    registerMap.put(entry.getKey(), element);
                } else {
                    registerMap.put(entry.getKey(), new Registerable() {
                        @Override
                        public Set<Object> getAdditionalRegisters(ResourceLocation id) {
                            Set<Object> set = new HashSet<>(additional);
                            set.add(element);
                            return Collections.unmodifiableSet(set);
                        }
                    });
                }
            }
        }
        return new RegistrationInfo(elementMap.build(), registerMap.build());
    }

    @Override
    public String toString() {
        return this.name + "[" + this.types.values().stream().map(DecorationType::name).filter(s -> !s.isEmpty()).sorted().collect(Collectors.joining(",")) + "]";
    }

    public static record RegistrationInfo(Map<DecorationType<?>, Object> elementMap, Map<String, Object> registerMap) {}
}
