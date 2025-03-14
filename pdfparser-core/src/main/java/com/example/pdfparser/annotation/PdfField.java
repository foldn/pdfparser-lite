package com.example.pdfparser.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PdfField {

    /**
     * 正则表达式匹配
     * @return
     */
    String pattern() default "";

    /**
     * 前缀匹配
     * @return
     */
    String prefix() default "";


    /**
     * 后缀匹配
     * @return
     */
    String suffix() default "";

    /**
     * 正则表达式分组索引
     * @return
     */
    int group() default 1;
    boolean required() default true;
} 