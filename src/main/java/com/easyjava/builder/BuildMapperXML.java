package com.easyjava.builder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.StringUtils;

public class BuildMapperXML {
    private static final Logger log = LoggerFactory.getLogger(BuildMapperXML.class);

    private static final String BASE_COLUMN_LIST = "base_column_list";
    private static final String BASE_QUERY_CONDITION = "base_query_condition_field";
    private static final String BASE_QUERY_CONDITION_EXTEND = "base_query_condition_extend";
    private static final String QUERY_CONDITION = "query_condition";

    private static final String tableAlias = "v";

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_MAPPER_XML);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File mapperXml = new File(folder, tableInfo.getBeanName() + Constants.SUFFIX_MAPPER + ".xml");

        try (OutputStream out = new FileOutputStream(
                mapperXml);
                OutputStreamWriter outw = new OutputStreamWriter(out);
                BufferedWriter bw = new BufferedWriter(outw)) {

            String className = tableInfo.getBeanName() + Constants.SUFFIX_MAPPER;

            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            bw.newLine();
            bw.newLine();
            bw.write("<!DOCTYPE mapper PUBLIC \r\n" + //
                    "\"-//mybatis.org//DTD Mappper 3.0 //EN\" \r\n" + //
                    "\"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
            bw.newLine();
            bw.newLine();

            bw.write("<mapper namespace=\"" + Constants.PACKAGE_MAPPER + "." + className + "\">");
            bw.newLine();
            bw.newLine();

            // 创建映射
            bw.write("\t<!-- 实体类映射 --> ");
            bw.newLine();
            String poName = Constants.PACKAGE_PO + "." + tableInfo.getBeanName();
            bw.write("\t<resultMap id = \"base_result_map\" type= \"" + poName + "\">");
            bw.newLine();

            FieldInfo idField = null;

            Map<String, List<FieldInfo>> keyIndex = tableInfo.getKeyIndexMap();

            for (Map.Entry<String, List<FieldInfo>> entry : keyIndex.entrySet()) {
                if ("PRIMARY".equals(entry.getKey())) {
                    List<FieldInfo> fInfos = entry.getValue();

                    if (fInfos.size() == 1) {
                        idField = fInfos.get(0);
                    }

                }
            }
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                bw.write("\t\t<!-- " + fieldInfo.getComment() + " --> ");

                bw.newLine();

                String key = null;
                if (idField != null && fieldInfo.getPropertyName().equals(idField.getPropertyName())) {
                    key = "id";
                } else {
                    key = "result";
                }

                bw.write("\t\t<" + key + " column = \"" + fieldInfo.getFieldName() + "\" property = \""
                        + fieldInfo.getPropertyName() + "\"/>");
                bw.newLine();
                bw.newLine();
            }

            bw.write("\t</resultMap>");
            bw.newLine();
            bw.newLine();

            // 通用查询列
            bw.write("\t<!-- 通用查询列 --> ");
            bw.newLine();

            StringBuilder columnBuilder = new StringBuilder();
            // 这个变量用于构造批量插入
            StringBuilder insertPropBuilder = new StringBuilder();
            StringBuilder insertColumBuilder = new StringBuilder();
            // 批量插入或更新的构造
            StringBuilder insertOrUpdateBD = new StringBuilder();

            bw.write("\t<sql id=\"" + BASE_COLUMN_LIST + "\">");
            bw.newLine();

            // 这里不小心写错了，返回查询列，不小心弄成了属性名了
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                columnBuilder.append(tableAlias + "." + fieldInfo.getFieldName()).append(",");

                if (fieldInfo.getIsAutoIncrement()) {
                    continue;
                }

                insertColumBuilder.append(fieldInfo.getFieldName()).append(",");
                insertPropBuilder.append("#{item." + fieldInfo.getPropertyName() + "}").append(",");
                insertOrUpdateBD
                        .append("\t\t" + fieldInfo.getFieldName() + " = VALUES(" + fieldInfo.getFieldName() + ")")
                        .append(",\n");
            }

            String column = columnBuilder.substring(0, columnBuilder.lastIndexOf(","));
            String insertColum = insertColumBuilder.substring(0, insertColumBuilder.lastIndexOf(","));
            String insertPropColum = insertPropBuilder.substring(0, insertPropBuilder.lastIndexOf(","));
            String InsertOrUpdateColum = insertOrUpdateBD.substring(0, insertOrUpdateBD.lastIndexOf(","));

            bw.write("\t\t" + column);
            bw.newLine();

            bw.write("\t</sql>");
            bw.newLine();
            bw.newLine();

            // 基础查询条件
            bw.write("\t<!-- 基础查询条件 --> ");
            bw.newLine();
            bw.write("\t<sql id=\"" + BASE_QUERY_CONDITION + "\">");
            bw.newLine();
            bw.newLine();

            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                String stringQuery = "";
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType())) {
                    stringQuery = " and query." + fieldInfo.getPropertyName() + "!=''";
                }

                // 这里也是，if里面的query也加了#{}，这是不行的
                bw.write("\t\t<if test=\"query." + fieldInfo.getPropertyName() + " != null" + stringQuery + "\">");
                bw.newLine();
                bw.write("\t\t\tand " + tableAlias + "." + fieldInfo.getFieldName() + "=#{ query."
                        + fieldInfo.getPropertyName() + " }");
                bw.newLine();
                bw.write("\t\t</if>");
                bw.newLine();
                bw.newLine();
            }

            bw.write("\t</sql>");
            bw.newLine();
            bw.newLine();

            // 扩展查询条件
            bw.write("\t<!-- 扩展查询条件 --> ");
            bw.newLine();
            bw.write("\t<sql id=\"" + BASE_QUERY_CONDITION_EXTEND + "\">");
            bw.newLine();
            bw.newLine();

            for (FieldInfo fieldInfo : tableInfo.getFieldListExtend()) {
                String andWhere = "";
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType())) {
                    andWhere = "and " + tableAlias + "." + fieldInfo.getFieldName() + " like concat('%',#{ query."
                            + fieldInfo.getPropertyName()
                            + " },'%')";
                } else if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType())
                        || ArrayUtils.contains(Constants.SQL_DATE_TYPE, fieldInfo.getSqlType())) {
                    if (fieldInfo.getPropertyName().endsWith(Constants.SUFFIX_BEAN_PARAM_TIME_START)) {
                        andWhere = "<![CDATA[ and " + tableAlias + "." + fieldInfo.getFieldName()
                                + " >= str_to_date(#{query."
                                + fieldInfo.getPropertyName() + "},'%Y-%m-%d')]]>";
                    } else {
                        andWhere = "<![CDATA[ and " + fieldInfo.getFieldName() + " <= date_add(str_to_date(#{query."
                                + fieldInfo.getPropertyName() + "},'%Y-%m-%d'),interval -1 day)]]>";
                    }
                }

                bw.write("\t\t<if test=\"query." + fieldInfo.getPropertyName() + " != null and query."
                        + fieldInfo.getPropertyName() + " != ''\">");
                bw.newLine();
                bw.write("\t\t\t" + andWhere);
                bw.newLine();
                bw.write("\t\t</if>");
                bw.newLine();
                bw.newLine();
            }

            bw.write("\t</sql>");
            bw.newLine();
            bw.newLine();

            // 扩展通用查询条件
            bw.write("\t<!-- 通用查询条件 --> ");
            bw.newLine();
            bw.write("\t<sql id=\"" + QUERY_CONDITION + "\">");
            bw.newLine();
            bw.write("\t\t<where>");
            bw.newLine();
            bw.write("\t\t\t<include refid=\"" + BASE_QUERY_CONDITION + "\" />");
            bw.newLine();
            bw.write("\t\t\t<include refid=\"" + BASE_QUERY_CONDITION_EXTEND + "\" />");
            bw.newLine();
            bw.write("\t\t</where>");
            bw.newLine();
            bw.write("\t</sql>");
            bw.newLine();
            bw.newLine();

            // 查询集合
            bw.write("\t<!-- 查询集合 --> ");
            bw.newLine();
            bw.write("\t<select id=\"SelectList\" resultMap=\"base_result_map\">\r\n" + //
                    "\t\tselect \r\n" + //
                    "\t\t<include refid = \"" + BASE_COLUMN_LIST + "\"/>\r\n" + //
                    "\t\tFROM " + tableInfo.getTableName() + " " + tableAlias + "\r\n" + //
                    "\t\t<include refid = \"" + QUERY_CONDITION + "\"/>");

            bw.newLine();
            bw.write("\t\t<if test=\"query.orderBy != null\">\r\n" + //
                    "\t\t\torder by ${query.orderBy}\r\n" + //
                    "\t\t</if>");
            bw.newLine();
            bw.write("\t\t<if test=\"query.simplePage != null\">\r\n" + //
                    "\t\t\tlimit #{query.simplePage.start},#{query.simplePage.end}\r\n" + //
                    "\t\t</if>");
            bw.newLine();
            bw.write("\t</select>");
            bw.newLine();
            bw.newLine();

            // 查询数量
            bw.write("\t<!-- 查询数量 --> ");
            bw.newLine();
            bw.write("\t<select id=\"SelectCount\" resultType = \"Integer\">");
            bw.newLine();
            bw.write("\t\tselect count(1) from " + tableInfo.getTableName() + " " + tableAlias);
            bw.newLine();
            bw.write("\t\t<include refid=\"" + QUERY_CONDITION + "\"/>");
            bw.newLine();
            bw.write("\t</select>");
            bw.newLine();
            bw.newLine();

            // 添加根据参数删除记录
            bw.write("\t<!-- 根据参数删除记录 --> ");
            bw.newLine();
            bw.write("\t<delete id=\"DeleteByParam\">");
            bw.newLine();
            bw.write("\t\tdelete from " + tableInfo.getTableName() + " " + tableAlias);
            bw.newLine();
            bw.write("\t\t<include refid=\"" + QUERY_CONDITION + "\"/>");
            bw.newLine();
            bw.write("\t</delete>");
            bw.newLine();
            bw.newLine();

            // 单条插入数据
            bw.write("\t<!-- 数据插入 --> ");
            bw.newLine();
            bw.write("\t<insert id=\"Insert\" parameterType=\"" + Constants.PACKAGE_PO + "." + tableInfo.getBeanName()
                    + "\">");
            bw.newLine();

            // 是否有自增长
            FieldInfo autoIncrement = null;
            for (FieldInfo fInfo : tableInfo.getFieldList()) {
                if (fInfo.getIsAutoIncrement() != null && fInfo.getIsAutoIncrement()) {
                    autoIncrement = fInfo;
                    break;
                }
            }

            if (autoIncrement != null) {
                bw.write("\t\t<selectKey keyProperty=\"bean." + autoIncrement.getPropertyName()
                        + "\" resultType=\"Integer\" order=\"AFTER\">\r\n" + //
                        "\t\t\tselect LAST_INSERT_ID()\r\n" + //
                        "\t\t</selectKey>");
                bw.newLine();
            }

            bw.write("\t\tinsert into " + tableInfo.getTableName());
            bw.newLine();
            bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
            bw.newLine();

            for (FieldInfo fInfo : tableInfo.getFieldList()) {
                bw.write("\t\t\t<if test = \"bean." + fInfo.getPropertyName() + " != null\">");
                bw.newLine();
                bw.write("\t\t\t\t" + fInfo.getFieldName() + ",");
                bw.newLine();
                bw.write("\t\t\t</if>");

                bw.newLine();
            }

            bw.write("\t\t</trim>");
            bw.newLine();

            bw.write("\t\t<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">");
            bw.newLine();

            for (FieldInfo fInfo : tableInfo.getFieldList()) {
                bw.write("\t\t\t<if test = \"bean." + fInfo.getPropertyName() + " != null\">");
                bw.newLine();
                bw.write("\t\t\t\t#{bean." + fInfo.getPropertyName() + "},");
                bw.newLine();
                bw.write("\t\t\t</if>");

                bw.newLine();
            }

            bw.write("\t\t</trim>");
            bw.newLine();

            bw.write("\t</insert>");
            bw.newLine();
            bw.newLine();

            // 生成插入或更新
            bw.write("\t<!-- 数据插入或更新 --> ");
            bw.newLine();

            bw.write("\t<insert id=\"InsertOrUpdate\" parameterType=\"" + Constants.PACKAGE_PO + "."
                    + tableInfo.getBeanName()
                    + "\">");
            bw.newLine();
            bw.write("\t\tinsert into " + tableInfo.getTableName());
            bw.newLine();
            bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
            bw.newLine();

            for (FieldInfo fInfo : tableInfo.getFieldList()) {
                bw.write("\t\t\t<if test = \"bean." + fInfo.getPropertyName() + " != null\">");
                bw.newLine();
                bw.write("\t\t\t\t" + fInfo.getFieldName() + ",");
                bw.newLine();
                bw.write("\t\t\t</if>");

                bw.newLine();
            }

            bw.write("\t\t</trim>");
            bw.newLine();

            bw.write("\t\t<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">");
            bw.newLine();

            for (FieldInfo fInfo : tableInfo.getFieldList()) {
                bw.write("\t\t\t<if test = \"bean." + fInfo.getPropertyName() + " != null\">");
                bw.newLine();
                bw.write("\t\t\t\t#{bean." + fInfo.getPropertyName() + "},");
                bw.newLine();

                bw.write("\t\t\t</if>");
                bw.newLine();
            }

            bw.write("\t\t</trim>");
            bw.newLine();

            bw.write("\t\ton DUPLICATE key update");
            bw.newLine();

            Map<String, String> keyTempMap = new HashMap<>();
            for (Map.Entry<String, List<FieldInfo>> entry : tableInfo.getKeyIndexMap().entrySet()) {
                List<FieldInfo> fieldInfos = entry.getValue();

                for (FieldInfo fInfo : fieldInfos) {
                    keyTempMap.put(fInfo.getFieldName(), fInfo.getFieldName());
                }
            }

            bw.write("\t\t<trim prefix=\"\" suffix=\"\" suffixOverrides=\",\">");
            bw.newLine();

            for (FieldInfo fInfo : tableInfo.getFieldList()) {

                if (keyTempMap.get(fInfo.getFieldName()) != null) {
                    continue;
                }

                bw.write("\t\t\t<if test = \"bean." + fInfo.getPropertyName() + " != null\">");
                bw.newLine();
                bw.write("\t\t\t\t" + fInfo.getFieldName() + " = VALUES(" + fInfo.getFieldName() + "),");
                bw.newLine();

                bw.write("\t\t\t</if>");
                bw.newLine();
            }

            bw.write("\t\t</trim>");
            bw.newLine();

            bw.write("\t</insert>");
            bw.newLine();
            bw.newLine();

            // 批量插入数据
            bw.write("\t<!-- 数据批量插入 --> ");
            bw.newLine();

            bw.write("\t<insert id=\"InsertBatch\" parameterType=\"" + Constants.PACKAGE_PO + "."
                    + tableInfo.getBeanName()
                    + "\">");
            bw.newLine();
            bw.write("\t\tinsert into " + tableInfo.getTableName() + " (" + insertColum + ") values");
            bw.newLine();
            bw.write("\t\t<foreach collection=\"list\" item=\"item\" separator=\",\">");
            bw.newLine();
            bw.write("\t\t\t(");
            bw.newLine();
            bw.write("\t\t\t\t" + insertPropColum);
            bw.newLine();
            bw.write("\t\t\t)");
            bw.newLine();
            bw.write("\t\t</foreach>");
            bw.newLine();
            bw.write("\t</insert>");
            bw.newLine();
            bw.newLine();

            // 批量插入或更新数据
            bw.write("\t<!-- 数据批量插入或更新 --> ");
            bw.newLine();
            bw.write("\t<insert id=\"InsertOrUpdateBtach\" parameterType=\"" + Constants.PACKAGE_PO + "."
                    + tableInfo.getBeanName()
                    + "\">");
            bw.newLine();
            bw.write("\t\tinsert into " + tableInfo.getTableName() + " (" + insertColum + ") values");
            bw.newLine();
            bw.write("\t\t<foreach collection=\"list\" item=\"item\" separator=\",\">");
            bw.newLine();
            bw.write("\t\t\t(");
            bw.newLine();
            bw.write("\t\t\t\t" + insertPropColum);
            bw.newLine();
            bw.write("\t\t\t)");
            bw.newLine();
            bw.write("\t\t</foreach>");
            bw.newLine();
            bw.write("\t\ton DUPLICATE key update");
            bw.newLine();
            bw.write(InsertOrUpdateColum);
            bw.newLine();
            bw.write("\t</insert>");
            bw.newLine();
            bw.newLine();

            // 根据索引构造相应的查询对象
            Map<String, List<FieldInfo>> keyIndexInfo = tableInfo.getKeyIndexMap();

            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexInfo.entrySet()) {
                List<FieldInfo> keyFieldInfos = entry.getValue();

                Integer index = 0;
                StringBuilder methodName = new StringBuilder();
                StringBuilder methodParam = new StringBuilder();
                StringBuilder paramName = new StringBuilder();
                StringBuilder paramNameAlias = new StringBuilder();

                for (FieldInfo fieldInfo : keyFieldInfos) {

                    methodName.append(StringUtils.uperCaseFirstLetter(fieldInfo.getPropertyName()));

                    paramName.append(fieldInfo.getFieldName() + " = #{" + fieldInfo.getPropertyName() + "}");

                    paramNameAlias.append(
                            tableAlias + "." + fieldInfo.getFieldName() + " = #{" + fieldInfo.getPropertyName() + "}");

                    index++;
                    if (index < keyFieldInfos.size()) {
                        methodName.append("And");

                        paramName.append(" and ");
                    }

                    methodParam.append("@Param(\"" + fieldInfo.getPropertyName() + "\"" + ") " + fieldInfo.getJavaType()
                            + " " + fieldInfo.getPropertyName());

                    if (index < keyFieldInfos.size()) {
                        methodParam.append(", ");
                    }
                }
                bw.write("\t<!-- 根据" + methodName + "查询 --> ");
                bw.newLine();
                bw.write("\t<select id=\"SelectBy" + methodName + "\" resultMap=\"base_result_map\">");
                bw.newLine();
                bw.write("\t\tselect\r\n" + //
                        "\t\t<include refid=\"base_column_list\"/>\r\n" + //
                        "\t\tfrom " + tableInfo.getTableName() + " " + tableAlias + " where " + paramNameAlias);
                bw.newLine();
                bw.write("\t</select>");
                bw.newLine();
                bw.newLine();

                bw.write("\t<!-- 根据" + methodName + "删除 --> ");
                bw.newLine();
                bw.write("\t<delete id=\"DeleteBy" + methodName + "\">\r\n" + //
                        "\t\tdelete from " + tableInfo.getTableName() + " where " + paramName);
                bw.newLine();
                bw.write("\t</delete>");
                bw.newLine();
                bw.newLine();

                bw.write("\t<!-- 根据" + methodName + "更新 --> ");
                bw.newLine();
                bw.write("\t<update id=\"UpdateBy" + methodName + "\" parameterType=\"" + Constants.PACKAGE_PO + "."
                        + tableInfo.getBeanName() + "\">");
                bw.newLine();
                bw.write("\t\tUPDATE " + tableInfo.getTableName());
                bw.newLine();
                bw.write("\t\t<set>");
                bw.newLine();
                for (FieldInfo fInfo : tableInfo.getFieldList()) {
                    bw.write("\t\t\t<if test = \"bean." + fInfo.getPropertyName() + " != null\">");
                    bw.newLine();
                    bw.write("\t\t\t\t" + fInfo.getFieldName() + " = #{bean." + fInfo.getPropertyName() + "},");
                    bw.newLine();
                    bw.write("\t\t\t</if>");
                    bw.newLine();
                }

                bw.write("\t\t</set>");
                bw.newLine();
                bw.write("\t\twhere " + paramName);
                bw.newLine();
                bw.write("\t</update>");
                bw.newLine();
                bw.newLine();
            }

            bw.write("</mapper>");

            bw.flush();

        } catch (Exception e) {
            log.error("{}的mapper构建失败", tableInfo.getBeanName(), e);
        }
    }
}
