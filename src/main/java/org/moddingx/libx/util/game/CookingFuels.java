package org.moddingx.libx.util.game;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import org.moddingx.libx.mod.ModX;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CookingFuels {

    private static final Map<ResourceKey<ContextIntProvider>, Integer> GENERATED = new ConcurrentHashMap<>();

    private CookingFuels() {}

    public static ResourceKey<ContextIntProvider> burnTime(ModX mod, int ticks) {
        if (ticks <= 0) {
            throw new IllegalArgumentException("Burn time must be positive: " + ticks);
        }

        ResourceKey<ContextIntProvider> key = ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, mod.id("cooking/time_" + ticks));
        CookingFuels.GENERATED.put(key, ticks);

        return key;
    }

    public static Item.Properties cookingFuel(Item.Properties properties, ModX mod, int ticks) {
        return properties.cookingFuel(CookingFuels.burnTime(mod, ticks));
    }

    public static Map<ResourceKey<ContextIntProvider>, Integer> generatedKeys(String namespace) {
        return CookingFuels.GENERATED.entrySet().stream()
                .filter(entry -> namespace.equals(entry.getKey().identifier().getNamespace()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
