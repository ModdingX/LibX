package io.github.noeppi_noeppi.libx.mod.registration;

import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import io.github.noeppi_noeppi.libx.base.tile.BlockBE;
import io.github.noeppi_noeppi.libx.impl.ModInternal;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * You should extends this instead of {@link ModX} if you want to use the LibX registration system.
 * 
 * This works like this:
 * 
 * You define your objects for registration in classes like {@code ModItems}. Create some static methods
 * there that register all the items. To register something you need to call
 * {@link ModXRegistration#register(String, Object)}. The in the constructor of you mod class you call
 * {@link ModXRegistration#addRegistrationHandler(Runnable)} for every registration method with a method
 * reference to it. (Example: {@code addRegistrationHandler(ModItems::init)}. The handlers will get called
 * in the order you added them.
 * 
 * This system has several advantages over the one recommended by forge:
 * 
 * <ul>
 *     <li>An object can have dependencies that are automatically registered with it. This is done with
 *     the {@link Registerable} interface. For example {@link BlockBE} registers a {@link Block block},
 *     an {@link Item item} for the block and a {@link BlockEntityType block entity type}. You could even
 *     go further with it and automatically register slabs, stairs, walls and doors for all of your
 *     decorative blocks.</li>
 *     <li>There's way less code you need to write.</li>
 *     <li>You don't need the {@code .get()} when you want to access a registration object</li>
 * </ul>
 * 
 * So you might want to know what exactly can be registered with this system. You can register everything
 * that has a forge registry such as items, block, biomes, enchantments... And if other mods add things
 * to register via forge registries you can register those as well. Another thing you can register are
 * thing that implement {@link Registerable}. See there for more info.
 * 
 * With registry transformers you can register literally everything. See {@link #initRegistration(RegistrationBuilder)}
 * for more information about registry conditions and transformers.
 * 
 * @deprecated See https://gist.github.com/noeppi-noeppi/9de9b6af950ee02f2dee611742fe2d6d
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.19")
public abstract class ModXRegistration extends ModX {

    private final Object registrationLock = new Object();
    private final RegistrationBuilder.RegistrationSettings settings;
    private final List<Runnable> registrationHandlers = new ArrayList<>();

    private boolean registered = false;
    private final List<RegEntry> registrationEntries = new ArrayList<>();
    
    protected ModXRegistration() {
        this(null);
    }
    
    protected ModXRegistration(@Nullable CreativeModeTab tab) {
        super(tab);
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonRegistration);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientRegistration);

        try {
            Method method = EventBus.class.getDeclaredMethod("addListener", EventPriority.class, Predicate.class, Class.class, Consumer.class);
            method.setAccessible(true);
            method.invoke(FMLJavaModLoadingContext.get().getModEventBus(), EventPriority.NORMAL, (Predicate<Object>) obj -> true, RegistryEvent.Register.class, (Consumer<RegistryEvent.Register<?>>) this::onRegistry);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not add generic listener to listen to all registry events for mod " + this.modid + ".", e);
        }
        
        // Initialise the registration system.
        RegistrationBuilder builder = new RegistrationBuilder();
        this.initRegistration(builder);
        this.settings = builder.build();
        
        // Call the generated code here as well
        ModInternal.get(this).callGeneratedCode();
    }

    /**
     * Adds a registration handler. Should be called only in constructor. See class description for more info.
     */
    public final void addRegistrationHandler(Runnable handler) {
        this.registrationHandlers.add(handler);
    }

    /**
     * Registers an object with a given id. The id must be a valid ResourceLocation path. It's automatically
     * prefixed with the mod id.
     */
    public final void register(String id, Object obj) {
        if (!ResourceLocation.isValidPath(id)) {
            throw new IllegalArgumentException("ModXRegistration#register called with invalid id argument.");
        }
        ResourceLocation rl = this.resource(id);
        synchronized (this.registrationLock) {
            if (this.settings.conditions().stream().allMatch(c -> c.shouldRegister(rl, obj))) {
                Object replaced = this.settings.replacers().stream()
                        .map(r -> r.getAdditional(rl, obj))
                        .filter(Objects::nonNull)
                        .findFirst().orElse(obj);
                this.registrationEntries.add(new RegEntry(id, replaced));
                if (replaced instanceof Registerable reg) {
                    reg.getAdditionalRegisters(rl).forEach(o -> this.register(id, o));
                    reg.getNamedAdditionalRegisters(rl).forEach((str, o) -> this.register(id + "_" + str, o));
                }
                this.settings.transformers().forEach(t -> {
                    Object additional = t.getAdditional(rl, replaced);
                    if (additional != null) this.register(id, additional);
                });
            }
        }
    }

    /**
     * This is called from the {@link ModXRegistration} constructor. It is used to configure the registration
     * system for this mod with the given builder. In all cases, you must set the version of the registration
     * system to be used for this mod, or it will fail.
     * 
     * @see RegistrationBuilder
     */
    protected abstract void initRegistration(RegistrationBuilder builder);

    private void runRegistration() {
        if (!this.registered) {
            synchronized (this.registrationLock) {
                if (!this.registered) {
                    this.registered = true;
                    this.registrationHandlers.forEach(Runnable::run);
                }
            }
        }
    }
    
    private void commonRegistration(FMLCommonSetupEvent event) {
        this.runRegistration();
        this.registrationEntries.forEach(entry -> entry.run(reg -> reg.registerCommon(this.resource(entry.id()), event::enqueueWork)));
    }
    
    private void clientRegistration(FMLClientSetupEvent event) {
        this.runRegistration();
        this.registrationEntries.forEach(entry -> entry.run(reg -> reg.registerClient(this.resource(entry.id()), event::enqueueWork)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void onRegistry(final RegistryEvent.Register<? extends IForgeRegistryEntry<?>> event) {
        this.runRegistration();
        this.registrationEntries.stream()
                .filter(entry -> entry.value() instanceof IForgeRegistryEntry<?>)
                .filter(entry -> event.getRegistry().getRegistrySuperType().equals(((IForgeRegistryEntry<?>) entry.value()).getRegistryType()))
                .forEach(entry -> {
                    ((IForgeRegistryEntry<?>) entry.value()).setRegistryName(this.resource(entry.id()));
                    ((IForgeRegistry) event.getRegistry()).register((IForgeRegistryEntry<?>) entry.value()); 
                });
    }
    
    private record RegEntry(String id, Object value) {
        
        public void run(Consumer<Registerable> action) {
            if (this.value instanceof Registerable) {
                action.accept((Registerable) this.value);
            }
        }
    }
}