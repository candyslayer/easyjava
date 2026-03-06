package com.easyjava.codegen;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Scanner;

public class ConsoleConflictResolver {

    private final Scanner scanner;
    private final PrintStream out;
    private final ConflictMarkerRenderer renderer = new ConflictMarkerRenderer();

    public ConsoleConflictResolver(InputStream inputStream, PrintStream out) {
        this.scanner = new Scanner(inputStream);
        this.out = out;
    }

    public ConflictResolutionChoice resolve(MergeOutcome outcome) {
        out.println();
        out.println("[CONFLICT] " + outcome.getTargetPath().toString().replace('\\', '/'));
        if (outcome.getConflictOutputPath() != null) {
            out.println("冲突文件已输出: " + outcome.getConflictOutputPath());
        }
        out.println();
        for (int i = 0; i < outcome.getConflictBlocks().size(); i++) {
            if (i > 0) {
                out.println();
                out.println("--------------------------------------------------");
                out.println();
            }
            out.println(renderer.renderConsoleBlock(outcome.getConflictBlocks().get(i)));
        }
        out.println();
        out.println("请选择该目标文件的处理方式：");
        out.println("1. 覆盖生成目录文件为 New");
        out.println("2. 保留生成目录中的 Local 文件");
        out.println("3. 用冲突标记内容覆盖生成目录文件");
        out.println("4. 仅保留冲突文件，不改目标文件");
        out.println("5. 跳过该文件");

        while (true) {
            out.print("请输入选项 [1-5]: ");
            if (!scanner.hasNextLine()) {
                out.println();
                return ConflictResolutionChoice.SKIP;
            }
            String value = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
            switch (value) {
                case "1":
                case "new":
                    return ConflictResolutionChoice.USE_NEW;
                case "2":
                case "local":
                    return ConflictResolutionChoice.KEEP_LOCAL;
                case "3":
                case "conflict":
                    return ConflictResolutionChoice.WRITE_CONFLICT_TO_TARGET;
                case "4":
                case "copy":
                    return ConflictResolutionChoice.KEEP_CONFLICT_COPY_ONLY;
                case "5":
                case "skip":
                    return ConflictResolutionChoice.SKIP;
                default:
                    out.println("无效输入，请输入 1、2、3、4 或 5。");
                    break;
            }
        }
    }
}
