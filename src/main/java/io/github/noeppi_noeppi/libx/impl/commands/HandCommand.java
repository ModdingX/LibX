package io.github.noeppi_noeppi.libx.impl.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.noeppi_noeppi.libx.command.CommandUtil;
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
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.Collections;
import java.util.List;

public class HandCommand implements Command<CommandSource> {
    private static final HoverEvent COPY_NAME = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("command.libx.copy_name"));
    private static final HoverEvent COPY_NBT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("command.libx.copy_nbt"));

    @Override
    public int run(CommandContext<CommandSource> ctx) throws CommandSyntaxException {

        ServerPlayerEntity player = ctx.getSource().asPlayer();
        ItemStack stack = player.getHeldItem(Hand.MAIN_HAND);

        Item item;
        int count;
        CompoundNBT nbt;

        if (stack.isEmpty()) {
            item = Items.AIR;
            count = 0;
            nbt = null;
        } else {
            item = stack.getItem();
            count = stack.getCount();
            nbt = stack.getTag();
        }

        NBTPathArgument.NBTPath path = CommandUtil.getArgumentOrDefault(ctx, "nbt_path", NBTPathArgument.NBTPath.class, null);

        @SuppressWarnings("ConstantConditions")
        IFormattableTextComponent tc = new StringTextComponent(item.getRegistryName().toString());
        Style copyName = Style.EMPTY.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, item.getRegistryName().toString()))
                .setHoverEvent(COPY_NAME);
        tc.setStyle(copyName);

        if (count != 1) {
            tc = tc.append(new StringTextComponent(" ")).append(new StringTextComponent(Integer.toString(count)));
        }

        if (nbt != null && !nbt.isEmpty()) {
            Style copyTag = Style.EMPTY.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbt.toString()))
                .setHoverEvent(COPY_NBT);
            List<INBT> printNBT = Collections.singletonList(nbt);
            if (path != null) {
                printNBT = path.func_218071_a(nbt);
            }

            for (INBT element : printNBT) {
                tc = tc.append(new StringTextComponent(" ")).append(NbtToTextComponent.toText(element).setStyle(copyTag));
            }
        }

        ctx.getSource().sendFeedback(tc, true);

        return 0;
    }
}
