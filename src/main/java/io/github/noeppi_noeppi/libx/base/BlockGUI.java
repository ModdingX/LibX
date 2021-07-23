package io.github.noeppi_noeppi.libx.base;

import com.google.common.collect.ImmutableSet;
import io.github.noeppi_noeppi.libx.inventory.container.TileContainerMenu;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Set;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * This class registers a container to it's {@link TileEntityType tile entity type} and handles the gui
 * opening when the block is right clicked. You still need to manually register the screen on the client.
 */
public class BlockGUI<T extends BlockEntity, C extends TileContainerMenu<T>> extends BlockBE<T> {

    public final MenuType<C> menu;

    public BlockGUI(ModX mod, Class<T> teClass, MenuType<C> menu, Properties properties) {
        super(mod, teClass, properties);
        this.menu = menu;
    }

    public BlockGUI(ModX mod, Class<T> teClass, MenuType<C> menu, Properties properties, Item.Properties itemProperties) {
        super(mod, teClass, properties, itemProperties);
        this.menu = menu;
    }

    @Override
    public Set<Object> getAdditionalRegisters(ResourceLocation id) {
        return ImmutableSet.builder().addAll(super.getAdditionalRegisters(id)).add(this.menu).build();
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")

    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (!level.isClientSide) {
            //noinspection ConstantConditions
            TileContainerMenu.openMenu((ServerPlayer) player, this.menu, new TranslatableComponent("screen." + BlockGUI.this.mod.modid + "." + BlockGUI.this.getRegistryName().getPath()), pos);
        }
        return InteractionResult.SUCCESS;
    }
}
