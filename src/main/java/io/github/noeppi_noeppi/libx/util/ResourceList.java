package io.github.noeppi_noeppi.libx.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
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
 * A {@link Predicate} for {@link ResourceLocation resource locations} implemented as
 * a list of rules that will be applied one after another. The first rule that matches
 * a resource location determines the result.
 * The resource list can either be a white list or a black list. If it is an allow list,
 * by default a matching rule will make the {@link #test(ResourceLocation) test} function
 * return true. If it's a deny list it'll return false by default for matching rules.
 * For allow lists if no rule matches {@code false} is returned. For deny lists it's
 * {@code true}.
 * Rules on an allow list can also make the {@code test} method false and the other
 * way round.
 * The order of the rules is important. Rules that are added first will have a higher
 * priority and only the first matching rule will be applied.
 * Resource lists are immutable.
 * 
 * <a name="use_resource_lists_in_configs"></a>
 * 
 * <h3>ResourceLists in LibX configs</h3>
 * 
 * This explains, how a resource list is used inj a config. In the {@code allowList} field you can specify
 * whether all entries will be accepted by default or rejected.
 * 
 * {@code elements} is an array of rules. Each resource location that is matched against this list, will
 * traverse these rules from top to bottom. The first rule that matches a resource location determines its result.
 * 
 * Rules are resource locations, where asterisks (*) can be added to match any number of characters.
 * However, an asterisk can not match a colon. The nly exception to this is the single asterisk which matches
 * everything. When a rule is matched, it will yield the result specified in `allowList` as a result. To alter
 * this, add a plus (+) or a minus (-) in front of the rule. This will make it a allow or deny rule
 * respectively. You can also add regex rules. These are json objects with two keys: `allow` - a boolean that
 * specifies whether this is an allow or a deny rule and `regex` - which is a regex that must match the
 * resource location.
 */
public class ResourceList implements Predicate<ResourceLocation> {

    /**
     * A resource list that accepts every item.
     */
    public static final ResourceList ALLOW_LIST = new ResourceList(true, b -> {});
    
    /**
     * A resource list that denies every item.
     */
    public static final ResourceList DENY_LIST = new ResourceList(false, b -> {});
    
    private static final WildcardString NAMESPACE_MC = new WildcardString(List.of("minecraft"));
    
    private final boolean allowList;
    private final List<Rule> rules;

    /**
     * Creates a new resource list.
     * 
     * @param allowList Whether this is an allow list or a deny list
     * @param rules A consumer that gets a {@code RuleBuilder} and should build the rules.
     */
    public ResourceList(boolean allowList, Consumer<RuleBuilder> rules) {
        this.allowList = allowList;
        RuleBuilder builder = new RuleBuilder();
        rules.accept(builder);
        this.rules = builder.rulesBuilderList.build();
    }

    /**
     * Reads a resource list from JSON.
     */
    public ResourceList(JsonObject json) {
        this.allowList = !json.has("allow_list") || json.get("allow_list").getAsBoolean();
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
                rules.add(this.parseRule(elem));
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
        this.allowList = buffer.readBoolean();
        int ruleSize = buffer.readVarInt();
        ImmutableList.Builder<Rule> rules = ImmutableList.builder();
        for (int i = 0; i < ruleSize; i++) {
            rules.add(this.ruleFromNetwork(buffer));
        }
        this.rules = rules.build();
    }

    /**
     * Serialises this resource list to JSON.
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("allow_list", this.allowList);
        JsonArray array = new JsonArray();
        for (Rule rule : this.rules) {
            array.add(rule.toJson());
        }
        json.add("elements", array);
        return json;
    }
    
    /**
     * Writes this resource list to a {@link FriendlyByteBuf}.
     */
    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.allowList);
        buffer.writeVarInt(this.rules.size());
        this.rules.forEach(rule -> rule.toNetwork(buffer));
    }

    /**
     * Gets whether this ResourceList is an allow list or a deny list.
     */
    public boolean isAllowList() {
        return this.allowList;
    }

    /**
     * Gets a list of {@link RuleEntry rule entries} for this ResourceList.
     */
    public List<RuleEntry> getRules() {
        return this.rules.stream().map(Rule::getEntry).toList();
    }

    /**
     * Tests whether the given {@link ResourceLocation} is on this resource list.
     */
    @Override
    public boolean test(ResourceLocation rl) {
        for (Rule rule : this.rules) {
            Boolean value = rule.test(rl);
            if (value != null) {
                return value;
            }
        }
        return !this.allowList;
    }

    private Rule parseRule(JsonElement json) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            String str = json.getAsJsonPrimitive().getAsString();
            return this.parseSimpleRule(str);
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            Boolean allow = obj.has("allow") ? obj.get("allow").getAsBoolean() : null;
            if (!obj.has("regex")) {
                throw new IllegalStateException("Failed to build rule for resource list: JSON object has no member 'regex': " + json);
            }
            String regex = obj.get("regex").getAsString();
            return new RegexRule(allow, regex);
        } else {
            throw new IllegalStateException("Failed to build rule for resource list: JSON is not a string and not an object: " + json);
        }
    }
    
    private Rule ruleFromNetwork(FriendlyByteBuf buffer) {
        byte id = buffer.readByte();
        if (id == 0) {
            Boolean allow = switch (buffer.readByte()) {
                case 0 -> false;
                case 1 -> true;
                default -> null;
            };
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
            Boolean allow = switch (buffer.readByte()) {
                case 0 -> false;
                case 1 -> true;
                default -> null;
            };
            String regex = buffer.readUtf(32767);
            return new RegexRule(allow, regex);
        } else {
            throw new IllegalStateException("Invalid packet: Unknown rule id: " + id);
        }
    }
    
    private Rule parseSimpleRule(String str) {
        Boolean allow;
        if (str.startsWith("+")) {
            allow = true;
            str = str.substring(1);
        } else if (str.startsWith("-")) {
            allow = false;
            str = str.substring(1);
        } else {
            allow = null;
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
    
    private sealed interface Rule permits SimpleRule, RegexRule {

        Boolean test(ResourceLocation rl);
        JsonElement toJson();
        void toNetwork(FriendlyByteBuf buffer);
        RuleEntry getEntry();
    }

    private final class SimpleRule implements Rule {

        @Nullable
        private final Boolean allow;
        private final WildcardString namespace;
        private final WildcardString path;

        public SimpleRule(@Nullable Boolean allow, WildcardString namespace, WildcardString path) {
            this.allow = allow;
            this.namespace = namespace;
            this.path = path;
        }

        @Nullable
        @Override
        public Boolean test(ResourceLocation rl) {
            if (this.namespace.matcher.get().test(rl.getNamespace())
                    && this.path.matcher.get().test(rl.getPath())) {
                return this.allow == null ? ResourceList.this.allowList : this.allow;
            } else {
                return null;
            }
        }

        @Override
        public JsonElement toJson() {
            StringBuilder sb = new StringBuilder();
            if (this.allow != null) {
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
        public void toNetwork(FriendlyByteBuf buffer) {
            buffer.writeByte(0);
            if (this.allow == null) {
                buffer.writeByte(-1);
            } else {
                buffer.writeByte(this.allow ? 1 : 0);
            }
            buffer.writeVarInt(this.namespace.parts.size());
            this.namespace.parts.forEach(str -> buffer.writeUtf(str, 32767));
            buffer.writeVarInt(this.path.parts.size());
            this.path.parts.forEach(str -> buffer.writeUtf(str, 32767));
        }

        @Override
        public RuleEntry getEntry() {
            StringBuilder sb = new StringBuilder();
            if (this.namespace.fullWildcard && this.path.fullWildcard) {
                sb.append("*");
            } else {
                this.namespace.parts.forEach(sb::append);
                sb.append(":");
                this.path.parts.forEach(sb::append);
            }
            return new RuleEntry(sb.toString(), false, this.allow);
        }
    }
    
    private final class RegexRule implements Rule {

        private final Boolean allow;
        private final String regex;
        public final LazyValue<Predicate<String>> matcher;

        public RegexRule(@Nullable Boolean allow, String regex) {
            this.allow = allow;
            this.regex = regex;
            this.matcher = new LazyValue<>(() -> Pattern.compile(regex).asMatchPredicate());
        }

        @Override
        public Boolean test(ResourceLocation rl) {
            if (this.matcher.get().test(rl.toString())) {
                return this.allow == null ? ResourceList.this.allowList : this.allow;
            } else {
                return null;
            }
        }

        @Override
        public JsonElement toJson() {
            JsonObject json = new JsonObject();
            if (this.allow != null) {
                json.addProperty("allow", this.allow);
            }
            json.addProperty("regex", this.regex);
            return json;
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer) {
            buffer.writeByte(1);
            if (this.allow == null) {
                buffer.writeByte(-1);
            } else {
                buffer.writeByte(this.allow ? 1 : 0);
            }
            buffer.writeUtf(this.regex, 32767);
        }

        @Override
        public RuleEntry getEntry() {
            return new RuleEntry(this.regex, true, this.allow);
        }
    }

    private static class WildcardString {

        public final List<String> parts;
        public final LazyValue<Predicate<String>> matcher;
        public final boolean fullWildcard;
        
        public WildcardString(List<String> parts) {
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
                            .collect(Collectors.joining()) + "$").asMatchPredicate()
            );
            this.fullWildcard = this.parts.stream().allMatch(str -> str.equals("*"));
        }
    }

    /**
     * A builder for rules.
     */
    public class RuleBuilder {
        
        private final ImmutableList.Builder<Rule> rulesBuilderList;
        
        private RuleBuilder() {
            this.rulesBuilderList = ImmutableList.builder();
        }

        /**
         * Adds a simple rule that only matches the given {@link ResourceLocation}.
         * When this rule matches it will return the allow list state of the resource list
         * as result.
         */
        public void simple(ResourceLocation rl) {
            this.rulesBuilderList.add(new SimpleRule(null, new WildcardString(List.of(rl.getNamespace())), new WildcardString(List.of(rl.getPath()))));
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
         * By default this will return the allow list state of the resource list as result.
         * To change this prepend a plus (+) to make it return true or a minus (-) to make
         * it return false on a match.
         */
        public void parse(String rule) {
            this.rulesBuilderList.add(ResourceList.this.parseSimpleRule(rule));
        }

        /**
         * Adds a rule that checks that a resource location matches a regex.
         * When this rule matches it will return the allow list state of the resource list
         * as result.
         */
        public void regex(@RegEx String regex) {
            this.rulesBuilderList.add(new RegexRule(null, regex));
        }
        
        /**
         * Adds a rule that checks that a resource location matches a regex.
         * When this rule matches it will return the value of {@code allow} as result.
         */
        public void regex(boolean allow, @RegEx String regex) {
            this.rulesBuilderList.add(new RegexRule(allow, regex));
        }
    }

    /**
     * And entry that represents a rule in this {@link ResourceList}.
     * This is only meant to give access to the values in a resource list, it can't
     * be used to build new rules.
     */
    public record RuleEntry(String value, boolean regex, @Nullable Boolean allow) {}
}
