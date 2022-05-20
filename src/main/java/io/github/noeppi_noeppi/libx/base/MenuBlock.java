package io.github.noeppi_noeppi.libx.base;

import com.google.common.collect.ImmutableSet;
import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import io.github.noeppi_noeppi.libx.menu.BlockMenu;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

/**
 * This class registers a menu with the Block
 * This also makes the Screen appear on right-click.
 * <p>
 * Note: You need to register the Screen by yourself using the {@link MenuScreens}
 * register function. This can be done on clientSetup or by overriding the initializeClient method on your block.
 *
 * @see BlockMenu
 *
 * @deprecated See https://gist.github.com/noeppi-noeppi/9de9b6af950ee02f2dee611742fe2d6d
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.19")
public class MenuBlock<C extends BlockMenu> extends BlockBase {

    public final MenuType<C> menu;

    public MenuBlock(ModX mod, MenuType<C> menu, Properties properties) {
        super(mod, properties);
        this.menu = menu;
    }

    public MenuBlock(ModX mod, MenuType<C> menu, Properties properties, Item.Properties itemProperties) {
        super(mod, properties, itemProperties);
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
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            BlockMenu.openMenu(sp, this.menu, new TranslatableComponent("screen." + MenuBlock.this.mod.modid + "." + Objects.requireNonNull(MenuBlock.this.getRegistryName()).getPath()), pos);
        }
        return InteractionResult.SUCCESS;
    }
}
