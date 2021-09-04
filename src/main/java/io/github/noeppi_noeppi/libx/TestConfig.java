package io.github.noeppi_noeppi.libx;

import io.github.noeppi_noeppi.libx.config.Config;
import io.github.noeppi_noeppi.libx.config.validator.DoubleRange;
import io.github.noeppi_noeppi.libx.util.ResourceList;
import net.minecraft.world.InteractionResult;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class TestConfig {
    
    @Config("1") public static ResourceList test1 = ResourceList.WHITELIST;
    @Config("2") public static InteractionResult test2 = InteractionResult.PASS;
    @Config("3") public static Optional<String> test3 = Optional.of("abc");
    @Config("4") public static Pair<String, Boolean> test4 = Pair.of("Hello", false);
    @Config("5") public static Triple<String, Integer, List<Integer>> test5 = Triple.of("Wow", 42, List.of(1, 2, 3, 4, 5));
    @Config("6") public static UUID test6 = UUID.randomUUID();
    @DoubleRange(min = -1, max = 3)
    @Config("7") public static double test7 = 2;
    @Config("8") public static Map<String, Integer> test8 = Map.of(
            "key1", 1,
            "key2", 2,
            "key3", 3,
            "key4", 4,
            "key5", 5
    );
    @Config("9") public static TestRecord test9 = new TestRecord("Hello", 44, Optional.empty());
    
    public static record TestRecord(
            String something,
            int something_else,
            Optional<String> option
    ) {}
}
