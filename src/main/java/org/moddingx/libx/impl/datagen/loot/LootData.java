package org.moddingx.libx.impl.datagen.loot;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.function.Function;

public class LootData {

    public static LootPoolSingletonContainer.Builder<?> stack(ItemStack stack) {
        LootPoolSingletonContainer.Builder<?> entry = LootItem.lootTableItem(stack.getItem());
        if (stack.getCount() != 1) {
            entry.apply(SetItemCountFunction.setCount(ConstantValue.exactly(stack.getCount())));
        }
        if (stack.getDamageValue() != 0) {
            float damage = (stack.getMaxDamage() - stack.getDamageValue()) / (float) stack.getMaxDamage();
            entry.apply(SetItemDamageFunction.setDamage(ConstantValue.exactly(damage)));
        }
        DataComponentPatch components = stack.getComponentsPatch();
        if (!components.isEmpty()) {
            entry.apply(LootItemConditionalFunction.simpleBuilder(conditions -> new SetComponentsFunction(conditions, components)));
        }
        return entry;
    }
    
    public static <T> LootPoolEntryContainer.Builder<?> combineBy(Function<List<LootPoolEntryContainer.Builder<?>>, LootPoolEntryContainer.Builder<?>> combineFunc, Function<T, LootPoolEntryContainer.Builder<?>> extract, List<T> loot) {
        return combineBy(combineFunc, loot.stream().map(extract).toList());
    }
    
    public static LootPoolEntryContainer.Builder<?> combineBy(Function<List<LootPoolEntryContainer.Builder<?>>, LootPoolEntryContainer.Builder<?>> combineFunc, List<LootPoolEntryContainer.Builder<?>> loot) {
        if (loot.isEmpty()) {
            return EmptyLootItem.emptyItem();
        } else if (loot.size() == 1) {
            return loot.getFirst();
        } else {
            return combineFunc.apply(loot);
        }
    }
}
