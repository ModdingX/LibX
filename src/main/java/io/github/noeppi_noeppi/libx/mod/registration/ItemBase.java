package io.github.noeppi_noeppi.libx.mod.registration;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.item.Item;

import java.util.function.Supplier;

public class ItemBase extends Item {

    public ItemBase(ModX mod, Properties properties) {
        super(((Supplier<Properties>) () -> {
            if (mod.tab != null) {
                properties.group(mod.tab);
            }
            return properties;
        }).get());
    }
}
