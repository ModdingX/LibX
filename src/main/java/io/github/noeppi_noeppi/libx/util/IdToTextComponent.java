package io.github.noeppi_noeppi.libx.util;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

/**
 * Translates {@link ResourceLocation resource locations} into {@link IFormattableTextComponent text components}.
 */
public class IdToTextComponent {

    private static final HoverEvent COPY_ID = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("libx.misc.copy_id"));

    /**
     * Gets the {@link ResourceLocation} as a {@link IFormattableTextComponent text component} with a click action that copies it into the clipboard.
     */
    public static IFormattableTextComponent toText(ResourceLocation id) {
        Style copyId = Style.EMPTY.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, id.toString())).setHoverEvent(COPY_ID);
        return new StringTextComponent(id.toString()).mergeStyle(copyId);
    }
}
