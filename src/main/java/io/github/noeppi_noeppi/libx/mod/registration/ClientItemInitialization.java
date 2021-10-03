package io.github.noeppi_noeppi.libx.mod.registration;

import net.minecraft.world.item.Item;
import net.minecraftforge.client.IItemRenderProperties;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public interface ClientItemInitialization {

    /**
     * Called from the item for this block from {@link Item#initializeClient(Consumer)}.
     * Can be used to set client properties for the block item.
     */
    default void initializeItemClient(@Nonnull Consumer<IItemRenderProperties> consumer) {

    }
}
