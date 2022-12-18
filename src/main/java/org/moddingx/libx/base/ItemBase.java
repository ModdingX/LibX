package org.moddingx.libx.base;

import net.minecraft.world.item.Item;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.mod.ModXRegistration;

/**
 * Base class for {@link Item items} for mods using {@link ModXRegistration}. This will automatically set the
 * creative tab if it's defined in the mod.
 */
public class ItemBase extends Item {

    protected final ModX mod;

    public ItemBase(ModX mod, Properties properties) {
        super(properties);
        this.mod = mod;
        mod.addItemToTab(this);
    }
}
