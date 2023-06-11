package org.moddingx.libx.impl.config.gui.screen.widget;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.render.RenderHelper;

import javax.annotation.Nonnull;
import java.util.List;

public class TextWidget extends AbstractWidget {

    private final List<Component> tooltip;
    
    public TextWidget(int x, int y, int width, int height, Component text, List<? extends Component> tooltip) {
        super(x, y, width, height, text);
        this.tooltip = ImmutableList.copyOf(tooltip);
    }

    @Override
    public void renderWidget(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        RenderHelper.resetColor();
        graphics.drawString(Minecraft.getInstance().font, this.getMessage(), this.getX(), this.getY() + ((this.height - 8) / 2), 0xFFFFFF, true);
        if (this.isHovered && !this.tooltip.isEmpty()) {
            graphics.renderComponentTooltip(Minecraft.getInstance().font, this.tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void playDownSound(@Nonnull SoundManager manager) {
        //
    }

    @Override
    public void updateWidgetNarration(@Nonnull NarrationElementOutput output) {
        output.add(NarratedElementType.HINT, this.tooltip.toArray(Component[]::new));
    }
}
