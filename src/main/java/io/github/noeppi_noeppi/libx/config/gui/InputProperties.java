package io.github.noeppi_noeppi.libx.config.gui;

public interface InputProperties<T> {

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
    
    T defaultValue();
    
    // May throw an exception but must success when isValid returns true.
    T valueOf(String str);

    default String toString(T t) {
        return t.toString();
    }

    default boolean canInputChar(char chr) {
        return true;
    }

    default boolean isValid(String str) {
        for (char chr : str.toCharArray()) {
            if (!this.canInputChar(chr)) {
                return false;
            }
        }
        return true;
    }
}
