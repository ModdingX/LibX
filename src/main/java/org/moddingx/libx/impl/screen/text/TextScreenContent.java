package org.moddingx.libx.impl.screen.text;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.moddingx.libx.render.RenderHelper;
import org.moddingx.libx.screen.text.AlignedComponent;
import org.moddingx.libx.screen.text.AlignedWidget;
import org.moddingx.libx.screen.text.FlowBox;
import org.moddingx.libx.screen.text.TextScreenEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TextScreenContent {

    private final Font font;
    private final List<PlacedText> lines;
    private final List<PlacedWidget> widgets;
    private final int width;
    private final int height;
    
    public TextScreenContent(Font font, int totalWidth, List<? extends TextScreenEntry> components) {
        ImmutableList.Builder<PlacedText> list = ImmutableList.builder();
        ImmutableList.Builder<PlacedWidget> widgets = ImmutableList.builder();
        int height = 0;
        for (TextScreenEntry entry : components) {
            if (entry instanceof TextScreenEntry.Direct direct) {
                DirectElementBox box = wrapElement(font, totalWidth, direct, false);
                box.placeAt(0, height, list, widgets);
                height += box.height;
            } else if (entry instanceof FlowBox flowBox) {
                height += flowBox.top();
                List<DirectElementBox> row = new ArrayList<>();
                int totalFlowBoxWidth = Math.max(totalWidth - flowBox.left() - flowBox.right(), 1);
                int remainingWidth = totalFlowBoxWidth;
                for (TextScreenEntry.Direct direct : flowBox.elements()) {
                    DirectElementBox box = wrapElement(font, remainingWidth, direct, row.isEmpty());
                    if (box.width() > remainingWidth && remainingWidth < totalFlowBoxWidth) {
                        // Box has been squished in a row shorter than total
                        // start a new row.
                        height += placeRow(
                                flowBox.horizontalAlignment(), flowBox.verticalAlignment(),
                                row, flowBox.left(), height, totalFlowBoxWidth, list, widgets
                        );
                        row.clear();
                        remainingWidth = totalFlowBoxWidth;
                        
                        // Now rebuild the box with the full width
                        //noinspection ConstantValue
                        box = wrapElement(font, remainingWidth, direct, row.isEmpty());
                    }
                    remainingWidth = Math.max(remainingWidth - box.width, 0);
                    row.add(box);
                }
                height += placeRow(
                        flowBox.horizontalAlignment(), flowBox.verticalAlignment(),
                        row, flowBox.left(), height, totalWidth, list, widgets
                );
            }
        }
        this.font = font;
        this.lines = list.build();
        this.widgets = widgets.build();
        this.width = totalWidth;
        this.height = height;
    }
    
    // Box includes padding from the element
    // Width should exceed maxWidth if needed. This is used to detect, where a new row in a FlowBox starts
    private static DirectElementBox wrapElement(Font font, int maxWidth, TextScreenEntry.Direct entry, boolean suppressLeft) {
        if (maxWidth == 0) maxWidth = 1;
        int theLeft = suppressLeft ? 0 : entry.left();
        if (entry instanceof AlignedComponent component) {
            int width = 0;
            int height = 0;
            ImmutableList.Builder<PlacedText> list = ImmutableList.builder();
            
            List<FormattedCharSequence> lines;
            if (component.wrapping() != null) {
                lines = ComponentRenderUtils.wrapComponents(component.text(), Math.max(component.wrapping().minWidth(), maxWidth), font);
            } else {
                lines = List.of(Language.getInstance().getVisualOrder(component.text()));
            }
            boolean first = true;
            for (FormattedCharSequence line : lines) {
                if (first) {
                    height += component.top();
                    first = false;
                } else {
                    height += 2;
                }
                width = Math.max(width, theLeft + font.width(line));
                list.add(new PlacedText(line, theLeft, height, 0xFFFFFF & component.color(), component.shadow()));
                height += font.lineHeight;
            }
            return new DirectElementBox(list.build(), List.of(), width, height, first ? 0 : component.top());
        } else if (entry instanceof AlignedWidget widget) {
            int width = theLeft + widget.widget().getWidth();
            int height = widget.top() + widget.widget().getHeight();
            return new DirectElementBox(List.of(), List.of(new PlacedWidget(widget.widget(), theLeft, widget.top())), width, height, widget.top());
        } else {
            return new DirectElementBox(List.of(), List.of(), 0, 0, 0);
        }
    }
    
    // Returns total height of the row
    private static int placeRow(FlowBox.HorizontalAlignment hor, FlowBox.VerticalAlignment ver, List<DirectElementBox> boxes, int globalXOff, int currentHeight, int totalWidth, ImmutableList.Builder<PlacedText> list, ImmutableList.Builder<PlacedWidget> widgets) {
        if (boxes.isEmpty()) return 0;
        int combinedWidth = 0;
        int maximumTopPadding = 0;
        int maximumContentHeight = 0;
        for (DirectElementBox box : boxes) {
            combinedWidth += box.width();
            maximumTopPadding = Math.max(maximumTopPadding, box.topPadding());
            maximumContentHeight = Math.max(maximumContentHeight, box.contentHeight());
        }
        int xOff = globalXOff + switch (hor) {
            case LEFT -> 0;
            case RIGHT -> totalWidth - combinedWidth;
            case CENTER -> (totalWidth - combinedWidth) / 2;
        };
        for (DirectElementBox box : boxes) {
            int yOff = switch (ver) {
                case TOP -> maximumTopPadding - box.topPadding();
                case CENTER -> maximumTopPadding - box.topPadding() + ((maximumContentHeight - box.contentHeight()) / 2);
                case BOTTOM -> maximumTopPadding - box.topPadding() + maximumContentHeight - box.contentHeight();
            };
            box.placeAt(xOff, currentHeight + yOff, list, widgets);
            xOff += box.width();
        }
        return maximumTopPadding + maximumContentHeight;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }
    
    public void addWidgets(int left, int top, Consumer<? super AbstractWidget> consumer) {
        for (PlacedWidget widget : this.widgets) {
            widget.widget().setX(widget.x() + left);
            widget.widget().setY(widget.y() + top);
            consumer.accept(widget.widget());
        }
    }
    
    public void render(GuiGraphics graphics, int left, int top) {
        for (PlacedText line : this.lines) {
            RenderHelper.resetColor();
            graphics.drawString(this.font, line.text(), left + line.x(), top + line.y(), line.color(), line.shadow());
        }
    }
    
    @Nullable
    public Style hoveredStyle(int mouseX, int mouseY) {
        for (PlacedText line : this.lines) {
            if (mouseX >= line.x() && mouseX <= line.x() + this.font.width(line.text()) && mouseY >= line.y() && mouseY <= line.y() + this.font.lineHeight) {
                int relX = mouseX - line.x();
                Style style = this.font.getSplitter().componentStyleAtWidth(line.text(), relX);
                if (style != null) return style;
            }
        }
        return null;
    }

    private record PlacedText(FormattedCharSequence text, int x, int y, int color, boolean shadow) {}
    private record PlacedWidget(AbstractWidget widget, int x, int y) {}
    private record DirectElementBox(List<PlacedText> text, List<PlacedWidget> widgets, int width, int height, int topPadding) {
        
        public int contentHeight() {
            return Math.max(this.height() - this.topPadding(), 0);
        }
        
        public void placeAt(int x, int y, ImmutableList.Builder<PlacedText> list, ImmutableList.Builder<PlacedWidget> widgets) {
            for (PlacedText text : this.text()) {
                list.add(new PlacedText(text.text(), x + text.x(), y + text.y(), text.color(), text.shadow()));
            }
            for (PlacedWidget widget : this.widgets()) {
                widgets.add(new PlacedWidget(widget.widget(), x + widget.x(), y + widget.y()));
            }
        }
    }
}
