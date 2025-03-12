# PDF Parser Lite

一个轻量级、低侵入性的PDF解析工具，支持文本提取、表格识别、OCR等功能。

## 项目架构
pdfparser-parent/
├── pdfparser-annotation # 注解支持模块
├── pdfparser-core # 核心解析模块
├── pdfparser-converter # 格式转换模块
├── pdfparser-extension # 扩展功能模块
└── pdfparser-extension # 测试demo模块


### 核心模块说明

#### 1. pdfparser-annotation
- 提供注解支持，实现声明式PDF解析配置
- 支持字段映射、解析规则配置等功能
- 主要注解：`@PdfField`

#### 2. pdfparser-core
- 实现PDF解析的核心功能
- 采用构建者模式和工厂模式
- 主要组件：
  - `PdfReader`: PDF读取接口
  - `ParseOptions`: 解析配置类
  - `ParseContext`: 解析上下文
  - `ParseEventListener`: 事件监听接口

#### 3. pdfparser-converter
- 负责格式转换功能
- 支持JSON、XML等多种输出格式
- 可扩展的转换器接口设计

#### 4. pdfparser-extension
- 提供额外功能扩展
- OCR支持
- 高级表格识别等特性

### 特性

- [x] 基本文本提取
- [x] 表格识别与提取
- [x] 元数据提取
- [x] 支持加密PDF
- [x] 注解支持
- [x] 事件监听机制
- [x] 可扩展的转换器
- [ ] OCR支持
- [ ] 批量处理支持
- [ ] 异步处理支持

## 快速开始

### Maven依赖
<dependency>
<groupId>com.example.pdfparser</groupId>
<artifactId>pdfparser-core</artifactId>
<version>1.0.0-SNAPSHOT</version>
</dependency>

### 基本使用



