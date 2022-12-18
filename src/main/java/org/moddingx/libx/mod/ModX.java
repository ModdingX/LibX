package org.moddingx.libx.mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.moddingx.libx.impl.ModInternal;
import org.moddingx.libx.impl.config.ModMappers;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A base class for a mod that uses LibX. Is required for many other features
 * of LibX.
 *
 * @see ModXRegistration
 */
public abstract class ModX {

    private final List<ItemStack> tabLists = new ArrayList<>();

    /**
     * Contains the Mod id of this mod.
     */
    public final String modid;

    /**
     * A creative tab for the mod.
     */
    @Nullable
    public CreativeModeTab tab;

    /**
     * Subclasses should provide a public no-arg constructor that calls this with
     * the values needed.
     */
    protected ModX() {
        Class<? extends ModX> cls = this.getClass();
        Mod mod = cls.getAnnotation(Mod.class);
        if (mod == null) throw new IllegalStateException("Mod class has no @Mod annotation.");
        this.modid = mod.value();

        ModInternal.init(this, FMLJavaModLoadingContext.get());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerCreativeTab);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerItems);

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

    private void registerCreativeTab(CreativeModeTabEvent.Register event) {
        Consumer<CreativeModeTab.Builder> consumer = this.createTab();
        if (consumer != null) {
            this.tab = event.registerCreativeModeTab(this.resource("tab"), consumer.andThen(builder -> {
                if (builder.displayName.getContents() == ComponentContents.EMPTY) {
                    builder.title(Component.literal("itemGroup." + this.modid));
                }
            }));
        }
    }

    private void registerItems(CreativeModeTabEvent.BuildContents event) {
        if (this.tab == event.getTab()) {
            for (ItemStack stack : this.tabLists) {
                event.accept(stack);
            }
        }
    }

    /**
     * Creates a new creative mode tab.
     */
    protected abstract Consumer<CreativeModeTab.Builder> createTab();

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

    /**
     * Adds an {@link ItemLike} to the mods creative mode tab if created in {@link ModX#createTab()}.
     */
    public final void addItemToTab(ItemLike item) {
        this.addItemToTab(new ItemStack(item));
    }

    /**
     * Adds an {@link ItemStack} to the mods creative mode tab if created in {@link ModX#createTab()}.
     */
    public final void addItemToTab(ItemStack stack) {
        if (this.createTab() == null) {
            throw new IllegalStateException("No creative mode tab created.");
        }

        this.tabLists.add(stack);
    }
}
