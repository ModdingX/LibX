package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.ClientAsset;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.animal.cow.CowVariant;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilusVariant;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.variant.BiomeCheck;
import net.minecraft.world.entity.variant.ModelAndTexture;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.entity.variant.StructureCheck;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

import java.util.List;

/**
 * SandBox provider for the entity variant registries, such as {@link Registries#CAT_VARIANT cat variants} or
 * {@link Registries#COW_VARIANT cow variants}.
 *
 * This provider must run in the {@link DatagenStage#REGISTRY_SETUP registry setup} stage.
 */
public abstract class VariantProviderBase extends RegistryProviderBase {

    protected VariantProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " variants";
    }

    /**
     * Registers an arbitrary value to the given registry. This can be used for variant registries that have no
     * dedicated factory method in this provider.
     *
     * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
     * added to the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
     * {@code public}, non-{@code static} field inside the provider.
     */
    public <T> Holder<T> variant(ResourceKey<? extends Registry<T>> registry, T variant) {
        return this.registries.writableRegistry(registry).createIntrusiveHolder(variant);
    }

    /**
     * Registers a {@link CatVariant cat variant}. The identifiers point at the texture without the
     * {@code textures/} prefix and the {@code .png} suffix.
     *
     * See {@link #variant(ResourceKey, Object)} for how the returned holder is registered.
     */
    public Holder<CatVariant> catVariant(Identifier texture, Identifier babyTexture, SpawnPrioritySelectors spawnConditions) {
        return this.variant(Registries.CAT_VARIANT, new CatVariant(texture(texture), texture(babyTexture), spawnConditions));
    }

    /**
     * Registers a {@link CowVariant cow variant}. The identifiers point at the texture without the
     * {@code textures/} prefix and the {@code .png} suffix.
     *
     * See {@link #variant(ResourceKey, Object)} for how the returned holder is registered.
     */
    public Holder<CowVariant> cowVariant(CowVariant.ModelType model, Identifier texture, Identifier babyTexture, SpawnPrioritySelectors spawnConditions) {
        return this.variant(Registries.COW_VARIANT, new CowVariant(new ModelAndTexture<>(model, texture(texture)), texture(babyTexture), spawnConditions));
    }

    /**
     * Registers a {@link PigVariant pig variant}. The identifiers point at the texture without the
     * {@code textures/} prefix and the {@code .png} suffix.
     *
     * See {@link #variant(ResourceKey, Object)} for how the returned holder is registered.
     */
    public Holder<PigVariant> pigVariant(PigVariant.ModelType model, Identifier texture, Identifier babyTexture, SpawnPrioritySelectors spawnConditions) {
        return this.variant(Registries.PIG_VARIANT, new PigVariant(new ModelAndTexture<>(model, texture(texture)), texture(babyTexture), spawnConditions));
    }

    /**
     * Registers a {@link ChickenVariant chicken variant}. The identifiers point at the texture without the
     * {@code textures/} prefix and the {@code .png} suffix.
     *
     * See {@link #variant(ResourceKey, Object)} for how the returned holder is registered.
     */
    public Holder<ChickenVariant> chickenVariant(ChickenVariant.ModelType model, Identifier texture, Identifier babyTexture, SpawnPrioritySelectors spawnConditions) {
        return this.variant(Registries.CHICKEN_VARIANT, new ChickenVariant(new ModelAndTexture<>(model, texture(texture)), texture(babyTexture), spawnConditions));
    }

    /**
     * Registers a {@link FrogVariant frog variant}. The identifier points at the texture without the
     * {@code textures/} prefix and the {@code .png} suffix.
     *
     * See {@link #variant(ResourceKey, Object)} for how the returned holder is registered.
     */
    public Holder<FrogVariant> frogVariant(Identifier texture, SpawnPrioritySelectors spawnConditions) {
        return this.variant(Registries.FROG_VARIANT, new FrogVariant(texture(texture), spawnConditions));
    }

    /**
     * Registers a {@link ZombieNautilusVariant zombie nautilus variant}. The identifier points at the texture
     * without the {@code textures/} prefix and the {@code .png} suffix.
     *
     * See {@link #variant(ResourceKey, Object)} for how the returned holder is registered.
     */
    public Holder<ZombieNautilusVariant> zombieNautilusVariant(ZombieNautilusVariant.ModelType model, Identifier texture, SpawnPrioritySelectors spawnConditions) {
        return this.variant(Registries.ZOMBIE_NAUTILUS_VARIANT, new ZombieNautilusVariant(new ModelAndTexture<>(model, texture(texture)), spawnConditions));
    }

    /**
     * Registers a {@link WolfVariant wolf variant}. Wolves use three textures per age, see
     * {@link #wolfAssets(Identifier, Identifier, Identifier)}.
     *
     * See {@link #variant(ResourceKey, Object)} for how the returned holder is registered.
     */
    public Holder<WolfVariant> wolfVariant(WolfVariant.AssetInfo assets, WolfVariant.AssetInfo babyAssets, SpawnPrioritySelectors spawnConditions) {
        return this.variant(Registries.WOLF_VARIANT, new WolfVariant(assets, babyAssets, spawnConditions));
    }

    /**
     * Creates the asset info for a {@link WolfVariant wolf variant}. The identifiers point at the texture
     * without the {@code textures/} prefix and the {@code .png} suffix.
     */
    public static WolfVariant.AssetInfo wolfAssets(Identifier wild, Identifier tame, Identifier angry) {
        return new WolfVariant.AssetInfo(texture(wild), texture(tame), texture(angry));
    }

    /**
     * Creates a client asset for the given texture. The identifier points at the texture without the
     * {@code textures/} prefix and the {@code .png} suffix.
     */
    public static ClientAsset.ResourceTexture texture(Identifier texture) {
        return new ClientAsset.ResourceTexture(texture);
    }

    /**
     * Spawn conditions that never match. A variant with these conditions is never chosen for a naturally
     * spawning entity, it can only be obtained explicitly, for example through commands.
     */
    public static SpawnPrioritySelectors spawnNever() {
        return SpawnPrioritySelectors.EMPTY;
    }

    /**
     * Spawn conditions that always match with the given priority. Used by vanilla with priority {@code 0} for
     * the default variant of an entity.
     */
    public static SpawnPrioritySelectors spawnAlways(int priority) {
        return SpawnPrioritySelectors.fallback(priority);
    }

    /**
     * Spawn conditions matching a single condition with the given priority. Of all variants whose conditions
     * match, one with the highest priority is chosen at random.
     */
    public static SpawnPrioritySelectors spawnWhen(SpawnCondition condition, int priority) {
        return SpawnPrioritySelectors.single(condition, priority);
    }

    /**
     * Spawn conditions matching entities spawning in the given biomes.
     */
    public SpawnPrioritySelectors spawnInBiomes(TagKey<Biome> biomes, int priority) {
        return this.spawnInBiomes(this.set(biomes), priority);
    }

    /**
     * Spawn conditions matching entities spawning in the given biomes.
     */
    public SpawnPrioritySelectors spawnInBiomes(HolderSet<Biome> biomes, int priority) {
        return spawnWhen(new BiomeCheck(biomes), priority);
    }

    /**
     * Spawn conditions matching entities spawning inside the given structures.
     */
    public SpawnPrioritySelectors spawnInStructures(TagKey<Structure> structures, int priority) {
        return this.spawnInStructures(this.set(structures), priority);
    }

    /**
     * Spawn conditions matching entities spawning inside the given structures.
     */
    public SpawnPrioritySelectors spawnInStructures(HolderSet<Structure> structures, int priority) {
        return spawnWhen(new StructureCheck(structures), priority);
    }

    /**
     * Spawn conditions built from multiple selectors, for variants that should spawn under more than one
     * condition. Use {@link #selector(SpawnCondition, int)} to create the selectors.
     */
    @SafeVarargs
    public static SpawnPrioritySelectors spawnConditions(PriorityProvider.Selector<SpawnContext, SpawnCondition>... selectors) {
        return new SpawnPrioritySelectors(List.of(selectors));
    }

    /**
     * Creates a single selector for {@link #spawnConditions(PriorityProvider.Selector[])}.
     */
    public static PriorityProvider.Selector<SpawnContext, SpawnCondition> selector(SpawnCondition condition, int priority) {
        return new PriorityProvider.Selector<>(condition, priority);
    }

    /**
     * Creates a single selector without a condition for {@link #spawnConditions(PriorityProvider.Selector[])}.
     * It matches everything with the given priority.
     */
    public static PriorityProvider.Selector<SpawnContext, SpawnCondition> selector(int priority) {
        return new PriorityProvider.Selector<>(priority);
    }
}
