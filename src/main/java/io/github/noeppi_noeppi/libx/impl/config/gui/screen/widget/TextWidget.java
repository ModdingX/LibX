package io.github.noeppi_noeppi.libx.impl.config.gui.screen.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public class TextWidget extends AbstractWidget {

    private final Screen screen;
    private final List<Component> tooltip;
    
    public TextWidget(Screen screen, int x, int y, int width, int height, Component text, List<? extends Component> tooltip) {
        super(x, y, width, height, text);
        this.screen = screen;
        this.tooltip = ImmutableList.copyOf(tooltip);
    }

    @Override
    public void renderButton(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        //noinspection IntegerDivisionInFloatingPointContext
        Minecraft.getInstance().font.drawShadow(poseStack, this.getMessage(), this.x, this.y + ((this.height - 8) / 2), 0xFFFFFF);
        if (this.isHovered && !this.tooltip.isEmpty()) {
            this.screen.renderComponentTooltip(poseStack, this.tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void playDownSound(@Nonnull SoundManager manager) {
        //
    }

    @Override
    public void updateNarration(@Nonnull NarrationElementOutput output) {
        //
    }
}
