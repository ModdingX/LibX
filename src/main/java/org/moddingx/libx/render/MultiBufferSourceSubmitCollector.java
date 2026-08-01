package org.moddingx.libx.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.MatrixUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.*;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link SubmitNodeCollector} that bridges the submission based rendering back to a
 * {@link MultiBufferSource}. Used by {@link RenderHelper#renderItem} to render items inside
 * a {@link org.moddingx.libx.render.target.RenderJob} without requiring callers to
 * implement all abstract methods of {@link SubmitNodeCollector}.
 */
public class MultiBufferSourceSubmitCollector implements SubmitNodeCollector {

    private static final RenderType SHADOW_RENDER_TYPE = RenderTypes.entityShadow(
            Identifier.withDefaultNamespace("textures/misc/shadow.png"));
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int[] NO_TINT = new int[0];
    private static final float SPECIAL_FOIL_TEXTURE_SCALE = 0.0078125f;

    private final MultiBufferSource buffer;
    private final QuadInstance quadInstance = new QuadInstance();
    private final RandomSource random = RandomSource.create();

    public MultiBufferSourceSubmitCollector(MultiBufferSource buffer) {
        this.buffer = buffer;
    }

    @Nonnull
    @Override
    public OrderedSubmitNodeCollector order(int index) {
        return this;
    }

    @Override
    public void submitItem(
            @Nonnull PoseStack poseStack,
            @Nonnull ItemDisplayContext displayContext,
            int packedLight,
            int packedOverlay,
            int outlineColor,
            @Nonnull int[] tintLayers,
            @Nonnull List<BakedQuad> quads,
            @Nonnull ItemStackRenderState.FoilType foilType
    ) {
        PoseStack.Pose pose = poseStack.last();
        PoseStack.Pose foilDecalPose = foilType == ItemStackRenderState.FoilType.SPECIAL ? computeFoilDecalPose(displayContext, pose) : null;
        this.quadInstance.setLightCoords(packedLight);
        this.quadInstance.setOverlayCoords(packedOverlay);
        for (BakedQuad quad : quads) {
            BakedQuad.MaterialInfo material = quad.materialInfo();
            RenderType renderType = material.itemRenderType();
            this.quadInstance.setColor(layerColor(tintLayers, material));
            if (foilType != ItemStackRenderState.FoilType.NONE) {
                VertexConsumer foilBuffer = this.buffer.getBuffer(ItemFeatureRenderer.getFoilRenderType(renderType, true));
                if (foilDecalPose != null) {
                    foilBuffer = new SheetedDecalTextureGenerator(foilBuffer, foilDecalPose, SPECIAL_FOIL_TEXTURE_SCALE);
                }
                foilBuffer.putBakedQuad(pose, quad, this.quadInstance);
            }
            this.buffer.getBuffer(renderType).putBakedQuad(pose, quad, this.quadInstance);
        }
    }

    private static PoseStack.Pose computeFoilDecalPose(ItemDisplayContext displayContext, PoseStack.Pose pose) {
        PoseStack.Pose foilDecalPose = pose.copy();
        if (displayContext == ItemDisplayContext.GUI) {
            MatrixUtil.mulComponentWise(foilDecalPose.pose(), 0.5f);
        } else if (displayContext.firstPerson()) {
            MatrixUtil.mulComponentWise(foilDecalPose.pose(), 0.75f);
        }
        return foilDecalPose;
    }

    private static int layerColor(int[] tintLayers, BakedQuad.MaterialInfo material) {
        if (!material.isTinted()) return -1;
        int layer = material.tintIndex();
        return layer >= 0 && layer < tintLayers.length ? tintLayers[layer] : -1;
    }

    @Override
    public void submitCustomGeometry(PoseStack poseStack, @Nonnull RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer renderer) {
        renderer.render(poseStack.last(), this.buffer.getBuffer(renderType));
    }

    @Override
    public <S> void submitModel(
            Model<? super S> model,
            @Nonnull S renderState,
            @Nonnull PoseStack poseStack,
            @Nonnull RenderType renderType,
            int packedLight,
            int packedOverlay,
            int tintColor,
            @Nullable TextureAtlasSprite sprite,
            int outlineColor,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        model.setupAnim(renderState);
        VertexConsumer consumer = this.buffer.getBuffer(renderType);
        VertexConsumer wrapped = sprite == null ? consumer : sprite.wrap(consumer);
        model.renderToBuffer(poseStack, wrapped, packedLight, packedOverlay, tintColor);
    }

    @Override
    public void submitModelPart(
            @Nonnull ModelPart modelPart,
            @Nonnull PoseStack poseStack,
            @Nonnull RenderType renderType,
            int packedLight,
            int packedOverlay,
            @Nullable TextureAtlasSprite sprite,
            boolean sheeted,
            boolean hasFoil,
            int tintColor,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            int outlineColor
    ) {
        VertexConsumer base = this.buffer.getBuffer(renderType);
        VertexConsumer consumer;
        if (sprite != null) {
            consumer = hasFoil
                    ? sprite.wrap(ItemFeatureRenderer.getFoilBuffer(this.buffer, renderType, sheeted, true))
                    : sprite.wrap(base);
        } else if (hasFoil) {
            consumer = ItemFeatureRenderer.getFoilBuffer(this.buffer, renderType, sheeted, true);
        } else {
            consumer = base;
        }
        modelPart.render(poseStack, consumer, packedLight, packedOverlay, tintColor);
    }

    @Override
    public void submitMovingBlock(@Nonnull PoseStack poseStack, MovingBlockRenderState renderState) {
        Minecraft mc = Minecraft.getInstance();
        BlockState blockState = renderState.blockState;
        BlockStateModel model = mc.getModelManager().getBlockStateModelSet().get(blockState);
        OptionsRenderState options = mc.gameRenderer.getGameRenderState().optionsRenderState;
        boolean forceOpaque = ModelBlockRenderer.forceOpaque(options.cutoutLeaves, blockState);
        PoseStack localStack = new PoseStack();
        localStack.last().set(poseStack.last());
        BlockQuadOutput output = (x, y, z, quad, instance) -> {
            localStack.pushPose();
            localStack.translate(x, y, z);
            ChunkSectionLayer layer = forceOpaque ? ChunkSectionLayer.SOLID : quad.materialInfo().layer();
            this.buffer.getBuffer(switch (layer) {
                case SOLID -> RenderTypes.solidMovingBlock();
                case CUTOUT -> RenderTypes.cutoutMovingBlock();
                case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
            }).putBakedQuad(localStack.last(), quad, instance);
            localStack.popPose();
        };
        ModelBlockRenderer renderer = new ModelBlockRenderer(options.ambientOcclusion, false, mc.getBlockColors());
        renderer.tesselateBlock(output, 0f, 0f, 0f, renderState, renderState.blockPos, blockState, model,
                blockState.getSeed(renderState.randomSeedPos));
    }

    @Override
    public void submitBlockModel(PoseStack poseStack, @Nonnull RenderType renderType, @Nonnull List<BlockStateModelPart> parts,
            @Nonnull int[] tintLayers, int packedLight, int packedOverlay, int outlineColor) {
        VertexConsumer consumer = this.buffer.getBuffer(renderType);
        this.quadInstance.setLightCoords(packedLight);
        this.quadInstance.setOverlayCoords(packedOverlay);
        for (BlockStateModelPart part : parts) {
            this.putPartQuads(part, poseStack.last(), tintLayers, consumer);
        }
    }

    @Override
    public void submitMultiLayerBlockModel(PoseStack poseStack, @Nonnull List<BlockStateModelPart> parts, boolean translucent,
            @Nonnull int[] tintLayers, int packedLight, int packedOverlay, int outlineColor) {
        this.quadInstance.setLightCoords(packedLight);
        this.quadInstance.setOverlayCoords(packedOverlay);
        for (BlockStateModelPart part : parts) {
            this.putPartQuads(part, poseStack.last(), tintLayers, null);
        }
    }

    @Override
    public void submitBreakingBlockModel(PoseStack poseStack, @Nonnull BlockStateModel model, long seed, int progress) {
        VertexConsumer consumer = new SheetedDecalTextureGenerator(
                this.buffer.getBuffer(ModelBakery.DESTROY_TYPES.get(progress)), poseStack.last(), 1f);
        this.quadInstance.setLightCoords(15728880);
        this.quadInstance.setOverlayCoords(OverlayTexture.NO_OVERLAY);
        List<BlockStateModelPart> parts = new ArrayList<>();
        this.random.setSeed(seed);
        model.collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), this.random, parts);
        for (BlockStateModelPart part : parts) {
            this.putPartQuads(part, poseStack.last(), NO_TINT, consumer);
        }
    }

    private void putPartQuads(BlockStateModelPart part, PoseStack.Pose pose, int[] tintLayers, @Nullable VertexConsumer consumer) {
        for (Direction direction : DIRECTIONS) {
            for (BakedQuad quad : part.getQuads(direction)) {
                this.putQuad(quad, pose, tintLayers, consumer);
            }
        }
        for (BakedQuad quad : part.getQuads(null)) {
            this.putQuad(quad, pose, tintLayers, consumer);
        }
    }

    private void putQuad(BakedQuad quad, PoseStack.Pose pose, int[] tintLayers, @Nullable VertexConsumer consumer) {
        int tintIndex = quad.materialInfo().tintIndex();
        boolean tinted = tintIndex != -1 && tintIndex < tintLayers.length;
        this.quadInstance.setColor(tinted ? tintLayers[tintIndex] : -1);
        VertexConsumer target = consumer != null ? consumer : this.buffer.getBuffer(switch (quad.materialInfo().layer()) {
            case SOLID -> NeoForgeRenderTypes.SOLID_BLOCK_SHEET;
            case CUTOUT -> Sheets.cutoutBlockSheet();
            case TRANSLUCENT -> Sheets.translucentBlockSheet();
        });
        target.putBakedQuad(pose, quad, this.quadInstance);
    }

    @Override
    public void submitShadow(PoseStack poseStack, float radius, List<EntityRenderState.ShadowPiece> pieces) {
        VertexConsumer consumer = this.buffer.getBuffer(SHADOW_RENDER_TYPE);
        Matrix4f pose = poseStack.last().pose();
        for (EntityRenderState.ShadowPiece piece : pieces) {
            AABB bounds = piece.shapeBelow().bounds();
            float x0 = piece.relativeX() + (float) bounds.minX;
            float x1 = piece.relativeX() + (float) bounds.maxX;
            float y = piece.relativeY() + (float) bounds.minY;
            float z0 = piece.relativeZ() + (float) bounds.minZ;
            float z1 = piece.relativeZ() + (float) bounds.maxZ;
            float u0 = -x0 / 2f / radius + 0.5f;
            float u1 = -x1 / 2f / radius + 0.5f;
            float v0 = -z0 / 2f / radius + 0.5f;
            float v1 = -z1 / 2f / radius + 0.5f;
            int color = ARGB.white(piece.alpha());
            shadowVertex(pose, consumer, color, x0, y, z0, u0, v0);
            shadowVertex(pose, consumer, color, x0, y, z1, u0, v1);
            shadowVertex(pose, consumer, color, x1, y, z1, u1, v1);
            shadowVertex(pose, consumer, color, x1, y, z0, u1, v0);
        }
    }

    private static void shadowVertex(Matrix4f pose, VertexConsumer consumer, int color, float x, float y, float z, float u, float v) {
        Vector3f pos = pose.transformPosition(x, y, z, new Vector3f());
        consumer.addVertex(pos.x(), pos.y(), pos.z(), color, u, v, OverlayTexture.NO_OVERLAY, 15728880, 0f, 1f, 0f);
    }

    @Override
    public void submitFlame(PoseStack poseStack, EntityRenderState renderState, @Nonnull Quaternionf rotation) {
        AtlasManager atlasManager = Minecraft.getInstance().getAtlasManager();
        TextureAtlasSprite fire0 = atlasManager.get(ModelBakery.FIRE_0);
        TextureAtlasSprite fire1 = atlasManager.get(ModelBakery.FIRE_1);
        float scale = renderState.boundingBoxWidth * 1.4f;
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        float height = renderState.boundingBoxHeight / scale;
        float vOff = 0f;
        float zOff = 0f;
        poseStack.last().rotate(rotation);
        poseStack.last().translate(0f, 0f, 0.3f - (int) height * 0.02f);
        VertexConsumer consumer = this.buffer.getBuffer(Sheets.cutoutBlockSheet());
        int light = LightCoordsUtil.withBlock(renderState.lightCoords, 15);
        float hw = 0.5f;
        for (int i = 0; height > 0f; i++) {
            TextureAtlasSprite sprite = i % 2 == 0 ? fire0 : fire1;
            float u0 = sprite.getU0(), u1 = sprite.getU1();
            float v0 = sprite.getV0(), v1 = sprite.getV1();
            if (i / 2 % 2 == 0) {
                float tmp = u1;
                u1 = u0;
                u0 = tmp;
            }
            flameVertex(poseStack.last(), consumer, -hw, 0f - vOff, zOff, u1, v1, light);
            flameVertex(poseStack.last(), consumer, hw, 0f - vOff, zOff, u0, v1, light);
            flameVertex(poseStack.last(), consumer, hw, 1.4f - vOff, zOff, u0, v0, light);
            flameVertex(poseStack.last(), consumer, -hw, 1.4f - vOff, zOff, u1, v0, light);
            height -= 0.45f;
            vOff -= 0.45f;
            hw *= 0.9f;
            zOff -= 0.03f;
        }
        poseStack.popPose();
    }

    private static void flameVertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, float u, float v, int light) {
        consumer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setUv1(0, 10).setLight(light).setNormal(pose, 0f, 1f, 0f);
    }

    @Override
    public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leashState) {
        Matrix4f pose = new Matrix4f(poseStack.last().pose());
        pose.translate((float) leashState.offset.x, (float) leashState.offset.y, (float) leashState.offset.z);
        float dx = (float) (leashState.end.x - leashState.start.x);
        float dy = (float) (leashState.end.y - leashState.start.y);
        float dz = (float) (leashState.end.z - leashState.start.z);
        float f3 = Mth.invSqrt(dx * dx + dz * dz) * 0.05f / 2f;
        float nx = dz * f3;
        float nz = dx * f3;
        VertexConsumer consumer = this.buffer.getBuffer(RenderTypes.leash());
        for (int i = 0; i <= 24; i++) {
            addLeashVertexPair(consumer, pose, dx, dy, dz, 0.05f, nx, nz, i, false, leashState);
        }
        for (int i = 24; i >= 0; i--) {
            addLeashVertexPair(consumer, pose, dx, dy, dz, 0f, nx, nz, i, true, leashState);
        }
    }

    private static void addLeashVertexPair(VertexConsumer consumer, Matrix4f pose, float dx, float dy, float dz,
            float yOffset, float nx, float nz, int index, boolean reverse, EntityRenderState.LeashState leashState) {
        float t = index / 24f;
        int blockLight = (int) Mth.lerp(t, leashState.startBlockLight, leashState.endBlockLight);
        int skyLight = (int) Mth.lerp(t, leashState.startSkyLight, leashState.endSkyLight);
        int light = LightCoordsUtil.pack(blockLight, skyLight);
        float bright = index % 2 == (reverse ? 1 : 0) ? 0.7f : 1f;
        float r = 0.5f * bright, g = 0.4f * bright, b = 0.3f * bright;
        float px = dx * t;
        float py = leashState.slack
                ? (dy > 0f ? dy * t * t : dy - dy * (1f - t) * (1f - t))
                : dy * t;
        float pz = dz * t;
        consumer.addVertex(pose, px - nx, py + yOffset, pz + nz).setColor(r, g, b, 1f).setLight(light);
        consumer.addVertex(pose, px + nx, py + 0.05f - yOffset, pz - nz).setColor(r, g, b, 1f).setLight(light);
    }

    @Override
    public void submitNameTag(@Nonnull PoseStack poseStack, @Nullable Vec3 pos, int yOffset, @Nonnull Component text,
            boolean seethrough, int packedLight, double distanceToCameraSq, @Nonnull CameraRenderState cameraRenderState) {
        if (pos == null) return;
        Minecraft mc = Minecraft.getInstance();
        poseStack.pushPose();
        poseStack.translate(pos.x, pos.y + 0.5, pos.z);
        poseStack.mulPose(cameraRenderState.orientation);
        poseStack.scale(0.025f, -0.025f, 0.025f);
        Matrix4f pose = poseStack.last().pose();
        float x = -mc.font.width(text) / 2f;
        int bgColor = (int) (mc.options.getBackgroundOpacity(0.25f) * 255f) << 24;
        if (seethrough) {
            mc.font.drawInBatch(text, x, yOffset, -1, false, pose, this.buffer,
                    Font.DisplayMode.NORMAL, 0, LightCoordsUtil.lightCoordsWithEmission(packedLight, 2));
            mc.font.drawInBatch(text, x, yOffset, -2130706433, false, pose, this.buffer,
                    Font.DisplayMode.SEE_THROUGH, bgColor, packedLight);
        } else {
            mc.font.drawInBatch(text, x, yOffset, -2130706433, false, pose, this.buffer,
                    Font.DisplayMode.NORMAL, bgColor, packedLight);
        }
        poseStack.popPose();
    }

    @Override
    public void submitText(PoseStack poseStack, float x, float y, @Nonnull FormattedCharSequence string,
            boolean dropShadow, @Nonnull Font.DisplayMode displayMode, int packedLight, int color,
            int backgroundColor, int outlineColor) {
        Font font = Minecraft.getInstance().font;
        Matrix4f pose = poseStack.last().pose();
        if (outlineColor != 0) {
            font.drawInBatch8xOutline(string, x, y, color, outlineColor, pose, this.buffer, packedLight);
        } else {
            font.drawInBatch(string, x, y, color, dropShadow, pose, this.buffer, displayMode, backgroundColor, packedLight);
        }
    }

    @Override
    public void submitParticleGroup(@Nonnull SubmitNodeCollector.ParticleGroupRenderer renderer) {}
}
