package io.github.noeppi_noeppi.libx.base;

import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import io.github.noeppi_noeppi.libx.mod.registration.Registerable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Base class for {@link Block blocks} for mods using {@link ModXRegistration}. This will automatically set the
 * creative tab if it's defined in the mod and register a {@link BlockItem block item}.
 *
 * @deprecated See https://gist.github.com/noeppi-noeppi/9de9b6af950ee02f2dee611742fe2d6d
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.19")
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
        this.item = new BlockItem(this, itemProperties) {
            @Override
            public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
                BlockBase.this.initializeItemClient(consumer);
            }
        };
    }

    @Override
    public Set<Object> getAdditionalRegisters(ResourceLocation id) {
        return Set.of(this.item);
    }

    /**
     * Called from the item for this block from {@link Item#initializeClient(Consumer)}.
     * Can be used to set client properties for the block item.
     */
    public void initializeItemClient(@Nonnull Consumer<IItemRenderProperties> consumer) {

    }
}
