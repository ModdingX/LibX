package org.moddingx.libx.impl.loot.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;

public class AdditionLootModifier extends LootModifier {
    
    public static final Codec<AdditionLootModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("loot_table").forGetter(lm -> lm.table),
            LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions)
    ).apply(instance, AdditionLootModifier::new));
    
    private final ResourceLocation table;
    
    public AdditionLootModifier(ResourceLocation table, LootItemCondition... conditions) {
        super(conditions);
        this.table = table;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        LootTable table = context.getLootTable(this.table);
        // EMPTY will keep all parameters but won't throw an exception
        LootContext copy = new LootContext.Builder(context).create(LootContextParamSets.EMPTY);
        ObjectArrayList<ItemStack> stacks = table.getRandomItems(LootContextHelper.copyWith(context, this.table));
        loot.addAll(stacks);
        return loot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
