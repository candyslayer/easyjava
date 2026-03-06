package com.easyjava.codegen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConflictFileWriter {

    private final Path mergeResultRoot;
    private final Path conflictRoot;
    private final Path reportPath;
    private final ConflictMarkerRenderer conflictMarkerRenderer = new ConflictMarkerRenderer();

    public ConflictFileWriter(Path mergeResultRoot) {
        this.mergeResultRoot = mergeResultRoot;
        this.conflictRoot = mergeResultRoot.resolve("conflicts");
        this.reportPath = mergeResultRoot.resolve("conflict-report.txt");
    }

    public Path writeConflictFile(String relativePath, String content) throws IOException {
        Path path = conflictRoot.resolve(relativePath);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content == null ? "" : content, StandardCharsets.UTF_8);
        return path;
    }

    public Path writeConflictFile(MergeOutcome outcome) throws IOException {
        String content = conflictMarkerRenderer.renderConflictFileContent(outcome.getConflictMarkedContent(),
                outcome.getConflictBlocks());
        Path path = writeConflictFile(outcome.getRelativePath(), content);
        outcome.setConflictOutputPath(path);
        return path;
    }

    public Path writeReport(List<MergeOutcome> outcomes) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (MergeOutcome outcome : outcomes) {
            if (outcome.getStatus() != MergeStatus.CONFLICT) {
                continue;
            }
            builder.append("FILE: ").append(outcome.getRelativePath()).append(System.lineSeparator());
            for (ConflictBlock block : outcome.getConflictBlocks()) {
                builder.append("  BLOCK: ").append(block.getBlockId()).append(System.lineSeparator());
                builder.append("  REASON: ").append(block.getReason()).append(System.lineSeparator());
            }
            builder.append(System.lineSeparator());
        }
        Files.createDirectories(mergeResultRoot);
        Files.writeString(reportPath, builder.toString(), StandardCharsets.UTF_8);
        return reportPath;
    }

    public Path getConflictRoot() {
        return conflictRoot;
    }

    public Path getReportPath() {
        return reportPath;
    }
}
