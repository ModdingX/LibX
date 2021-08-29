package io.github.noeppi_noeppi.libx.impl.config.gui.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.gui.ScrollPanel;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public abstract class ConfigBaseScreen extends Screen {

    protected final Minecraft mc;

    @Nullable
    private final ConfigScreenManager manager;
    private final boolean hasSearchBar;

    @Nullable
    private EditBox searchBar;
    
    @Nullable
    private BasePanel panel;

    protected ConfigBaseScreen(Component title, @Nullable ConfigScreenManager manager, boolean hasSearchBar) {
        super(title);
        this.mc = Minecraft.getInstance();
        this.manager = manager;
        this.hasSearchBar = hasSearchBar;
    }

    @Override
    protected void init() {
        if (this.manager != null) {
            Button back = new Button(5, 5, 42, 20, new TextComponent("← ").append(new TranslatableComponent("libx.config.gui.back")), button -> this.manager.close());
            this.addRenderableWidget(back);
        }

        if (this.hasSearchBar) {
            boolean shouldFocus = this.searchBar != null && this.searchBar.isFocused();
            boolean isActive = this.searchBar != null && this.getFocused() == this.searchBar;
            this.searchBar = new EditBox(this.mc.font, 20, 18 + this.mc.font.lineHeight, this.width - 40, 20, this.searchBar, new TranslatableComponent("libx.config.gui.search.title"));
            this.searchBar.setMaxLength(32767);
            this.searchBar.setFocus(shouldFocus);
            this.addRenderableWidget(this.searchBar);
            if (isActive) {
                this.setFocused(this.searchBar);
            }
            // Responder must be set last, or we'll trigger it while configuring the search bar
            this.searchBar.setResponder(this::searchChange);
        } else {
            this.searchBar = null;
        }

        this.rebuild();
    }

    protected void rebuild() {
        if (this.panel != null) {
            this.removeWidget(this.panel);
        }
        
        ImmutableList.Builder<AbstractWidget> widgetBuilder = ImmutableList.builder();
        this.buildGui(widgetBuilder::add);
        List<AbstractWidget> widgets = widgetBuilder.build();

        int totalHeight = 10 + widgets.stream().map(w -> w.y + w.getHeight()).max(Comparator.naturalOrder()).orElse(0);
        int paddingTop = 18 + this.mc.font.lineHeight + (this.hasSearchBar ? 26 : 0);

        this.panel = new BasePanel(this.mc, this.width - 5, this.height - paddingTop, paddingTop, 1) {

            @Override
            protected int getContentHeight() {
                return totalHeight;
            }

            @Override
            protected void drawPanel(PoseStack poseStack, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
                poseStack.pushPose();
                poseStack.translate(0, relativeY, 0);
                for (AbstractWidget widget : widgets) {
                    widget.render(poseStack, mouseX, mouseY - relativeY, ConfigBaseScreen.this.mc.getDeltaFrameTime());
                }
                poseStack.popPose();
            }

            @Override
            protected boolean clickPanel(double mouseX, double mouseY, int button) {
                for (GuiEventListener widget : widgets) {
                    if (widget.mouseClicked(mouseX, mouseY, button)) {
                        this.setFocused(widget);
                        if (button == 0) {
                            this.setDragging(true);
                        }
                        return true;
                    }
                }
                return false;
            }
        };
        this.addRenderableWidget(this.panel);
    }

    protected abstract void buildGui(Consumer<AbstractWidget> consumer);

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderDirtBackground(0);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        //noinspection IntegerDivisionInFloatingPointContext
        this.mc.font.drawShadow(poseStack, this.getTitle(), (this.width - this.mc.font.width(this.getTitle())) / 2, 12, 0xFFFFFF);
    }

    @Override
    public boolean keyPressed(int key, int i1, int i2) {
        if (key == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc() && this.manager != null) {
            this.manager.close();
            return true;
        } else {
            return super.keyPressed(key, i1, i2);
        }
    }

    public String searchTerm() {
        return this.searchBar == null ? "" : this.searchBar.getValue();
    }
    
    protected void searchChange(String term) {
        
    }

    private static abstract class BasePanel extends ScrollPanel implements NarratableEntry {

        public BasePanel(Minecraft mc, int width, int height, int top, int left) {
            super(mc, width, height, top, left);
        }

        @Nonnull
        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(@Nonnull NarrationElementOutput output) {
            //
        }

        @Override
        public boolean isActive() {
            return NarratableEntry.super.isActive();
        }
    }
}
