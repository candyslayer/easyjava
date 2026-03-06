package com.easyjava.codegen;

import java.util.LinkedHashMap;
import java.util.Map;

public class CodegenManifest {

    private long updatedAt;
    private Map<String, Entry> files = new LinkedHashMap<>();

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, Entry> getFiles() {
        return files;
    }

    public void setFiles(Map<String, Entry> files) {
        this.files = files;
    }

    public static class Entry {
        private String relativePath;
        private String fileType;
        private String snapshotPath;
        private String snapshotHash;
        private String localHash;
        private String lastStatus;
        private long updatedAt;

        public String getRelativePath() {
            return relativePath;
        }

        public void setRelativePath(String relativePath) {
            this.relativePath = relativePath;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        public String getSnapshotPath() {
            return snapshotPath;
        }

        public void setSnapshotPath(String snapshotPath) {
            this.snapshotPath = snapshotPath;
        }

        public String getSnapshotHash() {
            return snapshotHash;
        }

        public void setSnapshotHash(String snapshotHash) {
            this.snapshotHash = snapshotHash;
        }

        public String getLocalHash() {
            return localHash;
        }

        public void setLocalHash(String localHash) {
            this.localHash = localHash;
        }

        public String getLastStatus() {
            return lastStatus;
        }

        public void setLastStatus(String lastStatus) {
            this.lastStatus = lastStatus;
        }

        public long getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(long updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
