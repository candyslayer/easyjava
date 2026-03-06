package com.easyjava.codegen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextThreeWayMerger {

    private static final String CONFLICT_START_MARKER = "<<<<<<<";
    private static final String CONFLICT_SEPARATOR = "=======";
    private static final String CONFLICT_END_MARKER = ">>>>>>>";
    private static final String EXISTING_CONFLICT_REASON = "输入文本已包含冲突标记，停止再次合并以避免嵌套冲突";

    public MergeTextResult merge(String baseText, String localText, String newText) {
        String existingConflictText = chooseExistingConflictText(localText, newText);
        if (existingConflictText != null) {
            return new MergeTextResult(existingConflictText,
                    List.of(new ConflictBlock("text:existing-conflict", safeText(baseText), safeText(localText),
                            safeText(newText), "", CodegenFileType.TEXT, EXISTING_CONFLICT_REASON)));
        }

        List<String> base = splitLines(baseText);
        List<String> local = splitLines(localText);
        List<String> newer = splitLines(newText);

        if (equivalent(local, base) && !equivalent(newer, base)) {
            return new MergeTextResult(joinLines(newer), List.of());
        }
        if (!equivalent(local, base) && equivalent(newer, base)) {
            return new MergeTextResult(joinLines(local), List.of());
        }
        if (equivalent(local, newer)) {
            return new MergeTextResult(joinLines(prefer(local, newer, base)), List.of());
        }

        List<Change> localChanges = buildChanges(base, local);
        List<Change> newChanges = buildChanges(base, newer);

        List<String> merged = new ArrayList<>();
        List<ConflictBlock> conflicts = new ArrayList<>();

        int baseCursor = 0;
        int localIndex = 0;
        int newIndex = 0;

        while (baseCursor < base.size() || localIndex < localChanges.size() || newIndex < newChanges.size()) {
            int nextLocalStart = localIndex < localChanges.size() ? localChanges.get(localIndex).baseStart : Integer.MAX_VALUE;
            int nextNewStart = newIndex < newChanges.size() ? newChanges.get(newIndex).baseStart : Integer.MAX_VALUE;
            int nextChangeStart = Math.min(nextLocalStart, nextNewStart);

            if (nextChangeStart == Integer.MAX_VALUE) {
                merged.addAll(base.subList(baseCursor, base.size()));
                break;
            }

            if (baseCursor < nextChangeStart) {
                merged.addAll(base.subList(baseCursor, nextChangeStart));
                baseCursor = nextChangeStart;
            }

            int regionStart = nextChangeStart;
            int regionEnd = regionStart;
            List<Change> localRegion = new ArrayList<>();
            List<Change> newRegion = new ArrayList<>();
            boolean expanded;
            do {
                expanded = false;
                while (localIndex < localChanges.size()
                        && touches(localChanges.get(localIndex), regionStart, regionEnd, !localRegion.isEmpty() || !newRegion.isEmpty())) {
                    Change change = localChanges.get(localIndex++);
                    localRegion.add(change);
                    regionEnd = Math.max(regionEnd, change.baseEnd);
                    expanded = true;
                }
                while (newIndex < newChanges.size()
                        && touches(newChanges.get(newIndex), regionStart, regionEnd, !localRegion.isEmpty() || !newRegion.isEmpty())) {
                    Change change = newChanges.get(newIndex++);
                    newRegion.add(change);
                    regionEnd = Math.max(regionEnd, change.baseEnd);
                    expanded = true;
                }
            } while (expanded);

            List<String> baseSegment = materialize(base, regionStart, regionEnd, List.of());
            List<String> localSegment = materialize(base, regionStart, regionEnd, localRegion);
            List<String> newSegment = materialize(base, regionStart, regionEnd, newRegion);

            if (equivalent(localSegment, baseSegment) && !equivalent(newSegment, baseSegment)) {
                merged.addAll(newSegment);
            } else if (!equivalent(localSegment, baseSegment) && equivalent(newSegment, baseSegment)) {
                merged.addAll(localSegment);
            } else if (equivalent(localSegment, newSegment)) {
                merged.addAll(prefer(localSegment, newSegment, baseSegment));
            } else {
                conflicts.add(new ConflictBlock("text:block@" + (regionStart + 1), joinLines(baseSegment), joinLines(localSegment),
                        joinLines(newSegment), "", CodegenFileType.TEXT, "文本块在 Local 与 New 中都发生了重叠变更"));
                merged.add("<<<<<<< LOCAL");
                merged.addAll(localSegment);
                merged.add("=======");
                merged.addAll(newSegment);
                merged.add(">>>>>>> NEW");
            }

            baseCursor = regionEnd;
        }

        return new MergeTextResult(joinLines(merged), conflicts);
    }

    private String chooseExistingConflictText(String localText, String newText) {
        if (containsConflictMarker(localText)) {
            return safeText(localText);
        }
        if (containsConflictMarker(newText)) {
            return safeText(newText);
        }
        return null;
    }

    private boolean containsConflictMarker(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        return normalized.contains(CONFLICT_START_MARKER)
                && normalized.contains(CONFLICT_SEPARATOR)
                && normalized.contains(CONFLICT_END_MARKER);
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }

    private List<String> splitLines(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);
        List<String> result = new ArrayList<>();
        Collections.addAll(result, lines);
        if (!result.isEmpty() && result.get(result.size() - 1).isEmpty()) {
            result.remove(result.size() - 1);
        }
        return result;
    }

    private String joinLines(List<String> lines) {
        if (lines.isEmpty()) {
            return "";
        }
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }

    private List<Change> buildChanges(List<String> base, List<String> other) {
        int[][] dp = new int[base.size() + 1][other.size() + 1];
        for (int i = base.size() - 1; i >= 0; i--) {
            for (int j = other.size() - 1; j >= 0; j--) {
                if (sameLine(base.get(i), other.get(j))) {
                    dp[i][j] = dp[i + 1][j + 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i + 1][j], dp[i][j + 1]);
                }
            }
        }

        List<LineMatch> matches = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < base.size() && j < other.size()) {
            if (sameLine(base.get(i), other.get(j))) {
                matches.add(new LineMatch(i, j));
                i++;
                j++;
            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                i++;
            } else {
                j++;
            }
        }

        List<Change> changes = new ArrayList<>();
        int baseCursor = 0;
        int otherCursor = 0;
        for (LineMatch match : matches) {
            if (baseCursor < match.baseIndex || otherCursor < match.otherIndex) {
                changes.add(new Change(baseCursor, match.baseIndex,
                        new ArrayList<>(other.subList(otherCursor, match.otherIndex))));
            }
            baseCursor = match.baseIndex + 1;
            otherCursor = match.otherIndex + 1;
        }
        if (baseCursor < base.size() || otherCursor < other.size()) {
            changes.add(new Change(baseCursor, base.size(), new ArrayList<>(other.subList(otherCursor, other.size()))));
        }
        return changes;
    }

    private boolean touches(Change change, int regionStart, int regionEnd, boolean hasRegion) {
        if (!hasRegion) {
            return change.baseStart == regionStart;
        }
        if (regionStart == regionEnd) {
            return change.baseStart == regionStart;
        }
        if (change.baseStart == change.baseEnd) {
            return change.baseStart >= regionStart && change.baseStart < regionEnd;
        }
        return change.baseStart < regionEnd && change.baseEnd > regionStart;
    }

    private List<String> materialize(List<String> base, int start, int end, List<Change> changes) {
        if (changes.isEmpty()) {
            return new ArrayList<>(base.subList(start, end));
        }
        List<String> lines = new ArrayList<>();
        int cursor = start;
        for (Change change : changes) {
            if (cursor < change.baseStart) {
                lines.addAll(base.subList(cursor, change.baseStart));
            }
            lines.addAll(change.revisedLines);
            cursor = change.baseEnd;
        }
        if (cursor < end) {
            lines.addAll(base.subList(cursor, end));
        }
        return lines;
    }

    private boolean equivalent(List<String> left, List<String> right) {
        return normalizeLines(left).equals(normalizeLines(right));
    }

    private List<String> prefer(List<String> local, List<String> newer, List<String> base) {
        if (local.equals(base)) {
            return newer;
        }
        return local;
    }

    private List<String> normalizeLines(List<String> lines) {
        List<String> result = new ArrayList<>();
        boolean lastBlank = false;
        for (String line : lines) {
            String normalized = normalizeLine(line);
            if (normalized.isEmpty()) {
                if (!lastBlank) {
                    result.add("");
                    lastBlank = true;
                }
            } else {
                result.add(normalized);
                lastBlank = false;
            }
        }
        int start = 0;
        int end = result.size();
        while (start < end && result.get(start).isEmpty()) {
            start++;
        }
        while (end > start && result.get(end - 1).isEmpty()) {
            end--;
        }
        return new ArrayList<>(result.subList(start, end));
    }

    private boolean sameLine(String left, String right) {
        return normalizeLine(left).equals(normalizeLine(right));
    }

    private String normalizeLine(String line) {
        if (line == null) {
            return "";
        }
        return stripTrailingWhitespace(line).stripLeading().stripTrailing();
    }

    private String stripTrailingWhitespace(String line) {
        int end = line.length();
        while (end > 0 && Character.isWhitespace(line.charAt(end - 1))) {
            end--;
        }
        return line.substring(0, end);
    }

    public static class MergeTextResult {
        private final String mergedText;
        private final List<ConflictBlock> conflictBlocks;

        public MergeTextResult(String mergedText, List<ConflictBlock> conflictBlocks) {
            this.mergedText = mergedText;
            this.conflictBlocks = conflictBlocks;
        }

        public String getMergedText() {
            return mergedText;
        }

        public List<ConflictBlock> getConflictBlocks() {
            return conflictBlocks;
        }

        public List<String> getConflicts() {
            List<String> conflicts = new ArrayList<>();
            for (ConflictBlock block : conflictBlocks) {
                conflicts.add(block.getBlockId());
            }
            return conflicts;
        }

        public boolean hasConflict() {
            return !conflictBlocks.isEmpty();
        }
    }

    private static final class Change {
        private final int baseStart;
        private final int baseEnd;
        private final List<String> revisedLines;

        private Change(int baseStart, int baseEnd, List<String> revisedLines) {
            this.baseStart = baseStart;
            this.baseEnd = baseEnd;
            this.revisedLines = revisedLines;
        }
    }

    private static final class LineMatch {
        private final int baseIndex;
        private final int otherIndex;

        private LineMatch(int baseIndex, int otherIndex) {
            this.baseIndex = baseIndex;
            this.otherIndex = otherIndex;
        }
    }
}
