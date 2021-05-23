package io.github.noeppi_noeppi.libx.impl.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.noeppi_noeppi.libx.command.CommandUtil;
import io.github.noeppi_noeppi.libx.util.IdToTextComponent;
import io.github.noeppi_noeppi.libx.util.JsonToTextComponent;
import io.github.noeppi_noeppi.libx.util.NbtToJson;
import io.github.noeppi_noeppi.libx.util.NbtToTextComponent;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.NBTPathArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Collections;
import java.util.List;

public class HandCommand implements Command<CommandSource> {

    @Override
    public int run(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        NbtOutputType format = CommandUtil.getArgumentOrDefault(ctx, "output_format", NbtOutputType.class, NbtOutputType.NBT);

        ServerPlayerEntity player = ctx.getSource().asPlayer();
        ItemStack stack = player.getHeldItem(Hand.MAIN_HAND);

        Item item;
        int count;
        CompoundNBT nbt;

        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) {
            item = Items.AIR;
            count = 0;
            nbt = null;
        } else {
            item = stack.getItem();
            count = stack.getCount();
            nbt = stack.getTag();
        }

        NBTPathArgument.NBTPath path = CommandUtil.getArgumentOrDefault(ctx, "nbt_path", NBTPathArgument.NBTPath.class, null);

        //noinspection ConstantConditions
        IFormattableTextComponent tc = IdToTextComponent.toText(item.getRegistryName());

        if (count != 1) {
            tc = tc.appendSibling(new StringTextComponent(" ")).appendSibling(new StringTextComponent(Integer.toString(count)));
        }

        if (nbt != null && !nbt.isEmpty()) {
            List<INBT> printNBT = Collections.singletonList(nbt);
            if (path != null) {
                printNBT = path.findTags(nbt);
            }

            for (INBT element : printNBT) {
                tc = tc.appendSibling(new StringTextComponent(" "))
                        .appendSibling(format == NbtOutputType.NBT ?
                                NbtToTextComponent.toText(nbt) :
                                JsonToTextComponent.toText(NbtToJson.getJson(element, true)));
            }
        }

        ctx.getSource().sendFeedback(tc, true);

        return 0;
    }
}
