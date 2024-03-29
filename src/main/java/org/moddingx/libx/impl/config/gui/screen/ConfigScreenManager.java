package org.moddingx.libx.impl.config.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.moddingx.libx.impl.config.gui.ConfigDisplay;

import javax.annotation.Nullable;
import java.util.Stack;

public class ConfigScreenManager {
    
    public final Minecraft minecraft;
    public final ConfigDisplay display;
    
    @Nullable
    private final Screen root;
    private final Stack<Screen> history;

    // Can't use the current screen as root as the config selection screen
    // should not appear in the history, which means the root screen is
    // the mod list screen. However, when this object is constructed, the
    // mod list screen might have already been closed.
    public ConfigScreenManager(Minecraft minecraft, @Nullable Screen root, ConfigDisplay display) {
        this.minecraft = minecraft;
        this.display = display;
        this.root = root;
        this.history = new Stack<>();
    }

    @Nullable
    public Screen current() {
        return this.history.isEmpty() ? null : this.history.peek();
    }
    
    public void open(Screen screen) {
        if (screen != this.current()) {
            this.history.push(screen);
            this.minecraft.setScreen(screen);
        }
    }
    
    public void pushUnchecked(Screen screen) {
        this.history.push(screen);
    }
    
    public void close() {
        if (!this.history.empty()) {
            this.history.pop();
            if (this.history.empty()) {
                this.minecraft.setScreen(this.root);
                this.display.save();
            } else {
                this.minecraft.setScreen(this.history.peek());
            }
        }
    }
    
    public void closeAll() {
        while(!this.history.empty()) this.close();
    }
}
