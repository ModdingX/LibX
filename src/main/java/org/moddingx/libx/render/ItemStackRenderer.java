package org.moddingx.libx.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.base.BlockBase;
import org.moddingx.libx.datagen.provider.ItemModelProviderBase;
import org.moddingx.libx.impl.RendererOnDataGenException;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.SetupContext;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

/**
 * This class is meant to apply a {@link BlockEntityRenderer} to items. Using it is really straightforward:
 * 
 * <ul>
 *     <li>Add custom {@link IClientItemExtensions client extensions} to your item through
 *     {@link Item#initializeClient(Consumer)} or {@link BlockBase#initializeItemClient(Consumer)}</li>
 *     <li>In {@link Registerable#registerClient(SetupContext)} call
 *     {@link ItemStackRenderer#addRenderBlock(BlockEntityType, boolean)}</li>
 * </ul>
 * 
 * The required models will generate automatically if you're using {@link ItemModelProviderBase}.
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
        for (Block block : beType.validBlocks) {
            blocks.put(block, Pair.of(new LazyValue<>(() -> beType.create(BlockPos.ZERO, block.defaultBlockState())), readBlockEntityTag));
        }
    }

    @Override
    public void renderByItem(ItemStack stack, @Nonnull ItemTransforms.TransformType type, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int light, int overlay) {
        Block block = Block.byItem(stack.getItem());
        if (block != Blocks.AIR) {
            if (blocks.containsKey(block)) {
                Pair<LazyValue<BlockEntity>, Boolean> pair = blocks.get(block);
                BlockState state = block.defaultBlockState();
                BlockEntity blockEntity = pair.getLeft().get();
                BlockEntityType<?> teType = blockEntity.getType();

                BlockEntityRenderer<BlockEntity> renderer = this.blockEntityRenderDispatcher.getRenderer(blockEntity);
                if (renderer != null) {
                    if (pair.getRight()) {
                        if (!defaultTags.containsKey(teType)) {
                            setLevelAndState(blockEntity, state);
                            defaultTags.put(teType, blockEntity.saveWithFullMetadata());
                        }

                        CompoundTag nbt = stack.getTag();
                        setLevelAndState(blockEntity, state);
                        blockEntity.load(defaultTags.get(teType));
                        if (nbt != null && nbt.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
                            CompoundTag blockTag = nbt.getCompound("BlockEntityTag");
                            blockEntity.load(blockTag);
                        }
                    }

                    if (Minecraft.getInstance().level != null) {
                        blockEntity.setLevel(Minecraft.getInstance().level);
                    }
                    blockEntity.blockState = state;

                    poseStack.pushPose();

                    if (state.getRenderShape() != RenderShape.ENTITYBLOCK_ANIMATED) {
                        //noinspection deprecation
                        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block.defaultBlockState(), poseStack, buffer, light, overlay);
                    }
                    renderer.render(blockEntity, Minecraft.getInstance().getFrameTime(), poseStack, buffer, light, overlay);

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
        if (FMLLoader.getLaunchHandler().isData()) {
            throw new RendererOnDataGenException();
        } else {
            return INSTANCE.get();
        }
    }
    
    /**
     * Creates some {@link IClientItemExtensions} for with use the {@link ItemStackRenderer}.
     */
    public static IClientItemExtensions createProperties() {
        return new IClientItemExtensions() {

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ItemStackRenderer.get();
            }
        };
    }
}
