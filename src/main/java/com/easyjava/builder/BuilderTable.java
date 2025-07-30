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
import com.easyjava.utils.ShardingConfigUtils;

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

                // 初始化分表配置
                initShardingConfig(tableInfo);

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

        // 清空旧的索引信息
        tableInfo.getKeyIndexMap().clear();

        try {
            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_INDEX, tableInfo.getTableName()));
            fieldResult = ps.executeQuery();

            Map<String, FieldInfo> tempMap = new HashMap<>();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                tempMap.put(fieldInfo.getFieldName(), fieldInfo);
            }

            // 临时存储索引字段及其顺序
            Map<String, List<FieldInfoWithOrder>> indexFieldOrderMap = new HashMap<>();

            while (fieldResult.next()) {
                String keyName = fieldResult.getString("key_name");
                String columnName = fieldResult.getString("column_name");
                int seqInIndex = fieldResult.getInt("seq_in_index");

                FieldInfo field = tempMap.get(columnName);
                if (field == null) {
                    continue;
                }

                // 跳过时间类型字段
                String type = field.getSqlType();
                if (ArrayUtils.contains(Constants.SQL_DATE_TYPE, type)
                        || ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type)) {
                    continue;
                }

                List<FieldInfoWithOrder> fieldList = indexFieldOrderMap.computeIfAbsent(keyName, k -> new ArrayList<>());
                fieldList.add(new FieldInfoWithOrder(field, seqInIndex));
            }

            // 按顺序填充到 keyIndexMap
            for (Map.Entry<String, List<FieldInfoWithOrder>> entry : indexFieldOrderMap.entrySet()) {
                List<FieldInfoWithOrder> list = entry.getValue();
                list.sort((a, b) -> Integer.compare(a.seq, b.seq));
                List<FieldInfo> sortedFields = new ArrayList<>();
                for (FieldInfoWithOrder f : list) {
                    sortedFields.add(f.fieldInfo);
                }
                tableInfo.getKeyIndexMap().put(entry.getKey(), sortedFields);
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

    private static class FieldInfoWithOrder {
        FieldInfo fieldInfo;
        int seq;
        FieldInfoWithOrder(FieldInfo fieldInfo, int seq) {
            this.fieldInfo = fieldInfo;
            this.seq = seq;
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

    /**
     * 初始化分表配置
     * @param tableInfo 表信息
     */
    private static void initShardingConfig(TableInfo tableInfo) {
        // 检查分表功能是否启用
        if (!ShardingConfigUtils.isShardingEnabled()) {
            tableInfo.setEnableSharding(false);
            return;
        }
        
        String tableName = tableInfo.getTableName();
        
        // 检查配置文件中是否配置了该表的分表信息
        if (ShardingConfigUtils.isShardingTable(tableName)) {
            String shardingField = ShardingConfigUtils.getShardingField(tableName);
            String shardingStrategy = ShardingConfigUtils.getShardingStrategy(tableName);
            
            // 验证分表字段是否存在于表中
            if (isFieldExist(tableInfo, shardingField)) {
                tableInfo.setEnableSharding(true);
                tableInfo.setShardingField(shardingField);
                tableInfo.setShardingStrategy(shardingStrategy);
                
                log.info("表 {} 启用分表配置: 分表字段={}, 分表策略={}", 
                        tableName, shardingField, shardingStrategy);
            } else {
                log.warn("表 {} 配置的分表字段 {} 不存在，禁用分表", tableName, shardingField);
                tableInfo.setEnableSharding(false);
            }
        } else {
            // 自动检测是否需要分表
            autoDetectSharding(tableInfo);
        }
    }
    
    /**
     * 检查字段是否存在于表中
     * @param tableInfo 表信息
     * @param fieldName 字段名（属性名）
     * @return 是否存在
     */
    private static boolean isFieldExist(TableInfo tableInfo, String fieldName) {
        if (fieldName == null) return false;
        
        for (FieldInfo field : tableInfo.getFieldList()) {
            if (fieldName.equals(field.getPropertyName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 自动检测是否需要分表
     * @param tableInfo 表信息
     */
    private static void autoDetectSharding(TableInfo tableInfo) {
        // 如果分表功能未启用，直接返回
        if (!ShardingConfigUtils.isShardingEnabled()) {
            tableInfo.setEnableSharding(false);
            return;
        }
        
        String tableName = tableInfo.getTableName().toLowerCase();
        
        // 默认not启用分表
        tableInfo.setEnableSharding(false);
        
        // 示例：如果表名包含特定关键字，启用分表
        if (tableName.contains("log") || tableName.contains("order") || 
            tableName.contains("record") || tableName.contains("history")) {
            
            // 查找合适的分表字段
            String shardingField = findShardingField(tableInfo);
            if (shardingField != null) {
                tableInfo.setEnableSharding(true);
                tableInfo.setShardingField(shardingField);
                
                // 根据分表字段类型确定分表策略
                String strategy = determineShardingStrategy(tableInfo, shardingField);
                tableInfo.setShardingStrategy(strategy);
                
                log.info("自动检测表 {} 启用分表配置: 分表字段={}, 分表策略={}", 
                        tableName, shardingField, strategy);
            }
        }
    }

    /**
     * 查找合适的分表字段
     * @param tableInfo 表信息
     * @return 分表字段名
     */
    private static String findShardingField(TableInfo tableInfo) {
        // 优先查找时间类型字段
        for (FieldInfo field : tableInfo.getFieldList()) {
            String fieldName = field.getFieldName().toLowerCase();
            String sqlType = field.getSqlType();
            
            // 时间字段优先作为分表字段
            if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, sqlType) || 
                ArrayUtils.contains(Constants.SQL_DATE_TYPE, sqlType)) {
                if (fieldName.contains("create") || fieldName.contains("time") || 
                    fieldName.contains("date")) {
                    return field.getPropertyName();
                }
            }
        }
        
        // 如果没有时间字段，查找ID类型字段
        for (FieldInfo field : tableInfo.getFieldList()) {
            String fieldName = field.getFieldName().toLowerCase();
            String sqlType = field.getSqlType();
            
            if (ArrayUtils.contains(Constants.SQL_INTEGER_TYPE, sqlType) || 
                ArrayUtils.contains(Constants.SQL_LONG_TYPE, sqlType)) {
                if (fieldName.contains("id") && !fieldName.equals("id")) {
                    return field.getPropertyName();
                }
            }
        }
        
        // 最后考虑主键ID
        for (FieldInfo field : tableInfo.getFieldList()) {
            if (field.getIsAutoIncrement() != null && field.getIsAutoIncrement()) {
                return field.getPropertyName();
            }
        }
        
        return null;
    }

    /**
     * 确定分表策略
     * @param tableInfo 表信息
     * @param shardingField 分表字段
     * @return 分表策略
     */
    private static String determineShardingStrategy(TableInfo tableInfo, String shardingField) {
        // 根据分表字段找到对应的FieldInfo
        for (FieldInfo field : tableInfo.getFieldList()) {
            if (field.getPropertyName().equals(shardingField)) {
                String sqlType = field.getSqlType();
                
                // 时间类型使用时间分表
                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, sqlType) || 
                    ArrayUtils.contains(Constants.SQL_DATE_TYPE, sqlType)) {
                    return "time";
                }
                
                // 整数类型根据字段名判断
                if (ArrayUtils.contains(Constants.SQL_INTEGER_TYPE, sqlType) || 
                    ArrayUtils.contains(Constants.SQL_LONG_TYPE, sqlType)) {
                    String fieldName = field.getFieldName().toLowerCase();
                    if (fieldName.contains("id")) {
                        // ID字段使用哈希分表
                        return "hash";
                    } else {
                        // 其他数值字段使用范围分表
                        return "range";
                    }
                }
                
                // 字符串类型使用哈希分表
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, sqlType)) {
                    return "hash";
                }
                
                break;
            }
        }
        
        // 默认使用哈希分表
        return "hash";
    }

}
