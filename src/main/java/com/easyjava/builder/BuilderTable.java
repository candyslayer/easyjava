package com.easyjava.builder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.JsonUtils;
import com.easyjava.utils.PropertiesUtils;
import com.easyjava.utils.StringUtils;

public class BuilderTable {

    private final static Logger log = LoggerFactory.getLogger(BuilderTable.class);

    private static Connection conn = null;

    private static String SQL_SHOW_TABLES_STATUS = "show table status";
    private static String SQL_SHOW_TABLE_FIELDS = "show full fields from %s";
    private static String SQL_SHOW_TABLE_INDEX = "show index from %s";
    static {
        String driverName = PropertiesUtils.geString("spring.datasource.driver-class-name");
        String url = PropertiesUtils.geString("spring.datasource.url");
        String user = PropertiesUtils.geString("spring.datasource.username");
        String password = PropertiesUtils.geString("spring.datasource.password");

        try {
            Class.forName(driverName);

            conn = DriverManager.getConnection(url, user, password);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static List<TableInfo> GetTables() {
        PreparedStatement ps = null;
        ResultSet tableResult = null;

        List<TableInfo> tableInfos = new ArrayList<>();

        try {
            ps = conn.prepareStatement(SQL_SHOW_TABLES_STATUS);
            tableResult = ps.executeQuery();

            while (tableResult.next()) {
                String tableName = tableResult.getString("name");
                String comment = tableResult.getString("comment");

                TableInfo tableInfo = new TableInfo();

                String beanName = tableName;

                if (Constants.IGNORE_TABLE_PREFIX) {
                    beanName = tableName.substring(beanName.indexOf("_") + 1);
                }

                beanName = ProcessField(tableName, true);

                tableInfo.setTableName(tableName);
                tableInfo.setComment(comment);
                tableInfo.setBeanName(beanName);
                tableInfo.setBeanParamName(beanName + ProcessField(Constants.SUFFIX_BEAN_PARAM, true));

                // 初始化haveDate和haveDateTime属性
                tableInfo.setHaveDate(false);
                tableInfo.setHaveDateTime(false);

                tableInfo.setFieldList(ReadFieldInfo(tableInfo));

                GetKeyIndexInfo(tableInfo);

                tableInfos.add(tableInfo);
            }

            log.info(JsonUtils.convertObject2Json(tableInfos));
        } catch (Exception e) {
            log.error("获取表异常", e);
        } finally {
            if (tableResult != null) {
                try {
                    tableResult.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {

                }
            }
        }

        return tableInfos;
    }

    private static List<FieldInfo> ReadFieldInfo(TableInfo tableInfo) {
        PreparedStatement ps = null;
        ResultSet fieldResult = null;

        List<FieldInfo> fieldInfos = new ArrayList<>();

        // 添加扩展字段，这是做扩展表的时候临时添加的字段，会直接用tableInfo设置扩展表，但是之前用的是这个函数的返回值添加，感觉不太规范
        List<FieldInfo> fieldExtendList = new ArrayList<>();

        try {
            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_FIELDS, tableInfo.getTableName()));
            fieldResult = ps.executeQuery();

            while (fieldResult.next()) {
                String field = fieldResult.getString("field");
                String type = fieldResult.getString("type");
                String extra = fieldResult.getString("extra");
                String comment = fieldResult.getString("comment");

                String propertyName = ProcessField(field, false);

                FieldInfo fieldInfo = new FieldInfo();

                if (type.indexOf("(") > 0) {
                    type = type.substring(0, type.indexOf("("));
                }

                fieldInfo.setFieldName(field);
                fieldInfo.setPropertyName(propertyName);
                fieldInfo.setSqlType(type);
                fieldInfo.setComment(comment);
                fieldInfo.setIsAutoIncrement("auto_increment".equalsIgnoreCase(extra) ? true : false);
                fieldInfo.setJavaType(ProcessJavaType(type));

                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type)) {
                    tableInfo.setHaveDateTime(true);
                }

                if (ArrayUtils.contains(Constants.SQL_DATE_TYPE, type)) {
                    tableInfo.setHaveDate(true);
                }

                if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE, type)) {
                    tableInfo.setHaveBigDecimal(true);
                }

