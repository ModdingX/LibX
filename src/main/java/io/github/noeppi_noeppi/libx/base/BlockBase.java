package io.github.noeppi_noeppi.libx.base;

import com.google.common.collect.ImmutableSet;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import io.github.noeppi_noeppi.libx.mod.registration.Registerable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Set;

/**
 * Base class for {@link Block blocks} for mods using {@link ModXRegistration}. This will automatically set the
 * creative tab if it's defined in the mod and register a {@link BlockItem block item}.
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
            itemProperties.tab(mod.tab);
        }
        this.item = new BlockItem(this, itemProperties);
    }

    @Override
    public Set<Object> getAdditionalRegisters(ResourceLocation id) {
        return ImmutableSet.of(this.item);
    }
}
