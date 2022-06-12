package org.moddingx.libx.impl;

import com.mojang.serialization.Codec;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.moddingx.libx.impl.registration.RegistrationDispatcher;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.util.ClassUtil;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class ModInternal {

    private static final Object LOCK = new Object();
    private static final Map<Class<? extends ModX>, ModInternal> MAP = new HashMap<>();

    public static void init(ModX mod, FMLJavaModLoadingContext ctx) {
        synchronized (LOCK) {
            if (!Modifier.isFinal(mod.getClass().getModifiers())) {
                throw new IllegalStateException("Mod class must be final. Report to the author of the " + mod.modid + " mod.");
            }
            if (MAP.containsKey(mod.getClass())) {
                throw new IllegalStateException("ModInternal initialised twice for mod " + mod.getClass());
            }
            MAP.put(mod.getClass(), new ModInternal(mod, ctx));
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
    
    private final ModX mod;

    @Nullable
    private final Class<?> modInitClass;
    private final List<Runnable> setupTasks;
    private final List<Runnable> queueSetupTasks;
    private RegistrationDispatcher registrationDispatcher;

    private ModInternal(ModX mod, FMLJavaModLoadingContext ctx) {
        this.mod = mod;
        this.modInitClass = ClassUtil.forName(mod.getClass().getName() + "$");
        this.setupTasks = new ArrayList<>();
        this.queueSetupTasks = new ArrayList<>();
        this.registrationDispatcher = null;

        ctx.getModEventBus().addListener(this::runSetup);
    }

    public void initRegistration(RegistrationDispatcher dispatcher) {
        this.registrationDispatcher = dispatcher;
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
    
    public RegistrationDispatcher getRegistrationDispatcher() {
        if (this.registrationDispatcher == null) {
            throw new NoSuchElementException(this.mod.modid + " has no registration dispatcher. This is an error in LibX.");
        } else {
            return this.registrationDispatcher;
        }
    }

    private void runSetup(FMLCommonSetupEvent event) {
        this.setupTasks.forEach(Runnable::run);
        this.queueSetupTasks.forEach(event::enqueueWork);
    }
}
