package org.moddingx.libx;

import net.minecraft.advancements.Advancement;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.moddingx.libx.command.EnumArgumentIgnoreCase;
import org.moddingx.libx.crafting.ingredient.EffectIngredient;
import org.moddingx.libx.crafting.ingredient.EmptyIngredient;
import org.moddingx.libx.datagen.DatagenSystem;
import org.moddingx.libx.impl.BlockEntityUpdateQueue;
import org.moddingx.libx.impl.InternalDataGen;
import org.moddingx.libx.impl.command.client.ClientCommandsImpl;
import org.moddingx.libx.impl.command.common.CommandsImpl;
import org.moddingx.libx.impl.config.ConfigEvents;
import org.moddingx.libx.impl.crafting.recipe.EmptyRecipe;
import org.moddingx.libx.impl.datapack.DynamicPackLocator;
import org.moddingx.libx.impl.loot.AllLootEntry;
import org.moddingx.libx.impl.loot.CopyBlockEntityDataFunction;
import org.moddingx.libx.impl.loot.modifier.AdditionLootModifier;
import org.moddingx.libx.impl.loot.modifier.RemovalLootModifier;
import org.moddingx.libx.impl.network.NetworkImpl;
import org.moddingx.libx.impl.render.BlockOverlayQuadCache;
import org.moddingx.libx.impl.sandbox.EmptySurfaceRule;
import org.moddingx.libx.impl.sandbox.density.*;
import org.moddingx.libx.inventory.StackItemHandler;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.render.ClientTickHandler;
import org.moddingx.libx.render.ItemStackRenderer;
import org.moddingx.libx.sandbox.SandBox;
import org.moddingx.libx.sandbox.generator.BiomeLayer;
import org.moddingx.libx.sandbox.generator.ExtendedNoiseChunkGenerator;
import org.moddingx.libx.sandbox.generator.LayeredBiomeSource;
import org.moddingx.libx.sandbox.placement.HeightPlacementFilter;
import org.moddingx.libx.sandbox.placement.InvertPlacementFilter;
import org.moddingx.libx.sandbox.structure.PoolExtension;
import org.moddingx.libx.sandbox.surface.BiomeSurface;
import org.moddingx.libx.sandbox.surface.SurfaceRuleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LibX instance class.
 */
@Mod("libx")
public final class LibX extends ModX {

    public static final Logger logger = LoggerFactory.getLogger("libx");

    private static LibX instance;
    private static CommonNetwork networkWrapper;

    public LibX(IEventBus modBus) {
        instance = this;
        NetworkImpl network = new NetworkImpl(this);
        networkWrapper = new CommonNetwork(network);

        // level stem needs to be in extension phase, so it can reference surface data.
        DatagenSystem.registerExtensionRegistry(Registries.LEVEL_STEM);
        DatagenSystem.registerExtensionRegistry(NeoForgeRegistries.Keys.BIOME_MODIFIERS);
        DatagenSystem.registerExtensionRegistry(NeoForgeRegistries.Keys.STRUCTURE_MODIFIERS);
        DatagenSystem.registerExtensionRegistry(SandBox.BIOME_SURFACE);
        DatagenSystem.registerExtensionRegistry(SandBox.SURFACE_RULE_SET);
        DatagenSystem.registerExtensionRegistry(SandBox.TEMPLATE_POOL_EXTENSION);
        DatagenSystem.defineDatagenRegistry(Registries.LOOT_TABLE, LootTable.DIRECT_CODEC);
        DatagenSystem.defineDatagenRegistry(Registries.RECIPE, Recipe.CODEC);
        DatagenSystem.defineDatagenRegistry(Registries.ADVANCEMENT, Advancement.CODEC);
        InternalDataGen.init();

        modBus.addListener(EventPriority.LOW, DynamicPackLocator::locatePacks);
        modBus.addListener(this::createRegistries);
        modBus.addListener(this::registerStuff);
        if (FMLLoader.getDist().isClient()) {
            modBus.addListener(BlockOverlayQuadCache::resourcesReload);
            modBus.addListener(ItemStackRenderer::registerSpecialModelRenderer);
        }

        NeoForge.EVENT_BUS.addListener(ClientTickHandler::tick);
        NeoForge.EVENT_BUS.addListener(BlockEntityUpdateQueue::tick);
        NeoForge.EVENT_BUS.addListener(CommandsImpl::registerCommands);
        NeoForge.EVENT_BUS.register(new ConfigEvents());
        if (FMLLoader.getDist().isClient()) {
            NeoForge.EVENT_BUS.addListener(ClientCommandsImpl::registerClientCommands);
        }
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        //noinspection unchecked,rawtypes
        ArgumentTypeInfos.registerByClass((Class) EnumArgumentIgnoreCase.class, EnumArgumentIgnoreCase.Info.INSTANCE);
    }

