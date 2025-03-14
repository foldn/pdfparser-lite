package com.example.pdfparser.test;

import com.example.pdfparser.context.ParseContext;
import com.example.pdfparser.converter.JsonConverter;
import com.example.pdfparser.core.PdfReader;
import com.example.pdfparser.core.PdfReaderBuilder;
import com.example.pdfparser.event.ParseEventListener;
import com.example.pdfparser.model.PdfData;
import com.example.pdfparser.options.ParseOptions;
import com.example.pdfparser.test.model.Invoice;

import java.io.File;
import java.util.List;

/**
 * @program: pdfparser-parent
 * @description:
 * @author: wanghaifeng
 * @create: 2025-03-12 17:39
 **/
public class Main {
    public static void main(String[] args) {
        // 创建配置
        ParseOptions options = ParseOptions.builder()
                .maintainLayout(true)  // 保持原始布局以便更准确地提取信息
                .build();

        // 创建监听器
        ParseEventListener listener = new ParseEventListener() {

            @Override
            public void onBeforeParse(ParseContext context) {
                System.out.println("开始解析发票PDF");
            }

            @Override
            public void onAfterParse(ParseContext context) {
                System.out.println("发票PDF解析完成");
            }

            @Override
            public void onError(Exception e, ParseContext context) {
                System.err.println("解析出错: " + e.getMessage());
            }
        };

        // 创建PDF读取器
        PdfReader reader = PdfReaderBuilder.builder()
                .withListener(listener)
                .withOptions(options)
                .build();

        try {
            // 读取PDF文件，（该文件为示例，需使用真正的发票pdf）
            File pdfFile = new File("pdfparser-test/src/main/resources/example1.pdf");
            List<Invoice> invoices = reader.read(pdfFile, Invoice.class, options);

            // 创建PdfData对象
            PdfData pdfData = new PdfData();
            pdfData.setData(invoices.get(0));
            pdfData.setMetadata(reader.getContext().getContext());
            pdfData.setText((String) reader.getContext().getAttribute("rawText"));

            // 转换为JSON
            JsonConverter converter = new JsonConverter();
            String json = converter.convert(pdfData);
            // 输出结果
            System.out.println("解析结果：");
            System.out.println(json);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }


