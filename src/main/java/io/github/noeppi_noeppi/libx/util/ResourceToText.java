package io.github.noeppi_noeppi.libx.util;

import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;

/**
 * Translates {@link ResourceLocation resource locations} into {@link Component text components}.
 * 
 * @deprecated use a {@link TextComponent}. For the copy to clipboard on click, use {@link ComponentUtil#withCopyAction(Component, String)}
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.18.2")
public class ResourceToText {

    private static final HoverEvent COPY_ID = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("libx.misc.copy"));

    /**
     * Gets the {@link ResourceLocation} as a {@link MutableComponent mutable component} with a click action that copies it into the clipboard.
     */
    public static MutableComponent toText(ResourceLocation id) {
        Style copyId = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, id.toString())).withHoverEvent(COPY_ID);
        return new TextComponent(id.toString()).withStyle(copyId);
    }
}
