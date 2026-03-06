package org.moddingx.libx.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.datagen.provider.model.ItemModelProviderBase;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.SetupContext;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nonnull;
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
public class ItemStackRenderer extends BlockEntityWithoutLevelRenderer {

    private static final LazyValue<ItemStackRenderer> INSTANCE = new LazyValue<>(() -> new ItemStackRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()));

    private static final List<BlockEntityType<?>> types = Collections.synchronizedList(new LinkedList<>());
    private static final Map<Block, Pair<LazyValue<BlockEntity>, Boolean>> blocks = Collections.synchronizedMap(new HashMap<>());
    private static final Map<BlockEntityType<?>, CompoundTag> defaultTags = new HashMap<>();

    public ItemStackRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    /**
     * Registers a {@link BlockEntityType} to be rendered with the ItemStackRenderer.
     *
     * @param beType             The Block Entity Type.
     * @param readBlockEntityTag If this is set to true and an item has a {@code BlockEntityTag}, the block
     *                           entities {@code load} method will get called before rendering.
     */
    public static <T extends BlockEntity> void addRenderBlock(BlockEntityType<T> beType, boolean readBlockEntityTag) {
        types.add(beType);
        for (Block block : beType.getValidBlocks()) {
            blocks.put(block, Pair.of(new LazyValue<>(() -> beType.create(BlockPos.ZERO, block.defaultBlockState())), readBlockEntityTag));
        }
    }

    @Override
    public void renderByItem(ItemStack stack, @Nonnull ItemDisplayContext ctx, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int light, int overlay) {
        Block block = Block.byItem(stack.getItem());
        if (block != Blocks.AIR) {
            if (blocks.containsKey(block)) {
                Pair<LazyValue<BlockEntity>, Boolean> pair = blocks.get(block);
                BlockState state = block.defaultBlockState();
                BlockEntity blockEntity = pair.getLeft().get();
                BlockEntityType<?> teType = blockEntity.getType();

                BlockEntityRenderer<BlockEntity> renderer = this.blockEntityRenderDispatcher.getRenderer(blockEntity);
                if (renderer != null) {
                    setLevelAndState(blockEntity, state);
                    if (pair.getRight()) {
                        if (Minecraft.getInstance().level != null) {
                            if (!defaultTags.containsKey(teType)) {
                                defaultTags.put(teType, blockEntity.saveCustomOnly(Minecraft.getInstance().level.registryAccess()));
                            } else {
                                blockEntity.loadCustomOnly(defaultTags.get(teType), Minecraft.getInstance().level.registryAccess());
                            }
                            setLevelAndState(blockEntity, state);
                            
                            CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
                            if (customData != null) {
                                customData.loadInto(blockEntity, Minecraft.getInstance().level.registryAccess());
                            }
                        }
                    }

                    poseStack.pushPose();

                    if (state.getRenderShape() != RenderShape.ENTITYBLOCK_ANIMATED) {
                        //noinspection deprecation
                        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block.defaultBlockState(), poseStack, buffer, light, overlay);
                    }
                    renderer.render(blockEntity, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false), poseStack, buffer, light, overlay);
                    blockEntity.setLevel(null); // avoid storing the level for too long

                    poseStack.popPose();
                }
            }
        }
    }

    private static void setLevelAndState(BlockEntity blockEntity, BlockState state) {
        if (Minecraft.getInstance().level != null) {
            blockEntity.setLevel(Minecraft.getInstance().level);
        }
        blockEntity.blockState = state;
    }

    /**
     * Gets the instance of the ItemStackRenderer.
     */
    public static ItemStackRenderer get() {
        return INSTANCE.get();
    }
    
    /**
     * Creates some {@link IClientItemExtensions} for with use the {@link ItemStackRenderer}.
     */
    public static IClientItemExtensions createProperties() {
        return new IClientItemExtensions() {

            @Nonnull
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ItemStackRenderer.get();
            }
        };
    }
}
