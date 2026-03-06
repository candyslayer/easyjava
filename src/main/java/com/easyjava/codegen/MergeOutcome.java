package com.easyjava.codegen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MergeOutcome {

    private final MergeStatus status;
    private final Path targetPath;
    private final String relativePath;
    private final CodegenFileType fileType;
    private final String baseContent;
    private final String localContent;
    private final String newContent;
    private final String mergedContent;
    private final String conflictMarkedContent;
    private final List<ConflictBlock> conflictBlocks;
    private ConflictResolutionChoice resolutionChoice;
    private Path conflictOutputPath;

    public MergeOutcome(MergeStatus status, Path targetPath, String relativePath, CodegenFileType fileType,
            String baseContent, String localContent, String newContent, String mergedContent, String conflictMarkedContent,
            List<ConflictBlock> conflictBlocks) {
        this.status = status;
        this.targetPath = targetPath;
        this.relativePath = relativePath;
        this.fileType = fileType;
        this.baseContent = baseContent;
        this.localContent = localContent;
        this.newContent = newContent;
        this.mergedContent = mergedContent;
        this.conflictMarkedContent = conflictMarkedContent;
        this.conflictBlocks = Collections.unmodifiableList(new ArrayList<>(conflictBlocks));
    }

    public static MergeOutcome simple(MergeStatus status, Path targetPath, String relativePath, CodegenFileType fileType,
            String baseContent, String localContent, String newContent, String mergedContent) {
        return new MergeOutcome(status, targetPath, relativePath, fileType, baseContent, localContent, newContent,
                mergedContent, null, List.of());
    }

    public static MergeOutcome conflict(Path targetPath, String relativePath, CodegenFileType fileType,
            String baseContent, String localContent, String newContent, String mergedContent, String conflictMarkedContent,
            List<ConflictBlock> conflictBlocks) {
        return new MergeOutcome(MergeStatus.CONFLICT, targetPath, relativePath, fileType, baseContent, localContent,
                newContent, mergedContent, conflictMarkedContent, conflictBlocks);
    }

    public MergeStatus getStatus() {
        return status;
    }

    public Path getTargetPath() {
        return targetPath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public CodegenFileType getFileType() {
        return fileType;
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

    public String getMergedContent() {
        return mergedContent;
    }

    public String getConflictMarkedContent() {
        return conflictMarkedContent;
    }

    public List<ConflictBlock> getConflictBlocks() {
        return conflictBlocks;
    }

    public List<ConflictBlock> getConflicts() {
        return conflictBlocks;
    }

    public boolean hasConflict() {
        return status == MergeStatus.CONFLICT;
    }

    public List<String> getConflictMessages() {
        List<String> messages = new ArrayList<>();
        for (ConflictBlock block : conflictBlocks) {
            messages.add(block.getBlockId());
        }
        return messages;
    }

    public ConflictResolutionChoice getResolutionChoice() {
        return resolutionChoice;
    }

    public void setResolutionChoice(ConflictResolutionChoice resolutionChoice) {
        this.resolutionChoice = resolutionChoice;
    }

    public Path getConflictOutputPath() {
        return conflictOutputPath;
    }

    public void setConflictOutputPath(Path conflictOutputPath) {
        this.conflictOutputPath = conflictOutputPath;
    }
}
