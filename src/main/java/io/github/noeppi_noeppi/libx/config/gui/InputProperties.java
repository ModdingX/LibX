package io.github.noeppi_noeppi.libx.config.gui;

/**
 * Defines which input is valid and how to convert input for text input editors.
 */
public interface InputProperties<T> {

    /**
     * Input properties for a plain string.
     */
    InputProperties<String> PLAIN = new InputProperties<>() {

        @Override
        public String defaultValue() {
            return "";
        }

        @Override
        public String valueOf(String str) {
            return str;
        }

        @Override
        public String toString(String str) {
            return str;
        }

        @Override
        public boolean isValid(String str) {
            return true;
        }
    };

    /**
     * gets the default value for newly created inputs that have no other default value available.
     */
    T defaultValue();

    /**
     * Converts some input into a value matching this input properties. This may fail with an exception
     * only if {@link #isValid(String)} returns {@code false} for the same input.
     */
    T valueOf(String str);

    /**
     * Converts a value matching this input properties to a string.
     */
    default String toString(T t) {
        return t.toString();
    }

    /**
     * Gets whether the user can enter the given char into the input field.
     */
    default boolean canInputChar(char chr) {
        return true;
    }

    /**
     * Gets whether a given user input is valid or not.
     */
    default boolean isValid(String str) {
        for (char chr : str.toCharArray()) {
            if (!this.canInputChar(chr)) {
                return false;
            }
        }
        return true;
    }
}
