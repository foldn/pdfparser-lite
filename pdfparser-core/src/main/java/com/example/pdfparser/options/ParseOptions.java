package com.example.pdfparser.options;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * PDF解析配置选项类
 */
@Data
@Accessors(chain = true)
public class ParseOptions {
    /**
     * 是否提取表格数据
     */
    private boolean extractTables = true;

    /**
     * 是否提取元数据
     */
    private boolean extractMetadata = true;

    /**
     * 是否启用OCR（光学字符识别）
     */
    private boolean enableOcr = false;

    /**
     * 是否保留PDF原始布局
     */
    private boolean maintainLayout = false;

    /**
     * 开始页码（从1开始）
     */
    private int startPage = 1;

    /**
     * 结束页码（-1表示直到最后一页）
     */
    private int endPage = -1;

    /**
     * 密码（用于加密的PDF文档）
     */
    private String password;

    /**
     * 表格检测的置信度阈值（0.0-1.0）
     */
    private float tableDetectionConfidence = 0.8f;

    /**
     * 字符识别的最小置信度（0.0-1.0）
     */
    private float minimumCharConfidence = 0.5f;

    /**
     * 是否忽略隐藏内容
     */
    private boolean ignoreHiddenContent = true;

    /**
     * 文本提取模式
     */
    private TextExtractionMode textExtractionMode = TextExtractionMode.NORMAL;

    /**
     * 表格提取算法
     */
    private TableExtractionAlgorithm tableExtractionAlgorithm = TableExtractionAlgorithm.SPREADSHEET;

    /**
     * 创建默认配置的静态工厂方法
     */
    public static ParseOptions createDefault() {
        return new ParseOptions();
    }

    /**
     * 文本提取模式枚举
     */
    public enum TextExtractionMode {
        /**
         * 普通模式 - 基本文本提取
         */
        NORMAL,
        
        /**
         * 严格模式 - 保持精确的文本位置和格式
         */
        STRICT,
        
        /**
         * 宽松模式 - 优化可读性
         */
        RELAXED
    }

    /**
     * 表格提取算法枚举
     */
    public enum TableExtractionAlgorithm {
        /**
         * 电子表格算法 - 适用于规则表格
         */
        SPREADSHEET,
        
        /**
         * 基于规则的算法 - 适用于非规则表格
         */
        RULE_BASED,
        
        /**
         * 启发式算法 - 使用多种方法综合判断
         */
        HEURISTIC
    }

    /**
     * 验证配置选项的有效性
     */
    public void validate() {
        if (startPage < 1) {
            throw new IllegalArgumentException("Start page must be greater than 0");
        }
        if (endPage != -1 && endPage < startPage) {
            throw new IllegalArgumentException("End page must be greater than or equal to start page");
        }
        if (tableDetectionConfidence < 0.0f || tableDetectionConfidence > 1.0f) {
            throw new IllegalArgumentException("Table detection confidence must be between 0.0 and 1.0");
        }
        if (minimumCharConfidence < 0.0f || minimumCharConfidence > 1.0f) {
            throw new IllegalArgumentException("Minimum character confidence must be between 0.0 and 1.0");
        }
    }

    /**
     * 创建一个构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * ParseOptions的构建器类
     */
    public static class Builder {
        private final ParseOptions options = new ParseOptions();

        public Builder extractTables(boolean extract) {
            options.setExtractTables(extract);
            return this;
        }

        public Builder extractMetadata(boolean extract) {
            options.setExtractMetadata(extract);
            return this;
        }

        public Builder enableOcr(boolean enable) {
            options.setEnableOcr(enable);
            return this;
        }

        public Builder maintainLayout(boolean maintain) {
            options.setMaintainLayout(maintain);
            return this;
        }

        public Builder pageRange(int start, int end) {
            options.setStartPage(start);
            options.setEndPage(end);
            return this;
        }

        public Builder password(String password) {
            options.setPassword(password);
            return this;
        }

        public Builder tableDetectionConfidence(float confidence) {
            options.setTableDetectionConfidence(confidence);
            return this;
        }

        public Builder textExtractionMode(TextExtractionMode mode) {
            options.setTextExtractionMode(mode);
            return this;
        }

        public Builder tableExtractionAlgorithm(TableExtractionAlgorithm algorithm) {
            options.setTableExtractionAlgorithm(algorithm);
            return this;
        }

        public ParseOptions build() {
            options.validate();
            return options;
        }
    }
}