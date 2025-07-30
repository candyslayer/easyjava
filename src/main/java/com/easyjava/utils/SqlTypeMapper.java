package com.easyjava.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.easyjava.bean.Constants;

/**
 * SQL类型到Java类型的映射工具类
 * 支持多种数据库的类型映射（MySQL、PostgreSQL、Oracle、SQL Server等）
 * 
 * @author 唐伟
 * @since 2025-07-30
 */
public class SqlTypeMapper {
    
    /**
     * Java类型信息
     */
    public static class JavaTypeInfo {
        private String javaType;
        private String importPackage;
        private String defaultValue;
        private boolean needsImport;
        
        public JavaTypeInfo(String javaType, String importPackage, String defaultValue, boolean needsImport) {
            this.javaType = javaType;
            this.importPackage = importPackage;
            this.defaultValue = defaultValue;
            this.needsImport = needsImport;
        }
        
        // Getters
        public String getJavaType() { return javaType; }
        public String getImportPackage() { return importPackage; }
        public String getDefaultValue() { return defaultValue; }
        public boolean isNeedsImport() { return needsImport; }
    }
    
    /**
     * 类型映射缓存
     */
    private static final Map<String, JavaTypeInfo> TYPE_MAPPING_CACHE = new HashMap<>();
    
    static {
        initializeTypeMapping();
    }
    
    /**
     * 初始化类型映射
     */
    private static void initializeTypeMapping() {
        // 整数类型
        for (String type : Constants.SQL_INTEGER_TYPE) {
            TYPE_MAPPING_CACHE.put(type.toLowerCase(), 
                new JavaTypeInfo("Integer", null, "0", false));
        }
        
        // 长整数类型
        for (String type : Constants.SQL_LONG_TYPE) {
            TYPE_MAPPING_CACHE.put(type.toLowerCase(), 
                new JavaTypeInfo("Long", null, "0L", false));
        }
        
        // 浮点数类型
        for (String type : Constants.SQL_FLOAT_TYPE) {
            TYPE_MAPPING_CACHE.put(type.toLowerCase(), 
                new JavaTypeInfo("Float", null, "0.0F", false));
        }
        
        // 双精度类型
        for (String type : Constants.SQL_DOUBLE_TYPE) {
            TYPE_MAPPING_CACHE.put(type.toLowerCase(), 
                new JavaTypeInfo("Double", null, "0.0", false));
        }
        
        // 精确数值类型
        for (String type : Constants.SQL_DECIMAL_TYPE) {
            TYPE_MAPPING_CACHE.put(type.toLowerCase(), 
                new JavaTypeInfo("BigDecimal", "java.math.BigDecimal", "BigDecimal.ZERO", true));
        }
        
        // 字符串类型
        for (String type : Constants.SQL_STRING_TYPE) {
            TYPE_MAPPING_CACHE.put(type.toLowerCase(), 
                new JavaTypeInfo("String", null, "null", false));
        }
        
        // 日期类型
        for (String type : Constants.SQL_DATE_TYPE) {
            TYPE_MAPPING_CACHE.put(type.toLowerCase(), 
                new JavaTypeInfo("Date", "java.util.Date", "new Date()", true));
        }
        
        // 日期时间类型
        for (String type : Constants.SQL_DATE_TIME_TYPES) {
            TYPE_MAPPING_CACHE.put(type.toLowerCase(), 
                new JavaTypeInfo("Date", "java.util.Date", "new Date()", true));
        }
        
        // 时间类型
        for (String type : Constants.SQL_TIME_TYPE) {
            TYPE_MAPPING_CACHE.put(type.toLowerCase(), 
                new JavaTypeInfo("Time", "java.sql.Time", "new Time(System.currentTimeMillis())", true));
        }
        
        // 布尔类型
        for (String type : Constants.SQL_BOOLEAN_TYPE) {
            TYPE_MAPPING_CACHE.put(type.toLowerCase(), 
                new JavaTypeInfo("Boolean", null, "false", false));
        }
        
        // 二进制数据类型
        for (String type : Constants.SQL_BINARY_TYPE) {
            TYPE_MAPPING_CACHE.put(type.toLowerCase(), 
                new JavaTypeInfo("byte[]", null, "null", false));
        }
        
        // JSON类型
        for (String type : Constants.SQL_JSON_TYPE) {
            TYPE_MAPPING_CACHE.put(type.toLowerCase(), 
                new JavaTypeInfo("String", null, "null", false));
        }
        
        // UUID类型
        for (String type : Constants.SQL_UUID_TYPE) {
            TYPE_MAPPING_CACHE.put(type.toLowerCase(), 
                new JavaTypeInfo("String", null, "null", false));
        }
        
        // 枚举类型
        for (String type : Constants.SQL_ENUM_TYPE) {
            TYPE_MAPPING_CACHE.put(type.toLowerCase(), 
                new JavaTypeInfo("String", null, "null", false));
        }
        
        // 几何类型
        for (String type : Constants.SQL_GEOMETRY_TYPE) {
            TYPE_MAPPING_CACHE.put(type.toLowerCase(), 
                new JavaTypeInfo("String", null, "null", false));
        }
        
        // 数组类型
        for (String type : Constants.SQL_ARRAY_TYPE) {
            TYPE_MAPPING_CACHE.put(type.toLowerCase(), 
                new JavaTypeInfo("String", null, "null", false));
        }
    }
    
