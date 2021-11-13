package io.github.noeppi_noeppi.libx.impl.config.gui.screen.content.component;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.noeppi_noeppi.libx.impl.config.gui.screen.widget.TextWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.List;

public class PreviewWidget extends TextWidget {

    public PreviewWidget(Screen screen, int x, int y, int width, int height, Component text) {
        super(screen, x, y, width, height, text, List.of());
    }

    @Override
    public void renderButton(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        int color = this.getFGColor() | Mth.ceil(this.alpha * 255) << 24;
        poseStack.pushPose();
        //noinspection IntegerDivisionInFloatingPointContext
        poseStack.translate(this.x + (this.width / 2), this.y + ((this.height - 8) / 2), 20);
        poseStack.scale(2, 2, 2);
        drawCenteredString(poseStack, Minecraft.getInstance().font, this.getMessage(), 0, 0, color);
        poseStack.popPose();
    }
}
