package io.github.noeppi_noeppi.libx.base;

import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 * Base class for {@link Item items} for mods using {@link ModXRegistration}. This will automatically set the
 * creative tab if it's defined in the mod.
 */
// TODO figure out a way to make the render properties stuff more convenient
//  without breaking classloading on server.
public class ItemBase extends Item {

    protected final ModX mod;

    public ItemBase(ModX mod, Properties properties) {
        super(((Supplier<Properties>) () -> {
            if (mod.tab != null) {
                properties.tab(mod.tab);
            }
            return properties;
        }).get());
        this.mod = mod;
    }
}
