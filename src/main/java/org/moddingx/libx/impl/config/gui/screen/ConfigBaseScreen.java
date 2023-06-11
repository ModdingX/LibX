package org.moddingx.libx.impl.config.gui.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.moddingx.libx.render.FilterGuiGraphics;
import org.moddingx.libx.render.RenderHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
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
    // Because clipping is enabled, and they need to be rendered with
    // absolute coordinates as they should not be cut by the screen border.
    private final List<Pair<Matrix4f, Consumer<GuiGraphics>>> capturedTooltips = new LinkedList<>();
    private boolean isCapturingTooltips = false;
    private int currentScrollOffset = 0;

    protected ConfigBaseScreen(Component title, @Nullable ConfigScreenManager manager, boolean hasSearchBar) {
        super(title);
        this.mc = Minecraft.getInstance();
        this.manager = manager;
        this.hasSearchBar = hasSearchBar;
    }
    
    public int contentWidth() {
        return this.width - 12;
    }

    @Override
    protected void init() {
        if (this.manager != null) {
            Button back = Button.builder(Component.literal("\u2190 ").append(Component.translatable("libx.config.gui.back")), button -> this.manager.close())
                    .pos(5, 5)
                    .size(52, 20)
                    .build();
            this.addRenderableWidget(back);
        }

        if (this.hasSearchBar) {
            boolean shouldFocus = this.searchBar != null && this.searchBar.isFocused();
            boolean isActive = this.searchBar != null && this.getFocused() == this.searchBar;
            this.searchBar = new EditBox(this.mc.font, 20, 18 + this.mc.font.lineHeight, this.width - 40, 20, this.searchBar, Component.translatable("libx.config.gui.search.title"));
            this.searchBar.setMaxLength(32767);
            this.searchBar.setFocused(shouldFocus);
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

        int totalHeight = 10 + widgets.stream().map(w -> w.getY() + w.getHeight()).max(Comparator.naturalOrder()).orElse(0);
        int paddingTop = 18 + this.mc.font.lineHeight + (this.hasSearchBar ? 26 : 0);

        this.panel = new BasePanel(this.mc, this.width - 2, this.height - paddingTop, paddingTop, 1) {

            @Override
            protected int getContentHeight() {
                return totalHeight;
            }

            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                ConfigBaseScreen.this.isCapturingTooltips = true;
                graphics.pose().pushPose();
                super.render(new TooltipCapturingGuiGraphics(graphics), mouseX, mouseY, partialTicks);
                graphics.pose().popPose();
                ConfigBaseScreen.this.isCapturingTooltips = false;
                ConfigBaseScreen.this.capturedTooltips.forEach(pair -> {
                    graphics.pose().pushPose();
                    graphics.pose().setIdentity();
                    graphics.pose().mulPoseMatrix(pair.getLeft());
                    pair.getRight().accept(graphics);
                    graphics.pose().popPose();
                });
                ConfigBaseScreen.this.capturedTooltips.clear();
            }

            @Override
            protected void drawPanel(GuiGraphics graphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
                ConfigBaseScreen.this.currentScrollOffset = relativeY;
                graphics.pose().pushPose();
                graphics.pose().translate(0, relativeY, 0);
                for (AbstractWidget widget : widgets) {
                    widget.render(graphics, mouseX, mouseY - relativeY, ConfigBaseScreen.this.mc.getDeltaFrameTime());
                }
                graphics.pose().popPose();
                ConfigBaseScreen.this.currentScrollOffset = 0;
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                // Without this, the panel would process clicks from areas currently not on the screen
                if (mouseX >= this.left && mouseX <= this.left + this.width && mouseY >= this.top && mouseY <= this.top + this.height) {
                    return super.mouseClicked(mouseX, mouseY, button);
                } else {
                    return false;
                }
            }

            @Override
            protected boolean clickPanel(double mouseX, double mouseY, int button) {
                // Extra var required as we need to cal all listeners
                // so widgets can for example handle their loss of focus.
                boolean success = false;
                for (GuiEventListener widget : widgets) {
                    if (widget.mouseClicked(mouseX, mouseY, button)) {
                        this.setFocused(widget);
                        if (button == 0) {
                            this.setDragging(true);
                        }
                        success = true;
                    }
                }
                return success;
            }

            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
                if (!super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                    if (this.getFocused() != null && this.isDragging() && button == 0) {
                        return this.getFocused().mouseDragged(mouseX, mouseY - this.top + ((int) this.scrollDistance) - this.border, button, dragX, dragY);
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                if (!super.mouseReleased(mouseX, mouseY, button)) {
                    if (this.getFocused() != null) {
                        return this.getFocused().mouseReleased(mouseX, mouseY - this.top + ((int) this.scrollDistance) - this.border, button);
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
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderDirtBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        RenderHelper.resetColor();
        graphics.drawString(this.font, this.getTitle(), (this.width - this.mc.font.width(this.getTitle())) / 2, 11, 0xFFFFFF, true);
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

    private void captureTooltip(PoseStack.Pose pose, BiConsumer<GuiGraphics, Integer> action) {
        final int theOffset = this.currentScrollOffset;
        Matrix4f matrix = new Matrix4f(pose.pose());
        matrix.translate(0, -theOffset, 0);
        this.capturedTooltips.add(Pair.of(matrix, poseStack -> action.accept(poseStack, theOffset)));
    }
    
    private class TooltipCapturingGuiGraphics extends FilterGuiGraphics {
        
        public TooltipCapturingGuiGraphics(GuiGraphics parent) {
            super(parent);
        }

        @Override
        public void renderTooltip(@Nonnull Font font, @Nonnull ItemStack stack, int x, int y) {
            if (ConfigBaseScreen.this.isCapturingTooltips) {
                ConfigBaseScreen.this.captureTooltip(this.pose().last(), (graphics, scrollOffset) -> graphics.renderTooltip(font, stack, x, y + scrollOffset));
            } else {
                super.renderTooltip(font, stack, x, y);
            }
        }

        @Override
        public void renderTooltip(@Nonnull Font font, @Nonnull List<Component> text, @Nonnull Optional<TooltipComponent> component, @Nonnull ItemStack stack, int x, int y) {
            if (ConfigBaseScreen.this.isCapturingTooltips) {
                ConfigBaseScreen.this.captureTooltip(this.pose().last(), (graphics, scrollOffset) -> graphics.renderTooltip(font, text, component, stack, x, y + scrollOffset));
            } else {
                super.renderTooltip(font, text, component, stack, x, y);
            }
        }

        @Override
        public void renderTooltip(@Nonnull Font font, @Nonnull List<Component> text, @Nonnull Optional<TooltipComponent> component, int x, int y) {
            if (ConfigBaseScreen.this.isCapturingTooltips) {
                ConfigBaseScreen.this.captureTooltip(this.pose().last(), (graphics, scrollOffset) -> graphics.renderTooltip(font, text, component, x, y + scrollOffset));
            } else {
                super.renderTooltip(font, text, component, x, y);
            }
        }

        @Override
        public void renderTooltip(@Nonnull Font font, @Nonnull Component text, int x, int y) {
            if (ConfigBaseScreen.this.isCapturingTooltips) {
                ConfigBaseScreen.this.captureTooltip(this.pose().last(), (graphics, scrollOffset) -> graphics.renderTooltip(font, text, x, y + scrollOffset));
            } else {
                super.renderTooltip(font, text, x, y);
            }
        }

        @Override
        public void renderComponentTooltip(@Nonnull Font font, @Nonnull List<Component> text, int x, int y) {
            if (ConfigBaseScreen.this.isCapturingTooltips) {
                ConfigBaseScreen.this.captureTooltip(this.pose().last(), (graphics, scrollOffset) -> graphics.renderComponentTooltip(font, text, x, y + scrollOffset));
            } else {
                super.renderComponentTooltip(font, text, x, y);
            }
        }

        @Override
        public void renderComponentTooltip(@Nonnull Font font, @Nonnull List<? extends FormattedText> text, int x, int y, @Nonnull ItemStack stack) {
            if (ConfigBaseScreen.this.isCapturingTooltips) {
                ConfigBaseScreen.this.captureTooltip(this.pose().last(), (graphics, scrollOffset) -> graphics.renderComponentTooltip(font, text, x, y + scrollOffset, stack));
            } else {
                super.renderComponentTooltip(font, text, x, y, stack);
            }
        }

        @Override
        public void renderTooltip(@Nonnull Font font, @Nonnull List<? extends FormattedCharSequence> text, int x, int y) {
            if (ConfigBaseScreen.this.isCapturingTooltips) {
                ConfigBaseScreen.this.captureTooltip(this.pose().last(), (graphics, scrollOffset) -> graphics.renderTooltip(font, text, x, y + scrollOffset));
            } else {
                super.renderTooltip(font, text, x, y);
            }
        }

        @Override
        public void renderTooltip(@Nonnull Font font, @Nonnull List<FormattedCharSequence> text, @Nonnull ClientTooltipPositioner positioner, int x, int y) {
            if (ConfigBaseScreen.this.isCapturingTooltips) {
                ConfigBaseScreen.this.captureTooltip(this.pose().last(), (graphics, scrollOffset) -> graphics.renderTooltip(font, text, positioner, x, y + scrollOffset));
            } else {
                super.renderTooltip(font, text, positioner, x, y);
            }
        }
    }
}
