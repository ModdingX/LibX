package io.github.noeppi_noeppi.libx.base.decorative;

import io.github.noeppi_noeppi.libx.block.ChildBlock;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.Registerable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;

import java.util.Set;

public class WallBlockBase extends WallBlock implements Registerable, ChildBlock {

    protected final ModX mod;
    private final Item item;
    private final Block parent;

    public WallBlockBase(ModX mod, Properties properties, Block parent) {
        this(mod, properties, new Item.Properties(), parent);
    }

    public WallBlockBase(ModX mod, Properties properties, Item.Properties itemProperties, Block parent) {
        super(properties);
        this.mod = mod;
        if (mod.tab != null) {
            itemProperties.tab(mod.tab);
        }
        this.item = new BlockItem(this, itemProperties);
        this.parent = parent;
    }

    @Override
    public Set<Object> getAdditionalRegisters(ResourceLocation id) {
        return Set.of(this.item);
    }

    @Override
    public Block getParent() {
        return this.parent;
    }
}
