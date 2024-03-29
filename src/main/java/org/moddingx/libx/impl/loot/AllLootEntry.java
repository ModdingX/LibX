package org.moddingx.libx.impl.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.moddingx.libx.LibX;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// A loot entry that merges multiple entries into one to be found in one roll.
public class AllLootEntry extends CompositeEntryBase {

    public static final ResourceLocation ID = LibX.getInstance().resource("all");
    public static final LootPoolEntryType TYPE = new LootPoolEntryType(CompositeEntryBase.createSerializer(AllLootEntry::new));

    public AllLootEntry(LootPoolEntryContainer[] children, LootItemCondition[] conditions) {
        super(children, conditions);
    }

    @Nonnull
    @Override
    public LootPoolEntryType getType() {
        return TYPE;
    }

    @Nonnull
    @Override
    protected ComposableEntryContainer compose(ComposableEntryContainer[] entries) {
        return switch (entries.length) {
            case 0 -> ALWAYS_TRUE;
            case 1 -> entries[0];
            default -> (ctx, consumer) -> {
                List<LootPoolEntry> list = new ArrayList<>();
                boolean success = false;
                for (ComposableEntryContainer entry : entries) {
                    if (entry.expand(ctx, list::add)) {
                        success = true;
                    }
                }
                if (list.size() == 1) {
                    consumer.accept(list.get(0));
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
