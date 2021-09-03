package io.github.noeppi_noeppi.libx;

import io.github.noeppi_noeppi.libx.config.Config;
import io.github.noeppi_noeppi.libx.config.validator.DoubleRange;
import io.github.noeppi_noeppi.libx.config.validator.IntRange;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionResult;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class TestConfig {
    
    @Config("1") public static ChatFormatting test1 = ChatFormatting.RESET;
    @Config("2") public static InteractionResult test2 = InteractionResult.PASS;
    @Config("3") public static Optional<String> test3 = Optional.of("abc");
    @Config("4") public static Optional<String> test4 = Optional.empty();
    @Config("5") public static int test5 = 0;
    @IntRange(min = 20, max = 50)
    @Config("6") public static int test6 = 42;
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
