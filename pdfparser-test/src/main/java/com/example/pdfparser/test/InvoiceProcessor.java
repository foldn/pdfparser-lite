package com.example.pdfparser.test;

import com.example.pdfparser.annotation.PdfField;
import com.example.pdfparser.processor.DocumentProcessor;
import com.example.pdfparser.test.model.Invoice;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class InvoiceProcessor implements DocumentProcessor<Invoice> {

    @Override
    public Invoice process(String text, Class<Invoice> targetClass) {
        Invoice invoice = createInstance(targetClass);
        
        // 解析基本字段
        processBasicFields(invoice, text);
        
        // 解析商品明细
        processDetails(invoice, text);
        
        return invoice;
    }

    private void processBasicFields(Invoice invoice, String text) {
        Class<?> clazz = invoice.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            PdfField pdfField = field.getAnnotation(PdfField.class);
            
            if (pdfField != null && !field.getName().equals("detailList")) {
                try {
                    String value = extractFieldValue(text, pdfField);
                    if (value != null) {
                        Object convertedValue = convertToFieldType(value, field.getType());
                        field.set(invoice, convertedValue);
                    }
                } catch (Exception e) {
                    log.error("Error processing field {}: {}", field.getName(), e.getMessage());
                }
            }
        }
    }

    private void processDetails(Invoice invoice, String text) {
        List<Invoice.Detail> details = new ArrayList<>();
        Pattern pattern = Pattern.compile(
            "\\*水果\\*(.*?)\\s+(\\S+)\\s+份\\s+(\\d+)\\s+([\\d.]+)\\s+([\\d.]+)\\s+(\\d+%)\\s+([\\d.-]+)"
        );
        
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            try {
                Invoice.Detail detail = new Invoice.Detail();
                detail.setName(matcher.group(1).trim());
                detail.setModel(matcher.group(2).trim());
                detail.setUnit("份");
                detail.setCount(new BigDecimal(matcher.group(3)));
                detail.setPrice(new BigDecimal(matcher.group(4)));
                detail.setAmount(new BigDecimal(matcher.group(5)));
                detail.setTaxRate(new BigDecimal(matcher.group(6).replace("%", "")));
                detail.setTaxAmount(new BigDecimal(matcher.group(7)));
                details.add(detail);
            } catch (Exception e) {
                log.warn("Error processing detail line: {}", e.getMessage());
            }
        }
        
        invoice.setDetailList(details);
    }

    private String extractFieldValue(String text, PdfField pdfField) {
        String pattern = pdfField.pattern();
        if (!pattern.isEmpty()) {
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(text);
            if (matcher.find()) {
                return matcher.group(pdfField.group());
            }
        }

        String prefix = pdfField.prefix();
        String suffix = pdfField.suffix();
        if (!prefix.isEmpty()) {
            int startIndex = text.indexOf(prefix) + prefix.length();
            int endIndex = suffix.isEmpty() ? text.length() : text.indexOf(suffix, startIndex);
            if (startIndex >= 0 && endIndex >= 0) {
                return text.substring(startIndex, endIndex).trim();
            }
        }

        return null;
    }

    private Object convertToFieldType(String value, Class<?> fieldType) {
        if (fieldType == String.class) {
            return value;
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return Integer.parseInt(value);
        } else if (fieldType == Double.class || fieldType == double.class) {
            return Double.parseDouble(value);
        } else if (fieldType == BigDecimal.class) {
            return new BigDecimal(value);
        }
        return value;
    }

    private Invoice createInstance(Class<Invoice> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }
} 