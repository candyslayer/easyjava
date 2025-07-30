package com.easyjava.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 分表配置信息类
 * 
 * @author 唐伟
 * @since 2025-07-30
 */
public class ShardingConfig {
    
    /**
     * 分表功能是否启用
     */
    private boolean enabled = false;
    
    /**
     * 分表策略类型: hash(哈希分表), range(范围分表), time(时间分表), mod(取模分表)
     */
    private String strategyType = "hash";
    
    /**
     * 分表数量
     */
    private int tableCount = 8;
    
    /**
     * 分表后缀格式
     */
    private String suffixFormat = "_%d";
    
    /**
     * 分表映射配置 (目标表 -> 源表.字段名)
     */
    private Map<String, String> mappingConfig = new HashMap<>();
    
    /**
     * 分表字段配置 (表名 -> 分表字段名)
     */
    private Map<String, String> fieldConfig = new HashMap<>();
    
    /**
     * 是否启用分表写入数据库
     */
    private boolean databaseWriteEnabled = true;
    
    /**
     * 是否启用分表SQL生成
     */
    private boolean sqlGenerateEnabled = true;
    
    /**
     * 分表建表语句前缀
     */
    private String createTablePrefix = "CREATE TABLE IF NOT EXISTS";
    
    /**
     * 是否自动创建分表
     */
    private boolean autoCreateTable = true;
    
    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getStrategyType() {
        return strategyType;
    }
    
    public void setStrategyType(String strategyType) {
        this.strategyType = strategyType;
    }
    
    public int getTableCount() {
        return tableCount;
    }
    
    public void setTableCount(int tableCount) {
        this.tableCount = tableCount;
    }
    
    public String getSuffixFormat() {
        return suffixFormat;
    }
    
    public void setSuffixFormat(String suffixFormat) {
        this.suffixFormat = suffixFormat;
    }
    
    public Map<String, String> getMappingConfig() {
        return mappingConfig;
    }
    
    public void setMappingConfig(Map<String, String> mappingConfig) {
        this.mappingConfig = mappingConfig;
    }
    
    public Map<String, String> getFieldConfig() {
        return fieldConfig;
    }
    
    public void setFieldConfig(Map<String, String> fieldConfig) {
        this.fieldConfig = fieldConfig;
    }
    
    public boolean isDatabaseWriteEnabled() {
        return databaseWriteEnabled;
    }
    
    public void setDatabaseWriteEnabled(boolean databaseWriteEnabled) {
        this.databaseWriteEnabled = databaseWriteEnabled;
    }
    
    public boolean isSqlGenerateEnabled() {
        return sqlGenerateEnabled;
    }
    
    public void setSqlGenerateEnabled(boolean sqlGenerateEnabled) {
        this.sqlGenerateEnabled = sqlGenerateEnabled;
    }
    
    public String getCreateTablePrefix() {
        return createTablePrefix;
    }
    
    public void setCreateTablePrefix(String createTablePrefix) {
        this.createTablePrefix = createTablePrefix;
    }
    
    public boolean isAutoCreateTable() {
        return autoCreateTable;
    }
    
    public void setAutoCreateTable(boolean autoCreateTable) {
        this.autoCreateTable = autoCreateTable;
    }
    
    /**
     * 添加分表映射配置
     * @param targetTable 目标表名
     * @param sourceMapping 源表.字段名
     */
    public void addMapping(String targetTable, String sourceMapping) {
        this.mappingConfig.put(targetTable, sourceMapping);
    }
    
    /**
     * 添加分表字段配置
     * @param tableName 表名
     * @param fieldName 分表字段名
     */
    public void addField(String tableName, String fieldName) {
        this.fieldConfig.put(tableName, fieldName);
    }
    
    /**
     * 获取表的分表字段
     * @param tableName 表名
     * @return 分表字段名
     */
    public String getShardingField(String tableName) {
        return fieldConfig.get(tableName);
    }
    
    /**
     * 获取表的分表映射源
     * @param tableName 表名
     * @return 源表.字段名
     */
    public String getShardingMapping(String tableName) {
        return mappingConfig.get(tableName);
    }
    
    /**
     * 检查表是否需要分表
     * @param tableName 表名
     * @return true表示需要分表
     */
    public boolean needSharding(String tableName) {
        return enabled && (fieldConfig.containsKey(tableName) || mappingConfig.containsKey(tableName));
    }
    
    @Override
    public String toString() {
        return "ShardingConfig{" +
                "enabled=" + enabled +
                ", strategyType='" + strategyType + '\'' +
                ", tableCount=" + tableCount +
                ", suffixFormat='" + suffixFormat + '\'' +
                ", mappingConfig=" + mappingConfig +
                ", fieldConfig=" + fieldConfig +
                ", databaseWriteEnabled=" + databaseWriteEnabled +
                ", sqlGenerateEnabled=" + sqlGenerateEnabled +
                ", createTablePrefix='" + createTablePrefix + '\'' +
                ", autoCreateTable=" + autoCreateTable +
                '}';
    }
}
