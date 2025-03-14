package com.example.pdfparser.annotation;

import com.example.pdfparser.processor.DocumentProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PdfProcessor {
    /**
     * 指定处理器类
     */
    Class<? extends DocumentProcessor<?>> processor();
} 