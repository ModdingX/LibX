package io.github.noeppi_noeppi.libx.impl.config.gui.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.ScrollPanel;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
    
    // While rendering the scrollable view, tooltips must be delayed
    // Because scissors is enabled, and they need to be rendered with
    // absolute coordinates as they should not be cut by the screen border.
    private final List<Pair<PoseStack.Pose, Runnable>> capturedTooltips = new LinkedList<>();
    private boolean isCapturingTooltips = false;

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
            public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
                ConfigBaseScreen.this.isCapturingTooltips = true;
                super.render(poseStack, mouseX, mouseY, partialTicks);
                ConfigBaseScreen.this.isCapturingTooltips = false;
                ConfigBaseScreen.this.capturedTooltips.forEach(pair -> {
                    poseStack.pushPose();
                    poseStack.setIdentity();
                    poseStack.mulPoseMatrix(pair.getLeft().pose());
                    pair.getRight().run();
                    poseStack.popPose();
                });
                ConfigBaseScreen.this.capturedTooltips.clear();
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

            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
                if (!super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                    if (this.getFocused() != null && this.isDragging() && button == 0) {
                        return this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY);
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            }
        };
        this.addRenderableWidget(this.panel);
    }

    protected abstract void buildGui(Consumer<AbstractWidget> consumer);

    @Nullable
    public ConfigScreenManager getCurrentManager() {
        return this.manager;
    }

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
    }

    private void captureTooltip(PoseStack.Pose pose, Runnable action) {
        this.capturedTooltips.add(Pair.of(pose, action));
    }
    
    @Override
    protected void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull ItemStack stack, int x, int y) {
        if (this.isCapturingTooltips) {
            // Not inside lambda as the value may change
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, stack, x, y));
        } else {
            super.renderTooltip(poseStack, stack, x, y);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull List<Component> tooltip, @Nonnull Optional<TooltipComponent> special, int x, int y) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, special, x, y));
        } else {
            super.renderTooltip(poseStack, tooltip, special, x, y);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull Component tooltip, int x, int y) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, x, y));
        } else {
            super.renderTooltip(poseStack, tooltip, x, y);
        }
    }

    @Override
    public void renderComponentTooltip(@Nonnull PoseStack poseStack, @Nonnull List<Component> tooltip, int x, int y) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderComponentTooltip(poseStack, tooltip, x, y));
        } else {
            super.renderComponentTooltip(poseStack, tooltip, x, y);
        }
    }

    @Override
    public void renderComponentToolTip(@Nonnull PoseStack poseStack, @Nonnull List<? extends FormattedText> tooltip, int x, int y, @Nonnull Font font) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderComponentToolTip(poseStack, tooltip, x, y, font));
        } else {
            super.renderComponentToolTip(poseStack, tooltip, x, y, font);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull List<? extends FormattedCharSequence> tooltip, int x, int y) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, x, y));
        } else {
            super.renderTooltip(poseStack, tooltip, x, y);
        }
    }

    @Override
    public void renderToolTip(@Nonnull PoseStack poseStack, @Nonnull List<? extends FormattedCharSequence> tooltip, int x, int y, @Nonnull Font font) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderToolTip(poseStack, tooltip, x, y, font));
        } else {
            super.renderToolTip(poseStack, tooltip, x, y, font);
        }
    }
}