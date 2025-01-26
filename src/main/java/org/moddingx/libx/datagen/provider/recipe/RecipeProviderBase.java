package org.moddingx.libx.datagen.provider.recipe;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Provider for all kinds of recipes. By itself does not add support for any recipes. However you can
 * add extensions by implementing the interface. All extensions must implement {@link RecipeExtension}.
 * As this class implements {@link RecipeExtension} as well, you don't need to implement any methods when
 * adding an extension. For a list of available extensions, see the subclasses of {@link RecipeExtension}.
 */
public abstract class RecipeProviderBase extends RecipeProvider implements RecipeExtension {

    protected final ModX mod;
    private RecipeOutput output;

    public RecipeProviderBase(DatagenContext ctx) {
        super(ctx.output(), CompletableFuture.completedFuture(ctx.registries().registryAccess()));
        this.mod = ctx.mod();
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
    protected final void buildRecipes(@Nonnull RecipeOutput output) {
        this.output = output.withConditions(this.conditions().toArray(ICondition[]::new));
        this.setupExtensions();
        this.setup();
    }
    
    private void setupExtensions() {
        Set<Class<?>> collectedClasses = new HashSet<>();
        List<Method> extensionMethods = new ArrayList<>();
        // Collect all extensions, this class implements up to RecipeProviderBase
        Class<?> currentClass = this.getClass();
        while(currentClass != null && currentClass != RecipeProviderBase.class && currentClass != Object.class) {
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
     * Gets a {@link ResourceLocation} with the namespace being the modid of the mod given in constructor
     * and the path being the registry path of the given item.
     */
    public ResourceLocation loc(ItemLike item) {
        return ResourceLocation.fromNamespaceAndPath(this.mod.modid, Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item.asItem())).getPath());
    }

    /**
     * Gets a {@link ResourceLocation} with the namespace being the modid of the mod given in constructor
     * and the path being the registry path of the given item followed by an underscore and the
     * given suffix.
     */
    public ResourceLocation loc(ItemLike item, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(this.mod.modid, Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item.asItem())).getPath() + "_" + suffix);
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
        return has(item);
    }

    @Override
    public Criterion<?> criterion(TagKey<Item> item) {
        return has(item);
    }

    @Override
    public Criterion<?> criterion(ItemPredicate... items) {
        return inventoryTrigger(items);
    }
}
