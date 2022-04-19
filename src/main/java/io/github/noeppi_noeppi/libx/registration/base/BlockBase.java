package io.github.noeppi_noeppi.libx.registration.base;

import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.registration.Registerable;
import io.github.noeppi_noeppi.libx.registration.RegistrationContext;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class BlockBase extends Block implements Registerable {

    protected final ModX mod;
    
    @Nullable
    private final Item item;

    public BlockBase(ModX mod, Properties properties) {
        this(mod, properties, new Item.Properties());
    }

    public BlockBase(ModX mod, Properties properties, @Nullable Item.Properties itemProperties) {
        super(properties);
        this.mod = mod;
        if (itemProperties == null) {
            this.item = null;
        } else {
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
    }

    protected void initializeItemClient(@Nonnull Consumer<IItemRenderProperties> consumer) {

    }

    @Override
    public void buildAdditionalRegisters(RegistrationContext ctx, EntryCollector builder) {
        if (this.item != null) {
            builder.register(Registry.ITEM_REGISTRY, this.item);
        }
    }
}
