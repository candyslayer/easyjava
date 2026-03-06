package com.easyjava.codegen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

public class SafeGenerationEngineTest {

    @Test
    public void shouldAutoMergeDifferentJavaMethods() throws Exception {
        Path root = Files.createTempDirectory("easyjava-merge-java");
        SafeGenerationEngine engine = new SafeGenerationEngine(root);
        engine.prepareRun();

        Path target = root.resolve("src/main/java/demo/UserService.java");
        String base = """
                package demo;

                public class UserService {
                    public String findName() {
                        return "old";
                    }

                    public int count() {
                        return 1;
                    }
                }
                """;
        String local = """
                package demo;

                public class UserService {
                    public String findName() {
                        return "local";
                    }

                    public int count() {
                        return 1;
                    }
                }
                """;
        String newer = """
                package demo;

                public class UserService {
                    public String findName() {
                        return "old";
                    }

                    public int count() {
                        return 2;
                    }
                }
                """;

        assertEquals(MergeStatus.CREATED, engine.generate(target, base, CodegenFileType.JAVA).getStatus());
        Files.writeString(target, local, StandardCharsets.UTF_8);

        MergeOutcome outcome = engine.generate(target, newer, CodegenFileType.JAVA);
        String merged = Files.readString(target, StandardCharsets.UTF_8);

        assertEquals(MergeStatus.AUTO_MERGED, outcome.getStatus());
        assertTrue(merged.contains("return \"local\";"));
        assertTrue(merged.contains("return 2;"));
    }

    @Test
    public void shouldKeepCustomMapperXmlNodeAndUpdateCrudNode() throws Exception {
        Path root = Files.createTempDirectory("easyjava-merge-xml");
        SafeGenerationEngine engine = new SafeGenerationEngine(root);
        engine.prepareRun();

        Path target = root.resolve("src/main/resources/demo/UserMapper.xml");
        String base = """
                <?xml version="1.0" encoding="UTF-8"?>
                <mapper namespace="demo.UserMapper">
                    <select id="SelectById">select * from user where id = #{id}</select>
                    <update id="UpdateById">update user set name = #{name} where id = #{id}</update>
                </mapper>
                """;
        String local = """
                <?xml version="1.0" encoding="UTF-8"?>
                <mapper namespace="demo.UserMapper">
                    <select id="SelectById">select * from user where id = #{id}</select>
                    <update id="UpdateById">update user set name = #{name} where id = #{id}</update>
                    <select id="CustomQuery">select * from user where status = 'Y'</select>
                </mapper>
                """;
        String newer = """
                <?xml version="1.0" encoding="UTF-8"?>
                <mapper namespace="demo.UserMapper">
                    <select id="SelectById">select id,name from user where id = #{id}</select>
                    <update id="UpdateById">update user set name = #{name}, updated_at = now() where id = #{id}</update>
                </mapper>
                """;

        assertEquals(MergeStatus.CREATED, engine.generate(target, base, CodegenFileType.MAPPER_XML).getStatus());
        Files.writeString(target, local, StandardCharsets.UTF_8);

        MergeOutcome outcome = engine.generate(target, newer, CodegenFileType.MAPPER_XML);
        String merged = Files.readString(target, StandardCharsets.UTF_8);

        assertEquals(MergeStatus.AUTO_MERGED, outcome.getStatus());
        assertTrue(merged.contains("CustomQuery"));
        assertTrue(merged.contains("updated_at = now()"));
        assertTrue(merged.contains("select id,name from user"));
    }

    @Test
    public void shouldMarkConflictWhenSameJavaMethodChanged() throws Exception {
        Path root = Files.createTempDirectory("easyjava-merge-conflict");
        SafeGenerationEngine engine = new SafeGenerationEngine(root);
        engine.prepareRun();

        Path target = root.resolve("src/main/java/demo/UserController.java");
        String base = """
                package demo;

                public class UserController {
                    public String load() {
                        return "base";
                    }
                }
                """;
        String local = """
                package demo;

                public class UserController {
                    public String load() {
                        return "local";
                    }
                }
                """;
        String newer = """
                package demo;

                public class UserController {
                    public String load() {
                        return "new";
                    }
                }
                """;

        engine.generate(target, base, CodegenFileType.JAVA);
        Files.writeString(target, local, StandardCharsets.UTF_8);

        MergeOutcome outcome = engine.generate(target, newer, CodegenFileType.JAVA);
        String localAfter = Files.readString(target, StandardCharsets.UTF_8);
        Path conflictFile = root.resolve(".codegen/merge-result/conflicts/src/main/java/demo/UserController.java");
        String candidate = Files.readString(conflictFile, StandardCharsets.UTF_8);
        String report = Files.readString(root.resolve(".codegen/merge-result/conflict-report.txt"), StandardCharsets.UTF_8);

        assertEquals(MergeStatus.CONFLICT, outcome.getStatus());
        assertTrue(localAfter.contains("return \"local\";"));
        assertTrue(candidate.contains("<<<<<<< LOCAL"));
        assertTrue(candidate.contains(">>>>>>> NEW"));
        assertEquals("method:load()", outcome.getConflictBlocks().get(0).getBlockId());
        assertTrue(report.contains("FILE: src/main/java/demo/UserController.java"));
        assertTrue(report.contains("BLOCK: method:load()"));
    }

