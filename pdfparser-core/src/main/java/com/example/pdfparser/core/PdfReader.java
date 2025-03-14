package com.example.pdfparser.core;

import com.example.pdfparser.context.ParseContext;
import com.example.pdfparser.event.ParseEventListener;
import com.example.pdfparser.options.ParseOptions;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * PDF读取器接口，定义基本的PDF读取操作
 */
public interface PdfReader {
    /**
     * 从文件读取PDF内容
     */
    <T> List<T> read(File file, Class<T> clazz, ParseOptions options);
    
    /**
     * 从输入流读取PDF内容
     */
    <T> List<T> read(InputStream inputStream, Class<T> clazz, ParseOptions options);
    
    /**
     * 注册解析事件监听器
     */
    void registerListener(ParseEventListener listener);
    
    /**
     * 获取解析上下文
     */
    ParseContext getContext();

} 