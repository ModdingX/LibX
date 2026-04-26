package org.moddingx.libx.datagen.provider;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.PackTarget;
import org.moddingx.libx.datagen.RegistrySet;
import org.moddingx.libx.datapack.DatapackHelper;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Base provider for custom {@link Advancement advancements}. If you want to have multiple advancement
 * tabs, use multiple providers. Every provider has one root advancement. All advancements with no
 * explicit parent will be added to it. You should configure your advancements with the {@link #root()}
 * and {@link #advancement(String)} methods in {@link #setup() setup}.
 */
public abstract class AdvancementProviderBase implements DataProvider {
    
    protected final ModX mod;
    protected final PackTarget packTarget;
    private final RegistrySet registries;
    private final HolderGetter<Item> itemRegistries;
    private final HolderGetter<Enchantment> enchantmentRegistries;
    private final HolderGetter<EntityType<?>> entityRegistries;
    private final Map<ResourceLocation, Supplier<AdvancementInfo>> advancements = new HashMap<>();
    private String rootId = null;
    private Supplier<AdvancementInfo> rootSupplier = null;

    public AdvancementProviderBase(DatagenContext ctx) {
        this.mod = ctx.mod();
        this.packTarget = ctx.target();
        this.registries = ctx.registries();
        this.itemRegistries = this.registries.registryAccess().lookupOrThrow(Registries.ITEM);
        this.enchantmentRegistries = this.registries.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        this.entityRegistries = this.registries.registryAccess().lookupOrThrow(Registries.ENTITY_TYPE);
    }

    public abstract void setup();

    /**
     * Gets a list of conditions for all advancements added by this provider.
     */
    protected List<ICondition> conditions() {
        return List.of();
    }

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " advancements";
    }

    @Nonnull
    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput cache) {
        this.setup();
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, this.registries.registryAccess());
        return CompletableFuture.allOf(this.advancements.entrySet().stream().map(entry -> {
            ResourceLocation id = entry.getKey();
            ResourceKey<Advancement> key = ResourceKey.create(Registries.ADVANCEMENT, id);
            AdvancementInfo advancementInfo = entry.getValue().get();
            if (advancementInfo.advancement() == null) return CompletableFuture.completedFuture(null);
            WithConditions<Advancement> conditionalAdvancement = new WithConditions<>(this.conditions(), advancementInfo.advancement());
            JsonElement json = Advancement.CONDITIONAL_CODEC.encodeStart(ops, Optional.of(conditionalAdvancement)).getOrThrow(RuntimeException::new);
            Path path = this.packTarget.path(PackType.SERVER_DATA).resolve(DatapackHelper.registryPath(key));
            return DataProvider.saveStable(cache, json, path);
        }).toArray(CompletableFuture[]::new));
    }

    /**
     * Gets an {@link AdvancementFactory} to customise the root {@link Advancement advancement} for
     * this provider. The root id will be the modid.
     * 
     * @see #root(String, String)
     */
    public AdvancementFactory root() {
        return this.root(this.mod.modid);
    }

    /**
     * Gets an {@link AdvancementFactory} to customise the root {@link Advancement advancement} for
     * this provider.
     * 
     * @param id The root id. The actual advancement id will be {@code modid:id/root}
     * 
     * @see #root(String, String)
     */
    public AdvancementFactory root(String id) {
        return this.root(this.mod.modid, id);
    }

    /**
     * Gets an {@link AdvancementFactory} to customise the root {@link Advancement advancement} for
     * this provider.
     * 
     * @param namespace The namespace of the root advancement.
     * @param id The root id. The actual advancement id will be {@code namespace:id/root}
     */
    public AdvancementFactory root(String namespace, String id) {
        if (id.equals("recipes")) {
            throw new IllegalStateException("Can't 'recipes' as root advancement id. Use a recipe provider to generate recipe advancements.");
        }
        if (this.rootId != null || this.rootSupplier != null) {
            throw new IllegalStateException("Can't use multiple root advancements in the same provider. Use multiple providers for this.");
        }
        if (!this.advancements.isEmpty()) {
            throw new IllegalStateException("The root advancement must be the first advancement that is configured.");
        }
        AdvancementFactory factory = new AdvancementFactory(namespace, id);
        if (this.advancements.put(factory.id, factory::build) != null) {
            throw new IllegalStateException("Duplicate root advancement: " + id);
        }
        this.rootId = id;
        this.rootSupplier = factory::build;
        return factory;
    }

    /**
     * Adds a built {@link Advancement advancement} to the provider.
     */
    public void advancement(ResourceLocation id, Advancement advancement) {
        if (this.advancements.put(id, () -> new AdvancementInfo(id, advancement)) != null) {
            throw new IllegalStateException("Duplicate advancement: " + id);
        }
    }

    /**
     * Adds an advancement to the provider identified by its {@link ResourceLocation}. Returns
     * an {@link AdvancementFactory} to customise the advancement.
     */
    public AdvancementFactory advancement(ResourceLocation id) {
        AdvancementFactory factory = new AdvancementFactory(id);
        if (this.advancements.put(id, factory::build) != null) {
            throw new IllegalStateException("Duplicate advancement: " + id);
        }
        return factory;
    }

    /**
     * Adds an {@link Advancement advancement} to the provider identified by an id. The
     * {@link ResourceLocation} is built with the modid and the root advancement id. Returns an
     * {@link AdvancementFactory} to customise the advancement.
     */
    public AdvancementFactory advancement(String id) {
        return this.advancement(this.idFor(id));
    }

    private ResourceLocation idFor(String id) {
        if (this.rootId == null) {
            throw new IllegalStateException("On advancement providers without a root advancement only fully qualified resource locations are allowed, no plain ids.");
        }
        return this.mod.resource(this.rootId + "/" + id);
    }

    /**
     * Gets a {@link Criterion criterion} that requires all of the given items to be in
     * the inventory at the same time.
     */
    public Criterion<?> items(ItemLike... items) {
        return this.items(Arrays.stream(items).map(this::stack).toArray(ItemPredicate[]::new));
    }
    
    /**
     * Gets a {@link Criterion criterion} that requires all of the given items to be in
     * the inventory at the same time.
     */
    @SafeVarargs
    public final Criterion<?> items(TagKey<Item>... items) {
        return this.items(Arrays.stream(items).map(this::stack).toArray(ItemPredicate[]::new));
    }
        
    /**
     * Gets a {@link Criterion criterion} that requires all of the given items to be in
     * the inventory at the same time.
     */
    public Criterion<?> items(ItemPredicate... items) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(items);
    }

    /**
     * Gets a {@link TaskFactory} that adds a task for every item given to this method.
     */
    public TaskFactory itemTasks(ItemLike... items) {
        return this.itemTasks(Arrays.stream(items).map(this::stack).toArray(ItemPredicate[]::new));
    }

    /**
     * Gets a {@link TaskFactory} that adds a task for every item given to this method.
     */
    @SafeVarargs
    public final TaskFactory itemTasks(TagKey<Item>... items) {
        return this.itemTasks(Arrays.stream(items).map(this::stack).toArray(ItemPredicate[]::new));
    }

    /**
     * Gets a {@link TaskFactory} that adds a task for every item given to this method.
     */
    public TaskFactory itemTasks(ItemPredicate... items) {
        return () -> Arrays.stream(items).map(item -> List.<Criterion<?>>of(this.items(item))).toList();
    }
    
    /**
     * Gets a {@link Criterion criterion} that requires a player to consume (eat/drink) any item.
     */
    public Criterion<?> eat() {
        return ConsumeItemTrigger.TriggerInstance.usedItem();
    }

    /**
     * Gets a {@link Criterion criterion} that requires a player to consume (eat/drink) an item.
     */
    public Criterion<?> eat(ItemLike food) {
        return this.eat(this.stack(food));
    }

    /**
     * Gets a {@link Criterion criterion} that requires a player to consume (eat/drink) an item.
     */
    public Criterion<?> eat(TagKey<Item> food) {
        return this.eat(this.stack(food));
    }

    /**
     * Gets a {@link Criterion criterion} that requires a player to consume (eat/drink) an item.
     */
    public Criterion<?> eat(ItemPredicate food) {
        return ConsumeItemTrigger.TriggerInstance.usedItem(this.itemPredicateBuilder(food));
    }

    /**
     * Gets a {@link Criterion criterion} that requires a player to leave a dimension.
     */
    public Criterion<?> leave(ResourceKey<Level> dimension) {
        return ChangeDimensionTrigger.TriggerInstance.changedDimensionFrom(dimension);
    }

    /**
     * Gets a {@link Criterion criterion} that requires a player to enter a dimension.
     */
    public Criterion<?> enter(ResourceKey<Level> dimension) {
        return ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(dimension);
    }

    /**
     * Gets a {@link Criterion criterion} that requires a player to perform a specific dimension change.
     */
    public Criterion<?> changeDim(ResourceKey<Level> from, ResourceKey<Level> to) {
        return ChangeDimensionTrigger.TriggerInstance.changedDimension(from, to);
    }

    /**
     * Gets the given {@link EntityPredicate} as an {@link ContextAwarePredicate}.
     */
    public ContextAwarePredicate entity(EntityPredicate entity) {
        return ContextAwarePredicate.create(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, entity).build());
    }

    /**
     * Gets an {@link ContextAwarePredicate} that matches for a specific entity type.
     */
    public ContextAwarePredicate entity(EntityType<?> type) {
        return this.entity(EntityPredicate.Builder.entity().of(this.entityRegistries, type).build());
    }

    /**
     * Gets an {@link ItemPredicate} for an item and optionally some enchantments.
     */
    @SafeVarargs
    public final ItemPredicate stack(ItemLike item, ResourceKey<Enchantment>... enchs) {
        HolderSet<Item> items = HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(item.asItem()));
        return this.applyEnchantments(items, enchs);
    }

    /**
     * Gets an {@link ItemPredicate} for an item and optionally some enchantments.
     */
    @SafeVarargs
    public final ItemPredicate stack(TagKey<Item> item, ResourceKey<Enchantment>... enchs) {
        HolderSet<Item> items = this.itemRegistries.getOrThrow(item);
        return this.applyEnchantments(items, enchs);
    }

    /**
     * Gets an {@link ItemPredicate} for some enchantments.
     */
    @SafeVarargs
    public final ItemPredicate stack(ResourceKey<Enchantment>... enchs) {
        if (enchs.length == 0) {
            throw new IllegalStateException("stack() can't be used to obtain an allways matching predicate.");
        }
        return this.applyEnchantments(null, enchs);
    }

    /**
     * Gets an {@link ItemPredicate} for an enchantment with a minimum level.
     */
    public ItemPredicate stack(ResourceKey<Enchantment> ench, int min) {
        EnchantmentPredicate enchantmentPredicate = new EnchantmentPredicate(this.enchantmentRegistries.getOrThrow(ench), MinMaxBounds.Ints.atLeast(min));
        return this.itemPredicate(null, List.of(enchantmentPredicate));
    }
    
    private ItemPredicate applyEnchantments(@Nullable HolderSet<Item> items, ResourceKey<Enchantment>[] enchantments) {
        if (enchantments.length == 0) return this.itemPredicate(items, List.of());
        List<EnchantmentPredicate> enchantmentPredicates = Arrays.stream(enchantments)
                .map(key -> new EnchantmentPredicate(this.enchantmentRegistries.getOrThrow(key), MinMaxBounds.Ints.ANY))
                .toList();
        return this.itemPredicate(items, enchantmentPredicates);
    }

    private ItemPredicate itemPredicate(@Nullable HolderSet<Item> items, List<EnchantmentPredicate> enchantments) {
        DataComponentMatchers components;
        if (enchantments.isEmpty()) {
            components = DataComponentMatchers.ANY;
        } else {
            components = DataComponentMatchers.Builder.components()
                    .partial(DataComponentPredicates.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.copyOf(enchantments)))
                    .build();
        }
        return new ItemPredicate(Optional.ofNullable(items), MinMaxBounds.Ints.ANY, components);
    }

    private ItemPredicate.Builder itemPredicateBuilder(ItemPredicate predicate) {
        ItemPredicate.Builder builder = ItemPredicate.Builder.item()
                .withCount(predicate.count())
                .withComponents(predicate.components());
        predicate.items().ifPresent(items -> items.unwrapKey().ifPresentOrElse(
                tag -> builder.of(this.itemRegistries, tag),
                () -> builder.of(this.itemRegistries, items.stream().map(holder -> holder.value()).toArray(Item[]::new))
        ));
        return builder;
    }

    /**
     * An advancement factory can be used to customise an advancement in a builder style pattern.
     * Calling one of the {@link #parent(String) parent} methods for the root advancement will cause
     * an error. If this provider has no root advancement, you must always call one of the
     * {@link #parent(String) parent} methods.
     */
    public class AdvancementFactory {

        private final ResourceLocation id;
        private final boolean root;
        private Supplier<AdvancementInfo> parent;
        @Nullable private DisplayInfo display;
        @Nullable private ResourceLocation background;
        private final List<List<Criterion<?>>> criteria = new ArrayList<>();
        private AdvancementRewards reward = AdvancementRewards.EMPTY;
        private boolean telmetryEvent;

        private AdvancementFactory(String namespace, String rootId) {
            this.id = ResourceLocation.fromNamespaceAndPath(namespace, rootId + "/root");
            this.root = true;
            this.parent = () -> null;
            this.telmetryEvent = false;
        }
        
        private AdvancementFactory(ResourceLocation id) {
            this.id = id;
            this.root = false;
            this.parent = () -> null;
        }

        /**
         * Sets the parent of this advancement. The advancement must be found in this provider.
         */
        public AdvancementFactory parent(ResourceLocation id) {
            if (this.root) throw new IllegalStateException("Can't set parent for root advancement.");
            if (!AdvancementProviderBase.this.advancements.containsKey(id)) {
                throw new IllegalStateException("Parent advancement unknown: " + id);
            }
            this.parent = AdvancementProviderBase.this.advancements.get(id);
            return this;
        }

        /**
         * Sets the parent of this advancement. The advancement must be found in this provider. The
         * string given here should be the same string as given
         * to {@link AdvancementProviderBase#advancement(String)}
         */
        public AdvancementFactory parent(String id) {
            if (this.root) throw new IllegalStateException("Can't set parent for root advancement.");
            return this.parent(AdvancementProviderBase.this.idFor(id));
        }

        /**
         * Sets the parent of this advancement. This method should not be used with advancements from this provider
         * and is only meant to set foreign advancements as parent.
         */
        public AdvancementFactory foreignParent(ResourceLocation id) {
            if (this.root) throw new IllegalStateException("Can't set parent for root advancement.");
            this.parent = () -> new AdvancementInfo(id, null);
            return this;
        }

        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemLike icon) {
            return this.display(new ItemStack(icon));
        }

        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemLike icon, AdvancementType type) {
            return this.display(new ItemStack(icon), type);
        }
        
        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemLike icon, AdvancementType type, boolean toast, boolean chat, boolean hidden) {
            return this.display(new ItemStack(icon), type, toast, chat, hidden);
        }

        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemStack icon) {
            return this.display(icon, AdvancementType.TASK);
        }

        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemStack icon, AdvancementType type) {
            return this.display(icon, type, !this.root, !this.root, false);
        }
        
        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemStack icon, AdvancementType type, boolean toast, boolean chat, boolean hidden) {
            return this.display(icon,
                    Component.translatable("advancements." + this.id.getNamespace() + "." + this.id.getPath().replace('/', '.') + ".title"),
                    Component.translatable("advancements." + this.id.getNamespace() + "." + this.id.getPath().replace('/', '.') + ".description"),
                    type, toast, chat, hidden
            );
        }
        
        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemLike icon, Component title, Component description) {
            return this.display(new ItemStack(icon), title, description);
        }
        
        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemLike icon, Component title, Component description, AdvancementType type) {
            return this.display(new ItemStack(icon), title, description, type);
        }
        
        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemLike icon, Component title, Component description, AdvancementType type, boolean toast, boolean chat, boolean hidden) {
            return this.display(new ItemStack(icon), title, description, type, toast, chat, hidden);
        }
        
        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemStack icon, Component title, Component description) {
            return this.display(icon, title, description, AdvancementType.TASK);
        }
        
        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemStack icon, Component title, Component description, AdvancementType type) {
            return this.display(icon, title, description, type, !this.root, !this.root, false);
        }
        
        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemStack icon, Component title, Component description, AdvancementType type, boolean toast, boolean chat, boolean hidden) {
            this.display = new DisplayInfo(icon, title, description, Optional.empty(), type, toast, chat, hidden);
            return this;
        }

        /**
         * Sets the background of the advancement tab. Must be used on the root advancement as is not allowed on others.
         */
        public AdvancementFactory background(ResourceLocation background) {
            if (!this.root) {
                throw new IllegalStateException("Can't set background on non-root advancement.");
            }
            this.background = background;
            return this;
        }

        /**
         * Adds a task to the advancement. A task can consist of multiple criteria. In this case
         * <b>one</b> of the criteria must be completed to complete the whole task.
         */
        public AdvancementFactory task(Criterion<?>... criteria) {
            if (criteria.length == 0) {
                throw new IllegalStateException("Can not add empty task to advancement.");
            }
            this.criteria.add(List.of(criteria));
            return this;
        }

        /**
         * Adds multiple tasks to the advancement. Here <b>all</b> criteria must be completed to
         * complete the advancement.
         */
        public AdvancementFactory tasks(Criterion<?>... criteria) {
            if (criteria.length == 0) {
                throw new IllegalStateException("Can not add empty task to advancement.");
            }
            for (Criterion<?> criterion : criteria) {
                this.criteria.add(List.of(criterion));
            }
            return this;
        }

        /**
         * Adds multiple tasks to this advancement defined by the given {@link TaskFactory}.
         */
        public AdvancementFactory tasks(TaskFactory factory) {
            for (List<Criterion<?>> task : factory.apply()) {
                this.task(task.toArray(Criterion[]::new));
            }
            return this;
        }

        /**
         * Sets the reward for this advancement.
         */
        public AdvancementFactory reward(AdvancementRewards reward) {
            this.reward = reward;
            return this;
        }
        
        public AdvancementFactory sendsTelmetryEvent() {
            this.telmetryEvent = true;
            return this;
        }

        private AdvancementInfo build() {
            if (this.criteria.isEmpty()) {
                throw new IllegalStateException("Can not add advancement without tasks.");
            }
            Set<String> idsTaken = new HashSet<>();
            List<List<String>> criteriaIds = new ArrayList<>();
            Map<String, Criterion<?>> criteriaMap = new HashMap<>();
            for (List<Criterion<?>> criterionGroup : this.criteria) {
                List<String> criterionGroupIds = new ArrayList<>();
                for (Criterion<?> criterion : criterionGroup) {
                    CriterionTrigger<?> trigger = Objects.requireNonNull(criterion.trigger(), "Can't build advancement: Empty criterion");
                    ResourceLocation triggerId = Objects.requireNonNull(BuiltInRegistries.TRIGGER_TYPES.getKey(trigger), "Unregistered criterion trigger");
                    String baseName = "minecraft".equals(triggerId.getNamespace()) ? triggerId.getPath() : triggerId.toString();
                    baseName = baseName.replace(':', '_').replace('.', '_').replace('/', '_');
                    String nextId = baseName;
                    int num = 2;
                    while ((idsTaken.contains(nextId))) {
                        nextId = baseName + (num++);
                    }
                    idsTaken.add(nextId);
                    criterionGroupIds.add(nextId);
                    criteriaMap.put(nextId, criterion);
                }
                criteriaIds.add(List.copyOf(criterionGroupIds));
            }
            AdvancementInfo parent = this.parent.get();
            ResourceLocation parentId = parent == null ? null : parent.id();
            Advancement parentAdv = parentId == null ? null : parent.advancement();
            if (this.root && parentId != null) {
                throw new IllegalStateException("Root advancement can not have a parent.");
            } else if (!this.root && parentId == null) {
                if (AdvancementProviderBase.this.rootSupplier != null) {
                    parent = AdvancementProviderBase.this.rootSupplier.get();
                    parentId = parent == null ? null : parent.id();
                    parentAdv = parentId == null ? null : parent.advancement();
                    if (parentAdv == null) {
                        throw new IllegalStateException("Root advancement configured wrongly. This is an error in LibX.");
                    }
                } else {
                    throw new IllegalStateException("This advancement provider has no default root and the advancement " + this.id + " has no root specified.");
                }
            }
            DisplayInfo displayInfo = this.display;
            if (this.root) {
                if (this.display == null) {
                    throw new IllegalStateException("Can't build root advancement without display.");
                } else if (this.background == null) {
                    throw new IllegalStateException("Can't build root advancement without background.");
                }
                displayInfo = new DisplayInfo(
                        this.display.getIcon(),
                        this.display.getTitle(),
                        this.display.getDescription(),
                        Optional.ofNullable(this.background).map(ClientAsset::new),
                        this.display.getType(),
                        this.display.shouldShowToast(),
                        this.display.shouldAnnounceChat(),
                        this.display.isHidden()
                );
            }
            if (parentAdv != null && parentAdv.display().isEmpty() && displayInfo != null) {
                throw new IllegalStateException("Can't build advancement with display and display-less parent.");
            }
            if (parentAdv != null && parentAdv.display().isPresent() && displayInfo != null && parentAdv.display().get().isHidden() && !displayInfo.isHidden()) {
                throw new IllegalStateException("Can't build visible advancement with hidden parent.");
            }
            Advancement advancement = new Advancement(Optional.ofNullable(parentId), Optional.ofNullable(displayInfo), this.reward, criteriaMap, new AdvancementRequirements(List.copyOf(criteriaIds)), this.telmetryEvent);
            return new AdvancementInfo(this.id, advancement);
        }
    }

    /**
     * A task factory can define multiple tasks.
     */
    public interface TaskFactory {

        List<List<Criterion<?>>> apply();
    }
    
    // null advancement means unknown
    private record AdvancementInfo(ResourceLocation id, @Nullable Advancement advancement) {}
}
