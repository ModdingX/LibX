package org.moddingx.libx.util.math;

import com.mojang.serialization.Codec;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

/**
 * A polynomial with integer coefficients.
 */
public final class IntPolynomial extends Polynomial<Integer> implements IntUnaryOperator, IntFunction<Integer> {
    
    public static final Codec<IntPolynomial> CODEC = Codec.INT_STREAM.xmap(
            ints -> new IntPolynomial(ints.dropWhile(d -> d == 0).toArray(), true),
            p -> p.coefficients.length == 0 ? IntStream.of(0) : Arrays.stream(p.coefficients)
    );

    /**
     * The polynomial that is always zero.
     */
    public static final IntPolynomial ZERO = new IntPolynomial(0);

    /**
     * The polynomial that is always one.
     */
    public static final IntPolynomial ONE = new IntPolynomial(1);

    /**
     * The identity polynomial.
     */
    public static final IntPolynomial IDENTITY = new IntPolynomial(1, 0);
    
    private final int[] coefficients;
    private final boolean zero;
    private final boolean id;
    private List<Integer> coefficientsView;
    
    private IntPolynomial(int[] coefficients, boolean direct) {
        if (direct) {
            this.coefficients = coefficients;
        } else {
            int skip = 0;
            while (skip < coefficients.length && coefficients[skip] == 0) skip += 1;
            this.coefficients = new int[coefficients.length - skip];
            System.arraycopy(coefficients, skip, this.coefficients, 0, this.coefficients.length);
        }
        this.zero = this.coefficients.length == 0;
        this.id = this.coefficients.length == 2 && this.coefficients[0] == 1 && this.coefficients[1] == 0;
        this.coefficientsView = null;
    }

    /**
     * Creates a new polynomial with the given coefficients. {@code new IntPolynomial(2,0,1)} gives for example the
     * polynomial {@code 2x²+1}.
     */
    public IntPolynomial(int... coefficients) {
        this(coefficients, false);
    }

    @Override
    public List<Integer> coefficients() {
        if (this.coefficientsView != null) return this.coefficientsView;
        this.coefficientsView = Arrays.stream(this.coefficients).boxed().toList();
        return this.coefficientsView;
    }

    /**
     * Computes the value of the polynomial at the given input.
     */
    @Override
    public int applyAsInt(int x) {
        if (this.zero) return 0;
        if (this.id) return x;
        int pow = 1;
        int result = 0;
        for (int i = this.coefficients.length - 1; i >= 0; i--) {
            result += this.coefficients[i] * pow;
            pow *= x;
        }
        return result;
    }

    /**
     * Computes the value of the polynomial at the given input.
     */
    @Override
    public Integer apply(int x) {
        return this.applyAsInt(x);
    }

    /**
     * Computes the value of the polynomial at the given input.
     */
    @Override
    public Integer apply(Integer x) {
        return this.applyAsInt(x);
    }

    @Override
    public IntPolynomial derivative() {
        if (this.coefficients.length == 0) return this;
        int[] coefficients = new int[this.coefficients.length - 1];
        for (int i = 0; i < coefficients.length; i++) {
            coefficients[i] = this.coefficients[i] * (this.coefficients.length - i - 1);
        }
        // Direct constructor, there will never be leading zeros.
        return new IntPolynomial(coefficients, true);
    }

    @Override
    public IntPolynomial negate() {
        if (this.coefficients.length == 0) return this;
        int[] coefficients = new int[this.coefficients.length];
        for (int i = 0; i < coefficients.length; i++) {
            coefficients[i] = -this.coefficients[i];
        }
        // Direct constructor, there will never be leading zeros.
        return new IntPolynomial(coefficients, true);
    }

    @Override
    public IntPolynomial add(Polynomial<Integer> other) {
        int[] otherCoefficients = trustedCoefficients(other);
        int[] coefficients = new int[Math.max(this.coefficients.length, otherCoefficients.length)];
        int thisOffset = coefficients.length - this.coefficients.length;
        int otherOffset = coefficients.length - otherCoefficients.length;
        for (int i = 0; i < coefficients.length; i++) {
            int thisPart = (i - thisOffset) >= 0 ? this.coefficients[i - thisOffset] : 0;
            int otherPart = (i - otherOffset) >= 0 ? otherCoefficients[i - otherOffset] : 0;
            coefficients[i] = thisPart + otherPart;
        }
        return new IntPolynomial(coefficients, false);
    }

    @Override
    public IntPolynomial multiply(Polynomial<Integer> other) {
        int[] otherCoefficients = trustedCoefficients(other);
        int[] coefficients = new int[this.coefficients.length + otherCoefficients.length];
        for (int i = 0; i < this.coefficients.length; i++) {
            for (int j = 0; j < otherCoefficients.length; j++) {
                int idx = coefficients.length - ((this.coefficients.length - i - 1) + (otherCoefficients.length - j - 1)) - 1;
                coefficients[idx] += (this.coefficients[i] * otherCoefficients[j]);
            }
        }
        return new IntPolynomial(coefficients, false);
    }

    /**
     * Converts this polynomial to a {@link DoublePolynomial}.
     */
    public DoublePolynomial toDouble() {
        double[] coefficients = new double[this.coefficients.length];
        for (int i = 0; i < coefficients.length; i++) {
            coefficients[i] = this.coefficients[i];
        }
        return new DoublePolynomial(coefficients);
    }
    
    // Returns trusted arrays. Don't modify.
    private static int[] trustedCoefficients(Polynomial<Integer> other) {
        if (other instanceof IntPolynomial polynomial) {
            return polynomial.coefficients;
        } else {
            return other.coefficients().stream().mapToInt(Integer::valueOf).toArray();
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.coefficients);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IntPolynomial other && Arrays.equals(this.coefficients, other.coefficients);
    }
}
