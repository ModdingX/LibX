package org.moddingx.libx.menu.type;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A menu type for {@link AdvancedMenuFactory advanced men factories}.
 */
public class AdvancedMenuType<T extends AbstractContainerMenu, S> extends MenuType<T> {
    
    private final AdvancedMenuFactory<T, S> factory;
    @Nullable private final StreamCodec<? super RegistryFriendlyByteBuf, S> codec;
    
    private AdvancedMenuType(AtomicReference<MenuType<T>> menuTypeRef, AdvancedMenuFactory<T, S> factory, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, S> codec, FeatureFlagSet featureFlags) {
        super(new Factory<>(menuTypeRef, factory, codec), featureFlags);
        this.factory = factory;
        this.codec = codec;
        menuTypeRef.set(this);
    }

    /**
     * Opens a menu of this type to the provided player.
     */
    public void open(ServerPlayer player, Component title, S payload) {
        Provider<T, S> provider = new Provider<>(this, title, this.factory, payload);
        if (this.codec != null) {
            player.openMenu(provider, buf -> this.codec.encode(buf, payload));
        } else {
            player.openMenu(provider);
        }
    }

    /**
     * Creates a new {@link AdvancedMenuType} using the provided {@link AdvancedMenuFactory factory}.
     */
    public static <T extends AbstractContainerMenu> AdvancedMenuType<T, Void> create(AdvancedMenuFactory<T, Void> factory) {
        return create(factory, FeatureFlags.VANILLA_SET);
    }

    /**
     * Creates a new {@link AdvancedMenuType} using the provided {@link AdvancedMenuFactory factory}.
     */
    public static <T extends AbstractContainerMenu> AdvancedMenuType<T, Void> create(AdvancedMenuFactory<T, Void> factory, FeatureFlagSet featureFlags) {
        AtomicReference<MenuType<T>> menuTypeRef = new AtomicReference<>();
        Objects.requireNonNull(factory);
        return new AdvancedMenuType<>(menuTypeRef, factory, null, featureFlags);
    }

    /**
     * Creates a new {@link AdvancedMenuType} using the provided {@link AdvancedMenuFactory factory} and {@link StreamCodec}.
     */
    public static <T extends AbstractContainerMenu, S> AdvancedMenuType<T, S> create(AdvancedMenuFactory<T, S> factory, StreamCodec<? super RegistryFriendlyByteBuf, S> codec) {
        return create(factory, codec, FeatureFlags.VANILLA_SET);
    }

    /**
     * Creates a new {@link AdvancedMenuType} using the provided {@link AdvancedMenuFactory factory} and {@link StreamCodec}.
     */
    public static <T extends AbstractContainerMenu, S> AdvancedMenuType<T, S> create(AdvancedMenuFactory<T, S> factory, StreamCodec<? super RegistryFriendlyByteBuf, S> codec, FeatureFlagSet featureFlags) {
        AtomicReference<MenuType<T>> menuTypeRef = new AtomicReference<>();
        Objects.requireNonNull(factory);
        Objects.requireNonNull(codec);
        return new AdvancedMenuType<>(menuTypeRef, factory, codec, featureFlags);
    }

    private static class Factory<T extends AbstractContainerMenu, S> implements IContainerFactory<T> {

        private final AtomicReference<MenuType<T>> menuType;
        private final AdvancedMenuFactory<T, S> factory;
        @Nullable private final StreamCodec<? super RegistryFriendlyByteBuf, S> codec;

        private Factory(AtomicReference<MenuType<T>> menuType, AdvancedMenuFactory<T, S> factory, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, S> codec) {
            this.menuType = menuType;
            this.factory = factory;
            this.codec = codec;
        }

        @Nonnull
        @Override
        public T create(int windowId, @Nonnull Inventory inventory, @Nullable RegistryFriendlyByteBuf data) {
            MenuType<T> menuType = Objects.requireNonNull(this.menuType.get());
            if (this.codec != null && data == null) {
                @Nullable ResourceLocation id = BuiltInRegistries.MENU.getKey(menuType);
                throw new IllegalStateException("Can't open menus of type " + id + " without extra payload.");
            }
            S payload = this.codec == null ? null : this.codec.decode(data);
            return this.factory.createMenu(menuType, windowId, inventory.player.level(), payload, inventory.player, inventory);
        }
    }

    private static class Provider<T extends AbstractContainerMenu, S> implements MenuProvider {

        private final MenuType<T> menuType;
        private final Component title;
        private final AdvancedMenuFactory<T, S> factory;
        private final S payload;

        private Provider(MenuType<T> menuType, Component title, AdvancedMenuFactory<T, S> factory, S payload) {
            this.menuType = menuType;
            this.title = title;
            this.factory = factory;
            this.payload = payload;
        }

        @Nonnull
        @Override
        public Component getDisplayName() {
            return this.title;
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int windowId, @Nonnull Inventory inventory, @Nonnull Player player) {
            return this.factory.createMenu(this.menuType, windowId, player.level(), this.payload, player, inventory);
        }
    }
}
