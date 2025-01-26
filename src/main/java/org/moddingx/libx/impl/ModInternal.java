package org.moddingx.libx.impl;

import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import org.moddingx.libx.datagen.DatagenSystem;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.util.ClassUtil;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;

public class ModInternal {

    private static final Object LOCK = new Object();
    private static final Map<Class<? extends ModX>, ModInternal> MAP = new HashMap<>();
    private static final Map<String, ModInternal> ID_MAP = new HashMap<>();

    public static ModInternal init(ModX mod) {
        ModContainer modContainer = ModLoadingContext.get().getActiveContainer();
        if (!Objects.equals(modContainer.getModId(), mod.modid)) {
            throw new IllegalStateException("Mod entrypoint " + mod.modid + " (" + mod.getClass() + ") constructed while namespace " + modContainer.getModId() + " was active.");
        }
        synchronized (LOCK) {
            if (!Modifier.isFinal(mod.getClass().getModifiers())) {
                throw new IllegalStateException("Mod class must be final. Report to the author of the " + mod.modid + " mod.");
            }
            if (MAP.containsKey(mod.getClass())) {
                throw new IllegalStateException("The same ModX class can't be an entrypoint twice.");
            }
            if (ID_MAP.containsKey(mod.modid)) {
                throw new IllegalStateException("A mod can have multiple entry points but only one of them may be of instance ModX.");
            }
            ModInternal modInternal = new ModInternal(mod, modContainer);
            MAP.put(mod.getClass(), modInternal);
            ID_MAP.put(mod.modid, modInternal);
            return modInternal;
        }
    }

    public static ModInternal get(ModX mod) {
        return get(mod.getClass());
    }
    
    public static ModInternal get(Class<? extends ModX> modClass) {
        synchronized (LOCK) {
            if (!MAP.containsKey(modClass)) {
                throw new NoSuchElementException("ModInternal not found for mod " + modClass);
            }
            return MAP.get(modClass);
        }
    }
    
    public static Optional<ModInternal> get(String modid) {
        synchronized (LOCK) {
            if (ID_MAP.containsKey(modid)) {
                return Optional.of(ID_MAP.get(modid));
            } else {
                return Optional.empty();
            }
        }
    }
    
    private final ModX mod;

    @Nullable
    private final Class<?> modInitClass;
    private final FMLModContainer modContainer;
    private final IEventBus modEventBus;
    private final List<Runnable> setupTasks;
    private final List<Runnable> queueSetupTasks;
    private final List<Consumer<DatagenSystem>> datagenConfiguration;

    private ModInternal(ModX mod, ModContainer modContainer) {
        if (!(modContainer instanceof FMLModContainer fmlContainer)) {
            throw new IllegalStateException("ModX needs the fmljava language loader, got " + modContainer.getModInfo().getLoader().name());
        }
        this.mod = mod;
        this.modInitClass = ClassUtil.forName(mod.getClass().getName() + "$");
        this.modContainer = fmlContainer;
        this.modEventBus = Objects.requireNonNull(fmlContainer.getEventBus(), "FML mod container has no event bus: " + this.mod.modid);
        this.setupTasks = new ArrayList<>();
        this.queueSetupTasks = new ArrayList<>();
        this.datagenConfiguration = new ArrayList<>();

        this.modEventBus.addListener(this::runSetup);
    }
    
    public ModX instance() {
        return this.mod;
    }

    public FMLModContainer modContainer() {
        return this.modContainer;
    }

    public IEventBus modEventBus() {
        return this.modEventBus;
    }

    public void addSetupTask(Runnable task, boolean enqueue) {
        if (enqueue) {
            this.queueSetupTasks.add(task);
        } else {
            this.setupTasks.add(task);
        }
    }

    public void callGeneratedCode() {
        if (this.modInitClass != null) {
            try {
                Method method = this.modInitClass.getDeclaredMethod("init", ModX.class);
                method.invoke(null, this.mod);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Generated code threw an exception for mod " + this.mod.modid, e.getTargetException());
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to load generated code for mod " + this.mod.modid, e);
            }
        }
    }

    @Nullable
    public Map<Class<?>, Codec<?>> getCodecMap() {
        if (this.modInitClass != null) {
            try {
                //noinspection unchecked
                return (Map<Class<?>, Codec<?>>) this.modInitClass.getField("codecs").get(null);
            } catch (NoSuchFieldException e) {
                return null;
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Invalid generated code in " + this.modInitClass + ": Codec map not accessible", e);
            }
        } else {
            return null;
        }
    }
    
    public boolean addDatagenConfiguration(Consumer<DatagenSystem> configure) {
        boolean isFirst = this.datagenConfiguration.isEmpty();
        this.datagenConfiguration.add(configure);
        return isFirst;
    }
    
    public void configureDatagenSystem(DatagenSystem system) {
        for (Consumer<DatagenSystem> configure : this.datagenConfiguration) {
            configure.accept(system);
        }
    }

    private void runSetup(FMLCommonSetupEvent event) {
        this.setupTasks.forEach(Runnable::run);
        this.queueSetupTasks.forEach(event::enqueueWork);
    }
}
