package com.easyjava.test;

import com.easyjava.bean.Constants;
import org.apache.commons.lang3.ArrayUtils;

/**
 * SQL 类型映射测试类
 */
public class SqlTypeMappingTest {
    
    public static void main(String[] args) {
        System.out.println("=== SQL 类型到 Java 类型映射测试 ===\n");
        
        // 测试所有支持的 SQL 类型
        String[] testTypes = {
            // 整数类型
            "int", "tinyint", "smallint", "mediumint", "int unsigned", "tinyint unsigned",
            
            // 长整数类型
            "bigint", "bigint unsigned",
            
            // 浮点数类型
            "decimal", "double", "float", "numeric",
            
            // 日期时间类型
            "datetime", "timestamp", "date", "time",
            
            // 字符串类型
            "varchar", "char", "text", "mediumtext", "longtext", "tinytext",
            
            // 二进制类型
            "blob", "mediumblob", "longblob", "tinyblob", "binary", "varbinary",
            
            // 特殊类型
            "boolean", "bool", "bit", "json", "enum", "set",
            
            // 几何类型
            "geometry", "point", "linestring", "polygon"
        };
        
        for (String sqlType : testTypes) {
            try {
                String javaType = getJavaType(sqlType);
                String category = getTypeCategory(sqlType);
                boolean supportsFuzzy = supportsFuzzyQuery(sqlType);
                boolean supportsRange = supportsRangeQuery(sqlType);
                
                System.out.printf("%-15s -> %-12s [%s] 模糊查询:%s 范围查询:%s%n", 
                    sqlType, javaType, category, 
                    supportsFuzzy ? "✓" : "✗", 
                    supportsRange ? "✓" : "✗");
                    
            } catch (Exception e) {
                System.out.printf("%-15s -> ERROR: %s%n", sqlType, e.getMessage());
            }
        }
        
        System.out.println("\n=== 类型分类统计 ===");
        System.out.println("整数类型: " + Constants.SQL_INTEGER_TYPE.length + " 种");
        System.out.println("长整数类型: " + Constants.SQL_LONG_TYPE.length + " 种");
        System.out.println("浮点数类型: " + Constants.SQL_DECIMAL_TYPE.length + " 种");
        System.out.println("日期时间类型: " + Constants.SQL_DATE_TIME_TYPES.length + " 种");
        System.out.println("日期类型: " + Constants.SQL_DATE_TYPE.length + " 种");
        System.out.println("时间类型: " + Constants.SQL_TIME_TYPE.length + " 种");
        System.out.println("字符串类型: " + Constants.SQL_STRING_TYPE.length + " 种");
        System.out.println("二进制类型: " + Constants.SQL_BLOB_TYPE.length + " 种");
        System.out.println("布尔类型: " + Constants.SQL_BOOLEAN_TYPE.length + " 种");
        System.out.println("JSON类型: " + Constants.SQL_JSON_TYPE.length + " 种");
        System.out.println("枚举类型: " + Constants.SQL_ENUM_TYPE.length + " 种");
        System.out.println("集合类型: " + Constants.SQL_SET_TYPE.length + " 种");
        System.out.println("几何类型: " + Constants.SQL_GEOMETRY_TYPE.length + " 种");
    }
    
    /**
     * 模拟 ProcessJavaType 方法
     */
    private static String getJavaType(String type) {
        if (ArrayUtils.contains(Constants.SQL_INTEGER_TYPE, type)) {
            return "Integer";
        } else if (ArrayUtils.contains(Constants.SQL_DATE_TYPE, type)
                || ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type)) {
            return "Date";
        } else if (ArrayUtils.contains(Constants.SQL_TIME_TYPE, type)) {
            return "String";
        } else if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE, type)) {
            return "BigDecimal";
        } else if (ArrayUtils.contains(Constants.SQL_LONG_TYPE, type)) {
            return "Long";
        } else if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, type)) {
            return "String";
        } else if (ArrayUtils.contains(Constants.SQL_BLOB_TYPE, type)) {
            return "byte[]";
        } else if (ArrayUtils.contains(Constants.SQL_BOOLEAN_TYPE, type)) {
            return "Boolean";
        } else if (ArrayUtils.contains(Constants.SQL_JSON_TYPE, type)) {
            return "String";
        } else if (ArrayUtils.contains(Constants.SQL_ENUM_TYPE, type)) {
            return "String";
        } else if (ArrayUtils.contains(Constants.SQL_SET_TYPE, type)) {
            return "String";
        } else if (ArrayUtils.contains(Constants.SQL_GEOMETRY_TYPE, type)) {
            return "String";
        } else {
            throw new RuntimeException("不支持的类型: " + type);
        }
    }
    
    /**
     * 获取类型分类
     */
    private static String getTypeCategory(String type) {
        if (ArrayUtils.contains(Constants.SQL_INTEGER_TYPE, type)) return "整数";
        if (ArrayUtils.contains(Constants.SQL_LONG_TYPE, type)) return "长整数";
        if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE, type)) return "浮点数";
        if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type)) return "日期时间";
        if (ArrayUtils.contains(Constants.SQL_DATE_TYPE, type)) return "日期";
        if (ArrayUtils.contains(Constants.SQL_TIME_TYPE, type)) return "时间";
        if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, type)) return "字符串";
        if (ArrayUtils.contains(Constants.SQL_BLOB_TYPE, type)) return "二进制";
        if (ArrayUtils.contains(Constants.SQL_BOOLEAN_TYPE, type)) return "布尔";
        if (ArrayUtils.contains(Constants.SQL_JSON_TYPE, type)) return "JSON";
        if (ArrayUtils.contains(Constants.SQL_ENUM_TYPE, type)) return "枚举";
        if (ArrayUtils.contains(Constants.SQL_SET_TYPE, type)) return "集合";
        if (ArrayUtils.contains(Constants.SQL_GEOMETRY_TYPE, type)) return "几何";
        return "未知";
    }
    
    /**
     * 是否支持模糊查询
     */
    private static boolean supportsFuzzyQuery(String type) {
        return ArrayUtils.contains(Constants.SQL_STRING_TYPE, type) ||
               ArrayUtils.contains(Constants.SQL_JSON_TYPE, type) ||
               ArrayUtils.contains(Constants.SQL_ENUM_TYPE, type) ||
               ArrayUtils.contains(Constants.SQL_SET_TYPE, type);
    }
    
    /**
     * 是否支持范围查询
     */
    private static boolean supportsRangeQuery(String type) {
        return ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type) ||
               ArrayUtils.contains(Constants.SQL_DATE_TYPE, type);
    }
}
