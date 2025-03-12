package com.example.pdfparser.exception;

/**
 * PDF解析异常
 */
public class PdfParseException extends RuntimeException {

    public PdfParseException(String message) {
        super(message);
    }

    public PdfParseException(String message, Throwable cause) {
        super(message, cause);
    }
}