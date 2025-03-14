package com.example.pdfparser.processor;

/**
 * PDF文档处理器接口
 * @param <T> 处理结果的类型
 */
public interface DocumentProcessor<T> {
    /**
     * 处理PDF文档文本
     * @param text PDF文档文本
     * @param targetClass 目标类型
     * @return 处理结果
     */
    T process(String text, Class<T> targetClass);
} 