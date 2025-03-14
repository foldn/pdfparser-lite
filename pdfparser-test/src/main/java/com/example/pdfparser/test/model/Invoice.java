package com.example.pdfparser.test.model;

import com.example.pdfparser.annotation.PdfField;
import com.example.pdfparser.annotation.PdfProcessor;
import com.example.pdfparser.test.InvoiceProcessor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.List;

/**
 * 全电发票实体类
 */
@Data
@PdfProcessor(processor = InvoiceProcessor.class)
public class Invoice {
    /**
     * 发票号码
     */
    @PdfField(pattern = "发票号码：(\\d+)", group = 1)
    private String invoiceNumber;

    /**
     * 开票信息
     */
    @PdfField(pattern = "开票日期：([\\d]{4}年[\\d]{2}月[\\d]{2}日)", group = 1)
    private String invoiceDate;

    /**
     * 购买方名称
     */
    @PdfField(pattern = "购\\s*名称[:：]\\s*(.*?)\\s+销", group = 1)
    private String buyerName;

    /**
     * 购买方统一社会信用代码/纳税人识别号
     */
    @PdfField(prefix = "统一社会信用代码/纳税人识别号：", suffix = "信")
    private String buyerTaxNumber;

    /**
     * 销售方名称
     */
    @PdfField(pattern = "销\\s+名称：\\s*([^\\n]+)", group = 1)
    private String sellerName;

    /**
     * 销售方统一社会信用代码/纳税人识别号
     */
    @PdfField(prefix = " 信 统一社会信用代码/纳税人识别号：", suffix = "息")
    private String sellerTaxNumber;

    /**
     * 金额小记
     */
    @PdfField(pattern = "小\\s*计\\s*¥([\\d.]+)\\s*¥[\\d.]+", group = 1)

    private String amount;

    /**
     * 税额小记
     */
    @PdfField(pattern = "小\\s*计\\s*¥[\\d.]+\\s*¥([\\d.]+)", group = 1)

    private String tax;

    /**
     * 金额合计
     */

    @PdfField(pattern = "合\\s*计\\s*¥([\\d.]+)\\s*¥[\\d.]+", group = 1)
    private String totalAmount;

    /**
     * 税额合计
     */

    @PdfField(pattern = "合\\s*计\\s*¥[\\d.]+\\s*¥([\\d.]+)", group = 1)
    private String totalTax;


    /**
     * 价税合计（大写）
     */
    @PdfField(pattern = "价税合计（大写）\\s*([^（]+)", group = 1)

    private String totalPriceAndTaxInChinese;

    /**
     * 价税合计（小写写）
     */
    @PdfField(pattern = "（小写）¥([\\d.]+)", group = 1)


    private String totalPriceAndTax;

    /**
     * 开票人
     */
    @PdfField(pattern = "开票人：\\s*([^\\n]+)", group = 1)

    private String issuer;

    /**
     * 纳税详情
     */
    private List<Detail> detailList;


    @Data
    @XmlRootElement(name = "detail")
    @XmlAccessorType(XmlAccessType.FIELD)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Detail{
        /**
         * 项目名称
         */
        private String name;

        /**
         * 规格型号
         */
        private String model;

        /**
         * 单位
         */
        private String unit;

        /**
         * 数量
         */
        private BigDecimal count;

        /**
         * 单价
         */
        private BigDecimal price;

        /**
         * 金额
         */
        private BigDecimal amount;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 税额
         */
        private BigDecimal taxAmount;
    }
} 