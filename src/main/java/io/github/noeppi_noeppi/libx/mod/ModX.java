package io.github.noeppi_noeppi.libx.mod;

import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A base class for a mod that uses LibX. Is required for many other features
 * of LibX.
 */
public abstract class ModX {

    /**
     * Contains the Mod id  of this mod.
     */
    public final String modid;

    /**
     * A {@link Logger} for the mod.
     */
    public final Logger logger;

    public final ItemGroup tab;

    private final List<Runnable> setupTasks = new ArrayList<>();

    /**
     * Overriding classes should provide a public no-arg constructor that calls this with
     * the values needed.
     */
    protected ModX(String modid, @Nullable ItemGroup tab) {
        this.modid = modid;
        this.logger = LogManager.getLogger(modid);
        this.tab = tab;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::runSetupTasks);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        // As the generated code registers registration handlers this will produce a null pointer exception
        // as the list of handlers will be null. So for instances of ModXRegistration we don't call it here
        // but in the constructor of ModXRegistration
        if (!(this instanceof ModXRegistration))
            this.callGeneratedCode();
    }

    private void runSetupTasks(FMLCommonSetupEvent event) {
        this.setupTasks.forEach(Runnable::run);
    }

    /**
     * Automatically registered to the event bus.
     */
    protected abstract void setup(FMLCommonSetupEvent event);

    /**
     * Automatically registered to the event bus.
     */
    protected abstract void clientSetup(FMLClientSetupEvent event);

    /**
     * This is used internally to add tasks to this mod. <b>THIS SHOULD NOT BE CALLED BY PEOPLE USING
     * THIS LIBRARY.</b> Override {@link ModX#setup(FMLCommonSetupEvent)} instead. This method might
     * fail in the future if called from outside this library.
     *
     * @deprecated This is deprecated as it should only be used internally.
     */
    @Deprecated
    public final void addSetupTask(Runnable runnable) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 2) {
            StackTraceElement element = stackTrace[2];
            if (!element.getClassName().startsWith("io.github.noeppi_noeppi.libx")) {
                this.logger.warn("ModX#addSetupTask was called from outside the library. You should override ModX#setup instead. This might fail in future versions. Caller was: " + element.getClassName() + " Please report to mod author.");
            }
        }
        this.setupTasks.add(runnable);
    }
    
    protected final void callGeneratedCode() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 2) {
            StackTraceElement element = stackTrace[2];
            if (!element.getClassName().startsWith("io.github.noeppi_noeppi.libx")) {
                this.logger.warn("ModX#callGeneratedCode was called from outside the library. This might fail future versions. Caller was: " + element.getClassName() + " Please report to mod author.");
            }
        }
        try {
            Class<?> clazz;
            try {
                clazz = Class.forName(this.getClass().getCanonicalName() + "$");
            } catch (ClassNotFoundException e) {
                clazz = null;
            }
            if (clazz != null) {
                Method method = clazz.getDeclaredMethod("init", ModX.class);
                method.invoke(null, this);
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Generated code threw an exception", e.getTargetException());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to load generated code", e);
        }
    }
}