    @Override
    protected void clientSetup(FMLClientSetupEvent event) {
        //
    }

    /**
     * Gets the LibX instance.
     */
    public static LibX getInstance() {
        return instance;
    }

    /**
     * Gets the network implementation of LibX.
     */
    public static CommonNetwork getNetwork() {
        return networkWrapper;
    }

    private void createRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(SandBox.SURFACE_RULE_SET, SurfaceRuleSet.DIRECT_CODEC, null);
        event.dataPackRegistry(SandBox.BIOME_SURFACE, BiomeSurface.DIRECT_CODEC, null);
        event.dataPackRegistry(SandBox.BIOME_LAYER, BiomeLayer.DIRECT_CODEC, null);
        event.dataPackRegistry(SandBox.TEMPLATE_POOL_EXTENSION, PoolExtension.DIRECT_CODEC, null);
    }

    private void registerStuff(RegisterEvent event) {
        event.register(Registries.LOOT_POOL_ENTRY_TYPE, AllLootEntry.ID, () -> AllLootEntry.TYPE);
        event.register(Registries.LOOT_FUNCTION_TYPE, CopyBlockEntityDataFunction.ID, () -> CopyBlockEntityDataFunction.TYPE);
        event.register(NeoForgeRegistries.Keys.INGREDIENT_TYPES, this.resource("effect"), () -> EffectIngredient.TYPE);
        event.register(NeoForgeRegistries.Keys.INGREDIENT_TYPES, this.resource("empty"), () -> EmptyIngredient.TYPE);
        event.register(Registries.RECIPE_TYPE, EmptyRecipe.ID, () -> EmptyRecipe.TYPE);
        event.register(Registries.RECIPE_SERIALIZER, EmptyRecipe.ID, () -> EmptyRecipe.Serializer.INSTANCE);
        event.register(Registries.COMMAND_ARGUMENT_TYPE, this.resource("enum"), () -> EnumArgumentIgnoreCase.Info.INSTANCE);
        event.register(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, this.resource("addition"), () -> AdditionLootModifier.CODEC);
        event.register(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, this.resource("removal"), () -> RemovalLootModifier.CODEC);
        event.register(Registries.MATERIAL_RULE, this.resource("empty"), () -> EmptySurfaceRule.CODEC);
        event.register(Registries.CHUNK_GENERATOR, this.resource("noise"), () -> ExtendedNoiseChunkGenerator.CODEC);
        event.register(Registries.BIOME_SOURCE, this.resource("layered"), () -> LayeredBiomeSource.CODEC);
        event.register(Registries.PLACEMENT_MODIFIER_TYPE, this.resource("inverted"), () -> InvertPlacementFilter.TYPE);
        event.register(Registries.PLACEMENT_MODIFIER_TYPE, this.resource("height_filter"), () -> HeightPlacementFilter.TYPE);
        event.register(Registries.DENSITY_FUNCTION_TYPE, this.resource("smash"), DensitySmash.CODEC::codec);
        event.register(Registries.DENSITY_FUNCTION_TYPE, this.resource("influence"), DensityInfluence.CODEC::codec);
        event.register(Registries.DENSITY_FUNCTION_TYPE, this.resource("debug"), DensityDebug.CODEC::codec);
        event.register(Registries.DENSITY_FUNCTION_TYPE, this.resource("lerp"), DensityLerp.CODEC::codec);
        event.register(Registries.DENSITY_FUNCTION_TYPE, this.resource("clamp"), DensityClamp.CODEC::codec);
        event.register(Registries.DATA_COMPONENT_TYPE, this.resource("inventory"), () -> StackItemHandler.INVENTORY_DATA);
    }
}
