package org.moddingx.libx.util.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Utilities for {@link Component text components}.
 */
public class ComponentUtil {

    private static final HoverEvent HOVER_COPY = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("libx.misc.copy"));
    
    /**
     * Gets a {@link Component text component} as a string formatted with ANSI escape codes to
     * be printed on the console.
     */
    public static String getConsoleString(Component tc) {
        StringBuilder sb = new StringBuilder();
        tc.visit((style, string) -> {
            reset(sb);
            formattingCodes(sb, style);
            sb.append(string);
            return Optional.empty();
        }, Style.EMPTY);
        reset(sb);
        return sb.toString();
    }
    
    private static void formattingCodes(StringBuilder sb, Style style) {
        if (style.getColor() != null) {
            int color = style.getColor().getValue();
            sb.append("\u001B[38;2;").append((color >> 16) & 0xFF).append(";").append((color >> 8) & 0xFF).append(";").append(color & 0xFF).append("m");
        }
        if (style.bold != null) {
            if (style.bold) {
                sb.append("\u001B[1m");
            } else {
                sb.append("\u001B[22m");
            }
        }
        if (style.italic != null) {
            if (style.italic) {
                sb.append("\u001B[3m");
            } else {
                sb.append("\u001B[23m");
            }
        }
        if (style.underlined != null) {
            if (style.underlined) {
                sb.append("\u001B[4m");
            } else {
                sb.append("\u001B[24m");
            }
        }
        if (style.strikethrough != null) {
            if (style.strikethrough) {
                sb.append("\u001B[9m");
            } else {
                sb.append("\u001B[29m");
            }
        }
        if (style.obfuscated != null) {
            if (style.obfuscated) {
                sb.append("\u001B[8m");
            } else {
                sb.append("\u001B[28m");
            }
        }
    }
    
    private static void reset(StringBuilder sb) {
        sb.append("\u001B[0m");
    }

    /**
     * Turns a {@link JsonElement} into a {@link Component} with syntax highlighting that can be used for display.
     */
    public static Component toPrettyComponent(JsonElement json) {
        if (json.isJsonNull()) {
            return Component.literal("null").withStyle(ChatFormatting.RED);
        } else if (json instanceof JsonPrimitive primitive) {
            if (primitive.isString()) {
                return Component.literal(primitive.toString()).withStyle(ChatFormatting.GREEN);
            } else {
                return Component.literal(primitive.toString()).withStyle(ChatFormatting.GOLD);
            }
        } else if (json instanceof JsonArray array) {
            MutableComponent tc = Component.literal("[");
            boolean first = true;
            for (JsonElement element : array) {
                if (first) {
                    first = false;
                } else {
                    tc.append(Component.literal(", "));
                }
                tc = tc.append(toPrettyComponent(element));
            }
            tc = tc.append(Component.literal("]"));
            return tc;
        } else if (json instanceof JsonObject object) {
            MutableComponent tc = Component.literal("{");
            boolean first = true;
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    tc.append(Component.literal(", "));
                }
                tc = tc.append(Component.literal(new JsonPrimitive(entry.getKey()).toString()).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(": "))
                        .append(toPrettyComponent(entry.getValue()));
            }
            tc = tc.append(Component.literal("}"));
            return tc;
        } else {
            throw new IllegalArgumentException("JSON type unknown: " + json.getClass());
        }
    }

    /**
     * Turns a {@link DataComponentMap} into a {@link Component} with syntax highlighting that can be used for display.
     *
     * @param registry The registry in which the data component types for the {@link DataComponentMap data component map} are registered.
     * @param registryAccess A {@link RegistryAccess registry access} to provide access to datapack registries when encoding data component values.
     */
    public static Component toPrettyComponent(ResourceKey<Registry<DataComponentType<?>>> registry, RegistryAccess registryAccess, DataComponentMap components) {
        return toPrettyComponent(registry, registryAccess, components.stream().collect(Collectors.toUnmodifiableMap(TypedDataComponent::type, tc -> Optional.of(tc.value()))));
    }

    /**
     * Turns a {@link DataComponentPatch} into a {@link Component} with syntax highlighting that can be used for display.
     *
     * @param registry The registry in which the data component types for the {@link DataComponentPatch data component patch} are registered.
     * @param registryAccess A {@link RegistryAccess registry access} to provide access to datapack registries when encoding data component values.
     */
    public static Component toPrettyComponent(ResourceKey<Registry<DataComponentType<?>>> registry, RegistryAccess registryAccess, DataComponentPatch patch) {
        return toPrettyComponent(registry, registryAccess, patch.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
    
    private static Component toPrettyComponent(ResourceKey<Registry<DataComponentType<?>>> registry, RegistryAccess registryAccess, Map<DataComponentType<?>, Optional<?>> map) {
        Registry<DataComponentType<?>> theRegistry = registryAccess.registry(registry).orElse(null);
        if (theRegistry == null) {
            //noinspection unchecked
            theRegistry = (Registry<DataComponentType<?>>) BuiltInRegistries.REGISTRY.get(registry.location());
        }
        if (theRegistry == null) throw new IllegalStateException("Registry not found: " + registry);
        DynamicOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, registryAccess);
        
        Map<ResourceLocation, DataComponentType<?>> typeMap = new HashMap<>();
        for (DataComponentType<?> type : map.keySet()) {
            if (type.isTransient()) continue;
            ResourceLocation id = theRegistry.getKey(type);
            if (id == null) throw new IllegalStateException("Unregistered data component type: " + type);
            typeMap.put(id, type);
        }

        MutableComponent cmp = Component.literal("[");
        boolean first = true;
        for (ResourceLocation typeId : typeMap.keySet().stream().sorted().toList()) {
            if (!first) cmp = cmp.append(", ");
            first = false;
            DataComponentType<?> type = typeMap.get(typeId);
            Optional<?> maybeValue = map.get(type);
            if (maybeValue.isEmpty()) {
                cmp.append(Component.literal("!"));
            }
            cmp = cmp.append(Component.literal(typeId.toString()).withStyle(ChatFormatting.DARK_RED));
            if (maybeValue.isPresent()) {
                cmp = cmp.append("=");
                //noinspection unchecked
                DataResult<Tag> result = ((Codec<Object>) type.codecOrThrow()).encodeStart(registryOps, maybeValue.get());
                if (result instanceof DataResult.Error<Tag>) {
                    return Component.literal("encoder error for " + typeId).withStyle(ChatFormatting.RED);
                }
                cmp = cmp.append(NbtUtils.toPrettyComponent(result.getOrThrow()));
            }
        }
        cmp = cmp.append(Component.literal("]"));
        return cmp;
    }

    /**
     * Adds a {@link ClickEvent click event} to the given component to copy the given text to clipboard.
     */
    public static Component withCopyAction(Component component, String copyText) {
        return component.copy().withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyText)).withHoverEvent(HOVER_COPY));
    }
    
    /**
     * Gets a sub-sequence from the given {@link FormattedCharSequence}. The sub sequence will include
     * all characters from {@code start} (inclusive) to the end of the sequence.
     */
    public static FormattedCharSequence subSequence(FormattedCharSequence text, int start) {
        if (start == 0) return text;
        return subSequence(text, start, Integer.MAX_VALUE - 1);
    }
    
    /**
     * Gets a sub-sequence from the given {@link FormattedCharSequence}. The sub sequence will include
     * all characters between {@code start} (inclusive) and {@code end} (exclusive). {@code start} and
     * {@code end} may not be negative but may be greater that the length of the sequence.
     */
    public static FormattedCharSequence subSequence(FormattedCharSequence text, int start, int end) {
        if (start < 0 || end < 0) throw new IllegalArgumentException("Negative bounds");
        if (end <= start) return FormattedCharSequence.EMPTY;
        return sink -> {
            AtomicInteger relOff = new AtomicInteger(0);
            AtomicInteger total = new AtomicInteger(0);
            return text.accept((relPosition, style, codePoint) -> {
                int current = total.getAndIncrement();
                if (current == start) {
                    relOff.set(-relPosition);
                } else if (relPosition == 0) {
                    // Next sequence, reset offset
                    relOff.set(0);
                }
                if (current >= start && current < end) {
                    return sink.accept(relPosition + relOff.get(), style, codePoint);
                } else {
                    return true;
                }
            });
        };
    }
}
