package org.moddingx.libx.impl.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.moddingx.libx.LibX;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// A loot entry that merges multiple entries into one to be found in one roll.
public class AllLootEntry extends CompositeEntryBase {

    public static final Identifier ID = LibX.getInstance().id("all");
    public static final MapCodec<AllLootEntry> MAP_CODEC = createCodec(AllLootEntry::new);

    public AllLootEntry(List<LootPoolEntryContainer> children, List<LootItemCondition> conditions) {
        super(children, conditions);
    }

    @Nonnull
    @Override
    public MapCodec<AllLootEntry> codec() {
        return MAP_CODEC;
    }

    @Nonnull
    @Override
    protected ComposableEntryContainer compose(@Nonnull List<? extends ComposableEntryContainer> children) {
        return switch (children.size()) {
            case 0 -> ALWAYS_TRUE;
            case 1 -> children.getFirst();
            default -> (ctx, consumer) -> {
                List<LootPoolEntry> list = new ArrayList<>();
                boolean success = false;
                for (ComposableEntryContainer entry : children) {
                    if (entry.expand(ctx, list::add)) {
                        success = true;
                    }
                }
                if (list.size() == 1) {
                    consumer.accept(list.getFirst());
                } else if (!list.isEmpty()) {
                    // Just hand one entry to the parent consumer that will if picked call all entries from
                    // the children.
                    consumer.accept(new LootPoolEntry() {

                        @Override
                        public int getWeight(float luck) {
                            int total = 0;
                            for (LootPoolEntry gen : list) {
                                total += gen.getWeight(luck);
                            }
                            return total;
                        }

                        @Override
                        public void createItemStack(@Nonnull Consumer<ItemStack> stackConsumer, @Nonnull LootContext lootContext) {
                            for (LootPoolEntry gen : list) {
                                gen.createItemStack(stackConsumer, lootContext);
                            }
                        }
                    });
                }
                return success;
            };
        };
    }
}
