package com.easyjava.codegen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

public class JavaCodeMerger {

    private static final String CONFLICT_START_MARKER = "<<<<<<<";
    private static final String CONFLICT_SEPARATOR = "=======";
    private static final String CONFLICT_END_MARKER = ">>>>>>>";
    private static final String CONFLICT_REASON_EXISTING_MARKER = "Local 已包含冲突标记，停止再次合并以避免嵌套冲突";

    private final TextThreeWayMerger textMerger = new TextThreeWayMerger();
    private final ConflictMarkerRenderer conflictRenderer = new ConflictMarkerRenderer();

    public TextThreeWayMerger.MergeTextResult merge(String baseText, String localText, String newText) {
        String existingConflictText = chooseExistingConflictText(localText, newText);
        if (existingConflictText != null) {
            return existingConflictResult(baseText, localText, newText, existingConflictText);
        }
        try {
            CompilationUnit baseCu = parse(baseText);
            CompilationUnit localCu = parse(localText);
            CompilationUnit newCu = parse(newText);

            if (baseCu.getTypes().size() != 1 || localCu.getTypes().size() != 1 || newCu.getTypes().size() != 1) {
                return textMerger.merge(baseText, localText, newText);
            }

            TypeDeclaration<?> baseType = baseCu.getType(0);
            TypeDeclaration<?> localType = localCu.getType(0);
            TypeDeclaration<?> newType = newCu.getType(0);

            List<ConflictBlock> conflicts = new ArrayList<>();
            List<String> renderedMembers = new ArrayList<>();

            Map<String, BodyDeclaration<?>> baseMembers = memberMap(baseType);
            Map<String, BodyDeclaration<?>> localMembers = memberMap(localType);
            Map<String, BodyDeclaration<?>> newMembers = memberMap(newType);

            List<String> order = new ArrayList<>(newMembers.keySet());
            for (String key : localMembers.keySet()) {
                if (!order.contains(key) && !baseMembers.containsKey(key)) {
                    order.add(key);
                }
            }

            for (String key : order) {
                MergeChoice<BodyDeclaration<?>> memberChoice = mergeMember(baseMembers.get(key), localMembers.get(key),
                        newMembers.get(key), key);
                if (memberChoice.isConflict()) {
                    conflicts.addAll(memberChoice.getConflicts());
                    renderedMembers.add(memberChoice.getRenderedText());
                    continue;
                }
                if (memberChoice.getValue() != null) {
                    renderedMembers.add(memberChoice.getValue().toString());
                }
            }

            return new TextThreeWayMerger.MergeTextResult(buildCompilationUnit(baseCu, localCu, newCu, newType, renderedMembers),
                    conflicts);
        } catch (Exception e) {
            if (existingConflictText != null) {
                return existingConflictResult(baseText, localText, newText, existingConflictText);
            }
            return textMerger.merge(baseText, localText, newText);
        }
    }

    private CompilationUnit parse(String text) {
        return StaticJavaParser.parse(text == null || text.isBlank() ? "class Empty {}" : text);
    }

    private NodeList<ImportDeclaration> mergeImports(CompilationUnit baseCu, CompilationUnit localCu, CompilationUnit newCu) {
        Set<String> imports = new LinkedHashSet<>();
        addImports(imports, baseCu);
        addImports(imports, localCu);
        addImports(imports, newCu);

        NodeList<ImportDeclaration> result = new NodeList<>();
        for (String value : imports) {
            result.add(StaticJavaParser.parseImport(value));
        }
        return result;
    }

    private void addImports(Set<String> imports, CompilationUnit cu) {
        for (ImportDeclaration declaration : cu.getImports()) {
            imports.add(declaration.toString().trim());
        }
    }

