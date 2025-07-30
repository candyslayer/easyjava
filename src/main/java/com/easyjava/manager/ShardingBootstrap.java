package com.easyjava.manager;

import com.easyjava.bean.ShardingConfig;
import com.easyjava.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * 分表启动器 - 直接读取配置文件启动分表功能
 * 无需交互，自动根据配置文件创建分表
 * 
 * @author 唐伟
 * @since 2025-07-30
 */
public class ShardingBootstrap {
    
    private static final Logger logger = LoggerFactory.getLogger(ShardingBootstrap.class);
    
    /**
     * 主方法 - 直接启动分表
     */
    public static void main(String[] args) {
        System.out.println("=== EasyJava 分表启动器 ===");
        System.out.println("正在读取配置文件并启动分表功能...");
        System.out.println();
        
        ShardingBootstrap bootstrap = new ShardingBootstrap();
        
        try {
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "auto":
                        bootstrap.autoCreateShardingTables();
                        break;
                    case "check":
                        bootstrap.checkConfiguration();
                        break;
                    case "show":
                        bootstrap.showConfiguration();
                        break;
                    case "test":
                        bootstrap.testShardingFunction();
                        break;
                    default:
                        bootstrap.autoCreateShardingTables();
                        break;
                }
            } else {
                bootstrap.autoCreateShardingTables();
            }
            
        } catch (Exception e) {
            logger.error("分表启动失败", e);
            System.out.println("❌ 分表启动失败: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * 自动创建分表
     */
    public void autoCreateShardingTables() {
        System.out.println("🚀 开始自动分表流程...");
        
        // 1. 检查分表功能是否启用
        if (!isShardingEnabled()) {
            System.out.println("⚠️ 分表功能未启用，请在配置文件中设置 sharding.enabled=true");
            return;
        }
        
        // 2. 加载分表配置
        ShardingConfig config = loadShardingConfig();
        if (config == null) {
            System.out.println("❌ 无法加载分表配置");
            return;
        }
        
        System.out.println("✅ 分表配置加载成功:");
        printConfigSummary(config);
        
        // 3. 验证配置
        if (!validateConfiguration(config)) {
            System.out.println("❌ 分表配置验证失败，请检查配置");
            return;
        }
        
        // 4. 检查数据库写入权限
        if (!config.isDatabaseWriteEnabled()) {
            System.out.println("⚠️ 数据库写入功能未启用，只显示配置信息");
            return;
        }
        
        // 5. 创建分表
        createShardingTables(config);
        
        System.out.println("🎉 分表启动流程完成！");
    }
    
    /**
     * 检查分表功能是否启用
     */
    private boolean isShardingEnabled() {
        return PropertiesUtils.getBoolean("sharding.enabled", false);
    }
    
    /**
     * 加载分表配置
     */
    private ShardingConfig loadShardingConfig() {
        try {
            ShardingConfig config = new ShardingConfig();
            
            // 基础配置
            config.setEnabled(PropertiesUtils.getBoolean("sharding.enabled", false));
            config.setStrategyType(PropertiesUtils.getString("sharding.strategy.type", "hash"));
            config.setTableCount(PropertiesUtils.getInt("sharding.table.count", 8));
            config.setSuffixFormat(PropertiesUtils.getString("sharding.table.suffix.format", "_%d"));
            
            // 数据库写入配置
            config.setDatabaseWriteEnabled(PropertiesUtils.getBoolean("sharding.database.write.enabled", true));
            config.setSqlGenerateEnabled(PropertiesUtils.getBoolean("sharding.sql.generate.enabled", true));
            config.setCreateTablePrefix(PropertiesUtils.getString("sharding.sql.create.prefix", "CREATE TABLE IF NOT EXISTS"));
            config.setAutoCreateTable(PropertiesUtils.getBoolean("sharding.auto.create.table", true));
            
            // 加载分表映射配置
            Map<String, String> mappingConfig = new HashMap<>();
            Properties props = PropertiesUtils.loadProperties();
            for (String key : props.stringPropertyNames()) {
                if (key.startsWith("sharding.mapping.")) {
                    String tableName = key.substring("sharding.mapping.".length());
                    String mapping = props.getProperty(key);
                    mappingConfig.put(tableName, mapping);
                }
            }
            config.setMappingConfig(mappingConfig);
            
            // 加载分表字段配置
            Map<String, String> fieldConfig = new HashMap<>();
            for (String key : props.stringPropertyNames()) {
                if (key.startsWith("sharding.field.")) {
                    String tableName = key.substring("sharding.field.".length());
                    String fieldName = props.getProperty(key);
                    fieldConfig.put(tableName, fieldName);
                }
            }
            config.setFieldConfig(fieldConfig);
            
            return config;
            
        } catch (Exception e) {
            logger.error("加载分表配置失败", e);
            return null;
        }
    }
    
    /**
     * 验证分表配置
     */
    private boolean validateConfiguration(ShardingConfig config) {
        List<String> errors = new ArrayList<>();
        
        // 验证基础配置
        if (config.getTableCount() <= 0 || config.getTableCount() > 1024) {
            errors.add("分表数量超出有效范围 (1-1024): " + config.getTableCount());
        }
        
        if (!Arrays.asList("hash", "mod", "range", "time").contains(config.getStrategyType())) {
            errors.add("无效的分表策略: " + config.getStrategyType());
        }
        
        // 验证是否配置了分表表格
        if (config.getMappingConfig().isEmpty() && config.getFieldConfig().isEmpty()) {
            errors.add("没有配置任何分表映射或字段，请至少配置一项");
        }
        
        // 验证映射配置格式
        for (Map.Entry<String, String> entry : config.getMappingConfig().entrySet()) {
            if (!entry.getValue().matches("\\w+\\.\\w+")) {
                errors.add("无效的映射配置格式: " + entry.getKey() + " -> " + entry.getValue());
            }
        }
        
        // 验证字段配置
        for (Map.Entry<String, String> entry : config.getFieldConfig().entrySet()) {
            if (entry.getKey().isEmpty() || entry.getValue().isEmpty()) {
                errors.add("分表字段配置不能为空: " + entry.getKey() + " -> " + entry.getValue());
            }
        }
        
        if (!errors.isEmpty()) {
            System.out.println("❌ 配置验证失败:");
            errors.forEach(error -> System.out.println("  " + error));
            return false;
        }
        
        System.out.println("✅ 配置验证通过");
        return true;
    }
    
    /**
     * 打印配置摘要
     */
    private void printConfigSummary(ShardingConfig config) {
        System.out.println("  分表策略: " + config.getStrategyType());
        System.out.println("  分表数量: " + config.getTableCount());
        System.out.println("  后缀格式: " + config.getSuffixFormat());
        System.out.println("  数据库写入: " + (config.isDatabaseWriteEnabled() ? "启用" : "禁用"));
        System.out.println("  自动创建: " + (config.isAutoCreateTable() ? "启用" : "禁用"));
        
        if (!config.getMappingConfig().isEmpty()) {
            System.out.println("  跨表映射数: " + config.getMappingConfig().size());
        }
        
        if (!config.getFieldConfig().isEmpty()) {
            System.out.println("  分表字段数: " + config.getFieldConfig().size());
        }
        System.out.println();
    }
    
    /**
     * 创建分表
     */
    private void createShardingTables(ShardingConfig config) {
        Connection connection = null;
        
        try {
            // 建立数据库连接
            connection = createDatabaseConnection();
            System.out.println("✅ 数据库连接建立成功");
            
            // 获取所有需要创建分表的表
            Set<String> allTables = new HashSet<>();
            allTables.addAll(config.getFieldConfig().keySet());
            allTables.addAll(config.getMappingConfig().keySet());
            
            System.out.println("📋 开始创建分表，共 " + allTables.size() + " 个主表");
            System.out.println();
            
            int totalShardTables = 0;
            int successCount = 0;
            
            // 为每个表创建分表
            for (String tableName : allTables) {
                System.out.println("🔄 正在为表 '" + tableName + "' 创建分表...");
                
                int tableSuccessCount = createShardingTablesForTable(connection, config, tableName);
                totalShardTables += config.getTableCount();
                successCount += tableSuccessCount;
                
                System.out.println("  ✅ 完成: " + tableSuccessCount + "/" + config.getTableCount() + " 个分表创建成功");
                System.out.println();
            }
            
            System.out.println("📊 分表创建汇总:");
            System.out.println("  主表数量: " + allTables.size());
            System.out.println("  分表总数: " + totalShardTables);
            System.out.println("  成功创建: " + successCount);
            System.out.println("  失败数量: " + (totalShardTables - successCount));
            
            if (successCount == totalShardTables) {
                System.out.println("🎉 所有分表创建成功！");
            } else {
                System.out.println("⚠️ 部分分表创建失败，请检查日志");
            }
            
        } catch (Exception e) {
            logger.error("创建分表过程中发生错误", e);
            System.out.println("❌ 创建分表失败: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("✅ 数据库连接已关闭");
                } catch (SQLException e) {
                    logger.error("关闭数据库连接失败", e);
                }
            }
        }
    }
    
    /**
     * 为指定表创建分表
     */
    private int createShardingTablesForTable(Connection connection, ShardingConfig config, String tableName) {
        int successCount = 0;
        
        try {
            // 获取原表的建表语句
            String originalTableSQL = getCreateTableSQL(connection, tableName);
            if (originalTableSQL == null) {
                System.out.println("  ⚠️ 无法获取表 '" + tableName + "' 的建表语句，跳过");
                return 0;
            }
            
            // 创建每个分表
            for (int i = 0; i < config.getTableCount(); i++) {
                String shardTableName = tableName + String.format(config.getSuffixFormat(), i);
                
                try {
                    // 构造分表的建表语句
                    String shardTableSQL = originalTableSQL.replaceFirst(
                        "CREATE TABLE[\\s]+(?:IF NOT EXISTS[\\s]+)?`?" + java.util.regex.Pattern.quote(tableName) + "`?",
                        config.getCreateTablePrefix() + " `" + shardTableName + "`"
                    );
                    
                    // 执行建表语句
                    try (var stmt = connection.prepareStatement(shardTableSQL)) {
                        stmt.execute();
                        successCount++;
                        logger.debug("创建分表成功: {}", shardTableName);
                    }
                    
                } catch (SQLException e) {
                    logger.error("创建分表失败: {}", shardTableName, e);
                    System.out.println("    ❌ 创建分表失败: " + shardTableName + " - " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("为表 {} 创建分表时发生错误", tableName, e);
            System.out.println("  ❌ 处理表 '" + tableName + "' 时发生错误: " + e.getMessage());
        }
        
        return successCount;
    }
    
    /**
     * 获取表的建表SQL语句
     */
    private String getCreateTableSQL(Connection connection, String tableName) {
        try (var stmt = connection.prepareStatement("SHOW CREATE TABLE `" + tableName + "`")) {
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(2);
            }
        } catch (SQLException e) {
            logger.error("获取表 {} 的建表语句失败", tableName, e);
            System.out.println("  ⚠️ 获取表 '" + tableName + "' 的建表语句失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 创建数据库连接
     */
    private Connection createDatabaseConnection() throws SQLException, ClassNotFoundException {
        String driverClass = PropertiesUtils.getString("spring.datasource.driver-class-name");
        String url = PropertiesUtils.getString("spring.datasource.url");
        String username = PropertiesUtils.getString("spring.datasource.username");
        String password = PropertiesUtils.getString("spring.datasource.password");
        
        if (driverClass == null || url == null || username == null) {
            throw new IllegalStateException("数据库连接配置不完整，请检查配置文件");
        }
        
        Class.forName(driverClass);
        return DriverManager.getConnection(url, username, password);
    }
    
    /**
     * 检查配置
     */
    public void checkConfiguration() {
        System.out.println("🔍 检查分表配置...");
        System.out.println();
        
        // 检查基础配置
        boolean enabled = isShardingEnabled();
        System.out.println("分表功能: " + (enabled ? "✅ 启用" : "❌ 禁用"));
        
        if (!enabled) {
            System.out.println("⚠️ 分表功能未启用，配置检查结束");
            return;
        }
        
        // 加载并验证配置
        ShardingConfig config = loadShardingConfig();
        if (config == null) {
            System.out.println("❌ 无法加载分表配置");
            return;
        }
        
        printConfigSummary(config);
        
        boolean isValid = validateConfiguration(config);
        
        // 检查数据库连接
        System.out.println("🔗 检查数据库连接...");
        try {
            Connection conn = createDatabaseConnection();
            conn.close();
            System.out.println("✅ 数据库连接正常");
        } catch (Exception e) {
            System.out.println("❌ 数据库连接失败: " + e.getMessage());
            isValid = false;
        }
        
        System.out.println();
        if (isValid) {
            System.out.println("✅ 配置检查通过，可以启动分表功能");
        } else {
            System.out.println("❌ 配置检查失败，请修复配置后重试");
        }
    }
    
    /**
     * 显示配置
     */
    public void showConfiguration() {
        System.out.println("📋 当前分表配置详情:");
        System.out.println();
        
        ShardingConfig config = loadShardingConfig();
        if (config == null) {
            System.out.println("❌ 无法加载分表配置");
            return;
        }
        
        System.out.println("=== 基础配置 ===");
        System.out.println("启用状态: " + (config.isEnabled() ? "✅ 启用" : "❌ 禁用"));
        System.out.println("分表策略: " + config.getStrategyType());
        System.out.println("分表数量: " + config.getTableCount());
        System.out.println("后缀格式: " + config.getSuffixFormat());
        System.out.println("数据库写入: " + (config.isDatabaseWriteEnabled() ? "✅ 启用" : "❌ 禁用"));
        System.out.println("自动创建: " + (config.isAutoCreateTable() ? "✅ 启用" : "❌ 禁用"));
        System.out.println();
        
        if (!config.getMappingConfig().isEmpty()) {
            System.out.println("=== 跨表字段分表映射 ===");
            config.getMappingConfig().forEach((table, mapping) -> 
                System.out.println(table + " -> " + mapping));
            System.out.println();
        }
        
        if (!config.getFieldConfig().isEmpty()) {
            System.out.println("=== 分表字段配置 ===");
            config.getFieldConfig().forEach((table, field) -> 
                System.out.println(table + " -> " + field));
            System.out.println();
        }
        
        if (config.getMappingConfig().isEmpty() && config.getFieldConfig().isEmpty()) {
            System.out.println("⚠️ 尚未配置任何分表映射或字段");
        }
    }
    
    /**
     * 测试分表功能
     */
    public void testShardingFunction() {
        System.out.println("🧪 测试分表功能...");
        System.out.println();
        
        ShardingConfig config = loadShardingConfig();
        if (config == null || !config.isEnabled()) {
            System.out.println("❌ 分表功能未启用或配置无效");
            return;
        }
        
        ShardingManager manager = new ShardingManager();
        
        // 测试分表索引计算
        System.out.println("=== 分表索引计算测试 ===");
        String[] testValues = {"1", "100", "user123", "2023-07-30"};
        
        for (String tableName : config.getFieldConfig().keySet()) {
            System.out.println("表: " + tableName);
            for (String testValue : testValues) {
                int shardIndex = manager.calculateShardIndex(tableName, testValue);
                String shardTableName = manager.getShardTableName(tableName, testValue);
                System.out.println("  值 '" + testValue + "' -> 分表索引: " + shardIndex + ", 分表名: " + shardTableName);
            }
            System.out.println();
        }
        
        for (String tableName : config.getMappingConfig().keySet()) {
            System.out.println("映射表: " + tableName + " (映射: " + config.getMappingConfig().get(tableName) + ")");
            for (String testValue : testValues) {
                int shardIndex = manager.calculateShardIndex(tableName, testValue);
                String shardTableName = manager.getShardTableName(tableName, testValue);
                System.out.println("  值 '" + testValue + "' -> 分表索引: " + shardIndex + ", 分表名: " + shardTableName);
            }
            System.out.println();
        }
        
        System.out.println("✅ 分表功能测试完成");
    }
    
    /**
     * 打印使用说明
     */
    public static void printUsage() {
        System.out.println("EasyJava 分表启动器 - 使用说明");
        System.out.println();
        System.out.println("用法:");
        System.out.println("  java -cp target/classes com.easyjava.manager.ShardingBootstrap [命令]");
        System.out.println();
        System.out.println("可用命令:");
        System.out.println("  auto       - 自动创建分表 (默认)");
        System.out.println("  check      - 检查分表配置");
        System.out.println("  show       - 显示当前配置");
        System.out.println("  test       - 测试分表功能");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java -cp target/classes com.easyjava.manager.ShardingBootstrap auto");
        System.out.println("  java -cp target/classes com.easyjava.manager.ShardingBootstrap check");
        System.out.println();
        System.out.println("配置文件: src/main/resources/application.properties");
        System.out.println("配置项说明:");
        System.out.println("  sharding.enabled=true                    # 启用分表功能");
        System.out.println("  sharding.strategy.type=hash              # 分表策略");
        System.out.println("  sharding.table.count=8                   # 分表数量");
        System.out.println("  sharding.mapping.orders=users.id         # 跨表映射");
        System.out.println("  sharding.field.users=id                  # 分表字段");
        System.out.println();
    }
}
