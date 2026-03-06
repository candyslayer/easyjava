package com.easyjava.codegen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafeGenerationEngine {

    private static final Logger log = LoggerFactory.getLogger(SafeGenerationEngine.class);
    private static final SafeGenerationEngine INSTANCE = new SafeGenerationEngine(Paths.get("").toAbsolutePath());

    private final Path projectRoot;
    private final Path codegenRoot;
    private final Path snapshotRoot;
    private final Path tempRoot;
    private final Path mergeResultRoot;
    private final CodegenManifestStore manifestStore;
    private final JavaCodeMerger javaMerger = new JavaCodeMerger();
    private final MapperXmlMerger xmlMerger = new MapperXmlMerger();
    private final TextThreeWayMerger textMerger = new TextThreeWayMerger();
    private final ConflictFileWriter conflictFileWriter;
    private final ConflictMarkerRenderer conflictMarkerRenderer = new ConflictMarkerRenderer();
    private final List<MergeOutcome> outcomes = new ArrayList<>();

    public SafeGenerationEngine(Path projectRoot) {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        this.codegenRoot = this.projectRoot.resolve(".codegen");
        this.snapshotRoot = codegenRoot.resolve("snapshots");
        this.tempRoot = codegenRoot.resolve("temp");
        this.mergeResultRoot = codegenRoot.resolve("merge-result");
        mergeResultRoot.resolve("conflicts");
        this.manifestStore = new CodegenManifestStore(codegenRoot.resolve("manifest.json"));
        this.conflictFileWriter = new ConflictFileWriter(mergeResultRoot);
    }

    public static SafeGenerationEngine getInstance() {
        return INSTANCE;
    }

    public synchronized void prepareRun() {
        try {
            Files.createDirectories(snapshotRoot);
            recreateDirectory(tempRoot);
            recreateDirectory(mergeResultRoot);
            outcomes.clear();
        } catch (Exception e) {
            throw new IllegalStateException("初始化.codegen目录失败", e);
        }
    }

    public synchronized MergeOutcome generate(Path targetFile, String newContent, CodegenFileType fileType) {
        try {
            Path absoluteTarget = targetFile.toAbsolutePath().normalize();
            Files.createDirectories(absoluteTarget.getParent());

            CodegenManifest manifest = manifestStore.load();
            String relativePath = toRelativePath(absoluteTarget);
            Path tempFile = tempRoot.resolve(relativePath);
            Path mergeFile = mergeResultRoot.resolve(relativePath);
            Path snapshotFile = snapshotRoot.resolve(relativePath);

            writeFile(tempFile, newContent);

            String localContent = Files.exists(absoluteTarget)
                    ? Files.readString(absoluteTarget, StandardCharsets.UTF_8)
                    : null;
            String baseContent = Files.exists(snapshotFile)
                    ? Files.readString(snapshotFile, StandardCharsets.UTF_8)
                    : null;

            MergeOutcome outcome;
            if (localContent == null) {
                writeFile(absoluteTarget, newContent);
                writeFile(mergeFile, newContent);
                updateSnapshot(manifest, relativePath, snapshotFile, fileType, newContent, newContent, MergeStatus.CREATED);
                outcome = record(MergeOutcome.simple(MergeStatus.CREATED, absoluteTarget, relativePath, fileType, baseContent,
                        null, newContent, newContent));
            } else if (shouldTreatAsExistingConflict(localContent) && !isEquivalentAfterNormalize(localContent, newContent)) {
                outcome = recordExistingConflict(manifest, absoluteTarget, relativePath, snapshotFile, mergeFile, fileType,
                        baseContent, localContent, newContent);
            } else if (baseContent == null) {
                if (same(localContent, newContent)) {
                    writeFile(mergeFile, localContent);
                    updateSnapshot(manifest, relativePath, snapshotFile, fileType, newContent, localContent,
                            MergeStatus.KEPT_LOCAL);
                    outcome = record(MergeOutcome.simple(MergeStatus.KEPT_LOCAL, absoluteTarget, relativePath, fileType,
                            null, localContent, newContent, localContent));
                } else {
                    outcome = handleMissingBase(manifest, absoluteTarget, relativePath, snapshotFile, mergeFile, fileType,
                            localContent, newContent);
                }
            } else if (same(localContent, baseContent) && !same(newContent, baseContent)) {
                writeFile(absoluteTarget, newContent);
                writeFile(mergeFile, newContent);
                updateSnapshot(manifest, relativePath, snapshotFile, fileType, newContent, newContent, MergeStatus.UPDATED);
                outcome = record(MergeOutcome.simple(MergeStatus.UPDATED, absoluteTarget, relativePath, fileType, baseContent,
                        localContent, newContent, newContent));
            } else if (!same(localContent, baseContent) && same(newContent, baseContent)) {
                writeFile(mergeFile, localContent);
                updateManifest(manifest, relativePath, snapshotFile, fileType, baseContent, localContent,
                        MergeStatus.KEPT_LOCAL);
                outcome = record(MergeOutcome.simple(MergeStatus.KEPT_LOCAL, absoluteTarget, relativePath, fileType,
                        baseContent, localContent, newContent, localContent));
            } else {
                if (isEquivalentAfterNormalize(localContent, newContent)) {
                    writeFile(mergeFile, localContent);
                    updateSnapshot(manifest, relativePath, snapshotFile, fileType, localContent, localContent,
                            MergeStatus.AUTO_MERGED);
                    outcome = record(MergeOutcome.simple(MergeStatus.AUTO_MERGED, absoluteTarget, relativePath, fileType,
                            baseContent, localContent, newContent, localContent));
                }
                else {
                    TextThreeWayMerger.MergeTextResult mergeResult = merge(baseContent, localContent, newContent, fileType);
                    if (mergeResult.hasConflict()) {
                        MergeOutcome resolvedEquivalent = resolveEquivalentConflict(manifest, absoluteTarget, relativePath,
                                snapshotFile, mergeFile, fileType, baseContent, localContent, newContent, mergeResult);
                        if (resolvedEquivalent != null) {
                            outcome = resolvedEquivalent;
                        } else {
                            writeFile(mergeFile, mergeResult.getMergedText());
                            updateManifest(manifest, relativePath, snapshotFile, fileType, baseContent, localContent,
                                    MergeStatus.CONFLICT);
                            String conflictMarkedContent = resolveConflictMarkedContent(mergeResult);
                            outcome = record(MergeOutcome.conflict(absoluteTarget, relativePath, fileType, baseContent,
                                    localContent, newContent, mergeResult.getMergedText(), conflictMarkedContent,
                                    enrichConflictBlocks(relativePath, fileType, mergeResult.getConflictBlocks())));
                        }
                    } else {
                        writeFile(absoluteTarget, mergeResult.getMergedText());
                        writeFile(mergeFile, mergeResult.getMergedText());
                        updateSnapshot(manifest, relativePath, snapshotFile, fileType, newContent,
                                mergeResult.getMergedText(), MergeStatus.AUTO_MERGED);
                        outcome = record(MergeOutcome.simple(MergeStatus.AUTO_MERGED, absoluteTarget, relativePath, fileType,
                                baseContent, localContent, newContent, mergeResult.getMergedText()));
                    }
                }
            }

            manifestStore.save(manifest);
            return outcome;
        } catch (Exception e) {
            throw new IllegalStateException("安全生成失败: " + targetFile, e);
        }
    }

    public synchronized List<MergeOutcome> getOutcomes() {
        return new ArrayList<>(outcomes);
    }

    public synchronized void processConflictsInteractively() {
        List<MergeOutcome> conflicts = outcomes.stream()
                .filter(outcome -> outcome.getStatus() == MergeStatus.CONFLICT)
                .collect(Collectors.toList());
        if (conflicts.isEmpty()) {
            return;
        }

        ConsoleConflictResolver resolver = new ConsoleConflictResolver(System.in, System.out);
        for (MergeOutcome outcome : conflicts) {
            ConflictResolutionChoice choice = resolver.resolve(outcome);
            outcome.setResolutionChoice(choice);
            applyConflictChoice(outcome, choice);
        }
    }

    private MergeOutcome record(MergeOutcome outcome) {
        outcomes.add(outcome);
        if (outcome.hasConflict()) {
            persistConflictArtifacts(outcome);
            log.warn("检测到冲突: {}", toRelativePath(outcome.getTargetPath()));
        } else {
            log.info("安全生成 {} -> {}", outcome.getStatus(), toRelativePath(outcome.getTargetPath()));
        }
        return outcome;
    }

    private TextThreeWayMerger.MergeTextResult merge(String baseContent, String localContent, String newContent,
            CodegenFileType fileType) {
        String safeBase = baseContent == null ? "" : baseContent;
        String safeLocal = localContent == null ? "" : localContent;
        String safeNew = newContent == null ? "" : newContent;
        return switch (fileType) {
            case JAVA -> javaMerger.merge(safeBase, safeLocal, safeNew);
            case MAPPER_XML -> xmlMerger.merge(safeBase, safeLocal, safeNew);
            case TEXT -> textMerger.merge(safeBase, safeLocal, safeNew);
        };
    }

    private List<ConflictBlock> enrichConflictBlocks(String relativePath, CodegenFileType fileType,
            List<ConflictBlock> conflictBlocks) {
        List<ConflictBlock> result = new ArrayList<>();
        for (ConflictBlock block : conflictBlocks) {
            result.add(block.withFileContext(relativePath, fileType));
        }
        return result;
    }

    private MergeOutcome handleMissingBase(CodegenManifest manifest, Path absoluteTarget, String relativePath, Path snapshotFile,
            Path mergeFile, CodegenFileType fileType, String localContent, String newContent) throws IOException {
        if (isEquivalentAfterNormalize(localContent, newContent)) {
            writeFile(mergeFile, localContent);
            updateSnapshot(manifest, relativePath, snapshotFile, fileType, localContent, localContent,
                    MergeStatus.KEPT_LOCAL);
            return record(MergeOutcome.simple(MergeStatus.KEPT_LOCAL, absoluteTarget, relativePath, fileType, null,
                    localContent, newContent, localContent));
        }

        TextThreeWayMerger.MergeTextResult mergeResult = tryMergeWithoutBase(localContent, newContent, fileType);
        if (!mergeResult.hasConflict()) {
            String mergedText = mergeResult.getMergedText();
            writeFile(absoluteTarget, mergedText);
            writeFile(mergeFile, mergedText);
            updateSnapshot(manifest, relativePath, snapshotFile, fileType, mergedText, mergedText, MergeStatus.AUTO_MERGED);
            return record(MergeOutcome.simple(MergeStatus.AUTO_MERGED, absoluteTarget, relativePath, fileType, null,
                    localContent, newContent, mergedText));
        }

        MergeOutcome resolvedEquivalent = resolveEquivalentConflict(manifest, absoluteTarget, relativePath, snapshotFile,
                mergeFile, fileType, null, localContent, newContent, mergeResult);
        if (resolvedEquivalent != null) {
            return resolvedEquivalent;
        }

        if (shouldTreatAsExistingConflict(localContent)) {
            return recordExistingConflict(manifest, absoluteTarget, relativePath, snapshotFile, mergeFile, fileType,
                    null, localContent, newContent);
        }

        writeFile(mergeFile, mergeResult.getMergedText());
        updateManifest(manifest, relativePath, snapshotFile, fileType, null, localContent, MergeStatus.CONFLICT);
        String conflictMarkedContent = resolveConflictMarkedContent(mergeResult);
        return record(MergeOutcome.conflict(absoluteTarget, relativePath, fileType, null, localContent, newContent,
                mergeResult.getMergedText(), conflictMarkedContent,
                enrichConflictBlocks(relativePath, fileType, mergeResult.getConflictBlocks())));
    }

    private MergeOutcome recordExistingConflict(CodegenManifest manifest, Path absoluteTarget, String relativePath,
            Path snapshotFile, Path mergeFile, CodegenFileType fileType, String baseContent, String localContent,
            String newContent) throws IOException {
        List<ConflictBlock> blocks = List.of(new ConflictBlock("file:" + relativePath, "", localContent, newContent,
                relativePath, fileType, "检测到 Local 已包含冲突标记，保留 Local 原状以避免嵌套冲突"));
        writeFile(mergeFile, localContent);
        updateManifest(manifest, relativePath, snapshotFile, fileType, baseContent, localContent, MergeStatus.CONFLICT);
        return record(MergeOutcome.conflict(absoluteTarget, relativePath, fileType, baseContent, localContent, newContent,
                localContent, localContent, blocks));
    }

    private TextThreeWayMerger.MergeTextResult tryMergeWithoutBase(String localContent, String newContent,
            CodegenFileType fileType) {
        return merge("", localContent, newContent, fileType);
    }

    private MergeOutcome resolveEquivalentConflict(CodegenManifest manifest, Path absoluteTarget, String relativePath,
            Path snapshotFile, Path mergeFile, CodegenFileType fileType, String baseContent, String localContent,
            String newContent, TextThreeWayMerger.MergeTextResult mergeResult) throws IOException {
        String mergedText = mergeResult.getMergedText();
        if (isEquivalentAfterNormalize(mergedText, localContent)) {
            writeFile(mergeFile, localContent);
            updateSnapshot(manifest, relativePath, snapshotFile, fileType, localContent, localContent,
                    MergeStatus.AUTO_MERGED);
            return record(MergeOutcome.simple(MergeStatus.AUTO_MERGED, absoluteTarget, relativePath, fileType, baseContent,
                    localContent, newContent, localContent));
        }
        if (isEquivalentAfterNormalize(mergedText, newContent)) {
            writeFile(absoluteTarget, newContent);
            writeFile(mergeFile, newContent);
            updateSnapshot(manifest, relativePath, snapshotFile, fileType, newContent, newContent,
                    MergeStatus.AUTO_MERGED);
            return record(MergeOutcome.simple(MergeStatus.AUTO_MERGED, absoluteTarget, relativePath, fileType, baseContent,
                    localContent, newContent, newContent));
        }
        return null;
    }

    private String resolveConflictMarkedContent(TextThreeWayMerger.MergeTextResult mergeResult) {
        String mergedText = mergeResult.getMergedText();
        if (containsConflictMarkers(mergedText)) {
            return mergedText;
        }
        return conflictMarkerRenderer.renderConflictFileContent(mergedText, mergeResult.getConflictBlocks());
    }

    private boolean isEquivalentAfterNormalize(String left, String right) {
        return normalizeForEquivalence(left).equals(normalizeForEquivalence(right));
    }

    private boolean shouldTreatAsExistingConflict(String text) {
        return containsConflictMarkers(text);
    }

    private boolean containsConflictMarkers(String text) {
        String normalized = normalize(text);
        return normalized.contains("<<<<<<<")
                && normalized.contains("=======")
                && normalized.contains(">>>>>>>");
    }

    private void updateSnapshot(CodegenManifest manifest, String relativePath, Path snapshotFile, CodegenFileType fileType,
            String snapshotContent, String localContent, MergeStatus status) throws IOException {
        writeFile(snapshotFile, snapshotContent);
        updateManifest(manifest, relativePath, snapshotFile, fileType, snapshotContent, localContent, status);
    }

    private void updateManifest(CodegenManifest manifest, String relativePath, Path snapshotFile, CodegenFileType fileType,
            String snapshotContent, String localContent, MergeStatus status) {
        CodegenManifest.Entry entry = manifest.getFiles().getOrDefault(relativePath, new CodegenManifest.Entry());
        entry.setRelativePath(relativePath);
        entry.setFileType(fileType.name());
        entry.setSnapshotPath(toRelativePath(snapshotFile));
        entry.setSnapshotHash(hash(snapshotContent));
        entry.setLocalHash(hash(localContent));
        entry.setLastStatus(status.name());
        entry.setUpdatedAt(System.currentTimeMillis());
        manifest.getFiles().put(relativePath, entry);
    }

    private void writeFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content == null ? "" : content, StandardCharsets.UTF_8);
    }

    private void persistConflictArtifacts(MergeOutcome outcome) {
        try {
            conflictFileWriter.writeConflictFile(outcome);
            conflictFileWriter.writeReport(outcomes);
        } catch (IOException e) {
            throw new IllegalStateException("写出冲突文件失败: " + outcome.getRelativePath(), e);
        }
    }

    private void applyConflictChoice(MergeOutcome outcome, ConflictResolutionChoice choice) {
        try {
            switch (choice) {
                case USE_NEW:
                    applyNewVersion(outcome);
                    System.out.println("已覆盖生成目录文件为 New: " + outcome.getRelativePath());
                    break;
                case KEEP_LOCAL:
                    System.out.println("已保留生成目录中的 Local 文件: " + outcome.getRelativePath());
                    break;
                case WRITE_CONFLICT_TO_TARGET:
                    applyConflictMarkedVersion(outcome);
                    System.out.println("已用冲突标记内容覆盖生成目录文件: " + outcome.getRelativePath());
                    break;
                case KEEP_CONFLICT_COPY_ONLY:
                    System.out.println("目标文件未修改，已保留冲突文件供手工处理: " + outcome.getConflictOutputPath());
                    break;
                case SKIP:
                    System.out.println("已跳过: " + outcome.getRelativePath());
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            throw new IllegalStateException("处理冲突文件失败: " + outcome.getRelativePath(), e);
        }
    }

    private void applyNewVersion(MergeOutcome outcome) throws IOException {
        writeFile(outcome.getTargetPath(), outcome.getNewContent());
        writeFile(mergeResultRoot.resolve(outcome.getRelativePath()), outcome.getNewContent());
        Path snapshotFile = snapshotRoot.resolve(outcome.getRelativePath());
        CodegenManifest manifest = manifestStore.load();
        updateSnapshot(manifest, outcome.getRelativePath(), snapshotFile, outcome.getFileType(), outcome.getNewContent(),
                outcome.getNewContent(), MergeStatus.UPDATED);
        manifestStore.save(manifest);
    }

    private void applyConflictMarkedVersion(MergeOutcome outcome) throws IOException {
        String conflictMarkedContent = outcome.getConflictMarkedContent();
        writeFile(outcome.getTargetPath(), conflictMarkedContent);
        writeFile(mergeResultRoot.resolve(outcome.getRelativePath()), conflictMarkedContent);
    }

    private boolean same(String left, String right) {
        return normalize(left).equals(normalize(right));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r\n", "\n").replace('\r', '\n');
    }

    private String normalizeForEquivalence(String value) {
        String normalized = normalize(value);
        String[] lines = normalized.split("\n", -1);
        List<String> canonicalLines = new ArrayList<>();
        boolean previousBlank = false;
        for (String line : lines) {
            String trimmedLine = stripTrailingWhitespace(line);
            if (trimmedLine.isBlank()) {
                if (!previousBlank) {
                    canonicalLines.add("");
                    previousBlank = true;
                }
            } else {
                canonicalLines.add(trimmedLine);
                previousBlank = false;
            }
        }
        int start = 0;
        int end = canonicalLines.size();
        while (start < end && canonicalLines.get(start).isEmpty()) {
            start++;
        }
        while (end > start && canonicalLines.get(end - 1).isEmpty()) {
            end--;
        }
        return String.join("\n", canonicalLines.subList(start, end));
    }

    private String stripTrailingWhitespace(String line) {
        int end = line.length();
        while (end > 0 && Character.isWhitespace(line.charAt(end - 1))) {
            end--;
        }
        return line.substring(0, end);
    }

    private String hash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(normalize(content).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte hashByte : hashBytes) {
                builder.append(String.format("%02x", hashByte));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new IllegalStateException("计算hash失败", e);
        }
    }

    private String toRelativePath(Path path) {
        Path absolute = path.toAbsolutePath().normalize();
        if (absolute.startsWith(projectRoot)) {
            return projectRoot.relativize(absolute).toString().replace('\\', '/');
        }
        return absolute.toString().replace(':', '_').replace('\\', '/');
    }

    private void recreateDirectory(Path root) throws IOException {
        if (Files.exists(root)) {
            Files.walk(root)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new IllegalStateException("清理目录失败: " + path, e);
                        }
                    });
        }
        Files.createDirectories(root);
    }
}
