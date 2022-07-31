package org.moddingx.libx.mod;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.moddingx.libx.base.tile.BlockBE;
import org.moddingx.libx.impl.ModInternal;
import org.moddingx.libx.impl.registration.RegistrationDispatcher;
import org.moddingx.libx.registration.MultiRegisterable;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationBuilder;

import javax.annotation.Nullable;

/**
 * You need to extends this instead of {@link ModX} if you want to use the LibX registration system.
 *
 * This works like this:
 *
 * You define your objects for registration in classes like {@code ModItems}. Create some static methods
 * there that register all the items. To register something you need to call
 * {@link #register(ResourceKey, String, Object)} or {@link #createHolder(ResourceKey, String, Object)}.
 * Then in the constructor of your mod class, you call {@link #addRegistrationHandler(Runnable)} for every
 * registration method with a method reference to it. (Example: {@code addRegistrationHandler(ModItems::init)}.
 * The handlers will get called in the order you added them.
 *
 * This system has several advantages over the one recommended by forge:
 *
 * <ul>
 *     <li>An object can have dependencies that are automatically registered with it. This is done with
 *     the {@link Registerable} interface. For example {@link BlockBE} registers a {@link Block block},
 *     an {@link Item item} for the block and a {@link BlockEntityType block entity type}. You could even
 *     go further with it and automatically register slabs, stairs, walls and doors for all of your
 *     decorative blocks.</li>
 *     <li>You don't need the {@code .get()} when you want to access a registration object</li>
 * </ul>
 * 
 * The system allows to create {@link Holder holders}. However, this will only work for vanilla registries
 * that support holder creation prior to registering the item.
 * 
 * Another special class is {@link MultiRegisterable}. Instances of this class can be registered with
 * {@link #registerMulti(ResourceKey, String, MultiRegisterable)}. Instances of this class can't be registered
 * into a registry directly, however its children will inherit the registry specified for the parent.
 */
public abstract class ModXRegistration extends ModX {

    private final RegistrationDispatcher dispatcher;

    /**
     * Subclasses should provide a public no-arg constructor that calls this with
     * the values needed.
     */
    protected ModXRegistration() {
        this(null);
    }

    /**
     * Subclasses should provide a public no-arg constructor that calls this with
     * the values needed.
     */
    protected ModXRegistration(@Nullable CreativeModeTab tab) {
        super(tab);

        RegistrationBuilder builder = new RegistrationBuilder();
        this.initRegistration(builder);
        this.dispatcher = new RegistrationDispatcher(this, builder.build());
        ModInternal.get(this).initRegistration(this.dispatcher);
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this.dispatcher::registerBy);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this.dispatcher::registerCommon);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this.dispatcher::registerClient);
        
        // Call the generated code here as well
        ModInternal.get(this).callGeneratedCode();
    }

    /**
     * Initialised the registration system. See {@link RegistrationBuilder} for more information.
     */
    protected abstract void initRegistration(RegistrationBuilder builder);

    /**
     * Adds a registration handler that runs during the registry phase.
     */
    public final void addRegistrationHandler(Runnable handler) {
        this.dispatcher.addRegistrationHandler(handler);
    }

    /**
     * Registers an object to a given registry using a given name as the path part of the objects id.
     * The {@code registry} parameter may be {@code null} to allow registering
     * {@link Registerable registerables} that should not go into any specific registry.
     */
    public final <T> void register(@Nullable ResourceKey<? extends Registry<T>> registry, String id, T value) {
        this.dispatcher.register(registry, id, value);
    }

    /**
     * Registers the contents of a {@link MultiRegisterable} to a given registry using a given name as the base
     * path part of the objects id.
     */
    public final <T> void registerMulti(@Nullable ResourceKey<? extends Registry<T>> registry, String id, MultiRegisterable<T> value) {
        this.dispatcher.registerMulti(registry, id, value);
    }

    /**
     * Same as {@link #register(ResourceKey, String, Object)} but also creates a {@link Holder} for the
     * registered object. The holder is not required to hold a value directly after the execution of this method.
     */
    public final <T> Holder<T> createHolder(@Nullable ResourceKey<? extends Registry<T>> registry, String id, T value) {
        return this.dispatcher.register(registry, id, value).get();
    }
}
