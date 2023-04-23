package org.moddingx.libx.impl.screen.text;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.moddingx.libx.render.RenderHelper;
import org.moddingx.libx.screen.text.AlignedComponent;

import javax.annotation.Nullable;
import java.util.List;

public class TextScreenContent {

    private final Font font;
    private final List<PlacedText> lines;
    private final int width;
    private final int height;
    
    public TextScreenContent(Font font, int width, List<AlignedComponent> components) {
        ImmutableList.Builder<PlacedText> list = ImmutableList.builder();
        int height = 0;
        for (AlignedComponent component : components) {
            List<FormattedCharSequence> lines;
            if (component.wrap()) {
                lines = ComponentRenderUtils.wrapComponents(component.text(), width, font);
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
                list.add(new PlacedText(line, component.left(), height, 0xFFFFFF & component.color(), component.shadow()));
                height += font.lineHeight;
            }
        }
        this.font = font;
        this.lines = list.build();
        this.width = width;
        this.height = height;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }
    
    public void render(PoseStack poseStack, int left, int top) {
        for (PlacedText line : this.lines) {
            RenderHelper.resetColor();
            if (line.shadow()) {
                this.font.drawShadow(poseStack, line.text(), left + line.x(), top + line.y(), line.color());
            } else {
                this.font.draw(poseStack, line.text(), left + line.x(), top + line.y(), line.color());
            }
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
}
