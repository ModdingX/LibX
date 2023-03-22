package org.moddingx.libx.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.config.gui.EditorOps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link AbstractWidget} that is composed of multiple other widgets. These
 * widgets are positioned relative to this widget. You can add these widgets in
 * the constructor.
 */
public abstract class Panel extends AbstractWidget implements EditorOps {

    protected final Screen screen;
    private final List<GuiEventListener> children = new ArrayList<>();
    private final List<Renderable> renderables = new ArrayList<>();

    @Nullable
    protected GuiEventListener focused = null;

    public Panel(Screen screen, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.screen = screen;
    }

    /**
     * Adds a widget that can be rendered.
     */
    protected <T extends GuiEventListener & Renderable> T addRenderableWidget(T widget) {
        this.renderables.add(widget);
        this.children.add(widget);
        return widget;
    }
    
    /**
     * Adds a component that can be rendered.
     */
    protected <T extends Renderable> T addRenderableOnly(T widget) {
        this.renderables.add(widget);
        return widget;
    }
    
    /**
     * Adds a widget to listen to events.
     */
    protected <T extends GuiEventListener> T addWidget(T widget) {
        this.children.add(widget);
        return widget;
    }

    @Override
    public void renderWidget(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        poseStack.pushPose();
        poseStack.translate(this.getX(), this.getY(), 0);
        for (Renderable widget : this.renderables) {
            widget.render(poseStack, mouseX - this.getX(), mouseY - this.getY(), partialTicks);
        }
        poseStack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean success = false;
        for (GuiEventListener child : this.children) {
            if (child.mouseClicked(mouseX - this.getX(), mouseY - this.getY(), button)) {
                this.screen.setFocused(this);
                this.focused = child;
                if (button == 0) {
                    this.screen.setDragging(true);
                }
                success = true;
            }
        }
        return success;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.screen.setDragging(false);
        for (GuiEventListener child : this.children) {
            if (child.isMouseOver(mouseX - this.getX(), mouseY - this.getY())) {
                if (child.mouseReleased(mouseX - this.getX(), mouseY - this.getY(), button)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return this.focused != null && this.screen.isDragging() && this.focused.mouseDragged(mouseX - this.getX(), mouseY - this.getY(), button, dragX - this.getX(), dragY - this.getY());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.focused != null && this.focused.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return this.focused != null && this.focused.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char value, int modifiers) {
        return this.focused != null && this.focused.charTyped(value, modifiers);
    }

    @Override
    public void updateWidgetNarration(@Nonnull NarrationElementOutput output) {
        //
    }

    @Override
    public void enabled(boolean enabled) {
        for (Renderable child : this.renderables) {
            EditorOps.wrap(child).enabled(enabled);
        }
    }
}
