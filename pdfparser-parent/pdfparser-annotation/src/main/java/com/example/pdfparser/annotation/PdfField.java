package com.example.pdfparser.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PdfField {
    String name() default "";
    int order() default Integer.MAX_VALUE;
    boolean required() default false;
} 