    private MergeChoice<BodyDeclaration<?>> mergeMember(BodyDeclaration<?> baseMember, BodyDeclaration<?> localMember,
            BodyDeclaration<?> newMember, String key) {
        String base = stringify(baseMember);
        String local = stringify(localMember);
        String newer = stringify(newMember);
        String existingConflictText = chooseExistingConflictText(local, newer);

        if (existingConflictText != null) {
            ConflictBlock block = buildConflictBlock(key, baseMember, localMember, newMember, CONFLICT_REASON_EXISTING_MARKER);
            return MergeChoice.conflict(List.of(block), ensureTrailingLineSeparator(existingConflictText));
        }

        if (equalsText(local, base) && !equalsText(newer, base)) {
            return MergeChoice.value(cloneOrNull(newMember));
        }
        if (!equalsText(local, base) && equalsText(newer, base)) {
            return MergeChoice.value(cloneOrNull(localMember));
        }
        if (equalsText(local, newer)) {
            return MergeChoice.value(cloneOrNull(localMember));
        }
        if (baseMember == null) {
            if (localMember == null) {
                return MergeChoice.value(cloneOrNull(newMember));
            }
            if (newMember == null) {
                return MergeChoice.value(cloneOrNull(localMember));
            }
            ConflictBlock block = buildConflictBlock(key, baseMember, localMember, newMember,
                    "Java 成员在 Local 与 New 中同时新增且内容不同");
            return MergeChoice.conflict(List.of(block), renderConflict(baseMember, localMember, newMember, block));
        }
        if (localMember == null || newMember == null) {
            ConflictBlock block = buildConflictBlock(key, baseMember, localMember, newMember, "Java 成员出现删除/修改冲突");
            return MergeChoice.conflict(List.of(block), renderConflict(baseMember, localMember, newMember, block));
        }

        TextThreeWayMerger.MergeTextResult mergedText = textMerger.merge(base, local, newer);
        if (mergedText.hasConflict()) {
            return MergeChoice.conflict(adaptMemberConflicts(key, mergedText.getConflictBlocks()), mergedText.getMergedText());
        }
        try {
            return MergeChoice.value(StaticJavaParser.parseBodyDeclaration(mergedText.getMergedText()));
        } catch (Exception e) {
            ConflictBlock block = buildConflictBlock(key, baseMember, localMember, newMember, "Java 成员自动合并后无法重新解析");
            return MergeChoice.conflict(List.of(block), renderConflict(baseMember, localMember, newMember, block));
        }
    }

    private Map<String, BodyDeclaration<?>> memberMap(TypeDeclaration<?> type) {
        Map<String, BodyDeclaration<?>> members = new LinkedHashMap<>();
        for (BodyDeclaration<?> member : type.getMembers()) {
            members.put(buildKey(member), member);
        }
        return members;
    }

    private String buildKey(BodyDeclaration<?> member) {
        if (member instanceof MethodDeclaration method) {
            return "method:" + signature(method);
        }
        if (member instanceof ConstructorDeclaration constructor) {
            return "constructor:" + signature(constructor);
        }
        if (member instanceof FieldDeclaration field) {
            StringBuilder builder = new StringBuilder("field:");
            field.getVariables().forEach(variable -> builder.append(variable.getNameAsString()).append(","));
            return builder.toString();
        }
        if (member instanceof InitializerDeclaration initializer) {
            return initializer.isStatic() ? "initializer:static" : "initializer:instance";
        }
        if (member instanceof TypeDeclaration<?> type) {
            return "type:" + type.getNameAsString();
        }
        return member.getClass().getSimpleName() + ":" + member.toString().hashCode();
    }

    private String signature(CallableDeclaration<?> declaration) {
        StringBuilder builder = new StringBuilder(declaration.getNameAsString()).append("(");
        declaration.getParameters().forEach(parameter -> builder.append(parameter.getType()).append(","));
        builder.append(")");
        return builder.toString();
    }

    private boolean equalsText(String left, String right) {
        return normalize(left).equals(normalize(right));
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);
        List<String> compacted = new ArrayList<>();
        boolean lastBlank = false;
        for (String line : lines) {
            String trimmedLine = stripTrailingWhitespace(line);
            if (trimmedLine.isBlank()) {
                if (!lastBlank) {
                    compacted.add("");
                    lastBlank = true;
                }
            } else {
                compacted.add(trimmedLine);
                lastBlank = false;
            }
        }

