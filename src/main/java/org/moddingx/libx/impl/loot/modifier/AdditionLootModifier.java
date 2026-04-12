package org.moddingx.libx.impl.loot.modifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.Optional;

public class AdditionLootModifier extends LootModifier {
    
    public static final MapCodec<AdditionLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("loot_table").forGetter(lm -> lm.table.location()),
            ResourceLocation.CODEC.optionalFieldOf("random_sequence").forGetter(lm -> lm.randomSequence),
            LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions)
    ).apply(instance, AdditionLootModifier::new));
    
    private final ResourceKey<LootTable> table;
    private final Optional<ResourceLocation> randomSequence;
    
    public AdditionLootModifier(ResourceLocation table, LootItemCondition... conditions) {
        this(table, Optional.empty(), conditions);
    }
    
    public AdditionLootModifier(ResourceLocation table, ResourceLocation randomSequence, LootItemCondition... conditions) {
        this(table, Optional.of(randomSequence), conditions);
    }
    
    private AdditionLootModifier(ResourceLocation table, Optional<ResourceLocation> randomSequence, LootItemCondition... conditions) {
        super(conditions);
        this.table = ResourceKey.create(Registries.LOOT_TABLE, table);
        this.randomSequence = randomSequence;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(@Nonnull ObjectArrayList<ItemStack> loot, @Nonnull LootContext context) {
        Holder.Reference<LootTable> table = context.getResolver().get(this.table).orElse(null);
        if (table != null) {
            LootContext copy = new LootContext.Builder(context).withQueriedLootTableId(this.table.location()).create(this.randomSequence);
            ObjectArrayList<ItemStack> stacks = table.value().getRandomItems(copy);
            loot.addAll(stacks);
        }
        return loot;
    }

    @Nonnull
    @Override
    public MapCodec<? extends AdditionLootModifier> codec() {
        return CODEC;
    }
}
