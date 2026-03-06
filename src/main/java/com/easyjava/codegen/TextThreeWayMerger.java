package com.easyjava.codegen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextThreeWayMerger {

    public MergeTextResult merge(String baseText, String localText, String newText) {
        List<String> base = splitLines(baseText);
        List<String> local = splitLines(localText);
        List<String> newer = splitLines(newText);

        List<LineMatch> localMatches = buildMatches(base, local);
        List<LineMatch> newMatches = buildMatches(base, newer);
        int[] localMap = buildBaseToOtherMap(base.size(), localMatches);
        int[] newMap = buildBaseToOtherMap(base.size(), newMatches);

        List<Integer> anchors = new ArrayList<>();
        anchors.add(-1);
        for (int i = 0; i < base.size(); i++) {
            if (localMap[i] >= 0 && newMap[i] >= 0) {
                anchors.add(i);
            }
        }
        anchors.add(base.size());

        List<String> merged = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();
        int localCursor = 0;
        int newCursor = 0;

        for (int i = 0; i < anchors.size() - 1; i++) {
            int currentAnchor = anchors.get(i);
            int nextAnchor = anchors.get(i + 1);

            int localAnchorIndex = currentAnchor >= 0 ? localMap[currentAnchor] : -1;
            int newAnchorIndex = currentAnchor >= 0 ? newMap[currentAnchor] : -1;
            int localNextAnchor = nextAnchor < base.size() ? localMap[nextAnchor] : local.size();
            int newNextAnchor = nextAnchor < base.size() ? newMap[nextAnchor] : newer.size();

            int baseStart = currentAnchor + 1;
            int baseEnd = nextAnchor;
            int localStart = currentAnchor >= 0 ? localAnchorIndex + 1 : localCursor;
            int localEnd = localNextAnchor;
            int newStart = currentAnchor >= 0 ? newAnchorIndex + 1 : newCursor;
            int newEnd = newNextAnchor;

            List<String> baseSegment = new ArrayList<>(base.subList(baseStart, baseEnd));
            List<String> localSegment = new ArrayList<>(local.subList(localStart, localEnd));
            List<String> newSegment = new ArrayList<>(newer.subList(newStart, newEnd));

            if (localSegment.equals(baseSegment)) {
                merged.addAll(newSegment);
            } else if (newSegment.equals(baseSegment)) {
                merged.addAll(localSegment);
            } else if (localSegment.equals(newSegment)) {
                merged.addAll(localSegment);
            } else {
                conflicts.add("text-conflict@" + baseStart);
                merged.add("<<<<<<< LOCAL");
                merged.addAll(localSegment);
                merged.add("=======");
                merged.addAll(newSegment);
                merged.add(">>>>>>> NEW");
            }

            if (nextAnchor < base.size()) {
                merged.add(base.get(nextAnchor));
                localCursor = localNextAnchor + 1;
                newCursor = newNextAnchor + 1;
            }
        }

        return new MergeTextResult(joinLines(merged), conflicts);
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

    private int[] buildBaseToOtherMap(int size, List<LineMatch> matches) {
        int[] mapping = new int[size];
        for (int i = 0; i < size; i++) {
            mapping[i] = -1;
        }
        for (LineMatch match : matches) {
            mapping[match.baseIndex] = match.otherIndex;
        }
        return mapping;
    }

    private List<LineMatch> buildMatches(List<String> base, List<String> other) {
        int[][] dp = new int[base.size() + 1][other.size() + 1];
        for (int i = base.size() - 1; i >= 0; i--) {
            for (int j = other.size() - 1; j >= 0; j--) {
                if (base.get(i).equals(other.get(j))) {
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
            if (base.get(i).equals(other.get(j))) {
                matches.add(new LineMatch(i, j));
                i++;
                j++;
            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                i++;
            } else {
                j++;
            }
        }
        return matches;
    }

    public static class MergeTextResult {
        private final String mergedText;
        private final List<String> conflicts;

        public MergeTextResult(String mergedText, List<String> conflicts) {
            this.mergedText = mergedText;
            this.conflicts = conflicts;
        }

        public String getMergedText() {
            return mergedText;
        }

        public List<String> getConflicts() {
            return conflicts;
        }

        public boolean hasConflict() {
            return !conflicts.isEmpty();
        }
    }

    private static class LineMatch {
        private final int baseIndex;
        private final int otherIndex;

        private LineMatch(int baseIndex, int otherIndex) {
            this.baseIndex = baseIndex;
            this.otherIndex = otherIndex;
        }
    }
}
