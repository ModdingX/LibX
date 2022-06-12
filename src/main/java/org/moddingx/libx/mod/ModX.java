package org.moddingx.libx.mod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.moddingx.libx.impl.ModInternal;
import org.moddingx.libx.impl.config.ModMappers;

import javax.annotation.Nullable;

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
     * A creative tab for the mod.
     */
    @Nullable
    public final CreativeModeTab tab;

    /**
     * Subclasses should provide a public no-arg constructor that calls this with
     * the values needed.
     */
    protected ModX() {
        this(null);
    }
    
    /**
     * Subclasses should provide a public no-arg constructor that calls this with
     * the values needed.
     */
    protected ModX(@Nullable CreativeModeTab tab) {
        Class<? extends ModX> cls = this.getClass();
        Mod mod = cls.getAnnotation(Mod.class);
        if (mod == null) throw new IllegalStateException("Mod class has no @Mod annotation.");
        this.modid = mod.value();

        this.tab = tab;

        ModInternal.init(this, FMLJavaModLoadingContext.get());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        // Initialise config system for this mod container
        // Required so the extension point can be added when required
        ModMappers.get(this.modid).initAdapter(ModLoadingContext.get());

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
     * Creates a new {@link ResourceLocation} where the namespace is this mods id and the
     * path is the given string.
     */
    public final ResourceLocation resource(String path) {
        return new ResourceLocation(this.modid, path);
    }
}
