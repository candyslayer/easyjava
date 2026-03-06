package com.easyjava.codegen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.StringWriter;

public class CodegenWriter {

    private CodegenWriter() {
    }

    public static void write(File outputFile, CodegenFileType fileType, WriterAction action) throws Exception {
        try (StringWriter stringWriter = new StringWriter();
                BufferedWriter bufferedWriter = new BufferedWriter(stringWriter)) {
            action.write(bufferedWriter);
            bufferedWriter.flush();
            SafeGenerationEngine.getInstance().generate(outputFile.toPath(), stringWriter.toString(), fileType);
        }
    }

    @FunctionalInterface
    public interface WriterAction {
        void write(BufferedWriter writer) throws Exception;
    }
}
