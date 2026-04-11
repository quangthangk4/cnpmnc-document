package com.cnpmnc.document_management.entity;

public enum DocumentType {
    PDF("pdf", "PDF Document"),
    DOCX("docx", "Word Document"),
    DOC("doc", "Word 97-2003"),
    XLSX("xlsx", "Excel Spreadsheet"),
    XLS("xls", "Excel 97-2003"),
    PPTX("pptx", "PowerPoint Presentation"),
    PPT("ppt", "PowerPoint 97-2003"),
    TXT("txt", "Text File"),
    PNG("png", "PNG Image"),
    JPG("jpg", "JPEG Image"),
    JPEG("jpeg", "JPEG Image"),
    GIF("gif", "GIF Image"),
    PDF_SCAN("pdf-scan", "Scanned PDF"),
    ZIP("zip", "Compressed File"),
    RAR("rar", "Compressed File"),
    OTHER("other", "Other File Type");

    private final String value;
    private final String label;

    DocumentType(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static DocumentType fromValue(String value) {
        if (value == null) {
            return OTHER;
        }
        for (DocumentType type : DocumentType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return OTHER;
    }
}
