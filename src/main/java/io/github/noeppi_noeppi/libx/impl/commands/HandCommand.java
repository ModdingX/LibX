package io.github.noeppi_noeppi.libx.impl.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.noeppi_noeppi.libx.command.CommandUtil;
import io.github.noeppi_noeppi.libx.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Objects;

public class HandCommand implements Command<CommandSourceStack> {

    @Override
    public int run(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
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

        ResourceLocation id = Objects.requireNonNull(item.getRegistryName());
        MutableComponent tc = ComponentUtil.withCopyAction(new TextComponent(id.toString()), id.toString()).copy();

        if (count != 1) {
            tc = tc.append(new TextComponent(" ")).append(new TextComponent(Integer.toString(count)));
        }

        if (nbt != null && !nbt.isEmpty()) {
            List<Tag> printNBT = path == null ? List.of(nbt) : path.get(nbt);
            for (Tag element : printNBT) {
                tc = tc.append(new TextComponent(" ")).append(ComponentUtil.withCopyAction(NbtUtils.toPrettyComponent(element), element.toString()));
            }
        }

        ctx.getSource().sendSuccess(tc, true);

        return 0;
    }
}
