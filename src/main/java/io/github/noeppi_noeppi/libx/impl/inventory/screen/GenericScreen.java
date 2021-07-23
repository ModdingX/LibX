package io.github.noeppi_noeppi.libx.impl.inventory.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.noeppi_noeppi.libx.inventory.container.GenericContainerMenu;
import io.github.noeppi_noeppi.libx.render.RenderHelper;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

// Screen for the GenericContainer. Do not use manually.
public class GenericScreen extends AbstractContainerScreen<GenericContainerMenu> {
    
    private final GenericContainerMenu menu;

    public GenericScreen(GenericContainerMenu menu, Inventory playerContainer, Component title) {
        super(menu, playerContainer, title);
        this.menu = menu;
        this.imageWidth = menu.width;
        this.imageHeight = menu.height;
        this.inventoryLabelX = menu.invX;
        this.inventoryLabelY = menu.invY - 11;
    }

    @Override
    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }
    
    @Override
    protected void renderBg(@Nonnull PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        if (this.minecraft != null) {
            int i = (this.width - this.imageWidth) / 2;
            int j = (this.height - this.imageHeight) / 2;
            RenderHelper.renderGuiBackground(poseStack, i, j, this.imageWidth, this.imageHeight);
            for (Pair<Integer, Integer> slot : this.menu.slots) {
                this.blit(poseStack, i + slot.getLeft() - 1, j + slot.getRight() - 1, 25, 35, 18, 18);
            }
            this.blit(poseStack, i + this.menu.invX - 1, j + this.menu.invY - 1, 7, 139, 162, 76);
        }
    }
}
