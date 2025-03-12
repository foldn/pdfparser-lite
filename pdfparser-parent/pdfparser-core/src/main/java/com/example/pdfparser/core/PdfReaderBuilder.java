package com.example.pdfparser.core;

import com.example.pdfparser.event.ParseEventListener;
import com.example.pdfparser.options.ParseOptions;

/**
 * PDF读取器构建器，使用建造者模式创建PdfReader实例
 */
public class PdfReaderBuilder {
    private ParseOptions options;
    private ParseEventListener listener;
    
    public PdfReaderBuilder withOptions(ParseOptions options) {
        this.options = options;
        return this;
    }
    
    public PdfReaderBuilder withListener(ParseEventListener listener) {
        this.listener = listener;
        return this;
    }
    
    public PdfReader build() {
        // 创建并返回PdfReader实例
        return new DefaultPdfReader(options, listener);
    }
    
    public static PdfReaderBuilder builder() {
        return new PdfReaderBuilder();
    }
} 