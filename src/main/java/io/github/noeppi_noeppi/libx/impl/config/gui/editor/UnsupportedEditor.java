package io.github.noeppi_noeppi.libx.impl.config.gui.editor;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import io.github.noeppi_noeppi.libx.config.gui.EditorOps;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class UnsupportedEditor implements ConfigEditor<Void> {

    private static final UnsupportedEditor INSTANCE = new UnsupportedEditor();
    
    public static <T> ConfigEditor<T> instance() {
        //noinspection unchecked
        return (ConfigEditor<T>) INSTANCE;
    }
    
    @Override
    public AbstractWidget createWidget(Screen screen, @Nullable AbstractWidget oldWidget, int x, int y, int width, int height, Consumer<Void> inputChanged) {
        Button button = new UnsupportedButton(x, y, width, height, new TranslatableComponent("libx.config.editor.unsupported.title").withStyle(ChatFormatting.RED), b -> {}) {
            
            @Override
            public void renderToolTip(@Nonnull PoseStack poseStack, int mouseX, int mouseY) {
                screen.renderTooltip(poseStack, List.of(new TranslatableComponent("libx.config.editor.unsupported.description")), Optional.empty(), mouseX, mouseY);
            }
        };
        button.active = false;
        return button;
    }
    
    // Required so it's not possible to enable the button via EditorOps
    private static class UnsupportedButton extends Button implements EditorOps {

        public UnsupportedButton(int x, int y, int width, int height, Component title, OnPress action) {
            super(x, y, width, height, title, action);
        }

        @Override
        public void enabled(boolean enabled) {
            //
        }
    }
}