    /**
     * 将SQL类型转换为Java类型
     * 
     * @param sqlType SQL数据类型
     * @return Java类型信息
     */
    public static JavaTypeInfo getJavaTypeInfo(String sqlType) {
        if (StringUtils.isEmpty(sqlType)) {
            return new JavaTypeInfo("String", null, "null", false);
        }
        
        // 处理带参数的类型，如 varchar(255), decimal(10,2)
        String baseType = extractBaseType(sqlType);
        
        JavaTypeInfo typeInfo = TYPE_MAPPING_CACHE.get(baseType.toLowerCase());
        
        if (typeInfo != null) {
            return typeInfo;
        }
        
        // 如果没有找到匹配的类型，尝试模糊匹配
        JavaTypeInfo fuzzyMatch = fuzzyMatchType(baseType);
        if (fuzzyMatch != null) {
            return fuzzyMatch;
        }
        
        // 默认返回String类型
        return new JavaTypeInfo("String", null, "null", false);
    }
    
    /**
     * 获取Java类型名称
     * 
     * @param sqlType SQL数据类型
     * @return Java类型名称
     */
    public static String getJavaType(String sqlType) {
        return getJavaTypeInfo(sqlType).getJavaType();
    }
    
    /**
     * 获取需要导入的包
     * 
     * @param sqlType SQL数据类型
     * @return 导入包路径，如果不需要导入则返回null
     */
    public static String getImportPackage(String sqlType) {
        JavaTypeInfo typeInfo = getJavaTypeInfo(sqlType);
        return typeInfo.isNeedsImport() ? typeInfo.getImportPackage() : null;
    }
    
    /**
     * 获取默认值
     * 
     * @param sqlType SQL数据类型
     * @return 默认值
     */
    public static String getDefaultValue(String sqlType) {
        return getJavaTypeInfo(sqlType).getDefaultValue();
    }
    
    /**
     * 提取基础类型名（去除参数）
     * 
     * @param sqlType 完整的SQL类型
     * @return 基础类型名
     */
    private static String extractBaseType(String sqlType) {
        if (StringUtils.isEmpty(sqlType)) {
            return sqlType;
        }
        
        // 处理 varchar(255) -> varchar
        int parenthesesIndex = sqlType.indexOf('(');
        if (parenthesesIndex > 0) {
            return sqlType.substring(0, parenthesesIndex).trim();
        }
        
        // 处理 unsigned 关键字
        String lowerType = sqlType.toLowerCase().trim();
        if (lowerType.contains(" unsigned")) {
            return sqlType.trim();
        }
        
        return sqlType.trim();
    }
    
