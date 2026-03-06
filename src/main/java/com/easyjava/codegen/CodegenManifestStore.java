package com.easyjava.codegen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class CodegenManifestStore {

    private final Path manifestPath;

    public CodegenManifestStore(Path manifestPath) {
        this.manifestPath = manifestPath;
    }

    public CodegenManifest load() throws IOException {
        if (!Files.exists(manifestPath)) {
            return new CodegenManifest();
        }
        String content = Files.readString(manifestPath, StandardCharsets.UTF_8);
        if (content == null || content.trim().isEmpty()) {
            return new CodegenManifest();
        }
        CodegenManifest manifest = JSON.parseObject(content, CodegenManifest.class);
        return manifest == null ? new CodegenManifest() : manifest;
    }

    public void save(CodegenManifest manifest) throws IOException {
        manifest.setUpdatedAt(System.currentTimeMillis());
        Files.createDirectories(manifestPath.getParent());
        Files.writeString(manifestPath,
                JSON.toJSONString(manifest, SerializerFeature.PrettyFormat,
                        SerializerFeature.DisableCircularReferenceDetect),
                StandardCharsets.UTF_8);
    }
}
