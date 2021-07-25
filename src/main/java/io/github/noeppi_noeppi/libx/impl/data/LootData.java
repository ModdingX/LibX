package io.github.noeppi_noeppi.libx.impl.data;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

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
            entry.apply(SetNbtFunction.setTag(stack.getOrCreateTag()));
        }
        return entry;
    }
    
    public static <T> LootPoolEntryContainer.Builder<?> combineBy(Function<LootPoolEntryContainer.Builder<?>[], LootPoolEntryContainer.Builder<?>> combineFunc, Function<T, LootPoolEntryContainer.Builder<?>> extract, T[] loot) {
        LootPoolEntryContainer.Builder<?>[] builder = new LootPoolEntryContainer.Builder<?>[loot.length];
        for (int i = 0; i < loot.length; i++) {
            builder[i] = extract.apply(loot[i]);
        }
        return combineBy(combineFunc, builder);
    }
    
    public static LootPoolEntryContainer.Builder<?> combineBy(Function<LootPoolEntryContainer.Builder<?>[], LootPoolEntryContainer.Builder<?>> combineFunc, LootPoolEntryContainer.Builder<?>[] loot) {
        if (loot.length == 0) {
            return EmptyLootItem.emptyItem();
        } else if (loot.length == 1) {
            return loot[0];
        } else {
            return combineFunc.apply(loot);
        }
    }
}
