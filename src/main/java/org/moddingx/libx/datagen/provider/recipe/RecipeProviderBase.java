package org.moddingx.libx.datagen.provider.recipe;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.EmptyTagLookupWrapper;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;
import org.moddingx.libx.impl.datagen.registries.DatagenBootstrapContext;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Provider for all kinds of recipes. By itself does not add support for any recipes. However you can
 * add extensions by implementing the interface. All extensions must implement {@link RecipeExtension}.
 * As this class implements {@link RecipeExtension} as well, you don't need to implement any methods when
 * adding an extension. For a list of available extensions, see the subclasses of {@link RecipeExtension}.
 */
public abstract class RecipeProviderBase extends RegistryProviderBase implements RecipeExtension {

    private RecipeOutput output;

    public RecipeProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public void run() {
        BootstrapContext<Recipe<?>> recipes = new DatagenBootstrapContext<>(this.registries, Registries.RECIPE);
        BootstrapContext<Advancement> advancements = new DatagenBootstrapContext<>(this.registries, Registries.ADVANCEMENT);
        new RecipeProvider(recipes, advancements, this.mod.modid, this.conditions().toArray(ICondition[]::new)) {

            @Override
            protected void buildRecipes() {
                RecipeProviderBase.this.output = this.output;
                RecipeProviderBase.this.setupExtensions();
                RecipeProviderBase.this.setup();
            }
        }.buildRecipes();
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " recipes";
    }

    protected abstract void setup();

    /**
     * Gets a list of conditions for all recipes added by this provider.
     */
    protected List<ICondition> conditions() {
        return List.of();
    }

    @Override
    public HolderGetter<Item> items() {
        return EmptyTagLookupWrapper.wrap(this.registries.registry(Registries.ITEM));
    }

    private void setupExtensions() {
        Set<Class<?>> collectedClasses = new HashSet<>();
        List<Method> extensionMethods = new ArrayList<>();
        // Collect all extensions, this class implements up to RecipeProviderBase
        Class<?> currentClass = this.getClass();
        while (currentClass != null && currentClass != RecipeProviderBase.class && currentClass != Object.class) {
            for (Class<?> iface : currentClass.getInterfaces()) {
                if (RecipeExtension.class.isAssignableFrom(iface) && collectedClasses.add(iface)) {
                    try {
                        Method method = iface.getMethod("setup", ModX.class, iface);
                        if (!Modifier.isStatic(method.getModifiers())) {
                            throw new IllegalStateException("Recipe extension setup method must be static: " + iface.getName() + "#setup");
                        }
                        extensionMethods.add(method);
                    } catch (NoSuchMethodException error) {
                        //
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        for (Method method : extensionMethods) {
            try {
                method.invoke(null, this.mod, this);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Can't access recipe extension setup method: " + method.getDeclaringClass().getName() + "#setup", e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Failed to run recipe extension setup: " + method.getDeclaringClass().getName(), e.getTargetException());
            }
        }
    }

    /**
     * Gets a {@link Identifier} with the namespace being the modid of the mod given in constructor
     * and the path being the registry path of the given item.
     */
    public Identifier id(ItemLike item) {
        return Identifier.fromNamespaceAndPath(this.mod.modid, Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item.asItem())).getPath());
    }

    /**
     * Gets a {@link Identifier} with the namespace being the modid of the mod given in constructor
     * and the path being the registry path of the given item followed by an underscore and the
     * given suffix.
     */
    public Identifier id(ItemLike item, String suffix) {
        return Identifier.fromNamespaceAndPath(this.mod.modid, Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item.asItem())).getPath() + "_" + suffix);
    }

    @Override
    public RecipeProviderBase provider() {
        return this;
    }

    @Override
    public RecipeOutput output() {
        return Objects.requireNonNull(this.output, "Recipe output not yet available.");
    }

    @Override
    public Criterion<?> criterion(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(this.item(item));
    }

    @Override
    public Criterion<?> criterion(TagKey<Item> item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(this.item(item));
    }

    @Override
    public Criterion<?> criterion(ItemPredicate... items) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(items);
    }
}
