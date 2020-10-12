package io.github.noeppi_noeppi.libx.mod;

import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class ModX {

    public final String modid;

    @Nullable
    public final ItemGroup tab;

    private final List<Runnable> setupTasks = new ArrayList<>();

    protected ModX(String modid, @Nullable ItemGroup tab) {
        this.modid = modid;
        this.tab = tab;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::runSetupTasks);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    private void runSetupTasks(FMLCommonSetupEvent event) {
        this.setupTasks.forEach(Runnable::run);
    }

    protected abstract void setup(FMLCommonSetupEvent event);
    protected abstract void clientSetup(FMLClientSetupEvent event);

    @Deprecated
    public void addSetupTask(Runnable runnable) {
        this.setupTasks.add(runnable);
    }
}
