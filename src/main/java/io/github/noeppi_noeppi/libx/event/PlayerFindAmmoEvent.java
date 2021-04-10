package io.github.noeppi_noeppi.libx.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired whenever a bow or a crossbow looks for ammo. This will contain the ammo found by
 * vanilla. The ammo can then be changed. The ItemStack given here as ammo will be shrunk
 * whenever the bow is fired. It you don't want this behaviour, copy the stack. For bows
 * you can use {@code ArrowLooseEvent} to handle removal of the arrow.
 * 
 * If the player is in creative mode, vanilla will always find plain arrows for it. Use
 * {@code getFoundAmmo} to get an empty stack whenever vanilla returns a plain arrow for
 * a creative player.
 * 
 * When the event is canceled, for non-creative players no ammo will be found and for
 * creative players a plain arrow will be found.
 */
public class PlayerFindAmmoEvent extends Event {
    
    private final PlayerEntity player;
    private final ItemStack shootable;
    private ItemStack ammo;
    
    public PlayerFindAmmoEvent(PlayerEntity player, ItemStack shootable, ItemStack ammo) {
        this.player = player;
        this.shootable = shootable;
        this.ammo = ammo;
    }

    /**
     * Gets the player using the bow or crossbow.
     */
    public PlayerEntity getPlayer() {
        return this.player;
    }

    /**
     * Gets the shootable item.
     */
    public ItemStack getShootable() {
        return this.shootable;
    }

    /**
     * Gets the ammo currently found.
     */
    public ItemStack getAmmo() {
        return this.ammo;
    }

    /**
     * Gets the ammo currently found but returns empty for creative
     * players that have no ammo in their inventory.
     */
    public ItemStack getFoundAmmo() {
        return this.player.isCreative() && this.ammo.getItem() == Items.ARROW ? ItemStack.EMPTY : this.ammo;
    }

    /**
     * Sets the ammo that should be used.
     */
    public PlayerFindAmmoEvent setAmmo(ItemStack ammo) {
        this.ammo = ammo;
        return this;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
