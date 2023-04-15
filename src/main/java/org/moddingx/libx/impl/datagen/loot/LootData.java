package org.moddingx.libx.impl.datagen.loot;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
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
        if (stack.hasTag()) {
            //noinspection deprecation
            entry.apply(SetNbtFunction.setTag(stack.getOrCreateTag()));
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
            return loot.get(0);
        } else {
            return combineFunc.apply(loot);
        }
    }
}
