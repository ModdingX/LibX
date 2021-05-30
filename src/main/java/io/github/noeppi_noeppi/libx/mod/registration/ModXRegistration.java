package io.github.noeppi_noeppi.libx.mod.registration;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * You should extends this instead of {@link ModX} if you want to use the alternative registration system
 * in LibX.
 * <p>
 * This works like this:
 * You define your objects for registration in classes like {@code ModItems}. Create some static methods
 * there that register all the items. To register something you need to call
 * {@link ModXRegistration#register(String, Object)}. The in the constructor of you mod class you call
 * {@link ModXRegistration#addRegistrationHandler(Runnable)} for every registration method with a method
 * reference to it. (Example: {@code addRegistrationHandler(ModItems::init)}. The handlers will get called
 * in the order you added them.
 * <p>
 * This system has several advantages over the one recommended by forge:
 * <ul>
 *     <li>An object can have dependencies that are automatically registered with it. This is done with
 *     the {@link Registerable} interface. For example {@link BlockTE} registers a block, an item for
 *     the block and a tile entity type. You could even go further with it and automatically register
 *     slabs, stairs, walls and doors for all of your decorative blocks.</li>
 *     <li>There's way less code you need to write.</li>
 *     <li>You don't need the {@code .get()} when you want to access a registration object</li>
 * </ul>
 * <p>
 * So you might want to know what exactly can be registered with this system. You can register everything
 * that has a forge registry such as items, block, biomes, enchantments... And if other mods add things
 * to register via forge registries you can register those as well. Another thing you can register are
 * thing that implement {@link Registerable}. See there for more info.
 */
public abstract class ModXRegistration extends ModX {

    private final List<Runnable> registrationHandlers = new ArrayList<>();
    private boolean registered = false;
    private final List<Pair<String, Object>> registerables = new ArrayList<>();

    protected ModXRegistration(String modid, ItemGroup tab) {
        super(modid, tab);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonRegistration);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientRegistration);

        try {
            Method method = EventBus.class.getDeclaredMethod("addListener", EventPriority.class, Predicate.class, Class.class, Consumer.class);
            method.setAccessible(true);
            method.invoke(FMLJavaModLoadingContext.get().getModEventBus(), EventPriority.NORMAL, (Predicate<Object>) obj -> true, RegistryEvent.Register.class, (Consumer<RegistryEvent.Register<?>>) this::onRegistry);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("could not add generic listener to listen all registry events for mod " + modid + ".", e);
        }
        // Call the generated code here as well
        this.callGeneratedCode();
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
        if (!ResourceLocation.isPathValid(id)) {
            throw new IllegalArgumentException("ModXRegistration#register called with invalid id argument.");
        }
        this.registerables.add(Pair.of(id, obj));
        if (obj instanceof Registerable) {
            ((Registerable) obj).getAdditionalRegisters().forEach(o -> this.register(id, o));
            ((Registerable) obj).getNamedAdditionalRegisters().forEach((str, o) -> this.register(id + "_" + str, o));
        }
    }

    private void runRegistration() {
        if (!this.registered) {
            this.registered = true;
            this.registrationHandlers.forEach(Runnable::run);
        }
    }
    
    private void commonRegistration(FMLCommonSetupEvent event) {
        this.runRegistration();
        this.registerables.stream().filter(pair -> pair.getRight() instanceof Registerable)
                .forEach(pair -> ((Registerable) pair.getRight()).registerCommon(new ResourceLocation(this.modid, pair.getLeft()), event::enqueueWork));
    }
    
    private void clientRegistration(FMLClientSetupEvent event) {
        this.runRegistration();
        this.registerables.stream().filter(pair -> pair.getRight() instanceof Registerable)
                .forEach(pair -> ((Registerable) pair.getRight()).registerClient(new ResourceLocation(this.modid, pair.getLeft()), event::enqueueWork));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void onRegistry(final RegistryEvent.Register<? extends IForgeRegistryEntry<?>> event) {
        this.runRegistration();
        this.registerables.stream().filter(pair -> event.getRegistry().getRegistrySuperType().isAssignableFrom(pair.getRight().getClass())).forEach(pair -> {
            ((IForgeRegistryEntry<?>) pair.getRight()).setRegistryName(new ResourceLocation(this.modid, pair.getLeft()));
            ((IForgeRegistry) event.getRegistry()).register((IForgeRegistryEntry<?>) pair.getRight());
        });
    }
}
