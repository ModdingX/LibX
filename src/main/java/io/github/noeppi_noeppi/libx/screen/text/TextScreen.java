package io.github.noeppi_noeppi.libx.screen.text;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.noeppi_noeppi.libx.impl.screen.text.TextScreenContent;
import io.github.noeppi_noeppi.libx.render.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A screen to display some text defined by a {@link ComponentLayout}.
 */
public class TextScreen extends Screen {
    
    private final ComponentLayout layout;
    private final int displayWidth;
    
    @Nullable
    private TextScreenContent content;
    
    public TextScreen(ComponentLayout layout) {
        this(layout, 176);
    }
    
    public TextScreen(ComponentLayout layout, int width) {
        super(Optional.ofNullable(layout.title()).orElse(new TextComponent("")));
        this.layout = layout;
        this.displayWidth = width;
        this.content = null;
    }

    @Override
    protected void init() {
        this.content = new TextScreenContent(this.font, this.displayWidth, this.layout.alignComponents(this.font, this.displayWidth));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        if (this.content == null) return;
        int left = (this.width - this.content.width()) / 2;
        int top = (this.height - this.content.height()) / 2;
        poseStack.pushPose();
        poseStack.translate(0, 0, 20);
        RenderHelper.renderGuiBackground(poseStack, left - 10, top - 10, this.content.width() + 20, this.content.height() + 20);
        poseStack.translate(0, 0, 20);
        this.content.render(poseStack, left, top);
        Style tooltip = this.content.hoveredStyle(mouseX - left, mouseY - top);
        if (tooltip != null) {
            poseStack.translate(0, 0, 20);
            this.renderComponentHoverEffect(poseStack, tooltip, mouseX, mouseY);
        }
        poseStack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && this.content != null) {
            int left = (this.width - this.content.width()) / 2;
            int top = (this.height - this.content.height()) / 2;
            Style click = this.content.hoveredStyle(((int) mouseX) - left, ((int) mouseY) - top);
            if (click != null) {
                return this.handleComponentClicked(click);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void insertText(@Nonnull String text, boolean overwrite) {
        Minecraft.getInstance().openChatScreen(text);
    }
}
