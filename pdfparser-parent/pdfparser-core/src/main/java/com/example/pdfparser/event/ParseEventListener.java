package com.example.pdfparser.event;

import com.example.pdfparser.context.ParseContext;

/**
 * 解析事件监听器接口
 */
public interface ParseEventListener {
    void onBeforeParse(ParseContext context);
    void onAfterParse(ParseContext context);
    void onError(Exception e, ParseContext context);
} 