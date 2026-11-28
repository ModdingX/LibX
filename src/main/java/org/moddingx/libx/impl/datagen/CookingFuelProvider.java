package org.moddingx.libx.impl.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootPredicates;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConditionalValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;
import org.moddingx.libx.util.game.CookingFuels;

import java.util.Comparator;
import java.util.Map;

public class CookingFuelProvider extends RegistryProviderBase {

    public CookingFuelProvider(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public String getName() {
        return this.mod.modid + " cooking fuels";
    }

    @Override
    public void run() {
        Map<ResourceKey<ContextIntProvider>, Integer> keys = CookingFuels.generatedKeys(this.mod.modid);
        if (keys.isEmpty()) {
            return;
        }

        Registry<ContextIntProvider> registry = this.registries.registry(Registries.CONTEXT_INT_PROVIDER);
        Registry<LootItemCondition> predicates = this.registries.registry(Registries.PREDICATE);
        // Same as vanilla: burn twice as fast in smokers and blast furnaces
        ConditionalValue divisor = new ConditionalValue(
                predicates.getOrThrow(LootPredicates.FAST_FURNACE),
                registry.getOrThrow(ContextIntProviders.COOKING_FAST_BURN_TIME_REDUCTION_FACTOR),
                registry.getOrThrow(ContextIntProviders.COOKING_NORMAL_BURN_TIME_REDUCTION_FACTOR)
        );
        WritableRegistry<ContextIntProvider> writable = this.registries.writableRegistry(Registries.CONTEXT_INT_PROVIDER);
        keys.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceKey::identifier))).forEach(entry -> {
            if (!registry.containsKey(entry.getKey())) {
                ContextIntProvider provider = ContextIntProviders.div(ContextIntProviders.exactly(entry.getValue()), Holder.direct(divisor)).value();
                writable.register(entry.getKey(), provider, RegistrationInfo.BUILT_IN);
            }
        });
    }
}
