package com.easyjava.codegen;

public enum CodegenFileType {
    JAVA,
    MAPPER_XML,
    TEXT;

    public static CodegenFileType fromPath(String path) {
        String normalized = path.toLowerCase();
        if (normalized.endsWith(".java")) {
            return JAVA;
        }
        if (normalized.endsWith("mapper.xml") || normalized.endsWith(".xml")) {
            return MAPPER_XML;
        }
        return TEXT;
    }
}
