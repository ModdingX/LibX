package org.moddingx.libx.mod;

import net.minecraft.resources.Identifier;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.moddingx.libx.impl.ModInternal;
import org.moddingx.libx.impl.config.ModMappers;

/**
 * A base class for a mod that uses LibX. Is required for many other features
 * of LibX.
 *
 * @see ModXRegistration
 */
public abstract class ModX {
    
    /**
     * Contains the Mod id of this mod.
     */
    public final String modid;

    /**
     * Subclasses should provide a public no-arg constructor that calls this with
     * the values needed.
     */
    protected ModX() {
        Class<? extends ModX> cls = this.getClass();
        Mod mod = cls.getAnnotation(Mod.class);
        if (mod == null) throw new IllegalStateException("Mod class has no @Mod annotation.");
        this.modid = mod.value();

        ModInternal modInternal = ModInternal.init(this);

        modInternal.modEventBus().addListener(this::setup);
        modInternal.modEventBus().addListener(this::clientSetup);

        // Initialise config system for this mod container
        // Required, so the extension point can be added when required
        ModMappers.get(this.modid).initAdapter(modInternal.modContainer());

        // As the generated code registers registration handlers this will produce a null pointer exception
        // as the list of handlers will be null. So for instances of ModXRegistration we don't call it here
        // but in the constructor of ModXRegistration
        if (!(this instanceof ModXRegistration)) {
            ModInternal.get(this).callGeneratedCode();
        }
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
     * Creates a new {@link Identifier} where the namespace is this mods id and the
     * path is the given string.
     */
    public final Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(this.modid, path);
    }
}
