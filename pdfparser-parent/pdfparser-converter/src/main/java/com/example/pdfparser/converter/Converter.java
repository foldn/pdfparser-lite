package com.example.pdfparser.converter;

import com.example.pdfparser.model.PdfData;

/**
 * 转换器接口，定义将PDF数据转换为其他格式的方法
 */
public interface Converter<T> {
    /**
     * 将PDF数据转换为目标格式
     */
    T convert(PdfData pdfData);
} 