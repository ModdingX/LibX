package io.github.noeppi_noeppi.libx.tmp;

import com.google.common.collect.ImmutableSet;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import io.github.noeppi_noeppi.libx.mod.registration.Registerable;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.Set;

/**
 * Base class for blocks for mods using {@link ModXRegistration}. This will automatically set the
 * creative tab if it's defined in the mod and register a block item.
 */
public class BlockBase extends Block implements Registerable {

    protected final ModX mod;
    private final Item item;

    public BlockBase(ModX mod, Properties properties) {
        this(mod, properties, new Item.Properties());
    }

    public BlockBase(ModX mod, Properties properties, Item.Properties itemProperties) {
        super(properties);
        this.mod = mod;
        if (mod.tab != null) {
            itemProperties.group(mod.tab);
        }
        this.item = new BlockItem(this, itemProperties);
    }

    @Override
    public Set<Object> getAdditionalRegisters(ResourceLocation id) {
        return ImmutableSet.of(this.item);
    }
}
