package com.easyjava.utils;

import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分表配置工具类
 */
public class ShardingConfigUtils {
    
    private static final Logger log = LoggerFactory.getLogger(ShardingConfigUtils.class);
    
    private static Properties shardingProps = new Properties();
    
    static {
        loadShardingConfig();
    }
    
    /**
     * 加载分表配置
     */
    private static void loadShardingConfig() {
        InputStream is = null;
        try {
            is = ShardingConfigUtils.class.getClassLoader().getResourceAsStream("sharding-config.properties");
            if (is != null) {
                shardingProps.load(is);
                log.info("分表配置加载成功");
            } else {
                log.warn("未找到分表配置文件 sharding-config.properties");
            }
        } catch (Exception e) {
            log.error("加载分表配置失败", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 检查是否启用分表功能
     * @return 是否启用分表
     */
    public static boolean isShardingEnabled() {
        String enabled = shardingProps.getProperty("sharding.enabled", "false");
        return "true".equalsIgnoreCase(enabled);
    }
    
    /**
     * 获取需要分表的表名集合
     * @return 表名集合
     */
    public static Set<String> getShardingTables() {
        // 如果分表功能未启用，返回空集合
        if (!isShardingEnabled()) {
            return new HashSet<>();
        }
        
        String tables = shardingProps.getProperty("sharding.tables", "");
        Set<String> tableSet = new HashSet<>();
        if (!tables.isEmpty()) {
            String[] tableArray = tables.split(",");
            for (String table : tableArray) {
                tableSet.add(table.trim());
            }
        }
        return tableSet;
    }
    
    /**
     * 获取指定表的分表字段
     * @param tableName 表名
     * @return 分表字段名
     */
    public static String getShardingField(String tableName) {
        return shardingProps.getProperty(tableName + ".sharding.field");
    }
    
    /**
     * 获取指定表的分表策略
     * @param tableName 表名
     * @return 分表策略
     */
    public static String getShardingStrategy(String tableName) {
        return shardingProps.getProperty(tableName + ".sharding.strategy", "hash");
    }
    
    /**
     * 检查表是否需要分表
     * @param tableName 表名
     * @return 是否需要分表
     */
    public static boolean isShardingTable(String tableName) {
        // 如果分表功能未启用，直接返回false
        if (!isShardingEnabled()) {
            return false;
        }
        return getShardingTables().contains(tableName);
    }
}
