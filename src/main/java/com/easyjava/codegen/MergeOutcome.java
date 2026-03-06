package com.easyjava.codegen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MergeOutcome {

    private final MergeStatus status;
    private final Path targetPath;
    private final List<String> conflictMessages;

    public MergeOutcome(MergeStatus status, Path targetPath) {
        this(status, targetPath, Collections.emptyList());
    }

    public MergeOutcome(MergeStatus status, Path targetPath, List<String> conflictMessages) {
        this.status = status;
        this.targetPath = targetPath;
        this.conflictMessages = Collections.unmodifiableList(new ArrayList<>(conflictMessages));
    }

    public MergeStatus getStatus() {
        return status;
    }

    public Path getTargetPath() {
        return targetPath;
    }

    public List<String> getConflictMessages() {
        return conflictMessages;
    }
}
