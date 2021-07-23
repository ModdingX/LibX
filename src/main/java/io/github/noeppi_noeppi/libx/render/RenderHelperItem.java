package io.github.noeppi_noeppi.libx.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Helper to deal with item rendering.
 */
public class RenderHelperItem {

    /**
     * Renders an {@link ItemStack item} into a gui. This allows to set the size of the item and whether the
     * amount should be included.
     */
    // TODO needs to be tested
    public static void renderItemGui(PoseStack poseStack, MultiBufferSource buffer, ItemStack stack, int x, int y, int size, boolean includeAmount) {
        if (!stack.isEmpty()) {
            BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, null, Minecraft.getInstance().player, 0);
            
            poseStack.pushPose();
            Minecraft.getInstance().getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            poseStack.translate(x, y, 50);
            poseStack.scale(size / 16f, size / 16f, 1);
            poseStack.translate(8.0F, 8.0F, 0.0F);
            poseStack.scale(1.0F, -1.0F, 1.0F);
            poseStack.scale(16.0F, 16.0F, 16.0F);

            if (!model.usesBlockLight()) {
                Lighting.setupForFlatItems();
            }

            Minecraft.getInstance().getItemRenderer().render(stack, ItemTransforms.TransformType.GUI, false, poseStack, buffer, 0xf000f0, OverlayTexture.NO_OVERLAY, model);
            ((MultiBufferSource.BufferSource) buffer).endBatch();

            RenderSystem.enableDepthTest();
            if (!model.usesBlockLight()) {
                Lighting.setupFor3DItems();
            }

            poseStack.popPose();

            if (includeAmount && stack.getCount() > 1) {
                poseStack.pushPose();
                poseStack.translate(x, y, 90);

                Font fr = Minecraft.getInstance().font;
                String text = Integer.toString(stack.getCount());
                fr.drawInBatch(text, (float) (17 - fr.width(text)), 9, 16777215, true, poseStack.last().pose(), buffer, false, 0, 15728880);

                poseStack.popPose();
            }
        }
    }
}
