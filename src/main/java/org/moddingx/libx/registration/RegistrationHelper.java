package org.moddingx.libx.registration;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.moddingx.libx.mod.ModXRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A small factory helper that creates {@link BlockBehaviour.Properties} and {@link Item.Properties}
 * with the correct {@code setId} call pre-applied, and optionally tracks the created objects for
 * bulk registration via {@link #registerAll()}.
 *
 * <p>Intended for manual registration classes (those that do <em>not</em> use {@code @RegisterClass}).
 * Create a static instance and use it to build all property objects:
 *
 * <pre>{@code
 * private static final RegistrationHelper REG = new RegistrationHelper(MyMod.getInstance());
 *
 * public static final BlockBase myBlock = REG.block("my_block",
 *         props -> new BlockBase(MyMod.getInstance(), props, new Item.Properties()));
 *
 * public static void init() {
 *     REG.registerAll();
 * }
 * }</pre>
 *
 * <p>For {@code @RegisterClass} classes the coremod injects ids automatically — this helper is not needed there.
 */
public final class RegistrationHelper {

    private record RegEntry<T>(ResourceKey<? extends Registry<T>> registry, String name, T value) {}

    private final ModXRegistration mod;
    private final List<RegEntry<?>> entries = new ArrayList<>();

    public RegistrationHelper(ModXRegistration mod) {
        this.mod = mod;
    }

    public ModXRegistration mod() {
        return this.mod;
    }

    public BlockBehaviour.Properties blockOf(String name) {
        return BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, this.mod.resource(name)));
    }

    public BlockBehaviour.Properties blockCopy(String name, Block base) {
        return BlockBehaviour.Properties.ofFullCopy(base)
                .setId(ResourceKey.create(Registries.BLOCK, this.mod.resource(name)));
    }

    public Item.Properties itemOf(String name) {
        return new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, this.mod.resource(name)));
    }

    public <T extends Block> T block(String name, Function<BlockBehaviour.Properties, T> factory) {
        T block = factory.apply(this.blockOf(name));
        this.track(Registries.BLOCK, name, block);
        return block;
    }

    public <T extends Block> T block(String name, Block base, Function<BlockBehaviour.Properties, T> factory) {
        T block = factory.apply(this.blockCopy(name, base));
        this.track(Registries.BLOCK, name, block);
        return block;
    }

    public <T extends Item> T item(String name, Function<Item.Properties, T> factory) {
        T item = factory.apply(this.itemOf(name));
        this.track(Registries.ITEM, name, item);
        return item;
    }

    /**
     * Registers all blocks and items created through this helper with the mod's registration system.
     * Must be called before the first {@code RegisterEvent} fires — typically from the mod class
     * constructor or another early-setup method. Do not call from {@code FMLCommonSetupEvent} or later.
     * Calling this method clears the tracked entries, so it is safe to add more entries and call again.
     */
    public void registerAll() {
        for (RegEntry<?> entry : this.entries) {
            this.registerOne(entry);
        }

        this.entries.clear();
    }

    private <T> void registerOne(RegEntry<T> entry) {
        this.mod.register(entry.registry(), entry.name(), entry.value());
    }

    private <T> void track(ResourceKey<? extends Registry<?>> registry, String name, T value) {
        this.entries.add(new RegEntry<>((ResourceKey<Registry<T>>) registry, name, value));
    }
}
