package org.moddingx.libx.screen;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.config.gui.EditorOps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * An {@link AbstractWidget} that is composed of multiple other widgets. These
 * widgets are positioned relative to this widget. You can add these widgets in
 * the constructor.
 */
public abstract class Panel extends AbstractWidget implements ContainerEventHandler, EditorOps {

    private final List<GuiEventListener> children = new ArrayList<>();
    private final List<Renderable> renderables = new ArrayList<>();

    @Nullable
    private GuiEventListener focused = null;
    private boolean dragging = false;

    public Panel(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
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

    @Nonnull
    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.unmodifiableList(this.children);
    }

    @Override
    public void renderWidget(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.pose().pushPose();
        graphics.pose().translate(this.getX(), this.getY(), 0);
        for (Renderable widget : this.renderables) {
            widget.render(graphics, mouseX - this.getX(), mouseY - this.getY(), partialTicks);
        }
        graphics.pose().popPose();
    }

    @Nonnull
    @Override
    public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
        return ContainerEventHandler.super.getChildAt(mouseX - this.getX(), mouseY - this.getY());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener child : this.children) {
            if (child.mouseClicked(mouseX - this.getX(), mouseY - this.getY(), button)) {
                this.setFocused(child);
                if (button == 0) this.setDragging(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.setDragging(false);
        return this.getChildAt(mouseX, mouseY).filter(child -> child.mouseReleased(mouseX - this.getX(), mouseY - this.getY(), button)).isPresent();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return this.focused != null && this.dragging && button == 0 && this.focused.mouseDragged(mouseX - this.getX(), mouseY - this.getY(), button, dragX - this.getX(), dragY - this.getY());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return this.getChildAt(mouseX, mouseY).filter(child -> child.mouseScrolled(mouseX - this.getX(), mouseY - this.getY(), delta)).isPresent();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return ContainerEventHandler.super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return ContainerEventHandler.super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char value, int modifiers) {
        return ContainerEventHandler.super.charTyped(value, modifiers);
    }

    @Override
    public void updateWidgetNarration(@Nonnull NarrationElementOutput output) {
        //
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        this.updateChildFocus();
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        this.focused = this.children.contains(focused) ? focused : null;
        this.setFocused(focused != null);
    }

    private void updateChildFocus() {
        for (GuiEventListener child : this.children) {
            boolean shouldBeFocused = this.isFocused() && child == this.focused;
            if (child.isFocused() != shouldBeFocused) {
                child.setFocused(shouldBeFocused);
            }
        }
    }

    @Nullable
    @Override
    public ComponentPath getCurrentFocusPath() {
        return ContainerEventHandler.super.getCurrentFocusPath();
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(@Nonnull FocusNavigationEvent event) {
        return ContainerEventHandler.super.nextFocusPath(event);
    }

    @Override
    public boolean isDragging() {
        return this.dragging;
    }

    @Override
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    @Override
    public void enabled(boolean enabled) {
        for (GuiEventListener child : this.children) {
            EditorOps.wrap(child).enabled(enabled);
        }
    }
}
