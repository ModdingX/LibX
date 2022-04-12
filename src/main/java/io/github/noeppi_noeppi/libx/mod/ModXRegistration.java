package io.github.noeppi_noeppi.libx.mod;

import io.github.noeppi_noeppi.libx.impl.ModInternal;
import io.github.noeppi_noeppi.libx.impl.registration.RegistrationDispatcher;
import io.github.noeppi_noeppi.libx.registration.Registerable;
import io.github.noeppi_noeppi.libx.registration.RegistrationBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class ModXRegistration extends ModX {
    
    // Don't use LibX#resource because of classloading order.
    public static final ResourceKey<? extends Registry<Registerable>> ANY_REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation("libx", "any"));

    private final RegistrationDispatcher dispatcher;
    
    protected ModXRegistration() {
        this(null);
    }

    protected ModXRegistration(@Nullable CreativeModeTab tab) {
        super(tab);

        RegistrationBuilder builder = new RegistrationBuilder();
        this.initRegistration(builder);
        this.dispatcher = new RegistrationDispatcher(this.modid, builder.build());
        
        try {
            Method method = EventBus.class.getDeclaredMethod("addListener", EventPriority.class, Predicate.class, Class.class, Consumer.class);
            method.setAccessible(true);
            method.invoke(FMLJavaModLoadingContext.get().getModEventBus(), EventPriority.NORMAL, (Predicate<Object>) obj -> true, RegistryEvent.Register.class, (Consumer<RegistryEvent.Register<?>>) this.dispatcher::registerForge);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not add generic listener to listen to all registry events for mod " + this.modid + ".", e);
        }
        
        ModInternal.get(this).addSetupTask(this.dispatcher::registerVanilla, true);
        
        // Call the generated code here as well
        ModInternal.get(this).callGeneratedCode();
    }

    protected abstract void initRegistration(RegistrationBuilder builder);
}
