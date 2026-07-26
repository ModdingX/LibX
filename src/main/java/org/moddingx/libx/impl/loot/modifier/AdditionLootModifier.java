package org.moddingx.libx.impl.loot.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.Optional;

public class AdditionLootModifier extends LootModifier {
    
    public static final MapCodec<AdditionLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("loot_table").forGetter(lm -> lm.table.identifier()),
            Identifier.CODEC.optionalFieldOf("random_sequence").forGetter(lm -> lm.randomSequence),
            LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions),
            Codec.INT.optionalFieldOf("priority", IGlobalLootModifier.DEFAULT_PRIORITY).forGetter(lm -> lm.priority)
    ).apply(instance, AdditionLootModifier::new));

    private final ResourceKey<LootTable> table;
    private final Optional<Identifier> randomSequence;

    public AdditionLootModifier(Identifier table, LootItemCondition... conditions) {
        this(table, Optional.empty(), conditions, IGlobalLootModifier.DEFAULT_PRIORITY);
    }

    public AdditionLootModifier(Identifier table, Identifier randomSequence, LootItemCondition... conditions) {
        this(table, Optional.of(randomSequence), conditions, IGlobalLootModifier.DEFAULT_PRIORITY);
    }

    public AdditionLootModifier(Identifier table, int priority, LootItemCondition... conditions) {
        this(table, Optional.empty(), conditions, priority);
    }

    public AdditionLootModifier(Identifier table, Identifier randomSequence, int priority, LootItemCondition... conditions) {
        this(table, Optional.of(randomSequence), conditions, priority);
    }

    private AdditionLootModifier(Identifier table, Optional<Identifier> randomSequence, LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
        this.table = ResourceKey.create(Registries.LOOT_TABLE, table);
        this.randomSequence = randomSequence;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(@Nonnull ObjectArrayList<ItemStack> loot, @Nonnull LootContext context) {
        Holder.Reference<LootTable> table = context.getResolver().get(this.table).orElse(null);
        if (table != null) {
            LootContext copy = new LootContext.Builder(context).withQueriedLootTableId(this.table.identifier()).create(this.randomSequence);
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
