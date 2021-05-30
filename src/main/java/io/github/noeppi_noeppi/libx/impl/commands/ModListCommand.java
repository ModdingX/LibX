package io.github.noeppi_noeppi.libx.impl.commands;

import com.google.common.collect.Streams;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModListCommand implements Command<CommandSource> {

    private static final HoverEvent COPY_MODLIST = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("libx.misc.copy_modlist"));
    
    public final boolean detailed;

    public ModListCommand(boolean detailed) {
        this.detailed = detailed;
    }

    @Override
    public int run(CommandContext<CommandSource> context) {
        Stream<IFormattableTextComponent> lineStream = ModList.get().getMods().stream()
                .sorted(Comparator.comparing(ModInfo::getDisplayName))
                .filter(mod -> !mod.getModId().equalsIgnoreCase("minecraft"))
                .map(mod -> "" 
                    + mod.getDisplayName()
                    + mod.getConfigElement("authors")
                        .map(a -> " (by " + a.toString().trim() + ")")
                        .orElse("")
                    + (this.detailed && !mod.getDescription().trim().isEmpty() ?
                        ": " + mod.getDescription().split("\n")[0].trim() : ""))
                .map(StringTextComponent::new);
        //noinspection UnstableApiUsage
        List<IFormattableTextComponent> lines = Streams.mapWithIndex(lineStream, (line, idx) -> line.mergeStyle(Style.EMPTY.setColor(Color.fromInt(idx % 2 == 0 ? 0xFFFF00 : 0xFF00F6))))
                .map(line -> line.mergeStyle(Style.EMPTY.setHoverEvent(COPY_MODLIST)))
                .collect(Collectors.toList());
        String copyToClipboard = lines.stream()
                .map(ITextComponent::getString)
                .collect(Collectors.joining("\n"));
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyToClipboard);
        lines.stream()
                .map(line -> line.mergeStyle(Style.EMPTY.setClickEvent(clickEvent)))
                .forEach(line -> context.getSource().sendFeedback(line, false));
        
        return 0;
    }
}
