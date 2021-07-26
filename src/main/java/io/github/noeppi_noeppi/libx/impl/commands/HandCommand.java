package io.github.noeppi_noeppi.libx.impl.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.noeppi_noeppi.libx.command.CommandUtil;
import io.github.noeppi_noeppi.libx.util.ResourceToText;
import io.github.noeppi_noeppi.libx.util.JsonToText;
import io.github.noeppi_noeppi.libx.util.NbtToText;
import io.github.noeppi_noeppi.libx.util.NbtToJson;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.List;

public class HandCommand implements Command<CommandSourceStack> {

    @Override
    public int run(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        NbtOutputType format = CommandUtil.getArgumentOrDefault(ctx, "output_format", NbtOutputType.class, NbtOutputType.NBT);

        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);

        Item item;
        int count;
        CompoundTag nbt;

        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) {
            item = Items.AIR;
            count = 0;
            nbt = null;
        } else {
            item = stack.getItem();
            count = stack.getCount();
            nbt = stack.getTag();
        }

        NbtPathArgument.NbtPath path = CommandUtil.getArgumentOrDefault(ctx, "nbt_path", NbtPathArgument.NbtPath.class, null);

        //noinspection ConstantConditions
        MutableComponent tc = ResourceToText.toText(item.getRegistryName());

        if (count != 1) {
            tc = tc.append(new TextComponent(" ")).append(new TextComponent(Integer.toString(count)));
        }

        if (nbt != null && !nbt.isEmpty()) {
            List<Tag> printNBT = Collections.singletonList(nbt);
            if (path != null) {
                printNBT = path.get(nbt);
            }

            for (Tag element : printNBT) {
                tc = tc.append(new TextComponent(" "))
                        .append(format == NbtOutputType.NBT ?
                                NbtToText.toText(nbt) :
                                JsonToText.toText(NbtToJson.getJson(element, true)));
            }
        }

        ctx.getSource().sendSuccess(tc, true);

        return 0;
    }
}
