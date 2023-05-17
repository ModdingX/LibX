package org.moddingx.libx.screen.text;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.lwjgl.glfw.GLFW;
import org.moddingx.libx.impl.screen.text.TextScreenContent;
import org.moddingx.libx.render.RenderHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A screen to display mostly text and optionally some widgets defined by a {@link ComponentLayout}.
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
        super(Optional.ofNullable(layout.title()).orElse(Component.empty()));
        this.layout = layout;
        this.displayWidth = width;
        this.content = null;
    }

    private int left() {
        if (this.content == null) return 0;
        return (this.width - this.content.width()) / 2;
    }

    private int top() {
        if (this.content == null) return 0;
        return (this.height - this.content.height()) / 2;
    }
    
    @Override
    protected void init() {
        this.content = new TextScreenContent(this.font, this.displayWidth, this.layout.alignComponents(this.font, this.displayWidth));
        this.content.addWidgets(this.left(), this.top(), this::addRenderableWidget);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        if (this.content == null) return;
        int left = this.left();
        int top = this.top();
        poseStack.pushPose();
        poseStack.translate(0, 0, 20);
        RenderHelper.renderGuiBackground(poseStack, left - 10, top - 10, this.content.width() + 20, this.content.height() + 20);
        poseStack.translate(0, 0, 20);
        this.content.render(poseStack, left, top);
        Style tooltip = this.content.hoveredStyle(mouseX - left, mouseY - top);
        if (tooltip != null) {
            poseStack.translate(0, 0, 20);
            RenderHelper.resetColor();
            this.renderComponentHoverEffect(poseStack, tooltip, mouseX, mouseY);
        }
        poseStack.popPose();
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && this.content != null) {
            Style click = this.content.hoveredStyle(((int) mouseX) - this.left(), ((int) mouseY) - this.top());
            if (click != null) {
                return this.handleComponentClicked(click);
            }
        }
        return false;
    }

    @Override
    protected void insertText(@Nonnull String text, boolean overwrite) {
        Minecraft.getInstance().openChatScreen(text);
    }
}
