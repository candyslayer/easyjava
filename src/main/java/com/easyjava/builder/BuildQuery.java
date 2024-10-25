package com.easyjava.builder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.StringUtils;

//这个我写的是真的垃圾，我自己都不想看了
public class BuildQuery {

    private final static Logger log = LoggerFactory.getLogger(BuildQuery.class);

    public static void execute(TableInfo tableInfo) {

        File folder = new File(Constants.PATH_PARAM);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File poFile = new File(folder, tableInfo.getBeanName() + Constants.SUFFIX_BEAN_PARAM + ".java");

        // 采用这种方式会自动关流
        try (OutputStream out = new FileOutputStream(poFile);
                OutputStreamWriter outw = new OutputStreamWriter(out, "utf-8");
                BufferedWriter bw = new BufferedWriter(outw)) {

            // 构建生成的java类
            bw.write("package " + Constants.PACKAGE_PARAM + ";");
            bw.newLine();
            bw.newLine();

            if (tableInfo.getHaveDate() || tableInfo.getHaveDateTime()) {
                bw.write("import java.util.Date;");
                bw.newLine();
            }

            bw.newLine();

            BuildComment.CreateClassComment(bw, tableInfo.getComment() + "查询对象");
            bw.write("public class " + tableInfo.getBeanName() + Constants.SUFFIX_BEAN_PARAM + "  extends BaseParam {");
            bw.newLine();

            // 拿取表中的属性
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                BuildComment.CreateFieldComment(bw, fieldInfo.getComment());

                bw.write("\tprivate " + fieldInfo.getJavaType() + " " + fieldInfo.getPropertyName() + ";");
                bw.newLine();

                String propName = fieldInfo.getPropertyName();

                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType())) {
                    propName = propName + Constants.SUFFIX_BEAN_PARAM_FUZZY;

                    bw.write("\tprivate " + fieldInfo.getJavaType() + " " + propName + ";");
                    bw.newLine();
                }

                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType())
                        || ArrayUtils.contains(Constants.SQL_DATE_TYPE, fieldInfo.getSqlType())) {
                    bw.newLine();
                    bw.write("\tprivate  String " + " " + propName + Constants.SUFFIX_BEAN_PARAM_TIME_START + ";");
                    bw.newLine();
                    bw.newLine();

                    bw.write("\tprivate  String " + " " + propName + Constants.SUFFIX_BEAN_PARAM_TIME_END + ";");
                    bw.newLine();
                }
                bw.newLine();
            }

            // 生成get,set方法
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                String propName = fieldInfo.getPropertyName();

                String tmpField = StringUtils.uperCaseFirstLetter(propName);

                // 没有fuzzy后缀的set
                // 截断is前缀的set
                if (propName.startsWith("is")) {
                    bw.write("\t" + "public void " + "set" + tmpField.substring(2) + "(" + fieldInfo.getJavaType() + " "
                            + propName + ") " + "{");
                } else {
                    bw.write("\t" + "public void " + "set" + tmpField + "(" + fieldInfo.getJavaType() + " "
                            + propName + ") " + "{");
                }
                bw.newLine();
                bw.write("\t\tthis." + propName + " = " + propName + ";");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();

                // 没有fuzzy后缀的get
                if (propName.startsWith("is")) {
                    bw.write(
                            "\t" + "public " + fieldInfo.getJavaType() + " get" + tmpField.substring(2) + "(" + ") "
                                    + "{");
                } else {
                    bw.write("\t" + "public " + fieldInfo.getJavaType() + " get" + tmpField
                            + "(" + ") " + "{");
                }
                bw.newLine();
                bw.write("\t\treturn " + propName + ";");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();

                // 有fuzzy后缀的
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType())) {
                    propName = propName + Constants.SUFFIX_BEAN_PARAM_FUZZY;

                    // 截断is前缀的set
                    if (propName.startsWith("is")) {
                        bw.write("\t" + "public void " + "set" + tmpField.substring(2)
                                + Constants.SUFFIX_BEAN_PARAM_FUZZY + "(" + fieldInfo.getJavaType() + " "
                                + propName + ") " + "{");
                    } else {
                        bw.write("\t" + "public void " + "set" + tmpField + Constants.SUFFIX_BEAN_PARAM_FUZZY + "("
                                + fieldInfo.getJavaType() + " "
                                + propName + ") " + "{");
                    }
                    bw.newLine();
                    bw.write("\t\tthis." + propName + " = " + propName + ";");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();

                    // 截断is前缀的get
                    if (propName.startsWith("is")) {
                        bw.write("\t" + "public " + fieldInfo.getJavaType() + " get" + tmpField.substring(2)
                                + Constants.SUFFIX_BEAN_PARAM_FUZZY + "(" + ") "
                                + "{");
                    } else {
                        bw.write("\t" + "public " + fieldInfo.getJavaType() + " get" + tmpField
                                + Constants.SUFFIX_BEAN_PARAM_FUZZY + "(" + ") " + "{");
                    }
                    bw.newLine();
                    bw.write("\t\treturn " + propName + ";");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();
                }

                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType())
                        || ArrayUtils.contains(Constants.SQL_DATE_TYPE, fieldInfo.getSqlType())) {
                    if (propName.startsWith("is")) {
                        bw.write("\t" + "public void " + "set" + tmpField.substring(2) + "(String"
                                + " "
                                + propName + ") " + "{");
                    } else {
                        bw.write("\t" + "public void " + "set" + tmpField + Constants.SUFFIX_BEAN_PARAM_TIME_START
                                + "(String" + " "
                                + propName + Constants.SUFFIX_BEAN_PARAM_TIME_START + ") " + "{");
                    }
                    bw.newLine();
                    bw.write("\t\tthis." + propName + Constants.SUFFIX_BEAN_PARAM_TIME_START + " = " + propName
                            + Constants.SUFFIX_BEAN_PARAM_TIME_START + ";");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();

                    // 截断is前缀的get
                    if (propName.startsWith("is")) {
                        bw.write(
                                "\t" + "public String" + " get" + tmpField.substring(2) + "(" + ") "
                                        + "{");
                    } else {
                        bw.write("\t" + "public String" + " get" + tmpField
                                + Constants.SUFFIX_BEAN_PARAM_TIME_START + "(" + ") " + "{");
                    }
                    bw.newLine();
                    bw.write("\t\treturn " + propName + Constants.SUFFIX_BEAN_PARAM_TIME_START + ";");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();

                    if (propName.startsWith("is")) {
                        bw.write("\t" + "public void " + "set" + tmpField.substring(2) + "(String"
                                + " "
                                + propName + ") " + "{");
                    } else {
                        bw.write("\t" + "public void " + "set" + tmpField + Constants.SUFFIX_BEAN_PARAM_TIME_END
                                + "(String" + " "
                                + propName + Constants.SUFFIX_BEAN_PARAM_TIME_END + ") " + "{");
                    }
                    bw.newLine();
                    bw.write("\t\tthis." + propName + Constants.SUFFIX_BEAN_PARAM_TIME_END + " = " + propName
                            + Constants.SUFFIX_BEAN_PARAM_TIME_END + ";");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();

                    // 截断is前缀的get
                    if (propName.startsWith("is")) {
                        bw.write(
                                "\t" + "public String" + " get" + tmpField.substring(2) + "(" + ") "
                                        + "{");
                    } else {
                        bw.write("\t" + "public String" + " get" + tmpField
                                + Constants.SUFFIX_BEAN_PARAM_TIME_END + "(" + ") " + "{");
                    }
                    bw.newLine();
                    bw.write("\t\treturn " + propName + Constants.SUFFIX_BEAN_PARAM_TIME_END + ";");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();
                }
            }

            bw.newLine();
            bw.write("}");

            bw.flush();
        } catch (Exception e) {
            log.error("出现错误", e);
        }

    }
}