    /**
     * 模糊匹配类型
     * 
     * @param sqlType SQL类型
     * @return 匹配的Java类型信息
     */
    private static JavaTypeInfo fuzzyMatchType(String sqlType) {
        String lowerType = sqlType.toLowerCase();
        
        // 模糊匹配规则
        if (lowerType.contains("int")) {
            if (lowerType.contains("big")) {
                return new JavaTypeInfo("Long", null, "0L", false);
            } else if (lowerType.contains("tiny") || lowerType.contains("small")) {
                return new JavaTypeInfo("Integer", null, "0", false);
            } else {
                return new JavaTypeInfo("Integer", null, "0", false);
            }
        }
        
        if (lowerType.contains("char") || lowerType.contains("text") || lowerType.contains("string")) {
            return new JavaTypeInfo("String", null, "null", false);
        }
        
        if (lowerType.contains("decimal") || lowerType.contains("numeric")) {
            return new JavaTypeInfo("BigDecimal", "java.math.BigDecimal", "BigDecimal.ZERO", true);
        }
        
        if (lowerType.contains("float") || lowerType.contains("real")) {
            return new JavaTypeInfo("Float", null, "0.0F", false);
        }
        
        if (lowerType.contains("double")) {
            return new JavaTypeInfo("Double", null, "0.0", false);
        }
        
        if (lowerType.contains("date") || lowerType.contains("time")) {
            return new JavaTypeInfo("Date", "java.util.Date", "new Date()", true);
        }
        
        if (lowerType.contains("bool") || lowerType.contains("bit")) {
            return new JavaTypeInfo("Boolean", null, "false", false);
        }
        
        if (lowerType.contains("blob") || lowerType.contains("binary")) {
            return new JavaTypeInfo("byte[]", null, "null", false);
        }
        
        if (lowerType.contains("json")) {
            return new JavaTypeInfo("String", null, "null", false);
        }
        
        return null;
    }
    
    /**
     * 检查类型是否为数值类型
     * 
     * @param sqlType SQL类型
     * @return 是否为数值类型
     */
    public static boolean isNumericType(String sqlType) {
        String javaType = getJavaType(sqlType);
        return "Integer".equals(javaType) || "Long".equals(javaType) || 
               "Float".equals(javaType) || "Double".equals(javaType) || 
               "BigDecimal".equals(javaType);
    }
    
    /**
     * 检查类型是否为日期时间类型
     * 
     * @param sqlType SQL类型
     * @return 是否为日期时间类型
     */
    public static boolean isDateTimeType(String sqlType) {
        String baseType = extractBaseType(sqlType).toLowerCase();
        return ArrayUtils.contains(Constants.SQL_DATE_TYPE, baseType) ||
               ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, baseType) ||
               ArrayUtils.contains(Constants.SQL_TIME_TYPE, baseType);
    }
    
    /**
     * 检查类型是否为字符串类型
     * 
     * @param sqlType SQL类型
     * @return 是否为字符串类型
     */
    public static boolean isStringType(String sqlType) {
        String javaType = getJavaType(sqlType);
        return "String".equals(javaType);
    }
    
    /**
     * 检查类型是否为布尔类型
     * 
     * @param sqlType SQL类型
     * @return 是否为布尔类型
     */
    public static boolean isBooleanType(String sqlType) {
        String javaType = getJavaType(sqlType);
        return "Boolean".equals(javaType);
    }
    
    /**
     * 获取所有支持的SQL类型
     * 
     * @return 支持的SQL类型列表
     */
    public static String[] getSupportedSqlTypes() {
        return TYPE_MAPPING_CACHE.keySet().toArray(new String[0]);
    }
    
    /**
     * 检查SQL类型是否被支持
     * 
     * @param sqlType SQL类型
     * @return 是否支持
     */
    public static boolean isSupportedType(String sqlType) {
        if (StringUtils.isEmpty(sqlType)) {
            return false;
        }
        
        String baseType = extractBaseType(sqlType).toLowerCase();
        return TYPE_MAPPING_CACHE.containsKey(baseType) || fuzzyMatchType(baseType) != null;
    }
}
