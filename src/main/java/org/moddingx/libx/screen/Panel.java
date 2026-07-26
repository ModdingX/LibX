package org.moddingx.libx.screen;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
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
    public final void extractWidgetRenderState(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(this.getX(), this.getY());
        for (Renderable widget : this.renderables) {
            widget.extractRenderState(graphics, mouseX - this.getX(), mouseY - this.getY(), partialTicks);
        }
        graphics.pose().popMatrix();
        this.extractWidgetContent(graphics, mouseX, mouseY, partialTicks);
    }

    protected void extractWidgetContent(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        //
    }

    @Nonnull
    @Override
    public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
        return ContainerEventHandler.super.getChildAt(mouseX - this.getX(), mouseY - this.getY());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        for (GuiEventListener child : this.children) {
            MouseButtonEvent translatedEvent = new MouseButtonEvent(event.x() - this.getX(), event.y() - this.getY(), event.buttonInfo());
            if (child.mouseClicked(translatedEvent, isDoubleClick)) {
                this.setFocused(child);
                if (event.button() == 0) this.setDragging(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.setDragging(false);
        MouseButtonEvent translatedEvent = new MouseButtonEvent(event.x() - this.getX(), event.y() - this.getY(), event.buttonInfo());
        return this.getChildAt(event.x(), event.y()).filter(child -> child.mouseReleased(translatedEvent)).isPresent();
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        MouseButtonEvent translatedEvent = new MouseButtonEvent(event.x() - this.getX(), event.y() - this.getY(), event.buttonInfo());
        return this.focused != null && this.dragging && event.button() == 0 && this.focused.mouseDragged(translatedEvent, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.getChildAt(mouseX, mouseY).filter(child -> child.mouseScrolled(mouseX - this.getX(), mouseY - this.getY(), scrollX, scrollY)).isPresent();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return ContainerEventHandler.super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        return ContainerEventHandler.super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return ContainerEventHandler.super.charTyped(event);
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
