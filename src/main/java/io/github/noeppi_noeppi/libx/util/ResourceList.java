package io.github.noeppi_noeppi.libx.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.LibX;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import javax.annotation.RegEx;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A list of rules that will be applied one after another. The first rule that matches
 * a resource location determines the result.
 * The resource list can either be a white list or a black list. If it is a whitelist,
 * by default a matching rule will make the {@link #test(ResourceLocation) test} function
 * return true. If it's a blacklist it'll return false by default for matching rules.
 * For whitelists if no rule matches {@code false} is returned. For blacklists it's
 * {@code true}.
 * Rules on a whitelist can also make the {@code test} method false and the other
 * way round.
 * The order of the rules is important. Rules that are added first will have a higher
 * priority and only the first matching rule will be applied.
 * Resource lists are immutable.
 */
public class ResourceList {

    /**
     * A resource list that accepts every item.
     */
    public static final ResourceList WHITELIST = new ResourceList(true, b -> {});
    
    /**
     * A resource list that denies every item.
     */
    public static final ResourceList BLACKLIST = new ResourceList(false, b -> {});

    private static final WildcardString ANY = new WildcardString(List.of("*"));
    private static final WildcardString NAMESPACE_MC = new WildcardString(List.of("minecraft"));
    
    private final boolean whitelist;
    private final List<Rule> rules;

    /**
     * Creates a new resource list.
     * 
     * @param whitelist Whether this is a whitelist or a blacklist
     * @param rules A consumer that gets a {@code RuleBuilder} and should build the rules.
     */
    public ResourceList(boolean whitelist, Consumer<RuleBuilder> rules) {
        this.whitelist = whitelist;
        RuleBuilder builder = new RuleBuilder(whitelist);
        rules.accept(builder);
        this.rules = builder.rulesBuilderList.build();
    }

    /**
     * Reads a resource list from JSON.
     */
    public ResourceList(JsonObject json) {
        this.whitelist = !json.has("whitelist") || json.get("whitelist").getAsBoolean();
        if (!json.has("elements")) {
            throw new IllegalStateException("Resource list has no member 'elements': " + json);
        }
        if (!json.get("elements").isJsonArray()) {
            throw new IllegalStateException("Resource list has no array member 'elements': " + json.get("elements"));
        }
        JsonArray elements = json.get("elements").getAsJsonArray();
        ImmutableList.Builder<Rule> rules = ImmutableList.builder();
        for (JsonElement elem : elements) {
            try {
                rules.add(this.parseRule(this.whitelist, elem));
            } catch (IllegalStateException e) {
                LibX.logger.warn("Skipping invalid rule in resource list: " + e.getMessage());
            }
        }
        this.rules = rules.build();
    }

    /**
     * Reads a resource list from a {@link FriendlyByteBuf}.
     */
    public ResourceList(FriendlyByteBuf buffer) {
        this.whitelist = buffer.readBoolean();
        int ruleSize = buffer.readVarInt();
        ImmutableList.Builder<Rule> rules = ImmutableList.builder();
        for (int i = 0; i < ruleSize; i++) {
            rules.add(this.readRule(buffer));
        }
        this.rules = rules.build();
    }

    /**
     * Serialises this resource list to JSON.
     */
    public JsonObject toJSON() {
        JsonObject json = new JsonObject();
        json.addProperty("whitelist", this.whitelist);
        JsonArray array = new JsonArray();
        for (Rule rule : this.rules) {
            array.add(rule.toJSON());
        }
        json.add("elements", array);
        return json;
    }
    
    /**
     * Writes this resource list to a {@link FriendlyByteBuf}.
     */
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.whitelist);
        buffer.writeVarInt(this.rules.size());
        this.rules.forEach(rule -> rule.write(buffer));
    }

    /**
     * Tests whether the given {@link ResourceLocation} is on this resource list.
     */
    public boolean test(ResourceLocation rl) {
        for (Rule rule : this.rules) {
            Boolean value = rule.test(rl);
            if (value != null) {
                return value;
            }
        }
        return !this.whitelist;
    }

    private Rule parseRule(boolean whitelist, JsonElement json) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            String str = json.getAsJsonPrimitive().getAsString();
            return this.parseSimpleRule(whitelist, str);
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            boolean allow = obj.has("allow") ? obj.get("allow").getAsBoolean() : whitelist;
            if (!obj.has("regex")) {
                throw new IllegalStateException("Failed to build rule for resource list: JSON object has no member 'regex': " + json);
            }
            String regex = obj.get("regex").getAsString();
            return new RegexRule(allow, regex);
        } else {
            throw new IllegalStateException("Failed to build rule for resource list: JSON is not a string and not an object: " + json);
        }
    }
    
    private Rule readRule(FriendlyByteBuf buffer) {
        byte id = buffer.readByte();
        if (id == 0) {
            boolean allow = buffer.readBoolean();
            int namespaceSize = buffer.readVarInt();
            List<String> namespace = new ArrayList<>();
            for (int i = 0; i < namespaceSize; i++) {
                namespace.add(buffer.readUtf(32767));
            }
            int pathSize = buffer.readVarInt();
            List<String> path = new ArrayList<>();
            for (int i = 0; i < pathSize; i++) {
                path.add(buffer.readUtf(32767));
            }
            return new SimpleRule(allow, new WildcardString(namespace), new WildcardString(path));
        } else if (id == 1) {
            boolean allow = buffer.readBoolean();
            String regex = buffer.readUtf(32767);
            return new RegexRule(allow, regex);
        } else {
            throw new IllegalStateException("Invalid packet: Unknown rule id: " + id);
        }
    }
    
    private Rule parseSimpleRule(boolean whitelist, String str) {
        boolean allow;
        if (str.startsWith("+")) {
            allow = true;
            str = str.substring(1);
        } else if (str.startsWith("-")) {
            allow = false;
            str = str.substring(1);
        } else {
            allow = whitelist;
        }
        WildcardString namespace;
        WildcardString path;
        if (str.trim().equals("*")) {
            namespace = new WildcardString(List.of("*"));
            path = new WildcardString(parseString(str.substring(str.indexOf(':') + 1)));
        } else if (str.contains(":")) {
            if (str.indexOf(':') != str.lastIndexOf(':')) {
                throw new IllegalStateException("Failed to build rule for resource list: Invalid resource location: More than one colon." + str);
            }
            namespace = new WildcardString(parseString(str.substring(0, str.indexOf(':'))));
            path = new WildcardString(parseString(str.substring(str.indexOf(':') + 1)));
        } else {
            namespace = NAMESPACE_MC;
            path = new WildcardString(parseString(str));
        }
        return new SimpleRule(allow, namespace, path);
    }
    
    private static List<String> parseString(String str) {
        if (!ResourceLocation.isValidPath(str.replace("*", ""))) {
            throw new IllegalStateException("Failed to build rule for resource list: Invalid resource location identifier: " + str);
        }
        List<String> parts = new ArrayList<>();
        boolean lastWildcard = false;
        StringTokenizer t = new StringTokenizer(str, "*", true);
        while (t.hasMoreTokens()) {
            String elem = t.nextToken();
            boolean thisWildcard = elem.trim().equals("*");
            if (!elem.isEmpty() && !lastWildcard || !thisWildcard) {
                if (thisWildcard) {
                    parts.add("*");
                } else {
                    parts.add(elem);
                }
                lastWildcard = thisWildcard;
            }
        }
        return parts;
    }
    
    private interface Rule {

        Boolean test(ResourceLocation rl);
        JsonElement toJSON();
        void write(FriendlyByteBuf buffer);
    }

    private class SimpleRule implements Rule {

        private final boolean allow;
        private final WildcardString namespace;
        private final WildcardString path;

        public SimpleRule(boolean allow, WildcardString namespace, WildcardString path) {
            this.allow = allow;
            this.namespace = namespace;
            this.path = path;
        }

        @Nullable
        @Override
        public Boolean test(ResourceLocation rl) {
            if (this.namespace.matcher.get().test(rl.getNamespace())
                    && this.path.matcher.get().test(rl.getPath())) {
                return this.allow;
            } else {
                return null;
            }
        }

        @Override
        public JsonElement toJSON() {
            StringBuilder sb = new StringBuilder();
            if (this.allow != ResourceList.this.whitelist) {
                sb.append(this.allow ? "+" : "-");
            }
            if (this.namespace.fullWildcard && this.path.fullWildcard) {
                sb.append("*");
            } else {
                this.namespace.parts.forEach(sb::append);
                sb.append(":");
                this.path.parts.forEach(sb::append);
            }
            return new JsonPrimitive(sb.toString());
        }

        @Override
        public void write(FriendlyByteBuf buffer) {
            buffer.writeByte(0);
            buffer.writeBoolean(this.allow);
            buffer.writeVarInt(this.namespace.parts.size());
            this.namespace.parts.forEach(str -> buffer.writeUtf(str, 32767));
            buffer.writeVarInt(this.path.parts.size());
            this.path.parts.forEach(str -> buffer.writeUtf(str, 32767));
        }
    }
    
    private class RegexRule implements Rule {

        private final boolean allow;
        private final String regex;
        public final LazyValue<Predicate<String>> matcher;

        public RegexRule(boolean allow, String regex) {
            this.allow = allow;
            this.regex = regex;
            this.matcher = new LazyValue<>(() -> Pattern.compile(regex).asPredicate());
        }

        @Override
        public Boolean test(ResourceLocation rl) {
            return this.matcher.get().test(rl.toString()) ? this.allow : null;
        }

        @Override
        public JsonElement toJSON() {
            JsonObject json = new JsonObject();
            if (this.allow != ResourceList.this.whitelist) {
                json.addProperty("allow", this.allow);
            }
            json.addProperty("regex", this.regex);
            return json;
        }

        @Override
        public void write(FriendlyByteBuf buffer) {
            buffer.writeByte(1);
            buffer.writeBoolean(this.allow);
            buffer.writeUtf(this.regex, 32767);
        }
    }

    private static class WildcardString {

        public final List<String> parts;
        public final LazyValue<Predicate<String>> matcher;
        public final boolean fullWildcard;
        
        public WildcardString(List<String> parts) {
            //noinspection UnstableApiUsage
            ImmutableList<String> partList = parts.stream()
                    .map(String::trim)
                    .filter(str -> !str.isEmpty())
                    .collect(ImmutableList.toImmutableList());
            if (partList.isEmpty()) {
                this.parts = List.of("*");
            } else {
                this.parts = partList;
            }
            this.matcher = new LazyValue<>(
                    () -> Pattern.compile("^" + this.parts.stream()
                            .map(str -> str.equals("*") ? ".*" : Pattern.quote(str))
                            .collect(Collectors.joining()) + "$").asPredicate()
            );
            this.fullWildcard = this.parts.stream().allMatch(str -> str.equals("*"));
        }
    }

    /**
     * A builder for rules.
     */
    public class RuleBuilder {
        
        private final boolean whitelist;
        private final ImmutableList.Builder<Rule> rulesBuilderList;
        
        private RuleBuilder(boolean whitelist) {
            this.whitelist = whitelist;
            this.rulesBuilderList = ImmutableList.builder();
        }

        /**
         * Adds a simple rule that only matches the given {@link ResourceLocation}.
         * When this rule matches it will return the whitelist state of the resource list
         * as result.
         */
        public void simple(ResourceLocation rl) {
            this.simple(this.whitelist, rl);
        }
        
        /**
         * Adds a simple rule that only matches the given {@link ResourceLocation}.
         * When this rule matches it will return the value of {@code allow} as result.
         */
        public void simple(boolean allow, ResourceLocation rl) {
            this.rulesBuilderList.add(new SimpleRule(allow, new WildcardString(List.of(rl.getNamespace())), new WildcardString(List.of(rl.getPath()))));
        }

        /**
         * Parses a simple rule. This allows for a {@link ResourceLocation} string where asterisks (*) can
         * match any amount of characters in the {@link ResourceLocation}. However an asterisk can never
         * match a colon.
         * The special case '*' matches every {@link ResourceLocation}.
         * By default this will return the whitelist state of the resource list as result.
         * To change this prepend a plus (+) to make it return true or a minus (-) to make
         * it return false on a match.
         */
        public void parse(String rule) {
            this.rulesBuilderList.add(ResourceList.this.parseSimpleRule(this.whitelist, rule));
        }

        /**
         * Adds a rule that checks that a resource location matches a regex.
         * When this rule matches it will return the whitelist state of the resource list
         * as result.
         */
        public void regex(@RegEx String regex) {
            this.regex(this.whitelist, regex);
        }
        
        /**
         * Adds a rule that checks that a resource location matches a regex.
         * When this rule matches it will return the value of {@code allow} as result.
         */
        public void regex(boolean allow, @RegEx String regex) {
            this.rulesBuilderList.add(new RegexRule(allow, regex));
        }
    }
}
