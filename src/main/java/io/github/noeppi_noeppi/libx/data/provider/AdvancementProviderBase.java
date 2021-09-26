package io.github.noeppi_noeppi.libx.data.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Base provider for custom {@link Advancement advancements}. If you want to have multiple advancement
 * tabs, use multiple providers. Every provider has one root advancement. All advancements with no
 * explicit parent will be added to it. You should configure your advancements with the {@link #root()}
 * and {@link #advancement(String)} methods in {@link #setup() setup}.
 */
public abstract class AdvancementProviderBase implements DataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    protected final ModX mod;
    protected final DataGenerator generator;
    private final Map<ResourceLocation, Supplier<Advancement>> advancements = new HashMap<>();
    private String rootId = null;
    private Supplier<Advancement> rootSupplier = null;

    public AdvancementProviderBase(ModX mod, DataGenerator generator) {
        this.mod = mod;
        this.generator = generator;
    }

    public abstract void setup();

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " advancements";
    }

    @Override
    public void run(@Nonnull HashCache cache) throws IOException {
        this.setup();
        for (Supplier<Advancement> supplier : this.advancements.values()) {
            Advancement advancement = supplier.get();
            Path path = this.generator.getOutputFolder().resolve("data/" + advancement.getId().getNamespace() + "/advancements/" + advancement.getId().getPath() + ".json");
            DataProvider.save(GSON, cache, advancement.deconstruct().serializeToJson(), path);
        }
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
    public void advancement(Advancement advancement) {
        if (this.advancements.put(advancement.getId(), () -> advancement) != null) {
            throw new IllegalStateException("Duplicate advancement: " + advancement.getId());
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

    /**
     * Creates a dummy {@link Advancement advancement} with a given id to be used as a parent if your
     * advancement should have another advancement from another mod as parent.
     */
    public Advancement dummy(ResourceLocation id) {
        return this.dummy(id, false);
    }

    /**
     * Creates a dummy {@link Advancement advancement} with a given id to be used as a parent if your
     * advancement should have another advancement from another mod as parent.
     * 
     * @param hidden Whether the advancement is hidden.
     */
    public Advancement dummy(ResourceLocation id, boolean hidden) {
        return new Advancement(id, null, new DisplayInfo(new ItemStack(Items.BARRIER), new TextComponent(""), new TextComponent(""), null, FrameType.TASK, true, true, hidden), AdvancementRewards.EMPTY, new HashMap<>(), new String[][]{});
    }

    private ResourceLocation idFor(String id) {
        if (this.rootId == null) {
            throw new IllegalStateException("On advancement providers without a root advancement only fully qualified resource locations are allowed, no plain ids.");
        }
        return this.mod.resource(this.rootId + "/" + id);
    }

    /**
     * Gets a {@link CriterionTriggerInstance criterion} that requires all of the given items to be in
     * the inventory at the same time.
     */
    public CriterionTriggerInstance items(ItemLike... items) {
        return this.items(Arrays.stream(items).map(item -> ItemPredicate.Builder.item().of(item).build()).toArray(ItemPredicate[]::new));
    }
    
    /**
     * Gets a {@link CriterionTriggerInstance criterion} that requires all of the given items to be in
     * the inventory at the same time.
     */
    @SafeVarargs
    public final CriterionTriggerInstance items(Tag<Item>... items) {
        return this.items(Arrays.stream(items).map(item -> ItemPredicate.Builder.item().of(item).build()).toArray(ItemPredicate[]::new));
    }
        
    /**
     * Gets a {@link CriterionTriggerInstance criterion} that requires all of the given items to be in
     * the inventory at the same time.
     */
    public CriterionTriggerInstance items(ItemPredicate... items) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(items);
    }

    /**
     * Gets a {@link TaskFactory} that adds a task for every item given to this method.
     */
    public TaskFactory itemTasks(ItemLike... items) {
        return this.itemTasks(Arrays.stream(items).map(item -> ItemPredicate.Builder.item().of(item).build()).toArray(ItemPredicate[]::new));
    }

    /**
     * Gets a {@link TaskFactory} that adds a task for every item given to this method.
     */
    @SafeVarargs
    public final TaskFactory itemTasks(Tag<Item>... items) {
        return this.itemTasks(Arrays.stream(items).map(item -> ItemPredicate.Builder.item().of(item).build()).toArray(ItemPredicate[]::new));
    }

    /**
     * Gets a {@link TaskFactory} that adds a task for every item given to this method.
     */
    public TaskFactory itemTasks(ItemPredicate... items) {
        return () -> Arrays.stream(items).map(item -> new CriterionTriggerInstance[]{this.items(item) }).toArray(CriterionTriggerInstance[][]::new);
    }

    /**
     * Gets a {@link CriterionTriggerInstance criterion} that requires a player to consume (eat/drink) an item.
     */
    public CriterionTriggerInstance eat(ItemLike food) {
        return this.eat(ItemPredicate.Builder.item().of(food).build());
    }

    /**
     * Gets a {@link CriterionTriggerInstance criterion} that requires a player to consume (eat/drink) an item.
     */
    public CriterionTriggerInstance eat(Tag<Item> food) {
        return this.eat(ItemPredicate.Builder.item().of(food).build());
    }

    /**
     * Gets a {@link CriterionTriggerInstance criterion} that requires a player to consume (eat/drink) an item.
     */
    public CriterionTriggerInstance eat(ItemPredicate food) {
        return new ConsumeItemTrigger.TriggerInstance(EntityPredicate.Composite.ANY, food);
    }

    /**
     * Gets a {@link CriterionTriggerInstance criterion} that requires a player to leave a dimension.
     */
    public CriterionTriggerInstance leave(ResourceKey<Level> dimension) {
        return new ChangeDimensionTrigger.TriggerInstance(EntityPredicate.Composite.ANY, dimension, null);
    }

    /**
     * Gets a {@link CriterionTriggerInstance criterion} that requires a player to enter a dimension.
     */
    public CriterionTriggerInstance enter(ResourceKey<Level> dimension) {
        return ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(dimension);
    }

    /**
     * Gets a {@link CriterionTriggerInstance criterion} that requires a player to perform a specific dimension change.
     */
    public CriterionTriggerInstance changeDim(ResourceKey<Level> from, ResourceKey<Level> to) {
        return new ChangeDimensionTrigger.TriggerInstance(EntityPredicate.Composite.ANY, from, to);
    }

    /**
     * Gets the given {@link EntityPredicate} as an {@link EntityPredicate.Composite}.
     */
    public EntityPredicate.Composite entity(EntityPredicate entity) {
        return EntityPredicate.Composite.create(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, entity).build());
    }

    /**
     * Gets an {@link EntityPredicate.Composite} that matches for a specific entity type.
     */
    public EntityPredicate.Composite entity(EntityType<?> type) {
        return this.entity(EntityPredicate.Builder.entity().of(type).build());
    }

    /**
     * Gets an {@link ItemPredicate} for an item and optionally some enchantments.
     */
    public ItemPredicate stack(ItemLike item, Enchantment... enchs) {
        ItemPredicate.Builder builder = ItemPredicate.Builder.item().of(item);
        for (Enchantment ench : enchs) {
            builder.hasEnchantment(new EnchantmentPredicate(ench, MinMaxBounds.Ints.atLeast(1)));
        }
        return builder.build();
    }

    /**
     * Gets an {@link ItemPredicate} for an item and optionally some enchantments.
     */
    public ItemPredicate stack(Tag<Item> item, Enchantment... enchs) {
        ItemPredicate.Builder builder = ItemPredicate.Builder.item().of(item);
        for (Enchantment ench : enchs) {
            builder.hasEnchantment(new EnchantmentPredicate(ench, MinMaxBounds.Ints.atLeast(1)));
        }
        return builder.build();
    }

    /**
     * Gets an {@link ItemPredicate} for some enchantments.
     */
    public ItemPredicate stack(Enchantment... enchs) {
        if (enchs.length == 0) {
            throw new IllegalStateException("Don't use stack() for an any predicate. Use ItemPredicate.ANY instead.");
        }
        ItemPredicate.Builder builder = ItemPredicate.Builder.item();
        for (Enchantment ench : enchs) {
            builder.hasEnchantment(new EnchantmentPredicate(ench, MinMaxBounds.Ints.atLeast(1)));
        }
        return builder.build();
    }

    /**
     * Gets an {@link ItemPredicate} for an enchantment with a minimum level.
     */
    public ItemPredicate stack(Enchantment ench, int min) {
        return ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(ench, MinMaxBounds.Ints.atLeast(min))).build();
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
        private Supplier<Advancement> parent;
        private DisplayInfo display;
        private ResourceLocation background;
        private final List<List<Criterion>> criteria = new ArrayList<>();
        private AdvancementRewards reward = AdvancementRewards.EMPTY;

        private AdvancementFactory(String namespace, String rootId) {
            this.id = new ResourceLocation(namespace, rootId + "/root");
            this.root = true;
            this.parent = () -> null;
        }
        
        private AdvancementFactory(ResourceLocation id) {
            this.id = id;
            this.root = false;
            this.parent = () -> null;
        }

        /**
         * Sets the parent of this advancement.
         */
        public AdvancementFactory parent(Advancement parent) {
            if (this.root) throw new IllegalStateException("Can't set parent for root advancement.");
            this.parent = () -> parent;
            return this;
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
        public AdvancementFactory display(ItemLike icon, FrameType frame) {
            return this.display(new ItemStack(icon), frame);
        }
        
        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemLike icon, FrameType frame, boolean toast, boolean chat, boolean hidden) {
            return this.display(new ItemStack(icon), frame, toast, chat, hidden);
        }

        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemStack icon) {
            return this.display(icon, FrameType.TASK);
        }

        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemStack icon, FrameType frame) {
            return this.display(icon, frame, !this.root, !this.root, false);
        }
        
        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemStack icon, FrameType frame, boolean toast, boolean chat, boolean hidden) {
            return this.display(icon,
                    new TranslatableComponent("advancements." + this.id.getNamespace() + "." + this.id.getPath().replace('/', '.') + ".title"),
                    new TranslatableComponent("advancements." + this.id.getNamespace() + "." + this.id.getPath().replace('/', '.') + ".description"),
                    frame, toast, chat, hidden
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
        public AdvancementFactory display(ItemLike icon, Component title, Component description, FrameType frame) {
            return this.display(new ItemStack(icon), title, description, frame);
        }
        
        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemLike icon, Component title, Component description, FrameType frame, boolean toast, boolean chat, boolean hidden) {
            return this.display(new ItemStack(icon), title, description, frame, toast, chat, hidden);
        }
        
        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemStack icon, Component title, Component description) {
            return this.display(icon, title, description, FrameType.TASK);
        }
        
        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemStack icon, Component title, Component description, FrameType frame) {
            return this.display(icon, title, description, frame, !this.root, !this.root, false);
        }
        
        /**
         * Sets the display info for this advancement. If {@code display} is not called, the
         * advancement won't be visible.
         */
        public AdvancementFactory display(ItemStack icon, Component title, Component description, FrameType frame, boolean toast, boolean chat, boolean hidden) {
            this.display = new DisplayInfo(icon, title, description, null, frame, toast, chat, hidden);
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
        public AdvancementFactory task(CriterionTriggerInstance... criteria) {
            if (criteria.length == 0) {
                throw new IllegalStateException("Can not add empty task to advancement.");
            }
            this.criteria.add(Arrays.stream(criteria).map(Criterion::new).collect(Collectors.toList()));
            return this;
        }

        /**
         * Adds multiple tasks to the advancement. Here <b>all</b> criteria must be completed to
         * complete the advancement.
         */
        public AdvancementFactory tasks(CriterionTriggerInstance... criteria) {
            if (criteria.length == 0) {
                throw new IllegalStateException("Can not add empty task to advancement.");
            }
            for (CriterionTriggerInstance instance : criteria) {
                this.criteria.add(List.of(new Criterion(instance)));
            }
            this.criteria.add(Arrays.stream(criteria).map(Criterion::new).collect(Collectors.toList()));
            return this;
        }

        /**
         * Adds multiple tasks to this advancement defined by the given {@link TaskFactory}.
         */
        public AdvancementFactory tasks(TaskFactory factory) {
            for (CriterionTriggerInstance[] task : factory.apply()) {
                this.task(task);
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
        
        private Advancement build() {
            if (this.criteria.isEmpty()) {
                throw new IllegalStateException("Can not add advancement without tasks.");
            }
            Set<String> idsTaken = new HashSet<>();
            String[][] criteriaIds = new String[this.criteria.size()][];
            Map<String, Criterion> criteriaMap = new HashMap<>();
            for (int i = 0; i < this.criteria.size(); i++) {
                String[] criterionGroup = new String[this.criteria.get(i).size()];
                for (int j = 0; j < this.criteria.get(i).size(); j++) {
                    String baseName = Objects.requireNonNull(this.criteria.get(i).get(j).getTrigger(), "Can't build advancement: Empty criterion").getCriterion().getPath();
                    baseName = baseName.replace('.', '_').replace('/', '_');
                    String nextId = baseName;
                    int num = 2;
                    while ((idsTaken.contains(nextId))) {
                        nextId = baseName + (num++);
                    }
                    idsTaken.add(nextId);
                    criterionGroup[j] = nextId;
                    criteriaMap.put(nextId, this.criteria.get(i).get(j));
                }
                criteriaIds[i] = criterionGroup;
            }
            Advancement parentAdv = this.parent.get();
            if (this.root && parentAdv != null) {
                throw new IllegalStateException("Root advancement can not have a parent.");
            } else if (!this.root && parentAdv == null) {
                if (AdvancementProviderBase.this.rootSupplier != null) {
                    parentAdv = AdvancementProviderBase.this.rootSupplier.get();
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
                displayInfo = new DisplayInfo(this.display.getIcon(), this.display.getTitle(), this.display.getDescription(), this.background, this.display.getFrame(), this.display.shouldShowToast(), this.display.shouldAnnounceChat(), this.display.isHidden());
            }
            if (parentAdv != null && parentAdv.getDisplay() == null && displayInfo != null) {
                throw new IllegalStateException("Can't build advancement with display and display-less parent.");
            }
            if (parentAdv != null && parentAdv.getDisplay() != null && displayInfo != null && parentAdv.getDisplay().isHidden() && !displayInfo.isHidden()) {
                throw new IllegalStateException("Can't build visible advancement with hidden parent.");
            }
            return new Advancement(this.id, parentAdv, displayInfo, this.reward, criteriaMap, criteriaIds);
        }
    }

    /**
     * A task factory can define multiple tasks.
     */
    public interface TaskFactory {

        CriterionTriggerInstance[][] apply();
    }
}
