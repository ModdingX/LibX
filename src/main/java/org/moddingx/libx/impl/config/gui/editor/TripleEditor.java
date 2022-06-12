package org.moddingx.libx.impl.config.gui.editor;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.apache.commons.lang3.tuple.Triple;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.WidgetProperties;
import org.moddingx.libx.impl.config.gui.EditorHelper;
import org.moddingx.libx.screen.Panel;

import javax.annotation.Nullable;

public class TripleEditor<A, B, C> implements ConfigEditor<Triple<A, B, C>> {

    private final ConfigEditor<A> editor1;
    private final ConfigEditor<B> editor2;
    private final ConfigEditor<C> editor3;

    public TripleEditor(ConfigEditor<A> editor1, ConfigEditor<B> editor2, ConfigEditor<C> editor3) {
        this.editor1 = editor1;
        this.editor2 = editor2;
        this.editor3 = editor3;
    }

    @Override
    public Triple<A, B, C> defaultValue() {
        return Triple.of(this.editor1.defaultValue(), this.editor2.defaultValue(), this.editor3.defaultValue());
    }

    @Override
    public AbstractWidget createWidget(Screen screen, Triple<A, B, C> initialValue, WidgetProperties<Triple<A, B, C>> properties) {
        return new TripleWidget<>(screen, this.editor1, this.editor2, this.editor3, initialValue.getLeft(), initialValue.getMiddle(), initialValue.getRight(),null, null, null, properties);
    }

    @Override
    public AbstractWidget updateWidget(Screen screen, AbstractWidget old, WidgetProperties<Triple<A, B, C>> properties) {
        if (old instanceof TripleWidget<?, ?, ?>) {
            //noinspection unchecked
            return new TripleWidget<>(screen, this.editor1, this.editor2, this.editor3, ((TripleWidget<A, B, C>) old).left, ((TripleWidget<A, B, C>) old).middle, ((TripleWidget<A, B, C>) old).right, ((TripleWidget<A, B, C>) old).leftWidget, ((TripleWidget<A, B, C>) old).middleWidget, ((TripleWidget<A, B, C>) old).rightWidget, properties);
        } else {
            return this.createWidget(screen, this.defaultValue(), properties);
        }
    }
    
    private static class TripleWidget<A, B, C> extends Panel {

        public final AbstractWidget leftWidget;
        public final AbstractWidget middleWidget;
        public final AbstractWidget rightWidget;
        
        private A left;
        private B middle;
        private C right;
        
        public TripleWidget(Screen screen, ConfigEditor<A> editor1, ConfigEditor<B> editor2, ConfigEditor<C> editor3,
                            A left, B middle, C right, @Nullable AbstractWidget leftWidget, @Nullable AbstractWidget middleWidget,
                            @Nullable AbstractWidget rightWidget, WidgetProperties<Triple<A, B, C>> properties) {
            super(screen, properties.x(), properties.y(), properties.width(), properties.height());
            
            this.left = left;
            this.middle = middle;
            this.right = right;

            int width = (properties.width() - 2) / 3;
            
            WidgetProperties<A> leftProperties = new WidgetProperties<>(0, 0, width, properties.height(), a -> {
                this.left = a;
                properties.inputChanged().accept(Triple.of(this.left, this.middle, this.right));
            });
            
            WidgetProperties<B> middleProperties = new WidgetProperties<>(width + 1, 0, width, properties.height(), b -> {
                this.middle = b;
                properties.inputChanged().accept(Triple.of(this.left, this.middle, this.right));
            });
            
            WidgetProperties<C> rightProperties = new WidgetProperties<>((2 * width) + 2, 0, width, properties.height(), c -> {
                this.right = c;
                properties.inputChanged().accept(Triple.of(this.left, this.middle, this.right));
            });
            
            this.leftWidget = this.addRenderableWidget(EditorHelper.create(screen, editor1, this.left, leftWidget, leftProperties));
            this.middleWidget = this.addRenderableWidget(EditorHelper.create(screen, editor2, this.middle, middleWidget, middleProperties));
            this.rightWidget = this.addRenderableWidget(EditorHelper.create(screen, editor3, this.right, rightWidget, rightProperties));
        }
    }
}
