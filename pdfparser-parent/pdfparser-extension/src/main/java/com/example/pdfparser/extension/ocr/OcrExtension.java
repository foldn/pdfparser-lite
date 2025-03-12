package com.example.pdfparser.extension.ocr;

import com.example.pdfparser.model.PdfData;

/**
 * OCR扩展，用于从PDF图像中提取文本
 */
public interface OcrExtension {
    /**
     * 从PDF图像中提取文本
     */
    String extractTextFromImage(byte[] imageData);
    
    /**
     * 处理包含图像的PDF
     */
    PdfData processImageBasedPdf(byte[] pdfData);
} 