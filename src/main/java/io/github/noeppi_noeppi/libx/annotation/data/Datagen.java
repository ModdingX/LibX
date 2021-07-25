package io.github.noeppi_noeppi.libx.annotation.data;

import java.lang.annotation.*;

// Exactly one ctor
// public
// ctor arg types: ModX, DataGenerator, ExistingFileHelper
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface Datagen {
    
}
