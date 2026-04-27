package org.moddingx.libx.base;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.moddingx.libx.menu.BlockMenu;
import org.moddingx.libx.menu.type.AdvancedMenuType;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Objects;

/**
 * This class registers a menu with the Block.
 * This also makes the Screen appear on right-click.
 * <p>
 * Note: You need to register the Screen by yourself using the {@link MenuScreens} register function.
 *
 * @see BlockMenu
 */
public class MenuBlock<C extends BlockMenu> extends BlockBase {

    public final AdvancedMenuType<C, ? super BlockPos> menu;

    public MenuBlock(ModX mod, AdvancedMenuType<C, ? super BlockPos> menu, Properties properties) {
        this(mod, menu, properties, new Item.Properties());
    }

    public MenuBlock(ModX mod, AdvancedMenuType<C, ? super BlockPos> menu, Properties properties, @Nullable Item.Properties itemProperties) {
        super(mod, properties, itemProperties);
        this.menu = menu;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(RegistrationContext ctx, Registerable.EntryCollector builder) {
        super.registerAdditional(ctx, builder);
        builder.register(Registries.MENU, this.menu);
    }

    @Nonnull
    @Override
    public InteractionResult useWithoutItem(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            Component title = Component.translatable("screen." + this.mod.modid + "." + Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(this)).getPath());
            this.menu.open(serverPlayer, title, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
