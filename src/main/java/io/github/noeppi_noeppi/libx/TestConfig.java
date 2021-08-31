package io.github.noeppi_noeppi.libx;

import io.github.noeppi_noeppi.libx.config.Config;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class TestConfig {
    
    @Config("1") public static boolean test1 = true;
    @Config("2") public static boolean test2 = false;
    @Config("3") public static Optional<String> test3 = Optional.of("abc");
    @Config("4") public static Optional<String> test4 = Optional.empty();
    @Config("5") public static int test5 = 0;
    @Config("6") public static int test6 = 42;
    @Config("7") public static double test7 = 702;
    @Config("8") public static double test8 = -44e12;
    @Config("9") public static int test9 = 0;
}
