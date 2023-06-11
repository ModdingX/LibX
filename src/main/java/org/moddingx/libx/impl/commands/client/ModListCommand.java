package org.moddingx.libx.impl.commands.client;

import com.google.common.collect.Streams;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.moddingx.libx.util.game.ComponentUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModListCommand implements Command<CommandSourceStack> {
    
    public final boolean detailed;

    public ModListCommand(boolean detailed) {
        this.detailed = detailed;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        Stream<MutableComponent> lineStream = ModList.get().getMods().stream()
                .sorted(Comparator.comparing(IModInfo::getDisplayName))
                .filter(mod -> !mod.getModId().equalsIgnoreCase("minecraft"))
                .map(mod -> "" 
                    + mod.getDisplayName()
                    + mod.getConfig().getConfigElement("authors")
                        .map(a -> " (by " + a.toString().trim() + ")")
                        .orElse("")
                    + (this.detailed && !mod.getDescription().trim().isEmpty() ?
                        ": " + mod.getDescription().split("\n")[0].trim() : ""))
                .map(Component::literal);
        List<MutableComponent> lines = Streams.mapWithIndex(lineStream, (line, idx) -> Objects.requireNonNull(line).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(idx % 2 == 0 ? 0xBDBD28 : 0x8C4489)))).toList();
        String copyToClipboard = lines.stream()
                .map(Component::getString)
                .collect(Collectors.joining("\n"));
        lines.stream()
                .map(line -> ComponentUtil.withCopyAction(line, copyToClipboard))
                .forEach(line -> context.getSource().sendSuccess(() -> line, false));
        
        return 0;
    }
}
