package io.github.noeppi_noeppi.libx.mod.registration;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class ModXRegistration extends ModX {

    private final List<Runnable> registrationHandlers = new ArrayList<>();
    private boolean registered = false;
    private final List<Pair<String, Object>> registerables = new ArrayList<>();

    protected ModXRegistration(String modid, ItemGroup tab) {
        super(modid, tab);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientRegistration);
    }

    protected void addRegistrationsHandler(Runnable handler) {
        this.registrationHandlers.add(handler);
    }

    public void register(String id, Object obj) {
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

    private void clientRegistration(FMLClientSetupEvent event) {
        this.runRegistration();
        this.registerables.stream().filter(pair -> pair.getRight() instanceof Registerable)
                .forEach(pair -> ((Registerable) pair.getRight()).registerClient(pair.getLeft()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onRegistry(final RegistryEvent.Register<? extends IForgeRegistryEntry<?>> event) {
        this.runRegistration();
        this.registerables.stream().filter(pair -> event.getRegistry().getRegistrySuperType().isAssignableFrom(pair.getRight().getClass())).forEach(pair -> {
            ((IForgeRegistryEntry<?>) pair.getRight()).setRegistryName(new ResourceLocation(this.modid, pair.getLeft()));
            ((IForgeRegistry) event.getRegistry()).register((IForgeRegistryEntry<?>) pair.getRight());
        });
    }
}
