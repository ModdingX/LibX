package io.github.noeppi_noeppi.libx.data.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.loot.functions.CopyNbt;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * A base class for block loot provider. When overriding this you should call the {@code customLootTable} methods in
 * constructor to adjust the loot tables. Every block of you mod that is left untouched will get a default loot table.
 */
public class BlockLootProviderBase implements IDataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    protected final ModX mod;
    protected final DataGenerator generator;


    private final Set<Block> blacklist = new HashSet<>();
    private final Map<Block, Function<Block, LootTable.Builder>> functionMap = new HashMap<>();

    public BlockLootProviderBase(ModX mod, DataGenerator generator) {
        this.mod = mod;
        this.generator = generator;
    }

    /**
     * The given block will not be processed by this provider. Useful when you want to create the loot table manually.
     */
    protected void customLootTable(Block block) {
        this.blacklist.add(block);
    }

    /**
     * The given block will get the given loot table.
     */
    protected void customLootTable(Block block, LootTable.Builder loot) {
        this.functionMap.put(block, b -> loot);
    }

    /**
     * The given block will get the given loot table function.
     */
    protected void customLootTable(Block block, Function<Block, LootTable.Builder> loot) {
        this.functionMap.put(block, loot);
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " block loot tables";
    }

    @Override
    public void act(@Nonnull DirectoryCache cache) throws IOException {
        Map<ResourceLocation, LootTable.Builder> tables = new HashMap<>();

        for (ResourceLocation id : ForgeRegistries.BLOCKS.getKeys()) {
            Block block = ForgeRegistries.BLOCKS.getValue(id);
            if (block != null && this.mod.modid.equals(id.getNamespace()) && !this.blacklist.contains(block)) {
                Function<Block, LootTable.Builder> loot = this.functionMap.getOrDefault(block, BlockLootProviderBase::regular);
                tables.put(id, loot.apply(block));
            }
        }

        for (Map.Entry<ResourceLocation, LootTable.Builder> e : tables.entrySet()) {
            Path path = getPath(this.generator.getOutputFolder(), e.getKey());
            IDataProvider.save(GSON, cache, LootTableManager.toJson(e.getValue().setParameterSet(LootParameterSets.BLOCK).build()), path);
        }
    }

    private static Path getPath(Path root, ResourceLocation id) {
        return root.resolve("data/" + id.getNamespace() + "/loot_tables/blocks/" + id.getPath() + ".json");
    }

    /**
     * Creates an empty loot table for the given block.
     */
    public static LootTable.Builder empty(Block b) {
        return LootTable.builder();
    }

    /**
     * Creates a loot table that copies NBT-Data from a tile entity into the dropped item
     *
     * @param tags The toplevel tags of the tile entity to be copied.
     */
    public static LootTable.Builder copyNBT(Block b, String... tags) {
        LootEntry.Builder<?> entry = ItemLootEntry.builder(b);
        CopyNbt.Builder func = CopyNbt.builder(CopyNbt.Source.BLOCK_ENTITY);
        for (String tag : tags) {
            func = func.replaceOperation(tag, "BlockEntityTag." + tag);
        }
        LootPool.Builder pool = LootPool.builder().name("main").rolls(ConstantRange.of(1)).addEntry(entry)
                .acceptCondition(SurvivesExplosion.builder())
                .acceptFunction(func);
        return LootTable.builder().addLootPool(pool);
    }

    /**
     * Creates a default loot table for the given block.
     */
    public static LootTable.Builder regular(Block b) {
        LootEntry.Builder<?> entry = ItemLootEntry.builder(b);
        LootPool.Builder pool = LootPool.builder().name("main").rolls(ConstantRange.of(1)).addEntry(entry)
                .acceptCondition(SurvivesExplosion.builder());
        return LootTable.builder().addLootPool(pool);
    }
}
