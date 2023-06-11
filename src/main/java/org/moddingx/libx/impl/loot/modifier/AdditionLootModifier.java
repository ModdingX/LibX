package org.moddingx.libx.impl.loot.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.Optional;

public class AdditionLootModifier extends LootModifier {
    
    public static final Codec<AdditionLootModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("loot_table").forGetter(lm -> lm.table),
            ResourceLocation.CODEC.optionalFieldOf("random_sequence").forGetter(lm -> lm.randomSequence),
            LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions)
    ).apply(instance, AdditionLootModifier::new));
    
    private final ResourceLocation table;
    private final Optional<ResourceLocation> randomSequence;
    
    public AdditionLootModifier(ResourceLocation table, LootItemCondition... conditions) {
        this(table, Optional.empty(), conditions);
    }
    
    public AdditionLootModifier(ResourceLocation table, ResourceLocation randomSequence, LootItemCondition... conditions) {
        this(table, Optional.of(randomSequence), conditions);
    }
    
    private AdditionLootModifier(ResourceLocation table, Optional<ResourceLocation> randomSequence, LootItemCondition... conditions) {
        super(conditions);
        this.table = table;
        this.randomSequence = randomSequence;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        LootTable table = context.getResolver().getLootTable(this.table);
        LootContext copy = new LootContext.Builder(context).withQueriedLootTableId(this.table).create(this.randomSequence.orElse(null));
        ObjectArrayList<ItemStack> stacks = table.getRandomItems(copy);
        loot.addAll(stacks);
        return loot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
