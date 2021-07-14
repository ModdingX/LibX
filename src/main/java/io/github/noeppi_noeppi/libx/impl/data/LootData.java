package io.github.noeppi_noeppi.libx.impl.data;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.*;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.loot.functions.SetDamage;
import net.minecraft.loot.functions.SetNBT;

import java.util.function.Function;

public class LootData {

    public static StandaloneLootEntry.Builder<?> stack(ItemStack stack) {
        StandaloneLootEntry.Builder<?> entry = ItemLootEntry.builder(stack.getItem());
        if (stack.getCount() != 1) {
            entry.acceptFunction(SetCount.builder(ConstantRange.of(stack.getCount())));
        }
        if (stack.getDamage() != 0) {
            float damage = (stack.getMaxDamage() - stack.getDamage()) / (float) stack.getMaxDamage();
            entry.acceptFunction(SetDamage.builder(new RandomValueRange(damage)));
        }
        if (stack.hasTag()) {
            entry.acceptFunction(SetNBT.builder(stack.getOrCreateTag()));
        }
        return entry;
    }
    
    public static <T> LootEntry.Builder<?> combineBy(Function<LootEntry.Builder<?>[], LootEntry.Builder<?>> combineFunc, Function<T, LootEntry.Builder<?>> extract, T[] loot) {
        LootEntry.Builder<?>[] builder = new LootEntry.Builder<?>[loot.length];
        for (int i = 0; i < loot.length; i++) {
            builder[i] = extract.apply(loot[i]);
        }
        return combineBy(combineFunc, builder);
    }
    
    public static LootEntry.Builder<?> combineBy(Function<LootEntry.Builder<?>[], LootEntry.Builder<?>> combineFunc, LootEntry.Builder<?>[] loot) {
        if (loot.length == 0) {
            return EmptyLootEntry.builder();
        } else if (loot.length == 1) {
            return loot[0];
        } else {
            return combineFunc.apply(loot);
        }
    }
}
