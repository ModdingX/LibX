package org.moddingx.libx.impl.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.moddingx.libx.LibX;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CopyBlockEntityDataFunction extends LootItemConditionalFunction {

    public static final ResourceLocation ID = LibX.getInstance().resource("copy_block_entity_data");
    public static final MapCodec<CopyBlockEntityDataFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance).and(instance.group(
            BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter(function -> function.block),
            Codec.STRING.listOf().fieldOf("tags").forGetter(function -> function.tags.stream().sorted().toList())
    )).apply(instance, CopyBlockEntityDataFunction::new));
    public static final LootItemFunctionType<CopyBlockEntityDataFunction> TYPE = new LootItemFunctionType<>(CODEC);

    private final Holder<Block> block;
    private final Set<String> tags;

    private CopyBlockEntityDataFunction(List<LootItemCondition> conditions, Holder<Block> block, List<String> tags) {
        this(conditions, block, Set.copyOf(tags));
    }
    
    public CopyBlockEntityDataFunction(List<LootItemCondition> conditions, Holder<Block> block, Set<String> tags) {
        super(conditions);
        this.block = block;
        this.tags = Set.copyOf(tags);
    }

    @Nonnull
    @Override
    public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return TYPE;
    }

    @Nonnull
    @Override
    protected ItemStack run(@Nonnull ItemStack stack, @Nonnull LootContext context) {
        BlockEntity blockEntity = context.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity != null) {
            stack.update(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY, data -> {
                CompoundTag blockEntityData = blockEntity.saveCustomOnly(context.getLevel().registryAccess());
                for (String tagName : this.tags) {
                    Tag tag;
                    if (blockEntityData.contains(tagName) && (tag = blockEntityData.get(tagName)) != null) {
                        data = data.update(nbt -> nbt.put(tagName, tag.copy()));
                    }
                }
                data = data.update(nbt -> nbt.putString("id", Objects.requireNonNull(BlockEntityType.getKey(blockEntity.getType())).toString()));
                return data;
            });
        }
        return stack;
    }

    public static CopyBlockEntityDataFunction.Builder copyBlockEntityData(Block block, Set<String> tags) {
        return new CopyBlockEntityDataFunction.Builder(block, tags);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<CopyBlockEntityDataFunction.Builder> {
        
        private final Holder<Block> block;
        private final Set<String> tags;

        @SuppressWarnings("deprecation")
        private Builder(Block block, Set<String> tags) {
            this.block = block.builtInRegistryHolder();
            this.tags = Set.copyOf(tags);
        }

        @Nonnull
        @Override
        protected Builder getThis() {
            return this;
        }

        @Nonnull
        @Override
        public CopyBlockEntityDataFunction build() {
            return new CopyBlockEntityDataFunction(this.getConditions(), this.block, this.tags);
        }
    }
}
