package com.example.pdfparser.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 解析上下文，存储解析过程中的状态和数据
 */
public class ParseContext {
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    public void removeAttribute(String key) {
        attributes.remove(key);
    }
    
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }
} 