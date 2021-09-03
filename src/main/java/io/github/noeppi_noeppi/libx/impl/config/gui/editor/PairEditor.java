package io.github.noeppi_noeppi.libx.impl.config.gui.editor;

import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import io.github.noeppi_noeppi.libx.config.gui.WidgetProperties;
import io.github.noeppi_noeppi.libx.impl.config.gui.EditorHelper;
import io.github.noeppi_noeppi.libx.screen.Panel;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;

public class PairEditor<A, B> implements ConfigEditor<Pair<A, B>> {

    private final ConfigEditor<A> editor1;
    private final ConfigEditor<B> editor2;

    public PairEditor(ConfigEditor<A> editor1, ConfigEditor<B> editor2) {
        this.editor1 = editor1;
        this.editor2 = editor2;
    }

    @Override
    public Pair<A, B> defaultValue() {
        return Pair.of(this.editor1.defaultValue(), this.editor2.defaultValue());
    }

    @Override
    public AbstractWidget createWidget(Screen screen, Pair<A, B> initialValue, WidgetProperties<Pair<A, B>> properties) {
        return new PairWidget<>(screen, this.editor1, this.editor2, initialValue.getLeft(), initialValue.getRight(),null, null, properties);
    }

    @Override
    public AbstractWidget updateWidget(Screen screen, AbstractWidget old, WidgetProperties<Pair<A, B>> properties) {
        if (old instanceof PairWidget<?, ?>) {
            //noinspection unchecked
            return new PairWidget<>(screen, this.editor1, this.editor2, ((PairWidget<A, B>) old).left, ((PairWidget<A, B>) old).right, ((PairWidget<A, B>) old).leftWidget, ((PairWidget<A, B>) old).rightWidget, properties);
        } else {
            return this.createWidget(screen, this.defaultValue(), properties);
        }
    }
    
    private static class PairWidget<A, B> extends Panel {

        public final AbstractWidget leftWidget;
        public final AbstractWidget rightWidget;
        
        private A left;
        private B right;
        
        public PairWidget(Screen screen, ConfigEditor<A> editor1, ConfigEditor<B> editor2,
                          A left, B right, @Nullable AbstractWidget leftWidget, @Nullable AbstractWidget rightWidget,
                          WidgetProperties<Pair<A, B>> properties) {
            super(screen, properties.x(), properties.y(), properties.width(), properties.height());
            
            this.left = left;
            this.right = right;

            int width = properties.width() / 2;
            
            WidgetProperties<A> leftProperties = new WidgetProperties<>(0, 0, width, properties.height(), a -> {
                this.left = a;
                properties.inputChanged().accept(Pair.of(this.left, this.right));
            });
            
            WidgetProperties<B> rightProperties = new WidgetProperties<>(width, 0, width, properties.height(), b -> {
                this.right = b;
                properties.inputChanged().accept(Pair.of(this.left, this.right));
            });
            
            this.leftWidget = this.addRenderableWidget(EditorHelper.create(screen, editor1, this.left, leftWidget, leftProperties));
            this.rightWidget = this.addRenderableWidget(EditorHelper.create(screen, editor2, this.right, rightWidget, rightProperties));
        }
    }
}
