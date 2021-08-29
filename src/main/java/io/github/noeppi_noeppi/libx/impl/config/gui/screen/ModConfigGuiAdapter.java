package io.github.noeppi_noeppi.libx.impl.config.gui.screen;

import io.github.noeppi_noeppi.libx.impl.config.ConfigImpl;
import io.github.noeppi_noeppi.libx.impl.config.gui.ConfigDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fmlclient.ConfigGuiHandler;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class ModConfigGuiAdapter {
    
    private final String modid;
    private final ModContainer context;
    private boolean hasRegisteredExt;

    public ModConfigGuiAdapter(String modid, ModContainer context) {
        this.modid = modid;
        this.context = context;
        this.checkRegister();
    }
    
    public synchronized void checkRegister() {
        if (!this.hasRegisteredExt && ConfigImpl.getAllConfigs().stream().anyMatch(config -> this.modid.equals(config.id.getNamespace()))) {
            this.hasRegisteredExt = true;
            this.context.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory(this::createScreen));
        }
    }
    
    public Screen createScreen(Minecraft minecraft, Screen modListScreen) {
        List<ConfigImpl> configs = ConfigImpl.getAllConfigs().stream()
                .filter(config -> this.modid.equals(config.id.getNamespace()))
                .sorted(Comparator.comparing(config -> config.id.getPath()))
                .toList();
        if (configs.isEmpty()) {
            return modListScreen;
        } else if (configs.size() == 1) {
            return this.factory(minecraft, modListScreen).apply(configs.get(0));
        } else {
            return new ConfigSelectScreen(this.factory(minecraft, modListScreen), configs);
        }
    }
    
    private Function<ConfigImpl, Screen> factory(Minecraft minecraft, @Nullable Screen root) {
        return config -> {
            ConfigDisplay display = config.createDisplay();
            ConfigScreenManager manager = new ConfigScreenManager(minecraft, root, display);
            throw new RuntimeException("Not implemented");
        };
    }
}