    @Test
    public void shouldResolveConflictByOverwritingWithNew() throws Exception {
        Path root = Files.createTempDirectory("easyjava-merge-resolve");
        SafeGenerationEngine engine = new SafeGenerationEngine(root);
        engine.prepareRun();

        Path target = root.resolve("src/main/java/demo/UserController.java");
        String base = """
                package demo;

                public class UserController {
                    public String load() {
                        return "base";
                    }
                }
                """;
        String local = """
                package demo;

                public class UserController {
                    public String load() {
                        return "local";
                    }
                }
                """;
        String newer = """
                package demo;

                public class UserController {
                    public String load() {
                        return "new";
                    }
                }
                """;

        engine.generate(target, base, CodegenFileType.JAVA);
        Files.writeString(target, local, StandardCharsets.UTF_8);
        engine.generate(target, newer, CodegenFileType.JAVA);

        ByteArrayInputStream input = new ByteArrayInputStream("1\n".getBytes(StandardCharsets.UTF_8));
        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;
        try {
            System.setIn(input);
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            engine.processConflictsInteractively();
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        String resolved = Files.readString(target, StandardCharsets.UTF_8);
        assertTrue(resolved.contains("return \"new\";"));
    }

    @Test
    public void shouldParseConsoleConflictChoice() {
        ByteArrayInputStream input = new ByteArrayInputStream("0\n3\n".getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ConsoleConflictResolver resolver = new ConsoleConflictResolver(input, new PrintStream(output));

        MergeOutcome outcome = MergeOutcome.conflict(Path.of("demo/UserMapper.xml"), "src/main/resources/demo/UserMapper.xml",
                CodegenFileType.MAPPER_XML, "", "<select id=\"x\">local</select>", "<select id=\"x\">new</select>",
                null, "<<<<<<< LOCAL\nlocal\n=======\nnew\n>>>>>>> NEW\n",
                List.of(new ConflictBlock("select:selectById", "", "local", "new",
                        "src/main/resources/demo/UserMapper.xml", CodegenFileType.MAPPER_XML, "XML 节点冲突")));

        outcome.setConflictOutputPath(Path.of("d:/java/project/easyjava/.codegen/merge-result/conflicts/src/main/resources/demo/UserMapper.xml"));

        assertEquals(ConflictResolutionChoice.WRITE_CONFLICT_TO_TARGET, resolver.resolve(outcome));
        String console = output.toString(StandardCharsets.UTF_8);
        assertTrue(console.contains("冲突文件已输出"));
        assertTrue(console.contains("冲突块: select:selectById"));
        assertTrue(console.contains("无效输入，请输入 1、2、3、4 或 5。"));
    }

    @Test
    public void shouldResolveConflictByWritingConflictMarkersToTarget() throws Exception {
        Path root = Files.createTempDirectory("easyjava-merge-conflict-target");
        SafeGenerationEngine engine = new SafeGenerationEngine(root);
        engine.prepareRun();

        Path target = root.resolve("src/main/java/demo/UserController.java");
        String base = """
                package demo;

                public class UserController {
                    public String load() {
                        return "base";
                    }
                }
                """;
        String local = """
                package demo;

                public class UserController {
                    public String load() {
                        return "local";
                    }
                }
                """;
        String newer = """
                package demo;

                public class UserController {
                    public String load() {
                        return "new";
                    }
                }
                """;

        engine.generate(target, base, CodegenFileType.JAVA);
        Files.writeString(target, local, StandardCharsets.UTF_8);
        engine.generate(target, newer, CodegenFileType.JAVA);

        ByteArrayInputStream input = new ByteArrayInputStream("3\n".getBytes(StandardCharsets.UTF_8));
        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;
        try {
            System.setIn(input);
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            engine.processConflictsInteractively();
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        String resolved = Files.readString(target, StandardCharsets.UTF_8);
        assertTrue(resolved.contains("<<<<<<< LOCAL"));
        assertTrue(resolved.contains(">>>>>>> NEW"));
    }

    @Test
    public void shouldTryMergeWhenBaseSnapshotIsMissing() throws Exception {
        Path root = Files.createTempDirectory("easyjava-missing-base");
        SafeGenerationEngine engine = new SafeGenerationEngine(root);
        engine.prepareRun();

        Path target = root.resolve("src/main/java/demo/UserService.java");
        String local = """
                package demo;

                public class UserService {
                    public String findName() {
                        return "local";
                    }
                }
                """;
        String newer = """
                package demo;

                public class UserService {
                    public String findName() {
                        return "local";
                    }

                    public int count() {
                        return 2;
                    }
                }
                """;

        Files.createDirectories(target.getParent());
        Files.writeString(target, local, StandardCharsets.UTF_8);

        MergeOutcome outcome = engine.generate(target, newer, CodegenFileType.JAVA);
        String merged = Files.readString(target, StandardCharsets.UTF_8);
        String snapshot = Files.readString(root.resolve(".codegen/snapshots/src/main/java/demo/UserService.java"),
                StandardCharsets.UTF_8);

        assertEquals(MergeStatus.AUTO_MERGED, outcome.getStatus());
        assertTrue(merged.contains("return \"local\";"));
        assertTrue(merged.contains("public int count()"));
        assertEquals(merged, snapshot);
    }

    @Test
    public void shouldNotNestConflictMarkersWhenLocalAlreadyContainsConflict() throws Exception {
        Path root = Files.createTempDirectory("easyjava-existing-conflict");
        SafeGenerationEngine engine = new SafeGenerationEngine(root);
        engine.prepareRun();

        Path target = root.resolve("src/main/java/demo/UserService.java");
        String local = """
                package demo;

                public class UserService {
                    <<<<<<< LOCAL
                    public String findName() {
                        return "local";
                    }
                    =======
                    public String findName() {
                        return "other";
                    }
                    >>>>>>> NEW
                }
                """;
        String newer = """
                package demo;

                public class UserService {
                    public String findName() {
                        return "new";
                    }
                }
                """;

        Files.createDirectories(target.getParent());
        Files.writeString(target, local, StandardCharsets.UTF_8);

        MergeOutcome outcome = engine.generate(target, newer, CodegenFileType.JAVA);
        String targetAfter = Files.readString(target, StandardCharsets.UTF_8);
        String conflictCopy = Files.readString(
                root.resolve(".codegen/merge-result/conflicts/src/main/java/demo/UserService.java"), StandardCharsets.UTF_8);

        assertEquals(MergeStatus.CONFLICT, outcome.getStatus());
        assertEquals(normalize(local), normalize(targetAfter));
        assertEquals(normalize(local), normalize(conflictCopy));
        assertEquals(1, countOccurrences(conflictCopy, "<<<<<<< LOCAL"));
        assertTrue(outcome.getConflictBlocks().get(0).getReason().contains("避免嵌套冲突"));
    }

    @Test
    public void shouldReuseExistingConflictMarkersFromMergeResult() throws Exception {
        Path root = Files.createTempDirectory("easyjava-merge-marker");
        SafeGenerationEngine engine = new SafeGenerationEngine(root);
        engine.prepareRun();

        Path target = root.resolve("src/main/java/demo/UserController.java");
        String base = """
                package demo;

                public class UserController {
                    public String load() {
                        return "base";
                    }
                }
                """;
        String local = """
                package demo;

                public class UserController {
                    public String load() {
                        return "local";
                    }
                }
                """;
        String newer = """
                package demo;

                public class UserController {
                    public String load() {
                        return "new";
                    }
                }
                """;

        engine.generate(target, base, CodegenFileType.JAVA);
        Files.writeString(target, local, StandardCharsets.UTF_8);

        MergeOutcome outcome = engine.generate(target, newer, CodegenFileType.JAVA);

        assertEquals(MergeStatus.CONFLICT, outcome.getStatus());
        assertEquals(normalize(outcome.getMergedContent()), normalize(outcome.getConflictMarkedContent()));
        assertEquals(1, countOccurrences(outcome.getConflictMarkedContent(), "<<<<<<< LOCAL"));
    }

    @Test
    public void shouldNotConflictWhenLocalAndNewDifferOnlyByLineEndings() throws Exception {
        Path root = Files.createTempDirectory("easyjava-equivalent");
        SafeGenerationEngine engine = new SafeGenerationEngine(root);
        engine.prepareRun();

        Path target = root.resolve("src/main/resources/demo/sample.txt");
        String base = "line1\nline2\n";
        String local = "line1\r\nline2\r\n";
        String newer = "line1\nline2\n";

        engine.generate(target, base, CodegenFileType.TEXT);
        Files.writeString(target, local, StandardCharsets.UTF_8);

        MergeOutcome outcome = engine.generate(target, newer, CodegenFileType.TEXT);

        assertEquals(MergeStatus.AUTO_MERGED, outcome.getStatus());
        assertEquals(normalize(local), normalize(Files.readString(target, StandardCharsets.UTF_8)));
    }

    private int countOccurrences(String text, String token) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }

    private String normalize(String text) {
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }
}
