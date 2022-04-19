package io.github.noeppi_noeppi.libx.registration.base;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

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
