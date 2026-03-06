package com.easyjava.codegen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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
        Path mergeCandidate = root.resolve(".codegen/merge-result/src/main/java/demo/UserController.java");
        String candidate = Files.readString(mergeCandidate, StandardCharsets.UTF_8);

        assertEquals(MergeStatus.CONFLICT, outcome.getStatus());
        assertTrue(localAfter.contains("return \"local\";"));
        assertTrue(candidate.contains("<<<<<<< LOCAL"));
    }
}
