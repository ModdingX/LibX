package org.moddingx.libx.creativetab;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.moddingx.libx.base.BlockBase;
import org.moddingx.libx.base.ItemBase;
import org.moddingx.libx.impl.ModInternal;
import org.moddingx.libx.mod.ModX;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Class to register and populate a creative tab. You should subclass it and create an instance in your
 * mods' constructor.
 */
public abstract class CreativeTabX {
    
    // Use numeric ids for ordering as they should represent the order in which items were registered. 
    @SuppressWarnings("UnstableApiUsage")
    private static final Comparator<Item> REGISTRY_ORDER = Comparator.comparing(item -> ForgeRegistries.ITEMS instanceof ForgeRegistry<Item> reg ? reg.getID(item) : Integer.MAX_VALUE - 1);
    
    protected final ModX mod;
    protected final ResourceLocation id;
    private CreativeModeTab tab;
    
    protected CreativeTabX(ModX mod) {
        this(mod, "tab");
    }
    
    protected CreativeTabX(ModX mod, String name) {
        this.mod = mod;
        this.id = mod.resource(name);
        ModInternal.get(mod).modEventBus().addListener(this::registerCreativeTab);
    }

    /**
     * Gets the creative tab.
     * 
     * @throws IllegalArgumentException if the tab has not yet been created.
     */
    public CreativeModeTab tab() {
        if (this.tab == null) {
            throw new IllegalArgumentException("Creative tab " + this.id + " has not yet been created.");
        }
        return this.tab;
    }

    /**
     * Sets some basic creative tab information. This is not for tab contents. The default implementation just
     * sets the tab title.
     */
    protected void buildTab(CreativeModeTab.Builder builder) {
        builder.title(Component.translatable("itemGroup." + this.id.getNamespace() + ("tab".equals(this.id.getPath()) ? "" : "." + this.id.getPath())));
    }

    /**
     * Adds the items to the tab.
     */
    protected abstract void addItems(TabContext ctx);

    /**
     * Adds all items from the current mod into the tab.
     */
    protected void addModItems(TabContext ctx) {
        this.addModItems(ctx, REGISTRY_ORDER);
    }

    /**
     * Adds all items from the current mod into the tab using a custom order.
     */
    protected void addModItems(TabContext ctx, Comparator<Item> order) {
        this.addModItems(ctx, order, item -> ctx.context().hasPermissions() || !(item instanceof GameMasterBlockItem));
    }

    /**
     * Adds all items from the current mod, that match a predicate into the tab.
     */
    protected void addModItems(TabContext ctx, Predicate<Item> items) {
        this.addModItems(ctx, REGISTRY_ORDER, items);
    }
    
    /**
     * Adds all items from the current mod, that match a predicate into the tab using a custom order.
     */
    protected void addModItems(TabContext ctx, Comparator<Item> order, Predicate<Item> items) {
        this.addModItemStacks(ctx, order, item -> items.test(item) ? this.itemStream(item) : Stream.empty());
    }
    
    /**
     * Generates a stream of {@link ItemStack item stacks} for each item from the current mod and
     * adds the stacks to the tab.
     */
    protected void addModItemStacks(TabContext ctx, Function<Item, Stream<ItemStack>> stacks) {
        this.addModItemStacks(ctx, REGISTRY_ORDER, stacks);
    }
    
    /**
     * Generates a stream of {@link ItemStack item stacks} for each item from the current mod and
     * adds the stacks to the tab using a custom order.
     */
    protected void addModItemStacks(TabContext ctx, Comparator<Item> order, Function<Item, Stream<ItemStack>> stacks) {
        ForgeRegistries.ITEMS.getEntries().stream()
                .filter(entry -> this.mod.modid.equals(entry.getKey().location().getNamespace()))
                .map(Map.Entry::getValue)
                .filter(item -> item.requiredFeatures().isSubsetOf(ctx.features()))
                .sorted(order)
                .flatMap(stacks)
                .forEach(stack -> ctx.output().accept(stack));
    }
    
    private void registerCreativeTab(RegisterEvent event) {
        event.register(Registries.CREATIVE_MODE_TAB, reg -> {
            CreativeModeTab.Builder builder = CreativeModeTab.builder();
            this.buildTab(builder);
            builder.displayItems((context, output) -> this.addItems(new TabContext(context, context.enabledFeatures(), output)));
            this.tab = builder.build();
            reg.register(this.id, this.tab);
        });
    }
    
    private Stream<ItemStack> itemStream(Item item) {
        if (item instanceof ItemBase base) {
            return base.makeCreativeTabStacks();
        } else if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof BlockBase base) {
            return base.makeCreativeTabStacks();
        } else {
            return Stream.of(new ItemStack(item));
        }
    }
    
    public record TabContext(CreativeModeTab.ItemDisplayParameters context, FeatureFlagSet features, CreativeModeTab.Output output) {}
}
