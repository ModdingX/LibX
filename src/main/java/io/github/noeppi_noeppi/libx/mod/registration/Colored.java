package io.github.noeppi_noeppi.libx.mod.registration;

import com.google.common.collect.ImmutableMap;
import net.minecraft.item.DyeColor;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * A registerable that registers 16 things, one for each dye colour. This is
 * done via {@link Registerable#getNamedAdditionalRegisters()} so the color
 * names will be applied automatically.
 *
 * @param <T> The type of the thing to register.
 */
public class Colored<T> implements Registerable {

    public final T white;
    public final T orange;
    public final T magenta;
    public final T lightBlue;
    public final T yellow;
    public final T lime;
    public final T pink;
    public final T gray;
    public final T lightGray;
    public final T cyan;
    public final T purple;
    public final T blue;
    public final T brown;
    public final T green;
    public final T red;
    public final T black;

    /**
     * Creates a new instance of Colored.
     *
     * @param factory A factory function that creates the 16 things to be registered.
     */
    public Colored(Function<DyeColor, T> factory) {
        this.white = factory.apply(DyeColor.WHITE);
        this.orange = factory.apply(DyeColor.ORANGE);
        this.magenta = factory.apply(DyeColor.MAGENTA);
        this.lightBlue = factory.apply(DyeColor.LIGHT_BLUE);
        this.yellow = factory.apply(DyeColor.YELLOW);
        this.lime = factory.apply(DyeColor.LIME);
        this.pink = factory.apply(DyeColor.PINK);
        this.gray = factory.apply(DyeColor.GRAY);
        this.lightGray = factory.apply(DyeColor.LIGHT_GRAY);
        this.cyan = factory.apply(DyeColor.CYAN);
        this.purple = factory.apply(DyeColor.PURPLE);
        this.blue = factory.apply(DyeColor.BLUE);
        this.brown = factory.apply(DyeColor.BROWN);
        this.green = factory.apply(DyeColor.GREEN);
        this.red = factory.apply(DyeColor.RED);
        this.black = factory.apply(DyeColor.BLACK);
    }

    /**
     * Gets a value for a dye colour.
     */
    public T get(DyeColor color) {
        switch (color) {
            case ORANGE:
                return this.orange;
            case MAGENTA:
                return this.magenta;
            case LIGHT_BLUE:
                return this.lightBlue;
            case YELLOW:
                return this.yellow;
            case LIME:
                return this.lime;
            case PINK:
                return this.pink;
            case GRAY:
                return this.gray;
            case LIGHT_GRAY:
                return this.lightGray;
            case CYAN:
                return this.cyan;
            case PURPLE:
                return this.purple;
            case BLUE:
                return this.blue;
            case BROWN:
                return this.brown;
            case GREEN:
                return this.green;
            case RED:
                return this.red;
            case BLACK:
                return this.black;
            default:
                return this.white;
        }
    }

    @Override
    public Map<String, Object> getNamedAdditionalRegisters() {
        return ImmutableMap.<String, Object>builder()
                .put("white", this.white)
                .put("orange", this.orange)
                .put("magenta", this.magenta)
                .put("light_blue", this.lightBlue)
                .put("yellow", this.yellow)
                .put("lime", this.lime)
                .put("pink", this.pink)
                .put("gray", this.gray)
                .put("light_gray", this.lightGray)
                .put("cyan", this.cyan)
                .put("purple", this.purple)
                .put("blue", this.blue)
                .put("brown", this.brown)
                .put("green", this.green)
                .put("red", this.red)
                .put("black", this.black)
                .build();
    }

    /**
     * Gets a random object from the 16 coloured things.
     */
    public T random(Random random) {
        return this.get(DyeColor.byId(random.nextInt(DyeColor.values().length)));
    }
}
