package com.example.pdfparser.converter;

import com.example.pdfparser.model.PdfData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonConverter implements Converter<String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convert(PdfData pdfData) {
        try {
            ObjectNode rootNode = objectMapper.createObjectNode();
            
            // 添加基本信息
            rootNode.put("success", true);
            rootNode.put("message", "解析成功");
            
            // 添加解析时间信息
            ObjectNode timeNode = rootNode.putObject("time");
            Long startTime = (Long) pdfData.getMetadata().get("startTime");
            Long endTime = (Long) pdfData.getMetadata().get("endTime");
            timeNode.put("startTime", startTime != null ? startTime.toString() : "");
            timeNode.put("endTime", endTime != null ? endTime.toString() : "");
            timeNode.put("cost", startTime != null && endTime != null ? endTime - startTime : 0);
            
            // 添加原始文本
            rootNode.put("rawText", pdfData.getText());
            
            // 添加发票数据
            rootNode.set("data", objectMapper.valueToTree(pdfData.getData()));
            
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert invoice to JSON", e);
        }
    }
}