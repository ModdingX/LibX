package org.moddingx.libx.impl.loot.modifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RemovalLootModifier extends LootModifier {

    public static final MapCodec<RemovalLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance).and(
            BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("items").forGetter(lm -> lm.items)
    ).apply(instance, RemovalLootModifier::new));

    private final List<Item> items;

    public RemovalLootModifier(List<Item> items, LootItemCondition... conditions) {
        this(items, conditions, IGlobalLootModifier.DEFAULT_PRIORITY);
    }

    public RemovalLootModifier(List<Item> items, int priority, LootItemCondition... conditions) {
        this(items, conditions, priority);
    }

    private RemovalLootModifier(List<Item> items, LootItemCondition[] conditions, int priority) {
        this(ConditionUserBuilder.buildCondition(Arrays.stream(conditions).map(Holder::direct).toList()), priority, items);
    }

    private RemovalLootModifier(Optional<Holder<LootItemCondition>> condition, int priority, List<Item> items) {
        super(condition, priority);
        this.items = List.copyOf(items);
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(@Nonnull ObjectArrayList<ItemStack> loot, @Nonnull LootContext context) {
        loot.removeIf(stack -> this.items.contains(stack.getItem()));
        return loot;
    }

    @Nonnull
    @Override
    public MapCodec<? extends RemovalLootModifier> codec() {
        return CODEC;
    }
}
