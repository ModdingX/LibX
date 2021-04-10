package io.github.noeppi_noeppi.libx.impl.inventory.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.noeppi_noeppi.libx.inventory.container.GenericContainer;
import io.github.noeppi_noeppi.libx.render.RenderHelper;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

// Screen for the GenericContainer. Do not use manually.
public class GenericScreen extends ContainerScreen<GenericContainer> {

    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    private final GenericContainer container;

    public GenericScreen(GenericContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;
        this.xSize = container.width;
        this.ySize = container.height;
        this.playerInventoryTitleX = container.invX;
        this.playerInventoryTitleY = container.invY - 11;
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }
    

    @Override
    protected void drawGuiContainerBackgroundLayer(@Nonnull MatrixStack matrixStack, float partialTicks, int x, int y) {
        if (this.minecraft != null) {
            int i = (this.width - this.xSize) / 2;
            int j = (this.height - this.ySize) / 2;
            RenderHelper.renderGuiBackground(matrixStack, i, j, this.xSize, this.ySize);
            for (Pair<Integer, Integer> slot : this.container.slots) {
                this.blit(matrixStack, i + slot.getLeft() - 1, j + slot.getRight() - 1, 25, 35, 18, 18);
            }
            this.blit(matrixStack, i + this.container.invX - 1, j + this.container.invY - 1, 7, 139, 162, 76);
        }
    }
}
