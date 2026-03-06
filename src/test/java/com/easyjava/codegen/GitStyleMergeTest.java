package com.easyjava.codegen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GitStyleMergeTest {

    private final JavaCodeMerger javaMerger = new JavaCodeMerger();
    private final TextThreeWayMerger textMerger = new TextThreeWayMerger();

    @Test
    public void case1ShouldAutoMergeWhenLocalAndNewModifyDifferentMethods() {
        String base = """
                package demo;

                public class Sample {
                    public String methodA() {
                        return "baseA";
                    }

                    public String methodB() {
                        return "baseB";
                    }
                }
                """;
        String local = """
                package demo;

                public class Sample {
                    public String methodA() {
                        return "localA";
                    }

                    public String methodB() {
                        return "baseB";
                    }
                }
                """;
        String newer = """
                package demo;

                public class Sample {
                    public String methodA() {
                        return "baseA";
                    }

                    public String methodB() {
                        return "newB";
                    }
                }
                """;

        TextThreeWayMerger.MergeTextResult result = javaMerger.merge(base, local, newer);

        assertFalse(result.hasConflict());
        assertTrue(result.getMergedText().contains("return \"localA\";"));
        assertTrue(result.getMergedText().contains("return \"newB\";"));
    }

    @Test
    public void case2ShouldKeepLocalAddedMethodWhileApplyingNewChangeElsewhere() {
        String base = """
                package demo;

                public class Sample {
                    public String load() {
                        return "base";
                    }
                }
                """;
        String local = """
                package demo;

                public class Sample {
                    public String load() {
                        return "base";
                    }

                    public String custom() {
                        return "local";
                    }
                }
                """;
        String newer = """
                package demo;

                public class Sample {
                    public String load() {
                        return "new";
                    }
                }
                """;

        TextThreeWayMerger.MergeTextResult result = javaMerger.merge(base, local, newer);

        assertFalse(result.hasConflict());
        assertTrue(result.getMergedText().contains("return \"new\";"));
        assertTrue(result.getMergedText().contains("String custom()"));
        assertTrue(result.getMergedText().contains("return \"local\";"));
    }

    @Test
    public void case3ShouldReportConflictWhenLocalAndNewModifySameLine() {
        String base = """
                public class Sample {
                    void run() {
                        int value = 1;
                    }
                }
                """;
        String local = """
                public class Sample {
                    void run() {
                        int value = 2;
                    }
                }
                """;
        String newer = """
                public class Sample {
                    void run() {
                        int value = 3;
                    }
                }
                """;

        TextThreeWayMerger.MergeTextResult result = javaMerger.merge(base, local, newer);

        assertTrue(result.hasConflict());
        assertEquals("method:run()", result.getConflictBlocks().get(0).getBlockId());
        assertTrue(result.getMergedText().contains("<<<<<<< LOCAL"));
        assertTrue(result.getMergedText().contains("======="));
        assertTrue(result.getMergedText().contains(">>>>>>> NEW"));
        assertTrue(result.getMergedText().contains("int value = 2;"));
        assertTrue(result.getMergedText().contains("int value = 3;"));
    }

    @Test
    public void case4ShouldAutoMergeSeparatedChangesInsideSameFunction() {
        String base = """
                public class Sample {
                    void run() {
                        String start = "base";
                        String middle = "same";
                        String end = "base";
                    }
                }
                """;
        String local = """
                public class Sample {
                    void run() {
                        String start = "local";
                        String middle = "same";
                        String end = "base";
                    }
                }
                """;
        String newer = """
                public class Sample {
                    void run() {
                        String start = "base";
                        String middle = "same";
                        String end = "new";
                    }
                }
                """;

        TextThreeWayMerger.MergeTextResult result = javaMerger.merge(base, local, newer);

        assertFalse(result.hasConflict());
        assertTrue(result.getMergedText().contains("String start = \"local\";"));
        assertTrue(result.getMergedText().contains("String end = \"new\";"));
    }

    @Test
    public void case5ShouldIgnoreWhitespaceOnlyDifferences() {
        String base = """
                line1

                    line2
                line3
                """;
        String local = """
                line1

                        line2   

                line3
                """;
        String newer = """
                line1

                    line2
                line3
                """;

        TextThreeWayMerger.MergeTextResult result = textMerger.merge(base, local, newer);

        assertFalse(result.hasConflict());
        assertEquals(local.replace("\n", System.lineSeparator()), result.getMergedText());
    }
}
