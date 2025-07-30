package com.easyjava.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easyjava.bean.TableInfo;

/**
 * 分表自动创建工具类
 */
public class ShardingTableCreator {
    
    private static final Logger log = LoggerFactory.getLogger(ShardingTableCreator.class);
    
    /**
     * 为指定表创建所有分表
     * @param tableInfo 表信息
     */
    public static void createShardingTables(TableInfo tableInfo) {
        if (!tableInfo.getEnableSharding()) {
            log.info("表 {} 未启用分表，跳过创建", tableInfo.getTableName());
            return;
        }
        
        Connection conn = null;
        try {
            conn = getConnection();
            
            // 检查原表是否存在
            if (!isTableExists(conn, tableInfo.getTableName())) {
                log.warn("原表 {} 不存在，无法创建分表", tableInfo.getTableName());
                return;
            }
            
            // 获取原表的建表语句
            String createTableSql = getCreateTableSql(conn, tableInfo.getTableName());
            if (createTableSql == null) {
                log.error("无法获取表 {} 的建表语句", tableInfo.getTableName());
                return;
            }
            
            // 获取所有分表名称
            String[] shardingTableNames = ShardingUtils.getAllTableNames(
                tableInfo.getTableName(), 
                tableInfo.getShardingStrategy()
            );
            
            // 创建分表
            int createdCount = 0;
            int skippedCount = 0;
            
            for (String shardingTableName : shardingTableNames) {
                if (isTableExists(conn, shardingTableName)) {
                    log.debug("分表 {} 已存在，跳过创建", shardingTableName);
                    skippedCount++;
                    continue;
                }
                
                // 修改建表语句中的表名
                String shardingCreateSql = createTableSql.replaceFirst(
                    "`?" + tableInfo.getTableName() + "`?", 
                    "`" + shardingTableName + "`"
                );
                
                // 执行建表语句
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(shardingCreateSql);
                    log.info("成功创建分表: {}", shardingTableName);
                    createdCount++;
                } catch (Exception e) {
                    log.error("创建分表 {} 失败: {}", shardingTableName, e.getMessage());
                }
            }
            
            log.info("表 {} 分表创建完成 - 新建: {}, 跳过: {}", 
                    tableInfo.getTableName(), createdCount, skippedCount);
            
        } catch (Exception e) {
            log.error("创建分表失败", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    log.error("关闭数据库连接失败", e);
                }
            }
        }
    }
    
    /**
     * 批量创建多个表的分表
     * @param tableInfos 表信息列表
     */
    public static void createAllShardingTables(List<TableInfo> tableInfos) {
        log.info("开始批量创建分表...");
        
        int totalTables = 0;
        for (TableInfo tableInfo : tableInfos) {
            if (tableInfo.getEnableSharding()) {
                totalTables++;
                createShardingTables(tableInfo);
            }
        }
        
        log.info("批量创建分表完成，共处理 {} 张启用分表的表", totalTables);
    }
    
    /**
     * 为时间分表创建未来几个月的表
     * @param tableInfo 表信息
     * @param monthsAhead 提前创建几个月的表
     */
    public static void createFutureTimeTables(TableInfo tableInfo, int monthsAhead) {
        if (!tableInfo.getEnableSharding() || !"time".equals(tableInfo.getShardingStrategy())) {
            return;
        }
        
        Connection conn = null;
        try {
            conn = getConnection();
            
            // 获取原表建表语句
            String createTableSql = getCreateTableSql(conn, tableInfo.getTableName());
            if (createTableSql == null) {
                return;
            }
            
            // 创建未来几个月的表
            LocalDate now = LocalDate.now();
            for (int i = 1; i <= monthsAhead; i++) {
                LocalDate futureDate = now.plusMonths(i);
                String suffix = futureDate.format(DateTimeFormatter.ofPattern("yyyyMM"));
                String futureTableName = tableInfo.getTableName() + "_" + suffix;
                
                if (!isTableExists(conn, futureTableName)) {
                    String shardingCreateSql = createTableSql.replaceFirst(
                        "`?" + tableInfo.getTableName() + "`?", 
                        "`" + futureTableName + "`"
                    );
                    
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(shardingCreateSql);
                        log.info("成功创建未来分表: {}", futureTableName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("创建未来时间分表失败", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    log.error("关闭数据库连接失败", e);
                }
            }
        }
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
        return DriverManager.getConnection(url, user, password);
    }
    
    /**
     * 检查表是否存在
     */
    private static boolean isTableExists(Connection conn, String tableName) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM information_schema.tables WHERE table_name = ? AND table_schema = DATABASE()")) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            log.error("检查表 {} 是否存在失败: {}", tableName, e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取表的建表语句
     */
    private static String getCreateTableSql(Connection conn, String tableName) {
        try (PreparedStatement ps = conn.prepareStatement("SHOW CREATE TABLE `" + tableName + "`")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(2);
                }
            }
        } catch (Exception e) {
            log.error("获取表 {} 建表语句失败: {}", tableName, e.getMessage());
        }
        return null;
    }
    
    /**
     * 清理过期的时间分表（可选功能）
     * @param tableInfo 表信息
     * @param monthsToKeep 保留多少个月的历史表
     */
    public static void cleanupExpiredTimeTables(TableInfo tableInfo, int monthsToKeep) {
        if (!tableInfo.getEnableSharding() || !"time".equals(tableInfo.getShardingStrategy())) {
            return;
        }
        
        Connection conn = null;
        try {
            conn = getConnection();
            
            // 获取所有存在的分表
            List<String> existingTables = getExistingShardingTables(conn, tableInfo.getTableName());
            
            LocalDate cutoffDate = LocalDate.now().minusMonths(monthsToKeep);
            
            for (String tableName : existingTables) {
                // 提取表名中的日期部分
                String suffix = tableName.substring(tableInfo.getTableName().length() + 1);
                try {
                    LocalDate tableDate = LocalDate.parse(suffix + "01", DateTimeFormatter.ofPattern("yyyyMMdd"));
                    if (tableDate.isBefore(cutoffDate)) {
                        // 删除过期表（谨慎操作）
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute("DROP TABLE `" + tableName + "`");
                            log.info("删除过期分表: {}", tableName);
                        }
                    }
                } catch (Exception e) {
                    log.debug("跳过非时间格式的表: {}", tableName);
                }
            }
        } catch (Exception e) {
            log.error("清理过期分表失败", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    log.error("关闭数据库连接失败", e);
                }
            }
        }
    }
    
    /**
     * 获取已存在的分表列表
     */
    private static List<String> getExistingShardingTables(Connection conn, String baseTableName) {
        List<String> tables = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT table_name FROM information_schema.tables WHERE table_name LIKE ? AND table_schema = DATABASE()")) {
            ps.setString(1, baseTableName + "_%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
            }
        } catch (Exception e) {
            log.error("获取分表列表失败", e);
        }
        return tables;
    }
}
