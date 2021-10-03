package io.github.noeppi_noeppi.libx.base.decorative;

import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.ClientItemInitialization;
import io.github.noeppi_noeppi.libx.mod.registration.Registerable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraftforge.client.IItemRenderProperties;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Consumer;

public class FenceBlockBase extends FenceBlock implements Registerable, ClientItemInitialization {

    protected final ModX mod;
    private final Item item;

    public FenceBlockBase(ModX mod, Properties properties) {
        this(mod, properties, new Item.Properties());
    }

    public FenceBlockBase(ModX mod, Properties properties, Item.Properties itemProperties) {
        super(properties);
        this.mod = mod;
        if (mod.tab != null) {
            itemProperties.tab(mod.tab);
        }
        this.item = new BlockItem(this, itemProperties) {

            @Override
            public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
                FenceBlockBase.this.initializeItemClient(consumer);
            }
        };
    }

    @Override
    public Set<Object> getAdditionalRegisters(ResourceLocation id) {
        return Set.of(this.item);
    }
}
