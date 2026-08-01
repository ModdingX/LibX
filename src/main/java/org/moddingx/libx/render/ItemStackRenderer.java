package org.moddingx.libx.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.moddingx.libx.datagen.provider.model.ItemModelProviderBase;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.SetupContext;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

/**
 * This class is meant to apply a {@link BlockEntityRenderer} to items. Using it is really straightforward:
 *
 * <ul>
 *     <li>Add custom {@link SpecialModelRenderer} to your item.</li>
 *     <li>In {@link Registerable#setupClient(SetupContext)} call
 *     {@link ItemStackRenderer#addRenderBlock(BlockEntityType, boolean)}</li>
 * </ul>
 *
 * Your item also needs a special item model. {@link ItemModelProviderBase} provides a method to generate that for you.
 */
public class ItemStackRenderer implements SpecialModelRenderer<ItemStack> {

    public static final Identifier RENDERER_ID = Identifier.fromNamespaceAndPath("libx", "item_stack_renderer");
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

    private static final LazyValue<ItemStackRenderer> INSTANCE = new LazyValue<>(ItemStackRenderer::new);
    private static final List<BlockEntityType<?>> types = Collections.synchronizedList(new LinkedList<>());
    private static final Map<Block, Pair<LazyValue<BlockEntity>, Boolean>> blocks = Collections.synchronizedMap(new HashMap<>());
    private static final Map<Block, CompoundTag> defaultTags = new HashMap<>();

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
    public void submit(@Nullable ItemStack stack, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector nodeCollector, int light, int overlay, boolean hasFoilType, int outlineColor) {
        if (stack == null) return;
        Block block = Block.byItem(stack.getItem());
        if (block == Blocks.AIR || !blocks.containsKey(block)) return;

        Pair<LazyValue<BlockEntity>, Boolean> pair = blocks.get(block);
        BlockState state = block.defaultBlockState();
        BlockEntity blockEntity = pair.getLeft().get();
        BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();

        BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = dispatcher.getRenderer(blockEntity);
        if (renderer == null) return;

        setLevel(blockEntity);
        try {
            if (pair.getRight() && Minecraft.getInstance().level != null) {
                if (!defaultTags.containsKey(block)) {
                    defaultTags.put(block, blockEntity.saveCustomOnly(Minecraft.getInstance().level.registryAccess()));
                } else {
                    blockEntity.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, Minecraft.getInstance().level.registryAccess(), defaultTags.get(block)));
                }
                setLevel(blockEntity);

                TypedEntityData<BlockEntityType<?>> customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
                if (customData != null && customData.type() == blockEntity.getType()) {
                    customData.loadInto(blockEntity, Minecraft.getInstance().level.registryAccess());
                }
            }

            poseStack.pushPose();
            try {
                if (state.getRenderShape() == RenderShape.MODEL) {
                    BlockModelRenderState blockRenderState = new BlockModelRenderState();
                    Minecraft.getInstance().getBlockModelResolver().update(blockRenderState, state, BLOCK_DISPLAY_CONTEXT);
                    // submitMultiLayer instead of submit: the block being rendered is arbitrary, so its model
                    // may mix solid, cutout and translucent materials, which submit would collapse into one layer.
                    blockRenderState.submitMultiLayer(poseStack, nodeCollector, light, overlay, outlineColor);
                }
                float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
                BlockEntityRenderState renderState = renderer.createRenderState();
                renderer.extractRenderState(blockEntity, renderState, partialTick, Vec3.ZERO, null);
                CameraRenderState cameraRenderState = new CameraRenderState();
                cameraRenderState.initialized = true;
                renderer.submit(renderState, poseStack, nodeCollector, cameraRenderState);
            } finally {
                poseStack.popPose();
            }
        } finally {
            // avoid storing the level for too long
            blockEntity.setLevel(null);
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(0, 0, 0));
        output.accept(new Vector3f(1, 1, 1));
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

    public record Unbaked() implements SpecialModelRenderer.Unbaked<ItemStack> {

        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Nonnull
        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<ItemStack> bake(@Nonnull SpecialModelRenderer.BakingContext context) {
            return ItemStackRenderer.get();
        }
    }
}
