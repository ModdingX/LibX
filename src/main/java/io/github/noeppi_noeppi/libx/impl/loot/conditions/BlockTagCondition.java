package io.github.noeppi_noeppi.libx.impl.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.util.Misc;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// Condition to match a block and/or fluid tag
public class BlockTagCondition implements ILootCondition {

    public static final ResourceLocation ID = new ResourceLocation(LibX.getInstance().modid, "block_tag");
    public static final LootConditionType TYPE = new LootConditionType(new Serializer());

    @Nullable
    private final ITag<Block> block;
    @Nullable
    private final ITag<Fluid> fluid;

    public BlockTagCondition(@Nullable ITag<Block> block, @Nullable ITag<Fluid> fluid) {
        this.block = block;
        this.fluid = fluid;
    }


    @Nonnull
    @Override
    public LootConditionType func_230419_b_() {
        return TYPE;
    }

    @Override
    public boolean test(LootContext context) {
        BlockState state = context.get(LootParameters.BLOCK_STATE);
        if (state == null) {
            return true;
        }
        if (this.block != null && !this.block.contains(state.getBlock())) {
            return false;
        }
        //noinspection RedundantIfStatement
        if (this.fluid != null && !this.fluid.contains(state.getFluidState().getFluid())) {
            return false;
        }
        return true;
    }

    public static ILootCondition.IBuilder builder(ITag<Block> block, ITag<Fluid> fluid) {
        return () -> new BlockTagCondition(block, fluid);
    }

    public static class Serializer implements ILootSerializer<BlockTagCondition> {

        @Override
        public void serialize(@Nonnull JsonObject json, @Nonnull BlockTagCondition condition, @Nonnull JsonSerializationContext context) {
            json.addProperty("block", (condition.block == null ? Misc.MISSIGNO : BlockTags.getCollection().getValidatedIdFromTag(condition.block)).toString());
            json.addProperty("fluid", (condition.fluid == null ? Misc.MISSIGNO : FluidTags.getCollection().getValidatedIdFromTag(condition.fluid)).toString());
        }

        @Nonnull
        @Override
        public BlockTagCondition deserialize(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context) {
            ResourceLocation blockId = new ResourceLocation(JSONUtils.getString(json, "block"));
            ITag<Block> block = blockId.equals(Misc.MISSIGNO) ? null : BlockTags.getCollection().get(blockId);
            ResourceLocation fluidId = new ResourceLocation(JSONUtils.getString(json, "fluid"));
            ITag<Fluid> fluid = blockId.equals(Misc.MISSIGNO) ? null : FluidTags.getCollection().get(fluidId);
            return new BlockTagCondition(block, fluid);
        }
    }
}
