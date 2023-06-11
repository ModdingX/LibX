package org.moddingx.libx.impl.config.gui.screen.content.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.moddingx.libx.impl.config.gui.screen.widget.TextWidget;
import org.moddingx.libx.render.RenderHelper;

import javax.annotation.Nonnull;
import java.util.List;

public class PreviewWidget extends TextWidget {

    public PreviewWidget(int x, int y, int width, int height, Component text) {
        super(x, y, width, height, text, List.of());
    }

    @Override
    public void renderWidget(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int color = this.getFGColor() | Mth.ceil(this.alpha * 255) << 24;
        graphics.pose().pushPose();
        //noinspection IntegerDivisionInFloatingPointContext
        graphics.pose().translate(this.getX() + (this.width / 2), this.getY() + ((this.height - 8) / 2), 20);
        graphics.pose().scale(2, 2, 2);
        RenderHelper.resetColor();
        graphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), 0, 0, color);
        graphics.pose().popPose();
    }
}
