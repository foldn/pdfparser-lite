package com.example.pdfparser.test.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;

public class PdfTestUtil {
    
    public static void createTestPdf(String filePath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // 设置字体和大小
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("PDF Parser Lite Test Document");
                
                // 切换到正常字体
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                
                // 添加测试内容
                contentStream.newLineAtOffset(0, -30);
                contentStream.showText("This is a test document for PDF Parser Lite project.");
                
                // 添加更多内容...
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Section 1: Basic Text");
                
                contentStream.endText();
            }

            document.save(filePath);
        }
    }
    
    public static void main(String[] args) throws IOException {
        createTestPdf("pdfparser-test/src/main/resources/example.pdf");
    }
} 