package io.github.noeppi_noeppi.libx.impl.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.noeppi_noeppi.libx.LibX;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

// Condition to match a block tag
public class BlockTag implements ILootCondition {

    public static final ResourceLocation ID = new ResourceLocation(LibX.getInstance().modid, "block_tag");
    public static final LootConditionType TYPE = new LootConditionType(new Serializer());

    private final ITag.INamedTag<Block> tag;

    private BlockTag(ITag.INamedTag<Block> tag) {
        this.tag = tag;
    }

    @Nonnull
    @Override
    public LootConditionType func_230419_b_() {
        return TYPE;
    }

    @Override
    public boolean test(LootContext context) {
        BlockState state = context.get(LootParameters.BLOCK_STATE);
        return state != null && this.tag.contains(state.getBlock());
    }

    public static Builder builder(ITag.INamedTag<Block> tag) {
        return new Builder(tag);
    }

    public static class Builder implements ILootCondition.IBuilder {

        private final ITag.INamedTag<Block> tag;

        public Builder(ITag.INamedTag<Block> tag) {
            this.tag = tag;
        }

        @Nonnull
        @Override
        public ILootCondition build() {
            return new BlockTag(this.tag);
        }
    }

    public static class Serializer implements ILootSerializer<BlockTag> {

        @Override
        public void serialize(@Nonnull JsonObject json, @Nonnull BlockTag condition, @Nonnull JsonSerializationContext context) {
            json.addProperty("tag", condition.tag.getName().toString());
        }

        @Nonnull
        @Override
        public BlockTag deserialize(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context) {
            String s = JSONUtils.getString(json, "tag");
            ITag.INamedTag<Block> tag = BlockTags.makeWrapperTag(s);
            return new BlockTag(tag);
        }
    }
}
