package com.easyjava.builder;

import com.easyjava.bean.ShardingConfig;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 动态分表管理器
 * 支持多种分表策略，跨表字段分表，数据库写入等功能
 * 
 * @author 唐伟
 * @since 2025-07-30
 */
public class DynamicShardingManager {
    
    private static final Logger log = LoggerFactory.getLogger(DynamicShardingManager.class);
    
    private ShardingConfig shardingConfig;
    private Connection dbConnection;
    
    public DynamicShardingManager() {
        this.shardingConfig = loadShardingConfig();
        log.info("分表配置加载完成: {}", shardingConfig);
    }
    
    public DynamicShardingManager(Connection dbConnection) {
        this();
        this.dbConnection = dbConnection;
    }
    
    /**
     * 从配置文件加载分表配置
     */
    private ShardingConfig loadShardingConfig() {
        ShardingConfig config = new ShardingConfig();
        
        // 基础配置
        config.setEnabled(PropertiesUtils.getBoolean("sharding.enabled"));
        config.setStrategyType(PropertiesUtils.getString("sharding.strategy.type"));
        config.setTableCount(PropertiesUtils.getInt("sharding.table.count"));
        config.setSuffixFormat(PropertiesUtils.getString("sharding.table.suffix.format"));
        
        // 数据库写入配置
        config.setDatabaseWriteEnabled(PropertiesUtils.getBoolean("sharding.database.write.enabled"));
        config.setSqlGenerateEnabled(PropertiesUtils.getBoolean("sharding.sql.generate.enabled"));
        config.setCreateTablePrefix(PropertiesUtils.getString("sharding.sql.create.prefix"));
        config.setAutoCreateTable(PropertiesUtils.getBoolean("sharding.auto.create.table"));
        
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
     * 交互式配置分表
     */
    public void interactiveShardingConfiguration() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("\n=== 动态分表配置 ===");
            
            // 分表开关配置
            configureShardingSwitch(scanner);
            
            if (shardingConfig.isEnabled()) {
                // 分表策略配置
                configureShardingStrategy(scanner);
                
                // 分表数量配置
                configureTableCount(scanner);
                
                // 分表映射配置
                configureShardingMapping(scanner);
                
                // 分表字段配置
                configureShardingFields(scanner);
                
                // 数据库写入配置
                configureDatabaseWrite(scanner);
                
                // 保存配置
                saveShardingConfiguration();
                
                // 如果启用自动创建分表，执行创建
                if (shardingConfig.isAutoCreateTable() && dbConnection != null) {
                    createShardingTables();
                }
            }
            
            System.out.println("分表配置完成！");
        } catch (Exception e) {
            log.error("分表配置过程中发生错误", e);
        }
    }
    
    /**
     * 配置分表开关
     */
    private void configureShardingSwitch(Scanner scanner) {
        System.out.print("是否启用分表功能? (y/n) [" + (shardingConfig.isEnabled() ? "y" : "n") + "]: ");
        String input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            shardingConfig.setEnabled("y".equalsIgnoreCase(input) || "yes".equalsIgnoreCase(input));
        }
        
        System.out.println("分表功能: " + (shardingConfig.isEnabled() ? "启用" : "禁用"));
    }
    
    /**
     * 配置分表策略
     */
    private void configureShardingStrategy(Scanner scanner) {
        System.out.println("\n可选分表策略:");
        System.out.println("1. hash - 哈希分表");
        System.out.println("2. mod - 取模分表");
        System.out.println("3. range - 范围分表");
        System.out.println("4. time - 时间分表");
        
        System.out.print("请选择分表策略 [" + shardingConfig.getStrategyType() + "]: ");
        String strategy = scanner.nextLine().trim();
        if (!strategy.isEmpty()) {
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
                    if (Arrays.asList("hash", "mod", "range", "time").contains(strategy.toLowerCase())) {
                        shardingConfig.setStrategyType(strategy.toLowerCase());
                    }
                    break;
            }
        }
        
        System.out.println("分表策略: " + shardingConfig.getStrategyType());
    }
    
    /**
     * 配置分表数量
     */
    private void configureTableCount(Scanner scanner) {
        System.out.print("请输入分表数量 [" + shardingConfig.getTableCount() + "]: ");
        String input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            try {
                int count = Integer.parseInt(input);
                if (count > 0 && count <= 1024) {
                    shardingConfig.setTableCount(count);
                } else {
                    System.out.println("分表数量必须在1-1024之间，使用默认值: " + shardingConfig.getTableCount());
                }
            } catch (NumberFormatException e) {
                System.out.println("输入格式错误，使用默认值: " + shardingConfig.getTableCount());
            }
        }
        
        System.out.println("分表数量: " + shardingConfig.getTableCount());
    }
    
    /**
     * 配置分表映射（跨表字段分表）
     */
    private void configureShardingMapping(Scanner scanner) {
        System.out.println("\n=== 配置跨表字段分表映射 ===");
        System.out.println("格式: 目标表名=源表名.字段名");
        System.out.println("示例: info_table=user_table.id");
        System.out.println("输入 'done' 完成配置");
        
        while (true) {
            System.out.print("请输入分表映射配置: ");
            String input = scanner.nextLine().trim();
            
            if ("done".equalsIgnoreCase(input)) {
                break;
            }
            
            if (input.isEmpty()) {
                continue;
            }
            
            if (input.contains("=") && input.contains(".")) {
                String[] parts = input.split("=", 2);
                if (parts.length == 2) {
                    String targetTable = parts[0].trim();
                    String sourceMapping = parts[1].trim();
                    
                    if (Pattern.matches("\\w+\\.\\w+", sourceMapping)) {
                        shardingConfig.addMapping(targetTable, sourceMapping);
                        System.out.println("添加映射: " + targetTable + " -> " + sourceMapping);
                    } else {
                        System.out.println("格式错误，正确格式: 表名.字段名");
                    }
                } else {
                    System.out.println("格式错误，正确格式: 目标表名=源表名.字段名");
                }
            } else {
                System.out.println("格式错误，正确格式: 目标表名=源表名.字段名");
            }
        }
        
        System.out.println("当前分表映射: " + shardingConfig.getMappingConfig());
    }
    
    /**
     * 配置分表字段
     */
    private void configureShardingFields(Scanner scanner) {
        System.out.println("\n=== 配置分表字段 ===");
        System.out.println("格式: 表名=分表字段名");
        System.out.println("示例: user_table=id");
        System.out.println("输入 'done' 完成配置");
        
        while (true) {
            System.out.print("请输入分表字段配置: ");
            String input = scanner.nextLine().trim();
            
            if ("done".equalsIgnoreCase(input)) {
                break;
            }
            
            if (input.isEmpty()) {
                continue;
            }
            
            if (input.contains("=")) {
                String[] parts = input.split("=", 2);
                if (parts.length == 2) {
                    String tableName = parts[0].trim();
                    String fieldName = parts[1].trim();
                    
                    if (!tableName.isEmpty() && !fieldName.isEmpty()) {
                        shardingConfig.addField(tableName, fieldName);
                        System.out.println("添加分表字段: " + tableName + " -> " + fieldName);
                    } else {
                        System.out.println("表名和字段名不能为空");
                    }
                } else {
                    System.out.println("格式错误，正确格式: 表名=字段名");
                }
            } else {
                System.out.println("格式错误，正确格式: 表名=字段名");
            }
        }
        
        System.out.println("当前分表字段: " + shardingConfig.getFieldConfig());
    }
    
    /**
     * 配置数据库写入
     */
    private void configureDatabaseWrite(Scanner scanner) {
        System.out.print("是否启用分表写入数据库? (y/n) [" + (shardingConfig.isDatabaseWriteEnabled() ? "y" : "n") + "]: ");
        String input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            shardingConfig.setDatabaseWriteEnabled("y".equalsIgnoreCase(input) || "yes".equalsIgnoreCase(input));
        }
        
        if (shardingConfig.isDatabaseWriteEnabled()) {
            System.out.print("是否自动创建分表? (y/n) [" + (shardingConfig.isAutoCreateTable() ? "y" : "n") + "]: ");
            input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                shardingConfig.setAutoCreateTable("y".equalsIgnoreCase(input) || "yes".equalsIgnoreCase(input));
            }
        }
        
        System.out.println("数据库写入: " + (shardingConfig.isDatabaseWriteEnabled() ? "启用" : "禁用"));
        System.out.println("自动创建分表: " + (shardingConfig.isAutoCreateTable() ? "启用" : "禁用"));
    }
    
    /**
     * 保存分表配置到配置文件
     */
    private void saveShardingConfiguration() {
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
            props.stringPropertyNames().removeIf(key -> key.startsWith("sharding.mapping.") || key.startsWith("sharding.field."));
            
            // 保存分表映射配置
            for (Map.Entry<String, String> entry : shardingConfig.getMappingConfig().entrySet()) {
                props.setProperty("sharding.mapping." + entry.getKey(), entry.getValue());
            }
            
            // 保存分表字段配置
            for (Map.Entry<String, String> entry : shardingConfig.getFieldConfig().entrySet()) {
                props.setProperty("sharding.field." + entry.getKey(), entry.getValue());
            }
            
            PropertiesUtils.saveProperties(props);
            log.info("分表配置已保存到配置文件");
            
        } catch (Exception e) {
            log.error("保存分表配置失败", e);
        }
    }
    
    /**
     * 根据分表配置创建分表
     */
    public void createShardingTables() {
        if (!shardingConfig.isEnabled() || dbConnection == null) {
            return;
        }
        
        log.info("开始创建分表...");
        
        for (String tableName : shardingConfig.getFieldConfig().keySet()) {
            createShardingTablesForTable(tableName);
        }
        
        for (String tableName : shardingConfig.getMappingConfig().keySet()) {
            createShardingTablesForTable(tableName);
        }
        
        log.info("分表创建完成");
    }
    
    /**
     * 为指定表创建分表
     */
    private void createShardingTablesForTable(String tableName) {
        try {
            // 获取原表结构
            String originalTableSQL = getCreateTableSQL(tableName);
            if (originalTableSQL == null) {
                log.warn("无法获取表 {} 的建表语句", tableName);
                return;
            }
            
            // 创建分表
            for (int i = 0; i < shardingConfig.getTableCount(); i++) {
                String shardTableName = tableName + String.format(shardingConfig.getSuffixFormat(), i);
                String shardTableSQL = originalTableSQL.replaceFirst(
                    "CREATE TABLE[\\s]+(?:IF NOT EXISTS[\\s]+)?" + Pattern.quote(tableName),
                    shardingConfig.getCreateTablePrefix() + " " + shardTableName
                );
                
                try (PreparedStatement stmt = dbConnection.prepareStatement(shardTableSQL)) {
                    stmt.execute();
                    log.info("创建分表成功: {}", shardTableName);
                } catch (SQLException e) {
                    log.error("创建分表失败: {}", shardTableName, e);
                }
            }
            
        } catch (Exception e) {
            log.error("为表 {} 创建分表时发生错误", tableName, e);
        }
    }
    
    /**
     * 获取表的建表SQL语句
     */
    private String getCreateTableSQL(String tableName) {
        try (PreparedStatement stmt = dbConnection.prepareStatement("SHOW CREATE TABLE " + tableName)) {
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(2);
            }
        } catch (SQLException e) {
            log.error("获取表 {} 的建表语句失败", tableName, e);
        }
        return null;
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
                // 范围分表逻辑，此处简化处理
                if (shardValue instanceof Number) {
                    int value = ((Number) shardValue).intValue();
                    return Math.min(value / 1000, shardingConfig.getTableCount() - 1);
                }
                return 0;
            case "time":
                // 时间分表逻辑，此处简化处理
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
     * 显示当前分表配置
     */
    public void showShardingConfiguration() {
        System.out.println("\n=== 当前分表配置 ===");
        System.out.println("分表功能: " + (shardingConfig.isEnabled() ? "启用" : "禁用"));
        
        if (shardingConfig.isEnabled()) {
            System.out.println("分表策略: " + shardingConfig.getStrategyType());
            System.out.println("分表数量: " + shardingConfig.getTableCount());
            System.out.println("后缀格式: " + shardingConfig.getSuffixFormat());
            System.out.println("数据库写入: " + (shardingConfig.isDatabaseWriteEnabled() ? "启用" : "禁用"));
            System.out.println("自动创建分表: " + (shardingConfig.isAutoCreateTable() ? "启用" : "禁用"));
            
            if (!shardingConfig.getMappingConfig().isEmpty()) {
                System.out.println("\n跨表字段分表映射:");
                shardingConfig.getMappingConfig().forEach((table, mapping) -> 
                    System.out.println("  " + table + " -> " + mapping));
            }
            
            if (!shardingConfig.getFieldConfig().isEmpty()) {
                System.out.println("\n分表字段配置:");
                shardingConfig.getFieldConfig().forEach((table, field) -> 
                    System.out.println("  " + table + " -> " + field));
            }
        }
        System.out.println("========================\n");
    }
    
    // Getters
    public ShardingConfig getShardingConfig() {
        return shardingConfig;
    }
    
    public void setDbConnection(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }
}
