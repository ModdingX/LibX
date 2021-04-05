package io.github.noeppi_noeppi.libx.impl.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.github.noeppi_noeppi.libx.command.CommandUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ModListCommand implements Command<CommandSource> {

    private static final HoverEvent COPY_MODLIST = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("libx.misc.copy_modlist"));


    @Override
    public int run(CommandContext<CommandSource> context) {
        boolean extended = CommandUtil.getArgumentOrDefault(context, "extended", Boolean.class, false);

        AtomicInteger i = new AtomicInteger();
        IFormattableTextComponent component = new StringTextComponent("");
        List<ModInfo> mods = new ArrayList<>(ModList.get().getMods());
        mods.sort(Comparator.comparing(ModInfo::getDisplayName));

        mods.forEach(modInfo -> {
            if (!modInfo.getModId().equalsIgnoreCase("minecraft")) {
                if (i.get() != 0) {
                    component.appendString("\n");
                }

                StringBuilder builder = new StringBuilder();

                builder.append(modInfo.getDisplayName());
                modInfo.getConfigElement("authors").ifPresent(authors -> {
                    builder.append(" (by ");
                    builder.append(authors.toString().trim());
                    builder.append(")");
                });
                if (extended && !modInfo.getDescription().isEmpty()) {
                    builder.append(": ");
                    builder.append(modInfo.getDescription().split("\n")[0].trim());
                }

                component.append(new StringTextComponent(builder.toString()).mergeStyle(i.get() % 2 == 0 ? Style.EMPTY.setColor(Color.fromInt(0xFFFF00)) : Style.EMPTY.setColor(Color.fromInt(0xFF00F6))));
                i.getAndIncrement();
            }
        });

        component.mergeStyle(Style.EMPTY.setHoverEvent(COPY_MODLIST)
                .setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, component.getString())));
        context.getSource().sendFeedback(component, false);
        return 1;
    }
}
