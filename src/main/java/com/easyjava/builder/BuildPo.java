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
import com.easyjava.utils.DateUtils;
import com.easyjava.utils.StringUtils;

public class BuildPo {

    private final static Logger log = LoggerFactory.getLogger(BuildPo.class);

    public static void execute(TableInfo tableInfo) {

        File folder = new File(Constants.PATH_PO);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File poFile = new File(folder, tableInfo.getBeanName() + ".java");

        // 采用这种方式会自动关流
        try (OutputStream out = new FileOutputStream(poFile);
                OutputStreamWriter outw = new OutputStreamWriter(out, "utf-8");
                BufferedWriter bw = new BufferedWriter(outw)) {

            // 构建生成的java类
            bw.write("package " + Constants.PACKAGE_PO + ";");
            bw.newLine();
            bw.newLine();

            bw.write("import java.io.Serializable;");
            bw.newLine();

            if (tableInfo.getHaveDate() || tableInfo.getHaveDateTime()) {
                bw.write("import java.util.Date;");
                bw.newLine();

                bw.write(Constants.BEAN_DATE_SERIALIZATION_CLASS);
                bw.newLine();

                bw.write(Constants.BEAN_DATE_DESERIALIZATIO_CLASS);
                bw.newLine();

                bw.write("import " + Constants.PACKAGE_ENUMS + "." + "DateTimePatternEnum" + ";");
                bw.newLine();

                bw.write("import " + Constants.PACKAGE_UTILS + "." + "DateUtil" + ";");
                bw.newLine();
            }

            // 判断是否有忽略序列化的字段
            Boolean isBeanJsonIgnore = false;

            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                if (ArrayUtils.contains(Constants.IGNORE_BEAN_TOJSON_FIELD.split(","), fieldInfo.getPropertyName())) {
                    isBeanJsonIgnore = true;
                    break;
                }
            }

            if (isBeanJsonIgnore) {
                bw.write(Constants.IGNORE_BEAN_TOJSON_CLASS);
                bw.newLine();
            }

            bw.newLine();

            BuildComment.CreateClassComment(bw, tableInfo.getComment());
            bw.write("public class " + tableInfo.getBeanName() + " implements Serializable {");
            bw.newLine();

            // 拿取表中的属性
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                BuildComment.CreateFieldComment(bw, fieldInfo.getComment());

                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType())) {

                    bw.write("\t" + String.format(Constants.BEAN_DATE_SERIALIZATION, DateUtils.YYYYMMDDHHMMSS));
                    bw.newLine();

                    bw.write("\t" + String.format(Constants.BEAN_DATA_DESERIALIZATIO, DateUtils.YYYYMMDDHHMMSS));
                    bw.newLine();
                }

                if (ArrayUtils.contains(Constants.SQL_DATE_TYPE, fieldInfo.getSqlType())) {

                    bw.write("\t" + String.format(Constants.BEAN_DATE_SERIALIZATION, DateUtils.YYYY_MM_DD));
                    bw.newLine();
                    bw.write("\t" + String.format(Constants.BEAN_DATA_DESERIALIZATIO, DateUtils.YYYY_MM_DD));
                    bw.newLine();
                }

                if (ArrayUtils.contains(Constants.IGNORE_BEAN_TOJSON_FIELD.split(","), fieldInfo.getPropertyName())) {
                    bw.write("\t" + Constants.IGNORE_BEAN_TOJSON_EXPRESSION);
                    bw.newLine();
                }

                bw.write("\tprivate " + fieldInfo.getJavaType() + " " + fieldInfo.getPropertyName() + ";");
                bw.newLine();
                bw.newLine();
            }

            // 生成get,set方法
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                String tmpField = StringUtils.uperCaseFirstLetter(fieldInfo.getPropertyName());

                // 截断is前缀的set
                if (fieldInfo.getPropertyName().startsWith("is")) {
                    bw.write("\t" + "public void " + "set" + tmpField.substring(2) + "(" + fieldInfo.getJavaType() + " "
                            + fieldInfo.getPropertyName() + ") " + "{");
                } else {
                    bw.write("\t" + "public void " + "set" + tmpField + "(" + fieldInfo.getJavaType() + " "
                            + fieldInfo.getPropertyName() + ") " + "{");
                }
                bw.newLine();
                bw.write("\t\tthis." + fieldInfo.getPropertyName() + " = " + fieldInfo.getPropertyName() + ";");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();

                // 截断is前缀的get
                if (fieldInfo.getPropertyName().startsWith("is")) {
                    bw.write("\t" + "public " + fieldInfo.getJavaType() + " get" + tmpField.substring(2) + "(" + ") "
                            + "{");
                } else {
                    bw.write("\t" + "public " + fieldInfo.getJavaType() + " get" + tmpField + "(" + ") " + "{");
                }
                bw.newLine();
                bw.write("\t\treturn " + fieldInfo.getPropertyName() + ";");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();
            }

            StringBuffer toString = new StringBuffer();
            // 重写toString方法
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {

                String propName = fieldInfo.getPropertyName();
                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType())) {
                    propName = "DateUtil.Formate("+propName+", DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.GetPattern())";

                } else if (ArrayUtils.contains(Constants.SQL_DATE_TYPE, fieldInfo.getSqlType())) {
                    propName= "DateUtil.Formate(" + propName
                            + ", DateTimePatternEnum.YYYY_MM_DD.GetPattern())";
                }
                toString.append("\"" + fieldInfo.getComment() + "\"" + " + " + "\" : \"" + " + " + "("
                        + fieldInfo.getPropertyName() + " == null ? " + "\"" + "空" + "\"" + " : " + propName + ")");

                toString.append(" + ");
            }

            String poString = toString.substring(0, toString.lastIndexOf("+"));

            bw.write("\t@Override");
            bw.newLine();
            bw.write("\tpublic String toString() {");
            bw.newLine();
            bw.write("\t\treturn " + poString + ";");
            bw.newLine();
            bw.write("\t}");

            bw.newLine();
            bw.write("}");

            bw.flush();
        } catch (Exception e) {
            log.error("出现错误", e);
        }

    }
}
