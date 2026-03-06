package com.easyjava.codegen;

import java.util.Objects;

public class ConflictBlock {

    private final String blockId;
    private final String baseContent;
    private final String localContent;
    private final String newContent;
    private final String filePath;
    private final CodegenFileType fileType;
    private final String reason;

    public ConflictBlock(String blockId, String baseContent, String localContent, String newContent, String filePath,
            CodegenFileType fileType, String reason) {
        this.blockId = blockId;
        this.baseContent = valueOrEmpty(baseContent);
        this.localContent = valueOrEmpty(localContent);
        this.newContent = valueOrEmpty(newContent);
        this.filePath = filePath == null ? "" : filePath;
        this.fileType = fileType == null ? CodegenFileType.TEXT : fileType;
        this.reason = reason == null ? "" : reason;
    }

    public String getBlockId() {
        return blockId;
    }

    public String getBaseContent() {
        return baseContent;
    }

    public String getLocalContent() {
        return localContent;
    }

    public String getNewContent() {
        return newContent;
    }

    public String getFilePath() {
        return filePath;
    }

    public CodegenFileType getFileType() {
        return fileType;
    }

    public String getReason() {
        return reason;
    }

    public ConflictBlock withFileContext(String path, CodegenFileType type) {
        return new ConflictBlock(blockId, baseContent, localContent, newContent, path, type, reason);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockId, baseContent, localContent, newContent, filePath, fileType, reason);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ConflictBlock other)) {
            return false;
        }
        return Objects.equals(blockId, other.blockId)
                && Objects.equals(baseContent, other.baseContent)
                && Objects.equals(localContent, other.localContent)
                && Objects.equals(newContent, other.newContent)
                && Objects.equals(filePath, other.filePath)
                && fileType == other.fileType
                && Objects.equals(reason, other.reason);
    }
}
