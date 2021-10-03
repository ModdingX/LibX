package io.github.noeppi_noeppi.libx.base.decorative;

import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.Registerable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.FenceGateBlock;

import java.util.Set;

public class FenceGateBlockBase extends FenceGateBlock implements Registerable {

    protected final ModX mod;
    private final Item item;

    public FenceGateBlockBase(ModX mod, Properties properties) {
        this(mod, properties, new Item.Properties());
    }

    public FenceGateBlockBase(ModX mod, Properties properties, Item.Properties itemProperties) {
        super(properties);
        this.mod = mod;
        if (mod.tab != null) {
            itemProperties.tab(mod.tab);
        }
        this.item = new BlockItem(this, itemProperties);
    }

    @Override
    public Set<Object> getAdditionalRegisters(ResourceLocation id) {
        return Set.of(this.item);
    }
}
