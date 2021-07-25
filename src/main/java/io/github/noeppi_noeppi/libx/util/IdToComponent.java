package io.github.noeppi_noeppi.libx.util;

import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;

/**
 * Translates {@link ResourceLocation resource locations} into {@link MutableComponent text components}.
 */
public class IdToComponent {

    private static final HoverEvent COPY_ID = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("libx.misc.copy_id"));

    /**
     * Gets the {@link ResourceLocation} as a {@link MutableComponent text component} with a click action that copies it into the clipboard.
     */
    public static MutableComponent toText(ResourceLocation id) {
        Style copyId = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, id.toString())).withHoverEvent(COPY_ID);
        return new TextComponent(id.toString()).withStyle(copyId);
    }
}
