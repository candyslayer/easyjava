package com.easyjava.manager;

import com.easyjava.bean.ShardingConfig;
import com.easyjava.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 独立的分表管理器
 * 支持分表配置、创建分表、跨表字段分表等功能
 * 
 * @author 唐伟
 * @since 2025-07-30
 */
public class ShardingManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ShardingManager.class);
    
    private ShardingConfig shardingConfig;
    private Connection dbConnection;
    
    public ShardingManager() {
        this.shardingConfig = loadShardingConfigFromProperties();
    }
    
    public ShardingManager(Connection connection) {
        this();
        this.dbConnection = connection;
    }
    
    /**
     * 主方法 - 独立启动分表管理器
     */
    public static void main(String[] args) {
        System.out.println("=== EasyJava 分表管理器 ===");
        
        ShardingManager manager = new ShardingManager();
        
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "config":
                    manager.interactiveShardingConfiguration();
                    break;
                case "create":
                    manager.createShardingTablesFromConfig();
                    break;
                case "show":
                    manager.showCurrentConfiguration();
                    break;
                case "validate":
                    manager.validateShardingConfiguration();
                    break;
                case "help":
                default:
                    printUsage();
                    break;
            }
        } else {
            // 默认进入交互式配置
            manager.interactiveShardingConfiguration();
        }
    }
    
    /**
     * 从配置文件加载分表配置
     */
    private ShardingConfig loadShardingConfigFromProperties() {
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
    }
    
    /**
     * 交互式分表配置
     */
    public void interactiveShardingConfiguration() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("\n=== 动态分表配置向导 ===");
            System.out.println("此向导将帮助您配置EasyJava的分表功能");
            System.out.println();
            
            // 1. 分表开关配置
            configureShardingSwitch(scanner);
            
            if (!shardingConfig.isEnabled()) {
                System.out.println("分表功能已禁用，配置结束。");
                saveConfigurationToFile();
                return;
            }
            
            // 2. 分表策略配置
            configureShardingStrategy(scanner);
            
            // 3. 分表数量配置
            configureTableCount(scanner);
            
            // 4. 分表映射配置
            configureShardingMapping(scanner);
            
            // 5. 分表字段配置
            configureShardingFields(scanner);
            
            // 6. 数据库写入配置
            configureDatabaseWrite(scanner);
            
            // 7. 保存配置
            saveConfigurationToFile();
            
            // 8. 创建分表
            if (shardingConfig.isAutoCreateTable()) {
                System.out.print("\n是否立即创建分表? (y/n) [y]: ");
                String createNow = scanner.nextLine().trim();
                if (createNow.isEmpty() || "y".equalsIgnoreCase(createNow)) {
                    createShardingTablesFromConfig();
                }
            }
            
            System.out.println("\n✅ 分表配置完成！");
            showCurrentConfiguration();
            
        } catch (Exception e) {
            logger.error("分表配置过程中发生错误", e);
            System.out.println("❌ 分表配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 配置分表开关
     */
    private void configureShardingSwitch(Scanner scanner) {
        System.out.println("步骤 1/6: 分表功能开关");
        System.out.println("分表功能可以将大表按照一定规则拆分成多个小表，提高查询性能。");
        System.out.print("是否启用分表功能? (y/n) [" + (shardingConfig.isEnabled() ? "y" : "n") + "]: ");
        
        String input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            shardingConfig.setEnabled("y".equalsIgnoreCase(input) || "yes".equalsIgnoreCase(input));
        }
        
        System.out.println("✅ 分表功能: " + (shardingConfig.isEnabled() ? "启用" : "禁用"));
        System.out.println();
    }
    
    /**
     * 配置分表策略
     */
    private void configureShardingStrategy(Scanner scanner) {
        System.out.println("步骤 2/6: 分表策略");
        System.out.println("不同的分表策略适用于不同的业务场景：");
        System.out.println("1. hash - 哈希分表 (推荐，数据分布均匀)");
        System.out.println("2. mod - 取模分表 (适用于数值类型字段)");
        System.out.println("3. range - 范围分表 (适用于有序数据)");
        System.out.println("4. time - 时间分表 (适用于时间序列数据)");
        
        System.out.print("请选择分表策略 (1-4) [当前: " + shardingConfig.getStrategyType() + "]: ");
        String strategy = scanner.nextLine().trim();
        
        switch (strategy) {
            case "1":
                shardingConfig.setStrategyType("hash");
                break;
            case "2":
                shardingConfig.setStrategyType("mod");
                break;
            case "3":
                shardingConfig.setStrategyType("range");
                break;
            case "4":
                shardingConfig.setStrategyType("time");
                break;
            default:
                if (!strategy.isEmpty() && Arrays.asList("hash", "mod", "range", "time").contains(strategy.toLowerCase())) {
                    shardingConfig.setStrategyType(strategy.toLowerCase());
                }
                break;
        }
        
        System.out.println("✅ 分表策略: " + shardingConfig.getStrategyType());
        System.out.println();
    }
    
    /**
     * 配置分表数量
     */
    private void configureTableCount(Scanner scanner) {
        System.out.println("步骤 3/6: 分表数量");
        System.out.println("建议分表数量为2的幂次方 (如: 2, 4, 8, 16, 32...)");
        System.out.print("请输入分表数量 (1-1024) [" + shardingConfig.getTableCount() + "]: ");
        
        String input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            try {
                int count = Integer.parseInt(input);
                if (count > 0 && count <= 1024) {
                    shardingConfig.setTableCount(count);
                } else {
                    System.out.println("⚠️ 分表数量超出范围，使用当前值: " + shardingConfig.getTableCount());
                }
            } catch (NumberFormatException e) {
                System.out.println("⚠️ 输入格式错误，使用当前值: " + shardingConfig.getTableCount());
            }
        }
        
        System.out.println("✅ 分表数量: " + shardingConfig.getTableCount());
        System.out.println();
    }
    
    /**
     * 配置分表映射（跨表字段分表）
     */
    private void configureShardingMapping(Scanner scanner) {
        System.out.println("步骤 4/6: 跨表字段分表映射");
        System.out.println("配置通过其他表的字段来构建当前表的分表。");
        System.out.println("示例：通过用户表的id来构建信息表的分表");
        System.out.println("格式：目标表名=源表名.字段名");
        System.out.println("输入 'done' 完成配置，'skip' 跳过");
        System.out.println();
        
        while (true) {
            System.out.print("请输入分表映射配置 (或 done/skip): ");
            String input = scanner.nextLine().trim();
            
            if ("done".equalsIgnoreCase(input)) {
                break;
            } else if ("skip".equalsIgnoreCase(input)) {
                System.out.println("⏭️ 跳过跨表字段分表映射配置");
                break;
            }
            
            if (input.isEmpty()) {
                continue;
            }
            
            if (validateMappingInput(input)) {
                String[] parts = input.split("=", 2);
                String targetTable = parts[0].trim();
                String sourceMapping = parts[1].trim();
                
                shardingConfig.addMapping(targetTable, sourceMapping);
                System.out.println("✅ 添加映射: " + targetTable + " -> " + sourceMapping);
            } else {
                System.out.println("❌ 格式错误，请使用格式: 目标表名=源表名.字段名");
            }
        }
        
        if (!shardingConfig.getMappingConfig().isEmpty()) {
            System.out.println("当前跨表映射配置:");
            shardingConfig.getMappingConfig().forEach((table, mapping) -> 
                System.out.println("  " + table + " -> " + mapping));
        }
        System.out.println();
    }
    
    /**
     * 配置分表字段
     */
    private void configureShardingFields(Scanner scanner) {
        System.out.println("步骤 5/6: 分表字段配置");
        System.out.println("配置每个表用于分表的字段。");
        System.out.println("格式：表名=分表字段名");
        System.out.println("输入 'done' 完成配置，'skip' 跳过");
        System.out.println();
        
        while (true) {
            System.out.print("请输入分表字段配置 (或 done/skip): ");
            String input = scanner.nextLine().trim();
            
            if ("done".equalsIgnoreCase(input)) {
                break;
            } else if ("skip".equalsIgnoreCase(input)) {
                System.out.println("⏭️ 跳过分表字段配置");
                break;
            }
            
            if (input.isEmpty()) {
                continue;
            }
            
            if (validateFieldInput(input)) {
                String[] parts = input.split("=", 2);
                String tableName = parts[0].trim();
                String fieldName = parts[1].trim();
                
                shardingConfig.addField(tableName, fieldName);
                System.out.println("✅ 添加分表字段: " + tableName + " -> " + fieldName);
            } else {
                System.out.println("❌ 格式错误，请使用格式: 表名=字段名");
            }
        }
        
        if (!shardingConfig.getFieldConfig().isEmpty()) {
            System.out.println("当前分表字段配置:");
            shardingConfig.getFieldConfig().forEach((table, field) -> 
                System.out.println("  " + table + " -> " + field));
        }
        System.out.println();
    }
    
    /**
     * 配置数据库写入
     */
    private void configureDatabaseWrite(Scanner scanner) {
        System.out.println("步骤 6/6: 数据库写入配置");
        
        System.out.print("是否启用分表写入数据库? (y/n) [" + (shardingConfig.isDatabaseWriteEnabled() ? "y" : "n") + "]: ");
        String input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            shardingConfig.setDatabaseWriteEnabled("y".equalsIgnoreCase(input));
        }
        
        if (shardingConfig.isDatabaseWriteEnabled()) {
            System.out.print("是否自动创建分表? (y/n) [" + (shardingConfig.isAutoCreateTable() ? "y" : "n") + "]: ");
            input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                shardingConfig.setAutoCreateTable("y".equalsIgnoreCase(input));
            }
        }
        
        System.out.println("✅ 数据库写入: " + (shardingConfig.isDatabaseWriteEnabled() ? "启用" : "禁用"));
        System.out.println("✅ 自动创建分表: " + (shardingConfig.isAutoCreateTable() ? "启用" : "禁用"));
        System.out.println();
    }
    
    /**
     * 验证映射输入格式
     */
    private boolean validateMappingInput(String input) {
        return input.contains("=") && input.contains(".") && 
               input.split("=", 2).length == 2 &&
               input.split("=", 2)[1].matches("\\w+\\.\\w+");
    }
    
    /**
     * 验证字段输入格式
     */
    private boolean validateFieldInput(String input) {
        if (!input.contains("=")) return false;
        String[] parts = input.split("=", 2);
        return parts.length == 2 && !parts[0].trim().isEmpty() && !parts[1].trim().isEmpty();
    }
    
    /**
     * 保存配置到文件
     */
    private void saveConfigurationToFile() {
        try {
            Properties props = PropertiesUtils.loadProperties();
            
            // 保存基础配置
            props.setProperty("sharding.enabled", String.valueOf(shardingConfig.isEnabled()));
            props.setProperty("sharding.strategy.type", shardingConfig.getStrategyType());
            props.setProperty("sharding.table.count", String.valueOf(shardingConfig.getTableCount()));
            props.setProperty("sharding.table.suffix.format", shardingConfig.getSuffixFormat());
            
            // 保存数据库写入配置
            props.setProperty("sharding.database.write.enabled", String.valueOf(shardingConfig.isDatabaseWriteEnabled()));
            props.setProperty("sharding.sql.generate.enabled", String.valueOf(shardingConfig.isSqlGenerateEnabled()));
            props.setProperty("sharding.sql.create.prefix", shardingConfig.getCreateTablePrefix());
            props.setProperty("sharding.auto.create.table", String.valueOf(shardingConfig.isAutoCreateTable()));
            
            // 清除原有分表配置
            Set<String> keysToRemove = new HashSet<>();
            for (String key : props.stringPropertyNames()) {
                if (key.startsWith("sharding.mapping.") || key.startsWith("sharding.field.")) {
                    keysToRemove.add(key);
                }
            }
            keysToRemove.forEach(props::remove);
            
            // 保存分表映射配置
            for (Map.Entry<String, String> entry : shardingConfig.getMappingConfig().entrySet()) {
                props.setProperty("sharding.mapping." + entry.getKey(), entry.getValue());
            }
            
            // 保存分表字段配置
            for (Map.Entry<String, String> entry : shardingConfig.getFieldConfig().entrySet()) {
                props.setProperty("sharding.field." + entry.getKey(), entry.getValue());
            }
            
            // 写入配置文件
            String configPath = System.getProperty("user.dir") + "/src/main/resources/application.properties";
            try (FileOutputStream fos = new FileOutputStream(configPath);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                
                props.store(osw, "Updated sharding configuration - " + new Date());
                logger.info("分表配置已保存到配置文件: {}", configPath);
                System.out.println("✅ 分表配置已保存到配置文件");
            }
            
        } catch (Exception e) {
            logger.error("保存分表配置失败", e);
            System.out.println("❌ 保存分表配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建分表
     */
    public void createShardingTablesFromConfig() {
        if (!shardingConfig.isEnabled()) {
            System.out.println("⚠️ 分表功能未启用，无法创建分表");
            return;
        }
        
        if (!shardingConfig.isDatabaseWriteEnabled()) {
            System.out.println("⚠️ 数据库写入功能未启用，无法创建分表");
            return;
        }
        
        try {
            initializeDatabaseConnection();
            
            System.out.println("🔄 正在创建分表...");
            
            // 为每个配置的表创建分表
            Set<String> allTables = new HashSet<>();
            allTables.addAll(shardingConfig.getFieldConfig().keySet());
            allTables.addAll(shardingConfig.getMappingConfig().keySet());
            
            if (allTables.isEmpty()) {
                System.out.println("⚠️ 没有配置任何分表，请先配置分表字段或映射");
                return;
            }
            
            for (String tableName : allTables) {
                createShardingTablesForTable(tableName);
            }
            
            System.out.println("✅ 分表创建完成");
            
        } catch (Exception e) {
            logger.error("创建分表时发生错误", e);
            System.out.println("❌ 创建分表失败: " + e.getMessage());
        } finally {
            closeDatabaseConnection();
        }
    }
    
    /**
     * 为指定表创建分表
     */
    private void createShardingTablesForTable(String tableName) {
        try {
            System.out.println("📋 正在为表 " + tableName + " 创建分表...");
            
            // 获取原表结构
            String originalTableSQL = getCreateTableSQL(tableName);
            if (originalTableSQL == null) {
                System.out.println("⚠️ 无法获取表 " + tableName + " 的建表语句，跳过");
                return;
            }
            
            // 创建分表
            int successCount = 0;
            for (int i = 0; i < shardingConfig.getTableCount(); i++) {
                String shardTableName = tableName + String.format(shardingConfig.getSuffixFormat(), i);
                
                try {
                    String shardTableSQL = originalTableSQL.replaceFirst(
                        "CREATE TABLE[\\s]+(?:IF NOT EXISTS[\\s]+)?`?" + Pattern.quote(tableName) + "`?",
                        shardingConfig.getCreateTablePrefix() + " `" + shardTableName + "`"
                    );
                    
                    try (PreparedStatement stmt = dbConnection.prepareStatement(shardTableSQL)) {
                        stmt.execute();
                        successCount++;
                        System.out.println("  ✅ 创建分表: " + shardTableName);
                    }
                } catch (SQLException e) {
                    System.out.println("  ❌ 创建分表失败: " + shardTableName + " - " + e.getMessage());
                    logger.error("创建分表失败: {}", shardTableName, e);
                }
            }
            
            System.out.println("📊 表 " + tableName + " 分表创建完成: " + successCount + "/" + shardingConfig.getTableCount());
            
        } catch (Exception e) {
            logger.error("为表 {} 创建分表时发生错误", tableName, e);
            System.out.println("❌ 为表 " + tableName + " 创建分表时发生错误: " + e.getMessage());
        }
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
            logger.error("获取表 {} 的建表语句失败", tableName, e);
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
        
        Class.forName(driverClass);
        dbConnection = DriverManager.getConnection(url, username, password);
        logger.info("数据库连接初始化成功");
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
     * 显示当前配置
     */
    public void showCurrentConfiguration() {
        System.out.println("\n=== 当前分表配置 ===");
        System.out.println("分表功能: " + (shardingConfig.isEnabled() ? "✅ 启用" : "❌ 禁用"));
        
        if (shardingConfig.isEnabled()) {
            System.out.println("分表策略: " + shardingConfig.getStrategyType());
            System.out.println("分表数量: " + shardingConfig.getTableCount());
            System.out.println("后缀格式: " + shardingConfig.getSuffixFormat());
            System.out.println("数据库写入: " + (shardingConfig.isDatabaseWriteEnabled() ? "✅ 启用" : "❌ 禁用"));
            System.out.println("自动创建分表: " + (shardingConfig.isAutoCreateTable() ? "✅ 启用" : "❌ 禁用"));
            
            if (!shardingConfig.getMappingConfig().isEmpty()) {
                System.out.println("\n📋 跨表字段分表映射:");
                shardingConfig.getMappingConfig().forEach((table, mapping) -> 
                    System.out.println("  " + table + " -> " + mapping));
            }
            
            if (!shardingConfig.getFieldConfig().isEmpty()) {
                System.out.println("\n🔑 分表字段配置:");
                shardingConfig.getFieldConfig().forEach((table, field) -> 
                    System.out.println("  " + table + " -> " + field));
            }
            
            if (shardingConfig.getMappingConfig().isEmpty() && shardingConfig.getFieldConfig().isEmpty()) {
                System.out.println("\n⚠️ 尚未配置任何分表映射或字段");
            }
        }
        System.out.println("========================\n");
    }
    
    /**
     * 验证分表配置
     */
    public void validateShardingConfiguration() {
        System.out.println("\n=== 分表配置验证 ===");
        
        boolean isValid = true;
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // 验证基础配置
        if (shardingConfig.getTableCount() <= 0 || shardingConfig.getTableCount() > 1024) {
            errors.add("分表数量超出有效范围 (1-1024): " + shardingConfig.getTableCount());
            isValid = false;
        }
        
        if (!Arrays.asList("hash", "mod", "range", "time").contains(shardingConfig.getStrategyType())) {
            errors.add("无效的分表策略: " + shardingConfig.getStrategyType());
            isValid = false;
        }
        
        // 验证映射配置
        for (Map.Entry<String, String> entry : shardingConfig.getMappingConfig().entrySet()) {
            if (!entry.getValue().matches("\\w+\\.\\w+")) {
                errors.add("无效的映射配置格式: " + entry.getKey() + " -> " + entry.getValue());
                isValid = false;
            }
        }
        
        // 验证字段配置
        for (Map.Entry<String, String> entry : shardingConfig.getFieldConfig().entrySet()) {
            if (entry.getKey().isEmpty() || entry.getValue().isEmpty()) {
                errors.add("分表字段配置不能为空: " + entry.getKey() + " -> " + entry.getValue());
                isValid = false;
            }
        }
        
        // 检查是否有配置冲突
        Set<String> mappingTables = shardingConfig.getMappingConfig().keySet();
        Set<String> fieldTables = shardingConfig.getFieldConfig().keySet();
        Set<String> intersection = new HashSet<>(mappingTables);
        intersection.retainAll(fieldTables);
        
        if (!intersection.isEmpty()) {
            warnings.add("以下表同时配置了映射和字段，映射配置将优先生效: " + intersection);
        }
        
        // 输出验证结果
        if (isValid) {
            System.out.println("✅ 分表配置验证通过");
        } else {
            System.out.println("❌ 分表配置验证失败");
            errors.forEach(error -> System.out.println("  错误: " + error));
        }
        
        if (!warnings.isEmpty()) {
            System.out.println("⚠️ 警告信息:");
            warnings.forEach(warning -> System.out.println("  " + warning));
        }
        
        System.out.println("========================\n");
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
    private static void printUsage() {
        System.out.println("EasyJava 分表管理器 - 使用说明");
        System.out.println();
        System.out.println("用法:");
        System.out.println("  java -cp target/classes com.easyjava.manager.ShardingManager [命令]");
        System.out.println();
        System.out.println("可用命令:");
        System.out.println("  config     - 交互式配置分表功能");
        System.out.println("  create     - 根据配置创建分表");
        System.out.println("  show       - 显示当前分表配置");
        System.out.println("  validate   - 验证分表配置");
        System.out.println("  help       - 显示此帮助信息");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java -cp target/classes com.easyjava.manager.ShardingManager config");
        System.out.println("  java -cp target/classes com.easyjava.manager.ShardingManager create");
        System.out.println();
    }
    
    // Getters
    public ShardingConfig getShardingConfig() {
        return shardingConfig;
    }
}
