package com.easyjava.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ConflictMarkerRenderer {

    public String render(ConflictBlock block) {
        return render(block, "");
    }

    public String render(ConflictBlock block, String indent) {
        List<String> lines = new ArrayList<>();
        lines.add(indent + "<<<<<<< LOCAL");
        append(lines, block.getLocalContent(), indent);
        lines.add(indent + "=======");
        append(lines, block.getNewContent(), indent);
        lines.add(indent + ">>>>>>> NEW");
        return String.join(System.lineSeparator(), lines);
    }

    public String renderConsoleBlock(ConflictBlock block) {
        StringBuilder builder = new StringBuilder();
        builder.append("冲突块: ").append(block.getBlockId()).append(System.lineSeparator())
                .append(System.lineSeparator())
                .append(render(block));
        return builder.toString();
    }

    public String renderConflictFileContent(String conflictMarkedContent, List<ConflictBlock> conflictBlocks) {
        String normalized = normalize(conflictMarkedContent);
        if (!normalized.isEmpty()) {
            return normalized + System.lineSeparator();
        }
        StringJoiner joiner = new StringJoiner(System.lineSeparator() + System.lineSeparator());
        for (ConflictBlock block : conflictBlocks) {
            joiner.add(render(block));
        }
        String content = joiner.toString();
        if (content.isEmpty()) {
            return "";
        }
        return content + System.lineSeparator();
    }

    private void append(List<String> lines, String content, String indent) {
        String normalized = normalize(content);
        if (normalized.isEmpty()) {
            return;
        }
        String[] split = normalized.split("\n", -1);
        for (String line : split) {
            lines.add(indent + line);
        }
    }

    private String normalize(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        String normalized = content.replace("\r\n", "\n").replace('\r', '\n');
        while (normalized.endsWith("\n")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
