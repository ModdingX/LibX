package org.moddingx.libx.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * This class is meant to apply a {@link BlockEntityRenderer} to items. Using it is really straightforward:
 * 
 * <ul>
 *     <li>Add custom {@link IClientItemExtensions client extensions} to your item.</li>
 *     <li>In {@link Registerable#setupClient(SetupContext)} call
 *     {@link ItemStackRenderer#addRenderBlock(BlockEntityType, boolean)}</li>
 * </ul>
 * 
 * Your item also needs a special item model. {@link ItemModelProviderBase} provides a method to generate that for you.
 */
public class ItemStackRenderer implements SpecialModelRenderer<ItemStack> {

    public static final ResourceLocation RENDERER_ID = ResourceLocation.fromNamespaceAndPath("libx", "item_stack_renderer");

    private static final LazyValue<ItemStackRenderer> INSTANCE = new LazyValue<>(ItemStackRenderer::new);
    private static final List<BlockEntityType<?>> types = Collections.synchronizedList(new LinkedList<>());
    private static final Map<Block, Pair<LazyValue<BlockEntity>, Boolean>> blocks = Collections.synchronizedMap(new HashMap<>());
    private static final Map<BlockEntityType<?>, CompoundTag> defaultTags = new HashMap<>();

    /**
     * Registers a {@link BlockEntityType} to be rendered with the ItemStackRenderer.
     *
     * @param beType             The Block Entity Type.
     * @param readBlockEntityTag If this is set to true and an item has block entity data, it is loaded before rendering.
     */
    public static <T extends BlockEntity> void addRenderBlock(BlockEntityType<T> beType, boolean readBlockEntityTag) {
        types.add(beType);
        for (Block block : beType.getValidBlocks()) {
            blocks.put(block, Pair.of(new LazyValue<>(() -> beType.create(BlockPos.ZERO, block.defaultBlockState())), readBlockEntityTag));
        }
    }

    @Nullable
    @Override
    public ItemStack extractArgument(@Nonnull ItemStack stack) {
        return stack;
    }

    @Override
    public void render(@Nullable ItemStack stack, @Nonnull ItemDisplayContext ctx, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int light, int overlay, boolean hasFoilType) {
        if (stack == null) return;
        Block block = Block.byItem(stack.getItem());
        if (block == Blocks.AIR || !blocks.containsKey(block)) return;

        Pair<LazyValue<BlockEntity>, Boolean> pair = blocks.get(block);
        BlockState state = block.defaultBlockState();
        BlockEntity blockEntity = pair.getLeft().get();
        BlockEntityType<?> teType = blockEntity.getType();
        BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();

        BlockEntityRenderer<BlockEntity> renderer = dispatcher.getRenderer(blockEntity);
        if (renderer == null) return;

        setLevel(blockEntity);
        if (pair.getRight() && Minecraft.getInstance().level != null) {
            if (!defaultTags.containsKey(teType)) {
                defaultTags.put(teType, blockEntity.saveCustomOnly(Minecraft.getInstance().level.registryAccess()));
            } else {
                blockEntity.loadCustomOnly(defaultTags.get(teType), Minecraft.getInstance().level.registryAccess());
            }
            setLevel(blockEntity);

            CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            if (customData != null) {
                customData.loadInto(blockEntity, Minecraft.getInstance().level.registryAccess());
            }
        }

        poseStack.pushPose();

        if (state.getRenderShape() == RenderShape.MODEL) {
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block.defaultBlockState(), poseStack, buffer, light, overlay, EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO);
        }
        renderer.render(blockEntity, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false), poseStack, buffer, light, overlay, Vec3.ZERO);
                    blockEntity.setLevel(null); // avoid storing the level for too long

        poseStack.popPose();
    }

    private static void setLevel(BlockEntity blockEntity) {
        if (Minecraft.getInstance().level != null) {
            blockEntity.setLevel(Minecraft.getInstance().level);
        }
    }

    /**
     * Gets the instance of the ItemStackRenderer.
     */
    public static ItemStackRenderer get() {
        return INSTANCE.get();
    }

    public static void registerSpecialModelRenderer(RegisterSpecialModelRendererEvent event) {
        event.register(RENDERER_ID, Unbaked.MAP_CODEC);
    }

    /**
     * Compatibility method retained for old API users.
     */
    public static IClientItemExtensions createProperties() {
        return IClientItemExtensions.DEFAULT;
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Nonnull
        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(@Nonnull EntityModelSet modelSet) {
            return ItemStackRenderer.get();
        }
    }
}
