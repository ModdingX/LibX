package io.github.noeppi_noeppi.libx.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

import java.util.Map;

/**
 * Translates JSON into {@link Component text components}.
 * 
 * @deprecated Moved to {@link ComponentUtil}
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.18.2")
public class JsonToText {

    private static final HoverEvent COPY_JSON = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("libx.misc.copy"));

    /**
     * This translates JSON to a colored text component.
     */
    public static MutableComponent toText(JsonElement element) {
        Style copyTag = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, element.toString())).withHoverEvent(COPY_JSON);
        return ComponentUtil.toPrettyComponent(element).copy().withStyle(copyTag);
    }
}
