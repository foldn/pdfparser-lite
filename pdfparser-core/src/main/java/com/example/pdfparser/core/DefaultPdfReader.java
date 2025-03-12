package com.example.pdfparser.core;

import com.example.pdfparser.context.ParseContext;
import com.example.pdfparser.event.ParseEventListener;
import com.example.pdfparser.exception.PdfParseException;
import com.example.pdfparser.options.ParseOptions;
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
        //
        try (PDDocument document = PDDocument.load(file, options.getPassword())) {
            return processPdfDocument(document, clazz, options);
        }
    }

    private <T> List<T> processPdfDocument(PDDocument document, Class<T> clazz, ParseOptions options) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        
        // 设置页码范围
        if (options.getStartPage() > 0) {
            stripper.setStartPage(options.getStartPage());
        }
        if (options.getEndPage() > 0) {
            stripper.setEndPage(options.getEndPage());
        }

        // 提取文本
        String text = stripper.getText(document);
        context.setAttribute("rawText", text);

        // 如果目标类是String，直接返回文本
        if (clazz == String.class) {
            return (List<T>) Collections.singletonList(text);
        }

        // 创建结果对象并填充数据
        List<T> results = new ArrayList<>();
        T instance = createInstance(clazz);
        fillObjectWithData(instance, text);
        results.add(instance);

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
            if (field.getType() == String.class) {
                try {
                    field.set(instance, text);
                } catch (IllegalAccessException e) {
                    log.error("Error setting field value", e);
                }
            }
        }
    }
} 