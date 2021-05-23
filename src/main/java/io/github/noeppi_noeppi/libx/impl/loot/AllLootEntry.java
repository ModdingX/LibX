package io.github.noeppi_noeppi.libx.impl.loot;

import io.github.noeppi_noeppi.libx.LibX;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// A loot entry that merges multiple entries into one to be found in one roll.
public class AllLootEntry extends ParentedLootEntry {

    public static final ResourceLocation ID = new ResourceLocation(LibX.getInstance().modid, "all");
    public static final LootPoolEntryType TYPE = new LootPoolEntryType(ParentedLootEntry.getSerializer(AllLootEntry::new));

    public AllLootEntry(LootEntry[] children, ILootCondition[] conditions) {
        super(children, conditions);
    }

    @Nonnull
    @Override
    public LootPoolEntryType getEntryType() {
        return TYPE;
    }

    @Nonnull
    @Override
    protected ILootEntry combineChildren(ILootEntry[] entries) {
        switch (entries.length) {
            case 0:
                return SUCCESS;
            case 1:
                return entries[0];
            default:
                return (ctx, consumer) -> {
                    List<ILootGenerator> list = new ArrayList<>();
                    boolean success = false;
                    for (ILootEntry entry : entries) {
                        if (entry.expand(ctx, list::add)) {
                            success = true;
                        }
                    }
                    if (list.size() == 1) {
                        consumer.accept(list.get(0));
                    } else if (!list.isEmpty()) {
                        // Just hand one entry to the parent consumer that will if picked call all entries from
                        // the children.
                        consumer.accept(new ILootGenerator() {
                            
                            @Override
                            public int getEffectiveWeight(float luck) {
                                int total = 0;
                                for (ILootGenerator gen : list) {
                                    total += gen.getEffectiveWeight(luck);
                                }
                                return total;
                            }

                            @Override
                            public void generateLoot(@Nonnull Consumer<ItemStack> stacks, @Nonnull LootContext ctx) {
                                for (ILootGenerator gen : list) {
                                    gen.generateLoot(stacks, ctx);
                                }
                            }
                        });
                    }
                    return success;
                };
        }
    }
}