        int start = 0;
        int end = compacted.size();
        while (start < end && compacted.get(start).isEmpty()) {
            start++;
        }
        while (end > start && compacted.get(end - 1).isEmpty()) {
            end--;
        }
        if (start >= end) {
            return "";
        }
        return String.join("\n", compacted.subList(start, end));
    }

    private String stringify(Object value) {
        return value == null ? "" : value.toString();
    }

    private BodyDeclaration<?> cloneOrNull(BodyDeclaration<?> member) {
        return member == null ? null : member.clone();
    }

    private ConflictBlock buildConflictBlock(String key, BodyDeclaration<?> baseMember, BodyDeclaration<?> localMember,
            BodyDeclaration<?> newMember, String reason) {
        return new ConflictBlock(key, stringify(baseMember), stringify(localMember), stringify(newMember), "",
                CodegenFileType.JAVA, reason);
    }

    private String renderConflict(BodyDeclaration<?> baseMember, BodyDeclaration<?> localMember, BodyDeclaration<?> newMember,
            ConflictBlock block) {
        String indent = detectIndent(localMember, newMember, baseMember);
        return conflictRenderer.render(block, indent) + System.lineSeparator();
    }

    private List<ConflictBlock> adaptMemberConflicts(String key, List<ConflictBlock> conflicts) {
        List<ConflictBlock> result = new ArrayList<>();
        for (int i = 0; i < conflicts.size(); i++) {
            ConflictBlock block = conflicts.get(i);
            String blockId = i == 0 ? key : key + "#" + (i + 1);
            result.add(new ConflictBlock(blockId, block.getBaseContent(), block.getLocalContent(), block.getNewContent(), "",
                    CodegenFileType.JAVA, "Java 成员内部代码块冲突"));
        }
        return result;
    }

    private String detectIndent(BodyDeclaration<?> localMember, BodyDeclaration<?> newMember, BodyDeclaration<?> baseMember) {
        String content = !stringify(localMember).isBlank() ? stringify(localMember)
                : (!stringify(newMember).isBlank() ? stringify(newMember) : stringify(baseMember));
        String normalized = content.replace("\r\n", "\n").replace('\r', '\n');
        int index = normalized.indexOf('\n');
        if (index < 0 || index + 1 >= normalized.length()) {
            return "    ";
        }
        int cursor = index + 1;
        StringBuilder indent = new StringBuilder();
        while (cursor < normalized.length()) {
            char ch = normalized.charAt(cursor);
            if (ch == ' ' || ch == '\t') {
                indent.append(ch);
                cursor++;
                continue;
            }
            break;
        }
        return indent.length() == 0 ? "    " : indent.toString();
    }

    private String buildCompilationUnit(CompilationUnit baseCu, CompilationUnit localCu, CompilationUnit newCu,
            TypeDeclaration<?> newType, List<String> renderedMembers) {
        CompilationUnit resultCu = new CompilationUnit();
        newCu.getPackageDeclaration().ifPresent(resultCu::setPackageDeclaration);
        resultCu.setImports(mergeImports(baseCu, localCu, newCu));

        TypeDeclaration<?> skeleton = newType.clone();
        skeleton.getMembers().clear();
        String typeText = skeleton.toString();
        int openBrace = typeText.indexOf('{');
        int closeBrace = typeText.lastIndexOf('}');
        StringBuilder builder = new StringBuilder();
        builder.append(resultCu.toString());
        if (builder.length() > 0 && !builder.toString().endsWith(System.lineSeparator() + System.lineSeparator())) {
            builder.append(System.lineSeparator());
        }
        builder.append(typeText, 0, openBrace + 1).append(System.lineSeparator()).append(System.lineSeparator());
        for (String member : renderedMembers) {
            builder.append(member);
            if (!member.endsWith(System.lineSeparator())) {
                builder.append(System.lineSeparator());
            }
            builder.append(System.lineSeparator());
        }
        builder.append(typeText.substring(closeBrace));
        if (!builder.toString().endsWith(System.lineSeparator())) {
            builder.append(System.lineSeparator());
        }
        return builder.toString();
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

    private String chooseExistingConflictText(String localText, String newText) {
        if (containsConflictMarker(localText)) {
            return safeText(localText);
        }
        if (containsConflictMarker(newText)) {
            return safeText(newText);
        }
        return null;
    }

    private TextThreeWayMerger.MergeTextResult existingConflictResult(String baseText, String localText, String newText,
            String existingConflictText) {
        return new TextThreeWayMerger.MergeTextResult(existingConflictText,
                List.of(new ConflictBlock("java:file-existing-conflict", safeText(baseText), safeText(localText),
                        safeText(newText), "", CodegenFileType.JAVA, CONFLICT_REASON_EXISTING_MARKER)));
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }

    private String ensureTrailingLineSeparator(String text) {
        String value = safeText(text);
        if (value.isEmpty() || value.endsWith("\n") || value.endsWith("\r")) {
            return value;
        }
        return value + System.lineSeparator();
    }

    private String stripTrailingWhitespace(String line) {
        int end = line.length();
        while (end > 0 && Character.isWhitespace(line.charAt(end - 1))) {
            end--;
        }
        return line.substring(0, end);
    }

    private static class MergeChoice<T> {
        private final T value;
        private final List<ConflictBlock> conflicts;
        private final String renderedText;

        private MergeChoice(T value, List<ConflictBlock> conflicts, String renderedText) {
            this.value = value;
            this.conflicts = conflicts;
            this.renderedText = renderedText;
        }

        private static <T> MergeChoice<T> value(T value) {
            return new MergeChoice<>(value, List.of(), null);
        }

        private static <T> MergeChoice<T> conflict(List<ConflictBlock> conflicts, String renderedText) {
            return new MergeChoice<>(null, conflicts, renderedText);
        }

        private boolean isConflict() {
            return !conflicts.isEmpty();
        }

        private T getValue() {
            return value;
        }

        private List<ConflictBlock> getConflicts() {
            return conflicts;
        }

        private String getRenderedText() {
            return renderedText;
        }
    }
}