                // 后面添加的扩展表字段判断
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, type)) {
                    fieldExtendList.add(new FieldInfo(field, propertyName + Constants.SUFFIX_BEAN_PARAM_FUZZY,
                            type, ProcessJavaType(type), comment,
                            "auto_increment".equalsIgnoreCase(extra) ? true : false));
                }

                else if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type)
                        || ArrayUtils.contains(Constants.SQL_DATE_TYPE, type)) {

                    // 添加date类
                    // fieldExtendList.add(new FieldInfo(field, propertyName,
                    // type, ProcessJavaType(type), comment,
                    // "auto_increment".equalsIgnoreCase(extra) ? true : false));

                    fieldExtendList.add(new FieldInfo(field, propertyName + Constants.SUFFIX_BEAN_PARAM_TIME_START,
                            type, "String", comment,
                            "auto_increment".equalsIgnoreCase(extra) ? true : false));

                    fieldExtendList.add(new FieldInfo(field, propertyName + Constants.SUFFIX_BEAN_PARAM_TIME_END,
                            type,
                            "String", comment,
                            "auto_increment".equalsIgnoreCase(extra) ? true : false));
                }

                fieldInfos.add(fieldInfo);
            }

            tableInfo.setFieldListExtend(fieldExtendList);
        } catch (Exception e) {
            log.error("获取表异常", e);
        } finally {
            if (fieldResult != null) {
                try {
                    fieldResult.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return fieldInfos;
    }

    private static void GetKeyIndexInfo(TableInfo tableInfo) {
        PreparedStatement ps = null;
        ResultSet fieldResult = null;

        try {
            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_INDEX, tableInfo.getTableName()));
            fieldResult = ps.executeQuery();

            Map<String, FieldInfo> tempMap = new HashMap<>();

            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                tempMap.put(fieldInfo.getFieldName(), fieldInfo);
            }

            while (fieldResult.next()) {
                String keyName = fieldResult.getString("key_name");
                Integer nonUnique = fieldResult.getInt("non_unique");
                String columnName = fieldResult.getString("column_name");

                if (nonUnique == 1) {
                    continue;
                }

                List<FieldInfo> keyFieldList = tableInfo.getKeyIndexMap().get(keyName);

                if (keyFieldList == null) {
                    keyFieldList = new ArrayList<>();

                    tableInfo.getKeyIndexMap().put(keyName, keyFieldList);
                }

                // 这里是先将表的信息遍历一遍，获取是索引的那一行信息加入map，仅需遍历一次
                keyFieldList.add(tempMap.get(columnName));

                // 这里的代码被优化，这个写法是每获取一条索引信息就遍历一次，表的信息，执行效率太差
                // for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                // if (fieldInfo.getFieldName().equals(columnName)) {
                // keyFieldList.add(fieldInfo);
                // }
                // }

            }

        } catch (Exception e) {
            log.error("获取表异常", e);
        } finally {
            if (fieldResult != null) {
                try {
                    fieldResult.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static String ProcessField(String field, Boolean upCaseFirstLetter) {
        StringBuffer sb = new StringBuffer();
        String[] fields = field.split("_");
        sb.append(upCaseFirstLetter ? StringUtils.uperCaseFirstLetter(fields[0]) : fields[0]);

        for (int i = 1, len = fields.length; i < len; i++) {
            sb.append(StringUtils.uperCaseFirstLetter(fields[i]));
        }

        return sb.toString();
    }

    private static String ProcessJavaType(String type) {

        if (ArrayUtils.contains(Constants.SQL_INTEGER_TYPE, type)) {
            return "Integer";
        } else if (ArrayUtils.contains(Constants.SQL_DATE_TYPE, type)
                || ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES,
                        type)) {
            return "Date";
        } else if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE, type)) {
            return "BigDecimal";
        } else if (ArrayUtils.contains(Constants.SQL_LONG_TYPE, type)) {
            return "Long";
        } else if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, type)) {
            return "String";
        }

        else {
            throw new RuntimeException("can not identify type : " + type);
        }
    }

}
