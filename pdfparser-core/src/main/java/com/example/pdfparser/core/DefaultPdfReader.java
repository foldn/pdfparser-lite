package com.example.pdfparser.core;

import com.example.pdfparser.annotation.PdfField;
import com.example.pdfparser.annotation.PdfProcessor;
import com.example.pdfparser.context.ParseContext;
import com.example.pdfparser.event.ParseEventListener;
import com.example.pdfparser.exception.PdfParseException;
import com.example.pdfparser.options.ParseOptions;
import com.example.pdfparser.processor.DocumentProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PdfReader的默认实现类
 */
@Slf4j
public class DefaultPdfReader implements PdfReader {
    private final ParseOptions options;
    private final ParseEventListener listener;
    private final ParseContext context;

    public DefaultPdfReader(ParseOptions options, ParseEventListener listener) {
        this.options = options != null ? options : ParseOptions.createDefault();
        this.listener = listener;
        this.context = new ParseContext();
    }

    @Override
    public <T> List<T> read(File file, Class<T> clazz, ParseOptions options) {
        try {
            if (listener != null) {
                listener.onBeforeParse(context);
            }

            List<T> results = parseFile(file, clazz, options);

            if (listener != null) {
                listener.onAfterParse(context);
            }

            return results;
        } catch (Exception e) {
            log.error("Error parsing PDF file: {}", file.getName(), e);
            if (listener != null) {
                listener.onError(e, context);
            }
            throw new PdfParseException("Failed to parse PDF file", e);
        }
    }

    @Override
    public <T> List<T> read(InputStream inputStream, Class<T> clazz, ParseOptions options) {
        try {
            if (listener != null) {
                listener.onBeforeParse(context);
            }

            PDDocument document = PDDocument.load(inputStream);
            List<T> results = processPdfDocument(document, clazz, options);
            document.close();

            if (listener != null) {
                listener.onAfterParse(context);
            }

            return results;
        } catch (Exception e) {
            log.error("Error parsing PDF from input stream", e);
            if (listener != null) {
                listener.onError(e, context);
            }
            throw new PdfParseException("Failed to parse PDF from input stream", e);
        }
    }

    @Override
    public void registerListener(ParseEventListener listener) {
        // 由于当前实现使用final字段，此方法暂不支持运行时注册监听器
        throw new UnsupportedOperationException("Listener must be set during construction");
    }

    @Override
    public ParseContext getContext() {
        return context;
    }


    private <T> List<T> parseFile(File file, Class<T> clazz, ParseOptions options) throws IOException {
        try (PDDocument document = PDDocument.load(file, options.getPassword())) {
            return processPdfDocument(document, clazz, options);
        }
    }

    private <T> List<T> processPdfDocument(PDDocument document, Class<T> clazz, ParseOptions options) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        context.setAttribute("startTime", System.currentTimeMillis());
        // 设置页码范围
        if (options.getStartPage() > 0) {
            stripper.setStartPage(options.getStartPage());
        }
        if (options.getEndPage() > 0) {
            stripper.setEndPage(options.getEndPage());
        }

        // 配置文本提取选项
        stripper.setSortByPosition(true);
        stripper.setAddMoreFormatting(true);
        stripper.setSpacingTolerance(0.5f);
        // 设置字符间距容差
        stripper.setAverageCharTolerance(0.3f);


        // 提取文本
        String text = stripper.getText(document);

        // 处理文本，保持基本格式但去除多余空白
        // 删除行首尾空白
        text = text.replaceAll("(?m)^\\s+|\\s+$", "")
                // 将多个空格替换为单个空格
                .replaceAll("\\s{2,}", " ")
                // 删除空行
                .replaceAll("\\n\\s*\\n", "\n")
                .trim();

        context.setAttribute("rawText", text);
        context.setAttribute("startPage", stripper.getStartPage());
        context.setAttribute("endPage", stripper.getEndPage());

        // 如果目标类是String，直接返回文本
        if (clazz == String.class) {
            return (List<T>) Collections.singletonList(text);
        }

        // 检查是否有处理器注解
        PdfProcessor processorAnnotation = clazz.getAnnotation(PdfProcessor.class);
        if (processorAnnotation != null) {
            try {
                DocumentProcessor<T> processor = (DocumentProcessor<T>) processorAnnotation.processor()
                    .getDeclaredConstructor().newInstance();
                T result = processor.process(text, clazz);
                return Collections.singletonList(result);
            } catch (Exception e) {
                log.error("Error creating processor instance", e);
                throw new PdfParseException("Failed to process document", e);
            }
        }

        // 默认处理逻辑
        List<T> results = new ArrayList<>();
        T instance = createInstance(clazz);
        fillObjectWithData(instance, text);
        results.add(instance);

        context.setAttribute("endTime", System.currentTimeMillis());
        return results;
    }

    private <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new PdfParseException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    private <T> void fillObjectWithData(T instance, String text) {
        // 基本的字段填充实现
        // 这里可以根据@PdfField注解来进行更复杂的映射
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            PdfField pdfField = field.getAnnotation(PdfField.class);
            if (pdfField != null && pdfField.required()) {
                try {
                    String value = extractFieldValue(text, pdfField);
                    if (value != null) {
                        // 根据字段类型进行类型转换
                        Object convertedValue = convertToFieldType(value, field.getType());
                        field.set(instance, convertedValue);
                    }
                } catch (IllegalAccessException e) {
                    log.error("Error setting field value for {}", field.getName(), e);
                }
            }
        }
    }

    private String extractFieldValue(String text, PdfField pdfField) {
        String pattern = pdfField.pattern();
        if (!pattern.isEmpty()) {
            // 使用正则表达式提取值
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(text);
            if (matcher.find()) {
                return matcher.group(pdfField.group());
            }
        }

        // 使用关键字提取
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
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }
} 