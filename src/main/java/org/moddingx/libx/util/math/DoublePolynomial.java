package org.moddingx.libx.util.math;

import com.mojang.serialization.Codec;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

/**
 * A polynomial with double coefficients.
 */
public final class DoublePolynomial extends Polynomial<Double> implements DoubleUnaryOperator, DoubleFunction<Double> {
    
    public static final Codec<DoublePolynomial> CODEC = Codec.DOUBLE.listOf().xmap(
            doubles -> new DoublePolynomial(doubles.stream().dropWhile(d -> d == 0).mapToDouble(d -> d).toArray(), true),
            p -> p.coefficients.length == 0 ? List.of(0d) : Arrays.stream(p.coefficients).boxed().toList()
    );

    /**
     * The polynomial that is always zero.
     */
    public static final DoublePolynomial ZERO = new DoublePolynomial(0);

    /**
     * The polynomial that is always one.
     */
    public static final DoublePolynomial ONE = new DoublePolynomial(1);
    
    /**
     * The identity polynomial.
     */
    public static final DoublePolynomial IDENTITY = new DoublePolynomial(1, 0);
    
    private final double[] coefficients;
    private final boolean zero;
    private final boolean id;
    private List<Double> coefficientsView;

    private DoublePolynomial(double[] coefficients, boolean direct) {
        if (direct) {
            this.coefficients = coefficients;
        } else {
            int skip = 0;
            while (skip < coefficients.length && coefficients[skip] == 0) skip += 1;
            this.coefficients = new double[coefficients.length - skip];
            System.arraycopy(coefficients, skip, this.coefficients, 0, this.coefficients.length);
        }
        this.zero = this.coefficients.length == 0;
        this.id = this.coefficients.length == 2 && this.coefficients[0] == 1 && this.coefficients[1] == 0;
        this.coefficientsView = null;
    }

    /**
     * Creates a new polynomial with the given coefficients. {@code new DoublePolynomial(2,0,1)} gives for example the
     * polynomial {@code 2x²+1}.
     */
    public DoublePolynomial(double... coefficients) {
        this(coefficients, false);
    }

    @Override
    public List<Double> coefficients() {
        if (this.coefficientsView != null) return this.coefficientsView;
        this.coefficientsView = Arrays.stream(this.coefficients).boxed().toList();
        return this.coefficientsView;
    }
    
    /**
     * Computes the value of the polynomial at the given input.
     */
    @Override
    public double applyAsDouble(double x) {
        if (this.zero) return 0;
        if (this.id) return x;
        double pow = 1;
        double result = 0;
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
    public Double apply(double x) {
        return this.applyAsDouble(x);
    }

    /**
     * Computes the value of the polynomial at the given input.
     */
    @Override
    public Double apply(Double x) {
        return this.applyAsDouble(x);
    }

    @Override
    public DoublePolynomial derivative() {
        if (this.coefficients.length == 0) return this;
        double[] coefficients = new double[this.coefficients.length - 1];
        for (int i = 0; i < coefficients.length; i++) {
            coefficients[i] = this.coefficients[i] * (this.coefficients.length - i - 1);
        }
        // Direct constructor, there will never be leading zeros.
        return new DoublePolynomial(coefficients, true);
    }

    /**
     * Integrates this polynomial with constant part {@literal 0}.
     */
    public DoublePolynomial integrate() {
        return this.integrate(0);
    }

    /**
     * Integrates this polynomial with the given constant part.
     */
    public DoublePolynomial integrate(double constant) {
        double[] coefficients = new double[this.coefficients.length + 1];
        for (int i = 0; i < coefficients.length - 1; i++) {
            coefficients[i] = this.coefficients[i] / (this.coefficients.length - i);
        }
        coefficients[coefficients.length - 1] = constant;
        return new DoublePolynomial(coefficients, false);
    }

    @Override
    public DoublePolynomial negate() {
        if (this.coefficients.length == 0) return this;
        double[] coefficients = new double[this.coefficients.length];
        for (int i = 0; i < coefficients.length; i++) {
            coefficients[i] = -this.coefficients[i];
        }
        // Direct constructor, there will never be leading zeros.
        return new DoublePolynomial(coefficients, true);
    }

    @Override
    public DoublePolynomial add(Polynomial<Double> other) {
        double[] otherCoefficients = trustedCoefficients(other);
        double[] coefficients = new double[Math.max(this.coefficients.length, otherCoefficients.length)];
        int thisOffset = coefficients.length - this.coefficients.length;
        int otherOffset = coefficients.length - otherCoefficients.length;
        for (int i = 0; i < coefficients.length; i++) {
            double thisPart = (i - thisOffset) >= 0 ? this.coefficients[i - thisOffset] : 0;
            double otherPart = (i - otherOffset) >= 0 ? otherCoefficients[i - otherOffset] : 0;
            coefficients[i] = thisPart + otherPart;
        }
        return new DoublePolynomial(coefficients, false);
    }

    @Override
    public DoublePolynomial multiply(Polynomial<Double> other) {
        double[] otherCoefficients = trustedCoefficients(other);
        double[] coefficients = new double[this.coefficients.length + otherCoefficients.length];
        for (int i = 0; i < this.coefficients.length; i++) {
            for (int j = 0; j < otherCoefficients.length; j++) {
                int idx = coefficients.length - ((this.coefficients.length - i - 1) + (otherCoefficients.length - j - 1)) - 1;
                coefficients[idx] += (this.coefficients[i] * otherCoefficients[j]);
            }
        }
        return new DoublePolynomial(coefficients, false);
    }
    
    // Returns trusted arrays. Don't modify.
    private static double[] trustedCoefficients(Polynomial<Double> other) {
        if (other instanceof DoublePolynomial polynomial) {
            return polynomial.coefficients;
        } else {
            return other.coefficients().stream().mapToDouble(Double::valueOf).toArray();
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.coefficients);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DoublePolynomial other && Arrays.equals(this.coefficients, other.coefficients);
    }
}
