package io.github.noeppi_noeppi.libx.impl.commands;

import com.google.common.collect.Streams;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.util.text.*;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ModListCommand implements Command<CommandSourceStack> {

    private static final HoverEvent COPY_MODLIST = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("libx.misc.copy_modlist"));
    
    public final boolean detailed;

    public ModListCommand(boolean detailed) {
        this.detailed = detailed;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        Stream<MutableComponent> lineStream = ModList.get().getMods().stream()
                .sorted(Comparator.comparing(ModInfo::getDisplayName))
                .filter(mod -> !mod.getModId().equalsIgnoreCase("minecraft"))
                .map(mod -> "" 
                    + mod.getDisplayName()
                    + mod.getConfigElement("authors")
                        .map(a -> " (by " + a.toString().trim() + ")")
                        .orElse("")
                    + (this.detailed && !mod.getDescription().trim().isEmpty() ?
                        ": " + mod.getDescription().split("\n")[0].trim() : ""))
                .map(TextComponent::new);
        //noinspection UnstableApiUsage
        List<MutableComponent> lines = Streams.mapWithIndex(lineStream, (line, idx) -> line.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(idx % 2 == 0 ? 0xFFFF00 : 0xFF00F6))))
                .map(line -> line.withStyle(Style.EMPTY.withHoverEvent(COPY_MODLIST)))
                .collect(Collectors.toList());
        String copyToClipboard = lines.stream()
                .map(Component::getString)
                .collect(Collectors.joining("\n"));
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyToClipboard);
        lines.stream()
                .map(line -> line.withStyle(Style.EMPTY.withClickEvent(clickEvent)))
                .forEach(line -> context.getSource().sendSuccess(line, false));
        
        return 0;
    }
}
