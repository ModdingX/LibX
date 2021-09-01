package io.github.noeppi_noeppi.libx;

import io.github.noeppi_noeppi.libx.config.Config;
import io.github.noeppi_noeppi.libx.config.validator.DoubleRange;
import io.github.noeppi_noeppi.libx.config.validator.IntRange;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class TestConfig {
    
    @Config("1") public static boolean test1 = true;
    @Config("2") public static boolean test2 = false;
    @Config("3") public static Optional<String> test3 = Optional.of("abc");
    @Config("4") public static Optional<String> test4 = Optional.empty();
    @Config("5") public static int test5 = 0;
    @IntRange(min = 20, max = 50)
    @Config("6") public static int test6 = 42;
    @DoubleRange(min = -1, max = 3)
    @Config("7") public static double test7 = 2;
    @Config("8") public static double test8 = -44e12;
    @Config("9") public static TestRecord test9 = new TestRecord("Hello", 44, Optional.empty());
    
    public static record TestRecord(
            String something,
            int something_else,
            Optional<String> option
    ) {}
}
