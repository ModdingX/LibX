package io.github.noeppi_noeppi.libx.impl.config.mappers.advanced;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.util.ResourceList;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public class ResourceListValueMapper implements ValueMapper<ResourceList, JsonObject> {

    public static final ResourceListValueMapper INSTANCE = new ResourceListValueMapper();

    private ResourceListValueMapper() {

    }

    private final List<String> COMMENT = ImmutableList.of(
            "This is a resource list. In the `whitelist` field you can specify whether all entries will be accepted by",
            "default or rejected.",
            "`elements` is an array of rules. Each resource location that is matched against this list, will traverse these",
            "rules from top to bottom. The first rule that matches a resource location determines its result.",
            "Rules are resource locations, where asterisks (*) can be added to match any number of characters.",
            "However an asterisk can not match a colon. The nly exception to this is the single asterisk which matches everything.",
            "When a rule is matched, it will yield the result specified in `whitelist` as a result. To alter this",
            "add a plus (+) or a minus (-) in front of the rule. This will make it a whitelist or blacklist rule respectively.",
            "You can also add regex rules. These are objects with two keys: `allow` - a boolean that specifies whether this",
            "is a whitelist or blacklist rule and `regex` - which is a regex that must match the resource location."
    );

    @Override
    public Class<ResourceList> type() {
        return ResourceList.class;
    }

    @Override
    public Class<JsonObject> element() {
        return JsonObject.class;
    }

    @Override
    public ResourceList fromJSON(JsonObject json) {
        return new ResourceList(json);
    }

    @Override
    public JsonObject toJSON(ResourceList value) {
        return value.toJSON();
    }

    @Override
    public ResourceList read(FriendlyByteBuf buffer) {
        return new ResourceList(buffer);
    }

    @Override
    public void write(ResourceList value, FriendlyByteBuf buffer) {
        value.write(buffer);
    }

    @Override
    public List<String> comment() {
        return this.COMMENT;
    }
}