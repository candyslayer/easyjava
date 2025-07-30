package com.easyjava.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 分表工具类
 */
public class ShardingUtils {

    /**
     * 根据分表策略获取表名
     * @param baseTableName 基础表名
     * @param shardingValue 分表字段值
     * @param strategy 分表策略：hash, range, time
     * @return 分表后的表名
     */
    public static String getTableName(String baseTableName, Object shardingValue, String strategy) {
        if (shardingValue == null) {
            return baseTableName;
        }
        
        // 检查是否需要跨表分表
        if (CrossTableShardingUtils.isCrossTableSharding(baseTableName)) {
            String crossTableConfig = CrossTableShardingUtils.getCrossTableConfig(baseTableName);
            return CrossTableShardingUtils.getCrossTableShardingTableName(baseTableName, shardingValue, strategy, crossTableConfig);
        }

        switch (strategy.toLowerCase()) {
            case "hash":
                return getHashTableName(baseTableName, shardingValue);
            case "range":
                return getRangeTableName(baseTableName, shardingValue);
            case "time":
                return getTimeTableName(baseTableName, shardingValue);
            default:
                return baseTableName;
        }
    }

    /**
     * 哈希分表
     * @param baseTableName 基础表名
     * @param shardingValue 分表字段值
     * @return 分表名
     */
    private static String getHashTableName(String baseTableName, Object shardingValue) {
        int hash = Math.abs(shardingValue.hashCode());
        int tableIndex = hash % 10; // 默认分10张表
        return baseTableName + "_" + String.format("%02d", tableIndex);
    }

    /**
     * 范围分表
     * @param baseTableName 基础表名
     * @param shardingValue 分表字段值
     * @return 分表名
     */
    private static String getRangeTableName(String baseTableName, Object shardingValue) {
        if (shardingValue instanceof Number) {
            long value = ((Number) shardingValue).longValue();
            int tableIndex = (int) (value / 10000); // 每10000条记录一张表
            return baseTableName + "_" + String.format("%02d", tableIndex);
        }
        return baseTableName;
    }

    /**
     * 时间分表
     * @param baseTableName 基础表名
     * @param shardingValue 分表字段值
     * @return 分表名
     */
    private static String getTimeTableName(String baseTableName, Object shardingValue) {
        if (shardingValue instanceof Date) {
            Date date = (Date) shardingValue;
            LocalDate localDate = new Date(date.getTime()).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            String suffix = localDate.format(DateTimeFormatter.ofPattern("yyyyMM"));
            return baseTableName + "_" + suffix;
        } else if (shardingValue instanceof LocalDate) {
            LocalDate localDate = (LocalDate) shardingValue;
            String suffix = localDate.format(DateTimeFormatter.ofPattern("yyyyMM"));
            return baseTableName + "_" + suffix;
        } else if (shardingValue instanceof String) {
            String dateStr = (String) shardingValue;
            try {
                LocalDate localDate = LocalDate.parse(dateStr);
                String suffix = localDate.format(DateTimeFormatter.ofPattern("yyyyMM"));
                return baseTableName + "_" + suffix;
            } catch (Exception e) {
                return baseTableName;
            }
        }
        return baseTableName;
    }

    /**
     * 获取分表数量（用于多表查询）
     * @param strategy 分表策略
     * @return 分表数量
     */
    public static int getShardingCount(String strategy) {
        switch (strategy.toLowerCase()) {
            case "hash":
                return 10; // 默认哈希分10张表
            case "range":
                return 100; // 默认范围分100张表
            case "time":
                return 12; // 默认按月分表，一年12张表
            default:
                return 1;
        }
    }

    /**
     * 获取所有分表名称
     * @param baseTableName 基础表名
     * @param strategy 分表策略
     * @return 所有分表名称
     */
    public static String[] getAllTableNames(String baseTableName, String strategy) {
        int count = getShardingCount(strategy);
        String[] tableNames = new String[count];
        
        for (int i = 0; i < count; i++) {
            if ("time".equals(strategy.toLowerCase())) {
                // 时间分表：当前年份的12个月
                LocalDate now = LocalDate.now();
                LocalDate date = now.withMonth(i + 1);
                String suffix = date.format(DateTimeFormatter.ofPattern("yyyyMM"));
                tableNames[i] = baseTableName + "_" + suffix;
            } else {
                // 哈希或范围分表
                tableNames[i] = baseTableName + "_" + String.format("%02d", i);
            }
        }
        
        return tableNames;
    }
    
    /**
     * 自动创建分表（如果不存在）
     * @param baseTableName 基础表名
     * @param strategy 分表策略
     */
    public static void autoCreateShardingTables(String baseTableName, String strategy) {
        // 创建一个临时的TableInfo对象来调用创建方法
        com.easyjava.bean.TableInfo tableInfo = new com.easyjava.bean.TableInfo();
        tableInfo.setTableName(baseTableName);
        tableInfo.setEnableSharding(true);
        tableInfo.setShardingStrategy(strategy);
        
        ShardingTableCreator.createShardingTables(tableInfo);
    }
    
    /**
     * 检查并创建分表（在获取表名时自动触发）
     * @param baseTableName 基础表名
     * @param shardingValue 分表字段值
     * @param strategy 分表策略
     * @param autoCreate 是否自动创建不存在的分表
     * @return 分表后的表名
     */
    public static String getTableName(String baseTableName, Object shardingValue, String strategy, boolean autoCreate) {
        String tableName = getTableName(baseTableName, shardingValue, strategy);
        
        if (autoCreate && !tableName.equals(baseTableName)) {
            // 只有当确实需要分表时才自动创建
            autoCreateShardingTables(baseTableName, strategy);
        }
        
        return tableName;
    }
}
