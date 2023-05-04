package org.moddingx.libx.util.math;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Represents a polynomial with coefficients of type {@code T}.
 */
public abstract class Polynomial<T extends Number> implements UnaryOperator<T> {
    
    private String stringValue;
    
    protected Polynomial() {
        this.stringValue = null;
    }
    
    /**
     * Gets the coefficients of this polynomial. For example, the coefficients {@code [2, 0 1]} represent the
     * polynomial {@code 2x²+1}.
     */
    public abstract List<T> coefficients();

    @Override
    public String toString() {
        if (this.stringValue != null) return this.stringValue;
        List<T> coefficients = this.coefficients();
        if (coefficients.isEmpty()) {
            this.stringValue = "0";
        } else {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (int i = 0; i < coefficients.size(); i++) {
                if (coefficients.get(i).doubleValue() == 0) continue;
                if (!first && coefficients.get(i).doubleValue() >= 0) sb.append("+");
                first = false;
                int exp = coefficients.size() - i - 1;
                if (exp == 0) {
                    sb.append(this.coefficients().get(i).toString());
                } else {
                    if (coefficients.get(i).doubleValue() != 1) sb.append(coefficients.get(i).toString());
                    sb.append("x");
                    if (exp != 1) sb.append(Integer.toString(exp)
                            .replace('-', '⁻').replace('0', '⁰').replace('1', '¹')
                            .replace('2', '²').replace('3', '³').replace('4', '⁴')
                            .replace('5', '⁵').replace('6', '⁶').replace('7', '⁷')
                            .replace('8', '⁸').replace('9', '⁹')
                    );
                }
            }
            this.stringValue = sb.toString();
        }
        return this.stringValue;
    }
}
