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
    private final List<MergeOutcome> outcomes = new ArrayList<>();

    public SafeGenerationEngine(Path projectRoot) {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        this.codegenRoot = this.projectRoot.resolve(".codegen");
        this.snapshotRoot = codegenRoot.resolve("snapshots");
        this.tempRoot = codegenRoot.resolve("temp");
        this.mergeResultRoot = codegenRoot.resolve("merge-result");
        this.manifestStore = new CodegenManifestStore(codegenRoot.resolve("manifest.json"));
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
                outcome = record(new MergeOutcome(MergeStatus.CREATED, absoluteTarget));
            } else if (baseContent == null) {
                if (same(localContent, newContent)) {
                    writeFile(mergeFile, localContent);
                    updateSnapshot(manifest, relativePath, snapshotFile, fileType, newContent, localContent,
                            MergeStatus.KEPT_LOCAL);
                    outcome = record(new MergeOutcome(MergeStatus.KEPT_LOCAL, absoluteTarget));
                } else {
                    writeFile(mergeFile, newContent);
                    updateManifest(manifest, relativePath, snapshotFile, fileType, null, localContent,
                            MergeStatus.CONFLICT);
                    outcome = record(new MergeOutcome(MergeStatus.CONFLICT, absoluteTarget,
                            List.of("bootstrap-conflict:no-base-snapshot")));
                }
            } else if (same(localContent, baseContent) && !same(newContent, baseContent)) {
                writeFile(absoluteTarget, newContent);
                writeFile(mergeFile, newContent);
                updateSnapshot(manifest, relativePath, snapshotFile, fileType, newContent, newContent, MergeStatus.UPDATED);
                outcome = record(new MergeOutcome(MergeStatus.UPDATED, absoluteTarget));
            } else if (!same(localContent, baseContent) && same(newContent, baseContent)) {
                writeFile(mergeFile, localContent);
                updateManifest(manifest, relativePath, snapshotFile, fileType, baseContent, localContent,
                        MergeStatus.KEPT_LOCAL);
                outcome = record(new MergeOutcome(MergeStatus.KEPT_LOCAL, absoluteTarget));
            } else {
                TextThreeWayMerger.MergeTextResult mergeResult = merge(baseContent, localContent, newContent, fileType);
                if (mergeResult.hasConflict()) {
                    writeFile(mergeFile, mergeResult.getMergedText());
                    updateManifest(manifest, relativePath, snapshotFile, fileType, baseContent, localContent,
                            MergeStatus.CONFLICT);
                    outcome = record(new MergeOutcome(MergeStatus.CONFLICT, absoluteTarget, mergeResult.getConflicts()));
                } else {
                    writeFile(absoluteTarget, mergeResult.getMergedText());
                    writeFile(mergeFile, mergeResult.getMergedText());
                    updateSnapshot(manifest, relativePath, snapshotFile, fileType, newContent,
                            mergeResult.getMergedText(), MergeStatus.AUTO_MERGED);
                    outcome = record(new MergeOutcome(MergeStatus.AUTO_MERGED, absoluteTarget));
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

    private MergeOutcome record(MergeOutcome outcome) {
        outcomes.add(outcome);
        if (outcome.getStatus() == MergeStatus.CONFLICT) {
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

    private boolean same(String left, String right) {
        return normalize(left).equals(normalize(right));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r\n", "\n").replace('\r', '\n');
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
