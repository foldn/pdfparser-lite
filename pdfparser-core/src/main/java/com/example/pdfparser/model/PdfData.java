package com.example.pdfparser.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PdfData {
    /**
     * 全文本数据
     */
    private String text;

    /**
     * 解析处理后的实体类对象
     */
    private Object data;
    private List<List<String>> tables;
    private Map<String, Object> metadata;

} 