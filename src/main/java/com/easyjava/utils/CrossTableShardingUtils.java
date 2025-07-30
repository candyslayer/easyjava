package com.easyjava.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 跨表分表工具类
 * 支持根据其他表的字段值进行分表
 */
public class CrossTableShardingUtils {
    
    private static final Logger log = LoggerFactory.getLogger(CrossTableShardingUtils.class);
    
    // 缓存跨表查询结果，避免频繁数据库查询
    private static final Map<String, Object> crossTableCache = new HashMap<>();
    private static final long CACHE_EXPIRE_TIME = 5 * 60 * 1000; // 5分钟缓存
    private static final Map<String, Long> cacheTimestamp = new HashMap<>();
    
    /**
     * 根据跨表配置获取分表值
     * @param tableName 目标表名
     * @param referenceValue 参考值（比如用户ID）
     * @param crossTableConfig 跨表配置
     * @return 用于分表的值
     */
    public static Object getCrossTableShardingValue(String tableName, Object referenceValue, String crossTableConfig) {
        if (referenceValue == null || crossTableConfig == null) {
            return referenceValue;
        }
        
        try {
            // 解析跨表配置：格式为 "referenceTable.referenceField->targetField"
            // 例如：user.id->user_id  表示根据user表的id字段值来获取当前记录的user_id字段值进行分表
            String[] parts = crossTableConfig.split("->");
            if (parts.length != 2) {
                log.warn("跨表分表配置格式错误: {}", crossTableConfig);
                return referenceValue;
            }
            
            String[] refParts = parts[0].split("\\.");
            if (refParts.length != 2) {
                log.warn("跨表分表配置格式错误: {}", crossTableConfig);
                return referenceValue;
            }
            
            String referenceTable = refParts[0];
            String referenceField = refParts[1];
            String targetField = parts[1];
            
            // 构建缓存key
            String cacheKey = referenceTable + "." + referenceField + "." + referenceValue;
            
            // 检查缓存
            if (isCacheValid(cacheKey)) {
                return crossTableCache.get(cacheKey);
            }
            
            // 查询跨表数据
            Object shardingValue = queryCrossTableValue(referenceTable, referenceField, referenceValue, targetField);
            
            // 更新缓存
            updateCache(cacheKey, shardingValue);
            
            return shardingValue != null ? shardingValue : referenceValue;
            
        } catch (Exception e) {
            log.error("跨表分表查询失败", e);
            return referenceValue;
        }
    }
    
    /**
     * 查询跨表字段值
     */
    private static Object queryCrossTableValue(String referenceTable, String referenceField, Object referenceValue, String targetField) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            String sql = "SELECT " + targetField + " FROM " + referenceTable + " WHERE " + referenceField + " = ?";
            ps = conn.prepareStatement(sql);
            ps.setObject(1, referenceValue);
            
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getObject(1);
            }
            
        } catch (Exception e) {
            log.error("查询跨表字段值失败: table={}, field={}, value={}", referenceTable, referenceField, referenceValue, e);
        } finally {
            closeResources(conn, ps, rs);
        }
        
        return null;
    }
    
    /**
     * 获取跨表分表配置
     * @param tableName 表名
     * @return 跨表配置，格式：referenceTable.referenceField->targetField
     */
    public static String getCrossTableConfig(String tableName) {
        return ShardingConfigUtils.getProperty(tableName + ".sharding.cross.table", null);
    }
    
    /**
     * 检查是否需要跨表分表
     * @param tableName 表名
     * @return 是否需要跨表分表
     */
    public static boolean isCrossTableSharding(String tableName) {
        return getCrossTableConfig(tableName) != null;
    }
    
    /**
     * 根据跨表配置和分表策略获取表名
     * @param baseTableName 基础表名
     * @param referenceValue 参考值
     * @param strategy 分表策略
     * @param crossTableConfig 跨表配置
     * @return 分表后的表名
     */
    public static String getCrossTableShardingTableName(String baseTableName, Object referenceValue, String strategy, String crossTableConfig) {
        Object shardingValue = getCrossTableShardingValue(baseTableName, referenceValue, crossTableConfig);
        return ShardingUtils.getTableName(baseTableName, shardingValue, strategy);
    }
    
    /**
     * 检查缓存是否有效
     */
    private static boolean isCacheValid(String cacheKey) {
        if (!crossTableCache.containsKey(cacheKey)) {
            return false;
        }
        
        Long timestamp = cacheTimestamp.get(cacheKey);
        if (timestamp == null) {
            return false;
        }
        
        return System.currentTimeMillis() - timestamp < CACHE_EXPIRE_TIME;
    }
    
    /**
     * 更新缓存
     */
    private static void updateCache(String cacheKey, Object value) {
        crossTableCache.put(cacheKey, value);
        cacheTimestamp.put(cacheKey, System.currentTimeMillis());
    }
    
    /**
     * 清理过期缓存
     */
    public static void cleanExpiredCache() {
        long currentTime = System.currentTimeMillis();
        cacheTimestamp.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() >= CACHE_EXPIRE_TIME) {
                crossTableCache.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * 清空所有缓存
     */
    public static void clearCache() {
        crossTableCache.clear();
        cacheTimestamp.clear();
    }
    
    /**
     * 获取数据库连接
     */
    private static Connection getConnection() throws Exception {
        String driverName = PropertiesUtils.geString("spring.datasource.driver-class-name");
        String url = PropertiesUtils.geString("spring.datasource.url");
        String user = PropertiesUtils.geString("spring.datasource.username");
        String password = PropertiesUtils.geString("spring.datasource.password");
        
        Class.forName(driverName);
        return java.sql.DriverManager.getConnection(url, user, password);
    }
    
    /**
     * 关闭资源
     */
    private static void closeResources(Connection conn, PreparedStatement ps, ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (Exception e) { log.debug("关闭ResultSet失败", e); }
        }
        if (ps != null) {
            try { ps.close(); } catch (Exception e) { log.debug("关闭PreparedStatement失败", e); }
        }
        if (conn != null) {
            try { conn.close(); } catch (Exception e) { log.debug("关闭Connection失败", e); }
        }
    }
}
