# PDF Parser Lite

一个轻量级、低侵入性的PDF解析工具，支持文本提取、表格识别、OCR等功能。

## 项目架构
```java
pdfparser-parent/
├── pdfparser-core # 核心解析模块
├── pdfparser-extension # 扩展功能模块
└── pdfparser-test # 测试demo模块
```

### 核心模块说明

#### 1. pdfparser-core
- 实现PDF解析的核心功能
- 采用构建者模式和工厂模式
- 提供注解支持，实现声明式PDF解析配置
- 支持字段映射、解析规则配置等功能
- 负责格式转换功能
- 支持JSON、XML等多种输出格式
- 可扩展的转换器接口设计
- 主要注解：`@PdfField` `@@PdfProcessor`
- 主要组件：
  - `PdfReader`: PDF读取接口
  - `ParseOptions`: 解析配置类
  - `ParseContext`: 解析上下文
  - `ParseEventListener`: 事件监听接口
  - `PdfProcessor`: 指定处理器
  - `Converter`:格式转化器接口


#### 2. pdfparser-extension
- 提供额外功能扩展
- OCR支持
- 高级表格识别等特性

### 特性

- [x] 基本文本提取
- [x] 注解支持
- [x] 事件监听机制
- [x] 可拓展的文本处理器
- [x] 可扩展的转换器
- [ ] 表格识别与提取
- [ ] 元数据提取
- [ ] 支持加密PDF
- [ ] OCR支持
- [ ] 批量处理支持
- [ ] 异步处理支持

## 快速开始

### Maven依赖
```java
<dependency>
<groupId>com.example.pdfparser</groupId>
<artifactId>pdfparser-core</artifactId>
<version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 基本使用(详见pdfparser-test模块)
```java
// 定义数据模型类
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
}
```
```java
//特殊文本处理
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
}
```
```java
// 创建解析配置
ParseOptions options = ParseOptions.builder()
        .maintainLayout(true)  // 保持原始布局
        .build();

// 创建解析事件监听器
ParseEventListener listener = new ParseEventListener() {
    @Override
    public void onBeforeParse(ParseContext context) {
        System.out.println("开始解析PDF");
    }

    @Override
    public void onAfterParse(ParseContext context) {
        System.out.println("PDF解析完成");
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
```


