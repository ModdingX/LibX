package org.moddingx.libx.impl.screen.text;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.screen.text.AlignedComponent;
import org.moddingx.libx.screen.text.ComponentLayout;

import javax.annotation.Nullable;
import java.util.List;

public class SimpleLayout implements ComponentLayout {
    
    @Nullable
    private final Component title;
    private final List<Component> lines;

    public SimpleLayout(@Nullable Component title, List<Component> lines) {
        this.title = title;
        this.lines = List.copyOf(lines);
    }

    @Nullable
    @Override
    public Component title() {
        return this.title;
    }

    @Override
    public List<AlignedComponent> alignComponents(Font font, int width) {
        ImmutableList.Builder<AlignedComponent> list = ImmutableList.builder();
        if (this.title != null) {
            list.add(new AlignedComponent(this.title, (width - font.width(this.title)) / 2, 0, false));
        }
        boolean first = true;
        for (Component line : this.lines) {
            list.add(new AlignedComponent(line, 0, first ? (this.title != null ? 10 : 0) : 5));
            first = false;
        }
        return list.build();
    }
}
