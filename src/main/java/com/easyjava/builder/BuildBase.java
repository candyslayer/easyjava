package com.easyjava.builder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easyjava.bean.Constants;

public class BuildBase {

    private static final Logger log = LoggerFactory.getLogger(BuildBase.class);

    public static void execute() {

        List<String> headInfos = new ArrayList<>();

        // 生成枚举的工具类
        headInfos.add("package " + Constants.PACKAGE_ENUMS);
        build(headInfos, "DateTimePatternEnum", Constants.PATH_ENUMS);

        headInfos.clear();

        // 生成DateUtil工具类
        headInfos.add("package " + Constants.PACKAGE_UTILS);
        build(headInfos, "DateUtil", Constants.PATH_UTILS);

        // 生成BaseMapper
        headInfos.clear();
        headInfos.add("package " + Constants.PACKAGE_MAPPER);
        build(headInfos, "BaseMapper", Constants.PATH_MAPPER);

        // 生成分页所需的信息
        headInfos.clear();
        headInfos.add("package " + Constants.PACKAGE_ENUMS);
        build(headInfos, "PageSize", Constants.PATH_ENUMS);
        build(headInfos, "ResponseCodeEnum", Constants.PATH_ENUMS);

        headInfos.clear();
        headInfos.add("package " + Constants.PACKAGE_PARAM);
        headInfos.add("import " + Constants.PACKAGE_ENUMS + ".PageSize");
        build(headInfos, "SimplePage", Constants.PATH_PARAM);

        headInfos.clear();
        headInfos.add("package " + Constants.PACKAGE_PARAM);
        build(headInfos, "BaseParam", Constants.PATH_PARAM);

        headInfos.clear();
        headInfos.add("package " + Constants.PACKAGE_VO);
        build(headInfos, "PaginationResultVO", Constants.PATH_VO);
        build(headInfos, "ResponseVO", Constants.PATH_VO);

        // 生成异常处理类
        headInfos.clear();
        headInfos.add("package " + Constants.PACKAGE_EXCEPTION);
        headInfos.add("import " + Constants.PACKAGE_ENUMS + ".ResponseCodeEnum");
        build(headInfos, "BusinessException", Constants.PATH_EXCEPTION);

        // 生成策略接口
        headInfos.clear();
        headInfos.add("package " + Constants.PACKAGE_EXCEPTION);
        headInfos.add("import " + Constants.PACKAGE_VO + ".ResponseVO");
        build(headInfos, "ExceptionHandlerStrategy", Constants.PATH_EXCEPTION);

        //生成异常策略
        headInfos.clear();
        headInfos.add("package " + Constants.PACKAGE_EXCEPTION_STRATEGY);
        headInfos.add("import " + Constants.PACKAGE_ENUMS + ".ResponseCodeEnum");
        headInfos.add("import " + Constants.PACKAGE_VO + ".ResponseVO");
        headInfos.add("import " + Constants.PACKAGE_EXCEPTION + ".ExceptionHandlerStrategy");
        build(headInfos, "NoHandlerFoundExceptionStrategy", Constants.PATH_EXCEPTION_STRATEGY);
        build(headInfos, "DuplicateKeyExceptionStrategy", Constants.PATH_EXCEPTION_STRATEGY);
        build(headInfos, "MultipartExceptionStrategy", Constants.PATH_EXCEPTION_STRATEGY);
        build(headInfos, "BindOrMethodArgumentTypeMismatchExceptionStrategy", Constants.PATH_EXCEPTION_STRATEGY);
        build(headInfos, "DefaultExceptionStrategy", Constants.PATH_EXCEPTION_STRATEGY);

        headInfos.add("import " + Constants.PACKAGE_EXCEPTION + ".BusinessException");
        build(headInfos, "BusinessExceptionStrategy", Constants.PATH_EXCEPTION_STRATEGY);
        

        // 生成异常工厂
        headInfos.clear();
        headInfos.add("package " + Constants.PACKAGE_EXCEPTION);
        headInfos.add("import " + Constants.PACKAGE_EXCEPTION_STRATEGY + ".DefaultExceptionStrategy");
        build(headInfos, "ExceptionHandlerStrategyFactory", Constants.PATH_EXCEPTION);

        headInfos.clear();
        headInfos.add("package " + Constants.PACKAGE_CONTROLLER);
        headInfos.add("import " + Constants.PACKAGE_ENUMS + ".ResponseCodeEnum");
        headInfos.add("import " + Constants.PACKAGE_VO + ".ResponseVO");
        build(headInfos, "AbaseController", Constants.PATH_CONTROLLER);

        headInfos.add("import " + Constants.PACKAGE_EXCEPTION + ".BusinessException");
        headInfos.add("import " + Constants.PACKAGE_EXCEPTION + ".ExceptionHandlerStrategy");
        headInfos.add("import " + Constants.PACKAGE_EXCEPTION + ".ExceptionHandlerStrategyFactory");
        build(headInfos, "AGlobalExceptionHandlerController", Constants.PATH_CONTROLLER);

    }

    private static void build(List<String> headInfos, String fileName, String outputPath) {
        File folder = new File(outputPath);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File javaFile = new File(folder, fileName + ".java");

        String templatePath = BuildBase.class.getClassLoader().getResource("template/" + fileName + ".txt")
                .getPath();

        try (OutputStream out = new FileOutputStream(javaFile);
                OutputStreamWriter outw = new OutputStreamWriter(out, "utf-8");
                BufferedWriter bw = new BufferedWriter(outw);
                InputStream in = new FileInputStream(
                        templatePath);
                InputStreamReader inr = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(inr)) {

            String lineInfo = null;

            for (String head : headInfos) {
                bw.write(head + ";");
                bw.newLine();

                bw.newLine();
            }

            while ((lineInfo = br.readLine()) != null) {
                bw.write(lineInfo);
                bw.newLine();
            }

            bw.flush();

        } catch (Exception e) {
            log.error("生成基础类 {} 失败", fileName, e);
        }
    }
}
