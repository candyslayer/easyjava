package com.easyjava.sharding;

import com.easyjava.bean.ShardingConfig;
import com.easyjava.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 分表执行器 - 直接读取配置文件执行分表功能
 * 无交互，纯配置驱动的分表实现
 * 
 * @author 唐伟
 * @since 2025-07-30
 */
public class ShardingExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(ShardingExecutor.class);
    
    private ShardingConfig shardingConfig;
    private Connection dbConnection;
    
    /**
     * 主方法 - 直接启动分表执行
     */
    public static void main(String[] args) {
        System.out.println("=== EasyJava 分表执行器 ===");
        System.out.println("直接读取配置文件执行分表功能...");
        System.out.println();
        
        ShardingExecutor executor = new ShardingExecutor();
        
        try {
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "execute":
                    case "run":
                        executor.executeSharding();
                        break;
                    case "validate":
                        executor.validateConfiguration();
                        break;
                    case "show":
                        executor.showConfiguration();
                        break;
                    case "test":
                        executor.testShardingCalculation();
                        break;
                    default:
                        executor.executeSharding();
                        break;
                }
            } else {
                // 默认执行分表
                executor.executeSharding();
            }
        } catch (Exception e) {
            logger.error("分表执行失败", e);
            System.err.println("❌ 分表执行失败: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * 构造函数 - 加载配置
     */
    public ShardingExecutor() {
        this.shardingConfig = loadConfigurationFromProperties();
    }
    
    /**
     * 从配置文件加载分表配置
     */
    private ShardingConfig loadConfigurationFromProperties() {
        logger.info("正在从配置文件加载分表配置...");
        
        ShardingConfig config = new ShardingConfig();
        
        try {
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
                    logger.debug("加载分表映射: {} -> {}", tableName, mapping);
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
                    logger.debug("加载分表字段: {} -> {}", tableName, fieldName);
                }
            }
            config.setFieldConfig(fieldConfig);
            
            logger.info("分表配置加载完成");
            return config;
            
        } catch (Exception e) {
            logger.error("加载分表配置时发生错误", e);
            throw new RuntimeException("分表配置加载失败", e);
        }
    }
    
    /**
     * 执行分表创建
     */
    public void executeSharding() {
        logger.info("开始执行分表创建...");
        
        // 1. 检查分表功能是否启用
        if (!shardingConfig.isEnabled()) {
            System.out.println("⚠️ 分表功能未启用 (sharding.enabled=false)");
            logger.warn("分表功能未启用，跳过分表创建");
            return;
        }
        
        // 2. 验证配置
        if (!validateConfigurationInternal()) {
            System.out.println("❌ 分表配置验证失败，请检查配置文件");
            return;
        }
        
        // 3. 检查是否有分表任务
        Set<String> allTables = getAllTablesToShard();
        if (allTables.isEmpty()) {
            System.out.println("⚠️ 没有配置任何需要分表的表");
            logger.warn("没有找到任何分表配置");
            return;
        }
        
        System.out.println("✅ 分表配置验证通过");
        System.out.println("📋 发现 " + allTables.size() + " 个表需要创建分表");
        System.out.println("🔧 分表策略: " + shardingConfig.getStrategyType());
        System.out.println("📊 每表分表数量: " + shardingConfig.getTableCount());
        System.out.println();
        
        // 4. 创建数据库连接
        if (shardingConfig.isDatabaseWriteEnabled()) {
            try {
                initializeDatabaseConnection();
                logger.info("数据库连接建立成功");
                System.out.println("✅ 数据库连接建立成功");
            } catch (Exception e) {
                System.out.println("❌ 数据库连接失败: " + e.getMessage());
                logger.error("数据库连接失败", e);
                return;
            }
        } else {
            System.out.println("⚠️ 数据库写入功能未启用，只进行配置验证");
            return;
        }
        
        // 5. 执行分表创建
        try {
            int totalCreated = 0;
            int totalFailed = 0;
            
            for (String tableName : allTables) {
                System.out.println("🔄 正在为表 '" + tableName + "' 创建分表...");
                
                int[] result = createShardingTablesForTable(tableName);
                int created = result[0];
                int failed = result[1];
                
                totalCreated += created;
                totalFailed += failed;
                
                System.out.println("  ✅ 成功: " + created + " 个, ❌ 失败: " + failed + " 个");
                logger.info("表 {} 分表创建完成: 成功 {}, 失败 {}", tableName, created, failed);
            }
            
            System.out.println();
            System.out.println("🎉 分表创建完成!");
            System.out.println("📊 统计结果:");
            System.out.println("  主表数量: " + allTables.size());
            System.out.println("  分表总数: " + (totalCreated + totalFailed));
            System.out.println("  成功创建: " + totalCreated);
            System.out.println("  创建失败: " + totalFailed);
            
            if (totalFailed > 0) {
                System.out.println("⚠️ 部分分表创建失败，请检查日志获取详细信息");
            } else {
                System.out.println("✅ 所有分表创建成功!");
            }
            
        } catch (Exception e) {
            logger.error("执行分表创建时发生错误", e);
            System.out.println("❌ 分表创建过程中发生错误: " + e.getMessage());
        } finally {
            closeDatabaseConnection();
        }
    }
    
    /**
     * 获取所有需要分表的表名
     */
    private Set<String> getAllTablesToShard() {
        Set<String> allTables = new HashSet<>();
        allTables.addAll(shardingConfig.getFieldConfig().keySet());
        allTables.addAll(shardingConfig.getMappingConfig().keySet());
        return allTables;
    }
    
    /**
     * 为指定表创建分表
     * @return [成功数量, 失败数量]
     */
    private int[] createShardingTablesForTable(String tableName) {
        int successCount = 0;
        int failCount = 0;
        
        try {
            // 获取原表的建表语句
            String originalTableSQL = getCreateTableSQL(tableName);
            if (originalTableSQL == null) {
                System.out.println("  ⚠️ 无法获取表 '" + tableName + "' 的建表语句");
                logger.warn("无法获取表 {} 的建表语句", tableName);
                return new int[]{0, shardingConfig.getTableCount()};
            }
            
            // 为每个分片创建表
            for (int i = 0; i < shardingConfig.getTableCount(); i++) {
                String shardTableName = tableName + String.format(shardingConfig.getSuffixFormat(), i);
                
                try {
                    // 构造分表的建表语句，处理各种可能的表名格式
                    String shardTableSQL = replaceTableNameInCreateSQL(originalTableSQL, tableName, shardTableName);
                    
                    // 执行建表语句
                    try (PreparedStatement stmt = dbConnection.prepareStatement(shardTableSQL)) {
                        stmt.execute();
                        successCount++;
                        logger.debug("成功创建分表: {}", shardTableName);
                    }
                    
                } catch (SQLException e) {
                    failCount++;
                    logger.error("创建分表失败: {} - {}", shardTableName, e.getMessage());
                    
                    // 如果是表已存在的错误，不算作失败
                    if (e.getMessage().toLowerCase().contains("already exists")) {
                        System.out.println("    ℹ️ 分表 '" + shardTableName + "' 已存在，跳过");
                        failCount--; // 撤销失败计数
                    } else {
                        System.out.println("    ❌ 创建分表 '" + shardTableName + "' 失败: " + e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("为表 {} 创建分表时发生错误", tableName, e);
            failCount = shardingConfig.getTableCount();
        }
        
        return new int[]{successCount, failCount};
    }
    
    /**
     * 替换建表语句中的表名
     */
    private String replaceTableNameInCreateSQL(String originalSQL, String originalTableName, String newTableName) {
        // 处理各种可能的表名格式: table_name, `table_name`, "table_name"
        String[] patterns = {
            "CREATE TABLE[\\s]+IF[\\s]+NOT[\\s]+EXISTS[\\s]+`?" + Pattern.quote(originalTableName) + "`?",
            "CREATE TABLE[\\s]+`?" + Pattern.quote(originalTableName) + "`?"
        };
        
        String result = originalSQL;
        for (String pattern : patterns) {
            result = result.replaceFirst(pattern, 
                shardingConfig.getCreateTablePrefix() + " `" + newTableName + "`");
            if (!result.equals(originalSQL)) {
                break; // 如果替换成功，就不需要尝试其他模式
            }
        }
        
        return result;
    }
    
    /**
     * 获取表的建表SQL语句
     */
    private String getCreateTableSQL(String tableName) {
        try (PreparedStatement stmt = dbConnection.prepareStatement("SHOW CREATE TABLE `" + tableName + "`")) {
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(2);
            }
        } catch (SQLException e) {
            logger.error("获取表 {} 的建表语句失败: {}", tableName, e.getMessage());
        }
        return null;
    }
    
    /**
     * 初始化数据库连接
     */
    private void initializeDatabaseConnection() throws SQLException, ClassNotFoundException {
        if (dbConnection != null && !dbConnection.isClosed()) {
            return;
        }
        
        String driverClass = PropertiesUtils.getString("spring.datasource.driver-class-name");
        String url = PropertiesUtils.getString("spring.datasource.url");
        String username = PropertiesUtils.getString("spring.datasource.username");
        String password = PropertiesUtils.getString("spring.datasource.password");
        
        if (driverClass == null || url == null || username == null) {
            throw new IllegalStateException("数据库连接配置不完整，请检查配置文件");
        }
        
        Class.forName(driverClass);
        dbConnection = DriverManager.getConnection(url, username, password);
        logger.info("数据库连接建立成功: {}", url.replaceAll("\\?.*", "")); // 隐藏URL参数
    }
    
    /**
     * 关闭数据库连接
     */
    private void closeDatabaseConnection() {
        if (dbConnection != null) {
            try {
                dbConnection.close();
                logger.info("数据库连接已关闭");
            } catch (SQLException e) {
                logger.error("关闭数据库连接失败", e);
            }
        }
    }
    
    /**
     * 验证配置（内部方法）
     */
    private boolean validateConfigurationInternal() {
        List<String> errors = new ArrayList<>();
        
        // 验证基础配置
        if (shardingConfig.getTableCount() <= 0 || shardingConfig.getTableCount() > 1024) {
            errors.add("分表数量超出有效范围 (1-1024): " + shardingConfig.getTableCount());
        }
        
        if (!Arrays.asList("hash", "mod", "range", "time").contains(shardingConfig.getStrategyType())) {
            errors.add("无效的分表策略: " + shardingConfig.getStrategyType());
        }
        
        // 验证是否配置了分表表格
        if (shardingConfig.getMappingConfig().isEmpty() && shardingConfig.getFieldConfig().isEmpty()) {
            errors.add("没有配置任何分表映射或字段");
        }
        
        // 验证映射配置格式
        for (Map.Entry<String, String> entry : shardingConfig.getMappingConfig().entrySet()) {
            if (!entry.getValue().matches("\\w+\\.\\w+")) {
                errors.add("无效的映射配置格式: " + entry.getKey() + " -> " + entry.getValue());
            }
        }
        
        // 验证字段配置
        for (Map.Entry<String, String> entry : shardingConfig.getFieldConfig().entrySet()) {
            if (entry.getKey().isEmpty() || entry.getValue().isEmpty()) {
                errors.add("分表字段配置不能为空: " + entry.getKey() + " -> " + entry.getValue());
            }
        }
        
        if (!errors.isEmpty()) {
            logger.error("分表配置验证失败，错误信息: {}", errors);
            for (String error : errors) {
                System.out.println("  ❌ " + error);
            }
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证配置（公共方法）
     */
    public void validateConfiguration() {
        System.out.println("🔍 验证分表配置...");
        System.out.println();
        
        System.out.println("=== 基础配置检查 ===");
        System.out.println("分表功能: " + (shardingConfig.isEnabled() ? "✅ 启用" : "❌ 禁用"));
        System.out.println("分表策略: " + shardingConfig.getStrategyType());
        System.out.println("分表数量: " + shardingConfig.getTableCount());
        System.out.println("后缀格式: " + shardingConfig.getSuffixFormat());
        System.out.println("数据库写入: " + (shardingConfig.isDatabaseWriteEnabled() ? "✅ 启用" : "❌ 禁用"));
        System.out.println();
        
        boolean isValid = validateConfigurationInternal();
        
        // 检查数据库连接
        if (shardingConfig.isDatabaseWriteEnabled()) {
            System.out.println("=== 数据库连接检查 ===");
            try {
                initializeDatabaseConnection();
                System.out.println("✅ 数据库连接正常");
                closeDatabaseConnection();
            } catch (Exception e) {
                System.out.println("❌ 数据库连接失败: " + e.getMessage());
                isValid = false;
            }
            System.out.println();
        }
        
        System.out.println("=== 验证结果 ===");
        if (isValid) {
            System.out.println("✅ 所有配置验证通过，可以执行分表创建");
        } else {
            System.out.println("❌ 配置验证失败，请修复配置后重试");
        }
    }
    
    /**
     * 显示当前配置
     */
    public void showConfiguration() {
        System.out.println("📋 当前分表配置:");
        System.out.println();
        
        System.out.println("=== 基础配置 ===");
        System.out.println("启用状态: " + (shardingConfig.isEnabled() ? "✅ 启用" : "❌ 禁用"));
        System.out.println("分表策略: " + shardingConfig.getStrategyType());
        System.out.println("分表数量: " + shardingConfig.getTableCount());
        System.out.println("后缀格式: " + shardingConfig.getSuffixFormat());
        System.out.println("数据库写入: " + (shardingConfig.isDatabaseWriteEnabled() ? "✅ 启用" : "❌ 禁用"));
        System.out.println("SQL生成: " + (shardingConfig.isSqlGenerateEnabled() ? "✅ 启用" : "❌ 禁用"));
        System.out.println("自动创建: " + (shardingConfig.isAutoCreateTable() ? "✅ 启用" : "❌ 禁用"));
        System.out.println("建表前缀: " + shardingConfig.getCreateTablePrefix());
        System.out.println();
        
        if (!shardingConfig.getMappingConfig().isEmpty()) {
            System.out.println("=== 跨表字段分表映射 ===");
            shardingConfig.getMappingConfig().forEach((table, mapping) -> 
                System.out.println(table + " -> " + mapping));
            System.out.println();
        }
        
        if (!shardingConfig.getFieldConfig().isEmpty()) {
            System.out.println("=== 分表字段配置 ===");
            shardingConfig.getFieldConfig().forEach((table, field) -> 
                System.out.println(table + " -> " + field));
            System.out.println();
        }
        
        if (shardingConfig.getMappingConfig().isEmpty() && shardingConfig.getFieldConfig().isEmpty()) {
            System.out.println("⚠️ 尚未配置任何分表映射或字段");
            System.out.println();
        }
        
        // 统计信息
        Set<String> allTables = getAllTablesToShard();
        System.out.println("=== 统计信息 ===");
        System.out.println("需要分表的主表数量: " + allTables.size());
        System.out.println("预计创建的分表总数: " + (allTables.size() * shardingConfig.getTableCount()));
        
        if (!allTables.isEmpty()) {
            System.out.println("主表列表: " + String.join(", ", allTables));
        }
    }
    
    /**
     * 测试分表索引计算
     */
    public void testShardingCalculation() {
        System.out.println("🧪 测试分表索引计算...");
        System.out.println();
        
        if (!shardingConfig.isEnabled()) {
            System.out.println("❌ 分表功能未启用");
            return;
        }
        
        Set<String> allTables = getAllTablesToShard();
        if (allTables.isEmpty()) {
            System.out.println("❌ 没有配置任何分表");
            return;
        }
        
        String[] testValues = {"1", "100", "1000", "user123", "admin", "test_data", "2023-07-30"};
        
        System.out.println("=== 分表索引计算测试 ===");
        System.out.println("分表策略: " + shardingConfig.getStrategyType());
        System.out.println("分表数量: " + shardingConfig.getTableCount());
        System.out.println();
        
        for (String tableName : allTables) {
            System.out.println("表: " + tableName);
            
            String fieldOrMapping = shardingConfig.getFieldConfig().get(tableName);
            if (fieldOrMapping != null) {
                System.out.println("  分表字段: " + fieldOrMapping);
            } else {
                String mapping = shardingConfig.getMappingConfig().get(tableName);
                if (mapping != null) {
                    System.out.println("  分表映射: " + mapping);
                }
            }
            
            for (String testValue : testValues) {
                int shardIndex = calculateShardIndex(tableName, testValue);
                String shardTableName = getShardTableName(tableName, testValue);
                System.out.println("    值 '" + testValue + "' -> 索引: " + shardIndex + ", 分表: " + shardTableName);
            }
            System.out.println();
        }
        
        System.out.println("✅ 分表索引计算测试完成");
    }
    
    /**
     * 计算分表索引
     */
    public int calculateShardIndex(String tableName, Object shardValue) {
        if (!shardingConfig.needSharding(tableName)) {
            return 0;
        }
        
        switch (shardingConfig.getStrategyType().toLowerCase()) {
            case "hash":
                return Math.abs(shardValue.hashCode()) % shardingConfig.getTableCount();
            case "mod":
                if (shardValue instanceof Number) {
                    return ((Number) shardValue).intValue() % shardingConfig.getTableCount();
                }
                return Math.abs(shardValue.hashCode()) % shardingConfig.getTableCount();
            case "range":
                if (shardValue instanceof Number) {
                    int value = ((Number) shardValue).intValue();
                    return Math.min(value / 1000, shardingConfig.getTableCount() - 1);
                }
                return 0;
            case "time":
                return (int) (System.currentTimeMillis() / (24 * 60 * 60 * 1000L)) % shardingConfig.getTableCount();
            default:
                return Math.abs(shardValue.hashCode()) % shardingConfig.getTableCount();
        }
    }
    
    /**
     * 获取分表名称
     */
    public String getShardTableName(String originalTableName, Object shardValue) {
        if (!shardingConfig.needSharding(originalTableName)) {
            return originalTableName;
        }
        
        int shardIndex = calculateShardIndex(originalTableName, shardValue);
        return originalTableName + String.format(shardingConfig.getSuffixFormat(), shardIndex);
    }
    
    /**
     * 打印使用说明
     */
    public static void printUsage() {
        System.out.println("EasyJava 分表执行器 - 使用说明");
        System.out.println("直接读取配置文件执行分表功能，无需交互");
        System.out.println();
        System.out.println("用法:");
        System.out.println("  java -cp target/classes com.easyjava.sharding.ShardingExecutor [命令]");
        System.out.println();
        System.out.println("可用命令:");
        System.out.println("  execute    - 执行分表创建 (默认)");
        System.out.println("  run        - 执行分表创建 (同 execute)");
        System.out.println("  validate   - 验证分表配置");
        System.out.println("  show       - 显示当前配置");
        System.out.println("  test       - 测试分表索引计算");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java -cp target/classes com.easyjava.sharding.ShardingExecutor");
        System.out.println("  java -cp target/classes com.easyjava.sharding.ShardingExecutor execute");
        System.out.println("  java -cp target/classes com.easyjava.sharding.ShardingExecutor validate");
        System.out.println();
        System.out.println("配置文件: src/main/resources/application.properties");
        System.out.println("详细文档: docs/分表功能说明文档.md");
    }
    
    // Getter方法
    public ShardingConfig getShardingConfig() {
        return shardingConfig;
    }
}
