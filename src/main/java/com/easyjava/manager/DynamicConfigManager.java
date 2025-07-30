package com.easyjava.manager;

import com.easyjava.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 动态配置管理器 - 支持用户自定义数据库连接、包名、输出路径等配置
 * 
 * @author EasyJava
 * @since 2025-07-30
 */
public class DynamicConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(DynamicConfigManager.class);

    // 运行时配置存储
    private static final Map<String, String> runtimeConfig = new HashMap<>();

    // 配置项验证规则
    private static final Map<String, Pattern> configValidators = new HashMap<>();

    // 默认配置值
    private static final Map<String, String> defaultValues = new HashMap<>();

    static {
        initializeValidators();
        initializeDefaultValues();
        initializeShardingValidators();
        initializeShardingDefaults();
    }

    /**
     * 初始化配置验证规则
     */
    private static void initializeValidators() {
        // 数据库URL验证
        configValidators.put("spring.datasource.url",
                Pattern.compile("^jdbc:(mysql|postgresql|sqlserver|oracle)://[\\w\\.-]+:\\d+/\\w+.*$"));

        // 包名验证
        configValidators.put("package.base",
                Pattern.compile("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$"));

        // 路径验证（Windows和Linux）
        configValidators.put("path.base",
                Pattern.compile("^([a-zA-Z]:[/\\\\]|/)[^<>:\"|?*]*$"));

        // 用户名验证
        configValidators.put("spring.datasource.username",
                Pattern.compile("^[a-zA-Z0-9_-]{1,64}$"));
    }

    /**
     * 初始化默认配置值
     */
    private static void initializeDefaultValues() {
        defaultValues.put("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");
        defaultValues.put("spring.datasource.url", "jdbc:mysql://127.0.0.1:3306/your_database");
        defaultValues.put("spring.datasource.username", "root");
        defaultValues.put("spring.datasource.password", "password");

        defaultValues.put("package.base", "com.example.project");
        defaultValues.put("package.po", "entity.po");
        defaultValues.put("package.query", "entity.query");
        defaultValues.put("package.service", "service");
        defaultValues.put("package.service.impl", "service.impl");
        defaultValues.put("package.controller", "controller");
        defaultValues.put("package.mapper", "mapper");

        defaultValues.put("path.base", System.getProperty("user.dir") + File.separator + "generated");
        defaultValues.put("path.java", "${path.base}/src/main/java");
        defaultValues.put("path.resources", "${path.base}/src/main/resources");
        defaultValues.put("path.mappers.xml", "${path.resources}/mapper");

        defaultValues.put("auther.comment", "EasyJava");
        defaultValues.put("ignore.table.prefix", "true");
    }

    /**
     * 初始化分表配置验证规则
     */
    private static void initializeShardingValidators() {
        // 分表数量验证（1-1024）
        configValidators.put("sharding.table.count",
                Pattern.compile("^(?:[1-9]|[1-9][0-9]|[1-9][0-9]{2}|10(?:[0-1][0-9]|2[0-4]))$"));
        
        // 分表策略类型验证
        configValidators.put("sharding.strategy.type",
                Pattern.compile("^(hash|mod|range|time)$"));
        
        // 分表后缀格式验证
        configValidators.put("sharding.table.suffix.format",
                Pattern.compile("^[_-]%[sd]$"));
    }

    /**
     * 初始化分表默认配置值
     */
    private static void initializeShardingDefaults() {
        defaultValues.put("sharding.enabled", "false");
        defaultValues.put("sharding.strategy.type", "hash");
        defaultValues.put("sharding.table.count", "8");
        defaultValues.put("sharding.table.suffix.format", "_%d");
        defaultValues.put("sharding.database.write.enabled", "true");
        defaultValues.put("sharding.sql.generate.enabled", "true");
        defaultValues.put("sharding.sql.create.prefix", "CREATE TABLE IF NOT EXISTS");
        defaultValues.put("sharding.auto.create.table", "true");
    }

    /**
     * 交互式配置数据库连接
     */
    public static void configureDatabase() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("\n=== 数据库配置 ===");

            // 选择数据库类型
            System.out.println("支持的数据库类型:");
            System.out.println("1. MySQL");
            System.out.println("2. PostgreSQL");
            System.out.println("3. SQL Server");
            System.out.println("4. Oracle");
            System.out.println("5. 自定义");

            System.out.print("请选择数据库类型 (1-5): ");
            String dbChoice = scanner.nextLine().trim();

            String driverClass = "";
            String urlTemplate = "";
            int defaultPort = 3306;

            switch (dbChoice) {
                case "1":
                    driverClass = "com.mysql.cj.jdbc.Driver";
                    urlTemplate = "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=GMT%%2B8";
                    defaultPort = 3306;
                    break;
                case "2":
                    driverClass = "org.postgresql.Driver";
                    urlTemplate = "jdbc:postgresql://%s:%d/%s";
                    defaultPort = 5432;
                    break;
                case "3":
                    driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                    urlTemplate = "jdbc:sqlserver://%s:%d;databaseName=%s";
                    defaultPort = 1433;
                    break;
                case "4":
                    driverClass = "oracle.jdbc.OracleDriver";
                    urlTemplate = "jdbc:oracle:thin:@%s:%d:%s";
                    defaultPort = 1521;
                    break;
                case "5":
                    System.out.print("请输入驱动类名: ");
                    driverClass = scanner.nextLine().trim();
                    System.out.print("请输入完整的JDBC URL: ");
                    String customUrl = scanner.nextLine().trim();
                    setRuntimeConfig("spring.datasource.driver-class-name", driverClass);
                    setRuntimeConfig("spring.datasource.url", customUrl);

                    System.out.print("数据库用户名: ");
                    String username = scanner.nextLine().trim();
                    setRuntimeConfig("spring.datasource.username", username);

                    System.out.print("数据库密码: ");
                    String password = scanner.nextLine().trim();
                    setRuntimeConfig("spring.datasource.password", password);
                    return;
                default:
                    System.out.println("无效选择，将使用MySQL默认配置");
                    driverClass = "com.mysql.cj.jdbc.Driver";
                    urlTemplate = "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=GMT%%2B8";
                    defaultPort = 3306;
            }

            // 配置连接信息
            System.out.print("数据库主机地址 [127.0.0.1]: ");
            String host = scanner.nextLine().trim();
            if (host.isEmpty())
                host = "127.0.0.1";

            System.out.print("数据库端口 [" + defaultPort + "]: ");
            String portStr = scanner.nextLine().trim();
            int port = portStr.isEmpty() ? defaultPort : Integer.parseInt(portStr);

            System.out.print("数据库名称: ");
            String database = scanner.nextLine().trim();

            System.out.print("数据库用户名: ");
            String username = scanner.nextLine().trim();

            System.out.print("数据库密码: ");
            String password = scanner.nextLine().trim();

            // 构建连接URL
            String url = String.format(urlTemplate, host, port, database);

            // 保存配置
            setRuntimeConfig("spring.datasource.driver-class-name", driverClass);
            setRuntimeConfig("spring.datasource.url", url);
            setRuntimeConfig("spring.datasource.username", username);
            setRuntimeConfig("spring.datasource.password", password);

            System.out.println("✅ 数据库配置完成");
            System.out.println("连接URL: " + url);
        }
    }

    /**
     * 交互式配置包名
     */
    public static void configurePackages() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("\n=== 包名配置 ===");

            System.out.print("基础包名 [" + defaultValues.get("package.base") + "]: ");
            String basePackage = scanner.nextLine().trim();
            if (basePackage.isEmpty()) {
                basePackage = defaultValues.get("package.base");
            }

            // 验证包名格式
            if (!isValidConfig("package.base", basePackage)) {
                System.out.println("❌ 包名格式不正确，请使用小写字母和点号分隔，如: com.example.project");
                return;
            }

            setRuntimeConfig("package.base", basePackage);

            // 配置子包
            System.out.println("\n配置子包名（按Enter使用默认值）:");

            configureSubPackage(scanner, "package.po", "实体类包", "entity.po");
            configureSubPackage(scanner, "package.query", "查询参数包", "entity.query");
            configureSubPackage(scanner, "package.service", "Service包", "service");
            configureSubPackage(scanner, "package.service.impl", "Service实现包", "service.impl");
            configureSubPackage(scanner, "package.controller", "Controller包", "controller");
            configureSubPackage(scanner, "package.mapper", "Mapper包", "mapper");

            System.out.println("✅ 包名配置完成");
        }
    }

    /**
     * 配置子包
     */
    private static void configureSubPackage(Scanner scanner, String key, String description, String defaultValue) {
        System.out.print(description + " [" + defaultValue + "]: ");
        String value = scanner.nextLine().trim();
        if (value.isEmpty()) {
            value = defaultValue;
        }
        setRuntimeConfig(key, value);
    }

    /**
     * 交互式配置输出路径
     */
    public static void configureOutputPaths() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("\n=== 输出路径配置 ===");

            System.out.print("项目根路径 [" + defaultValues.get("path.base") + "]: ");
            String basePath = scanner.nextLine().trim();
            if (basePath.isEmpty()) {
                basePath = defaultValues.get("path.base");
            }

            // 验证路径格式
            if (!isValidConfig("path.base", basePath)) {
                System.out.println("❌ 路径格式不正确");
                return;
            }

            setRuntimeConfig("path.base", basePath);

            // 自动生成其他路径
            setRuntimeConfig("path.java", basePath + "/src/main/java");
            setRuntimeConfig("path.resources", basePath + "/src/main/resources");
            setRuntimeConfig("path.mappers.xml", basePath + "/src/main/resources/mapper");
            setRuntimeConfig("path.test", basePath + "/src/test/java");

            // 询问是否自定义各个路径
            System.out.print("是否自定义各个输出路径？(y/n) [n]: ");
            String customize = scanner.nextLine().trim();

            if ("y".equalsIgnoreCase(customize)) {
                configureSpecificPaths(scanner, basePath);
            }

            System.out.println("✅ 输出路径配置完成");
            System.out.println("Java源码路径: " + getRuntimeConfig("path.java"));
            System.out.println("资源文件路径: " + getRuntimeConfig("path.resources"));
            System.out.println("Mapper XML路径: " + getRuntimeConfig("path.mappers.xml"));
        }
    }

    /**
     * 配置具体路径
     */
    private static void configureSpecificPaths(Scanner scanner, String basePath) {
        System.out.println("\n自定义具体路径:");

        System.out.print("Java源码路径 [" + basePath + "/src/main/java]: ");
        String javaPath = scanner.nextLine().trim();
        if (!javaPath.isEmpty()) {
            setRuntimeConfig("path.java", javaPath);
        }

        System.out.print("资源文件路径 [" + basePath + "/src/main/resources]: ");
        String resourcesPath = scanner.nextLine().trim();
        if (!resourcesPath.isEmpty()) {
            setRuntimeConfig("path.resources", resourcesPath);
        }

        System.out.print("Mapper XML路径 [" + basePath + "/src/main/resources/mapper]: ");
        String mapperPath = scanner.nextLine().trim();
        if (!mapperPath.isEmpty()) {
            setRuntimeConfig("path.mappers.xml", mapperPath);
        }

        System.out.print("测试代码路径 [" + basePath + "/src/test/java]: ");
        String testPath = scanner.nextLine().trim();
        if (!testPath.isEmpty()) {
            setRuntimeConfig("path.test", testPath);
        }
    }

    /**
     * 交互式配置其他选项
     */
    public static void configureOtherOptions() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("\n=== 其他配置 ===");

            System.out.print("作者名称 [" + defaultValues.get("auther.comment") + "]: ");
            String author = scanner.nextLine().trim();
            if (author.isEmpty()) {
                author = defaultValues.get("auther.comment");
            }
            setRuntimeConfig("auther.comment", author);

            System.out.print("是否忽略表前缀？(y/n) [y]: ");
            String ignorePrefix = scanner.nextLine().trim();
            boolean ignore = ignorePrefix.isEmpty() || "y".equalsIgnoreCase(ignorePrefix);
            setRuntimeConfig("ignore.table.prefix", String.valueOf(ignore));

            System.out.print("查询参数类后缀 [Query]: ");
            String querySuffix = scanner.nextLine().trim();
            if (querySuffix.isEmpty()) {
                querySuffix = "Query";
            }
            setRuntimeConfig("suffix.bean.param", querySuffix);

            System.out.print("Mapper接口后缀 [Mapper]: ");
            String mapperSuffix = scanner.nextLine().trim();
            if (mapperSuffix.isEmpty()) {
                mapperSuffix = "Mapper";
            }
            setRuntimeConfig("suffix.mapper", mapperSuffix);

            System.out.println("✅ 其他配置完成");
        }
    }

    /**
     * 交互式配置分表功能
     */
    public static void configureSharding() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("\n=== 动态分表配置 ===");
            
            // 1. 分表开关配置
            System.out.print("是否启用分表功能? (y/n) [n]: ");
            String enableSharding = scanner.nextLine().trim();
            boolean shardingEnabled = "y".equalsIgnoreCase(enableSharding) || "yes".equalsIgnoreCase(enableSharding);
            setRuntimeConfig("sharding.enabled", String.valueOf(shardingEnabled));
            
            if (!shardingEnabled) {
                System.out.println("✅ 分表功能已禁用");
                return;
            }
            
            // 2. 分表策略配置
            System.out.println("\n可选分表策略:");
            System.out.println("1. hash - 哈希分表 (推荐)");
            System.out.println("2. mod - 取模分表");
            System.out.println("3. range - 范围分表");
            System.out.println("4. time - 时间分表");
            
            System.out.print("请选择分表策略 (1-4) [1]: ");
            String strategyChoice = scanner.nextLine().trim();
            String strategy = "hash";
            switch (strategyChoice) {
                case "2":
                    strategy = "mod";
                    break;
                case "3":
                    strategy = "range";
                    break;
                case "4":
                    strategy = "time";
                    break;
                default:
                    strategy = "hash";
                    break;
            }
            setRuntimeConfig("sharding.strategy.type", strategy);
            System.out.println("选择的分表策略: " + strategy);
            
            // 3. 分表数量配置
            System.out.print("请输入分表数量 (1-1024) [8]: ");
            String tableCountStr = scanner.nextLine().trim();
            int tableCount = 8;
            if (!tableCountStr.isEmpty()) {
                try {
                    tableCount = Integer.parseInt(tableCountStr);
                    if (tableCount < 1 || tableCount > 1024) {
                        System.out.println("分表数量超出范围，使用默认值: 8");
                        tableCount = 8;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("输入格式错误，使用默认值: 8");
                    tableCount = 8;
                }
            }
            setRuntimeConfig("sharding.table.count", String.valueOf(tableCount));
            System.out.println("分表数量: " + tableCount);
            
            // 4. 配置跨表字段分表映射
            System.out.println("\n=== 配置跨表字段分表映射 ===");
            System.out.println("示例: 通过用户表的id来构建信息表的分表");
            System.out.println("格式: 目标表名=源表名.字段名");
            System.out.println("输入 'done' 完成配置，'skip' 跳过");
            
            int mappingCount = 1;
            while (true) {
                System.out.print("映射配置 " + mappingCount + ": ");
                String input = scanner.nextLine().trim();
                
                if ("done".equalsIgnoreCase(input)) {
                    break;
                } else if ("skip".equalsIgnoreCase(input)) {
                    System.out.println("跳过跨表字段分表映射配置");
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
                        
                        if (sourceMapping.matches("\\w+\\.\\w+")) {
                            setRuntimeConfig("sharding.mapping." + targetTable, sourceMapping);
                            System.out.println("✅ 添加映射: " + targetTable + " -> " + sourceMapping);
                            mappingCount++;
                        } else {
                            System.out.println("❌ 格式错误，正确格式: 表名.字段名");
                        }
                    } else {
                        System.out.println("❌ 格式错误，正确格式: 目标表名=源表名.字段名");
                    }
                } else {
                    System.out.println("❌ 格式错误，正确格式: 目标表名=源表名.字段名");
                }
            }
            
            // 5. 配置分表字段
            System.out.println("\n=== 配置分表字段 ===");
            System.out.println("格式: 表名=分表字段名");
            System.out.println("输入 'done' 完成配置，'skip' 跳过");
            
            int fieldCount = 1;
            while (true) {
                System.out.print("分表字段配置 " + fieldCount + ": ");
                String input = scanner.nextLine().trim();
                
                if ("done".equalsIgnoreCase(input)) {
                    break;
                } else if ("skip".equalsIgnoreCase(input)) {
                    System.out.println("跳过分表字段配置");
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
                            setRuntimeConfig("sharding.field." + tableName, fieldName);
                            System.out.println("✅ 添加分表字段: " + tableName + " -> " + fieldName);
                            fieldCount++;
                        } else {
                            System.out.println("❌ 表名和字段名不能为空");
                        }
                    } else {
                        System.out.println("❌ 格式错误，正确格式: 表名=字段名");
                    }
                } else {
                    System.out.println("❌ 格式错误，正确格式: 表名=字段名");
                }
            }
            
            // 6. 数据库写入配置
            System.out.print("\n是否启用分表写入数据库? (y/n) [y]: ");
            String enableDbWrite = scanner.nextLine().trim();
            boolean dbWriteEnabled = enableDbWrite.isEmpty() || "y".equalsIgnoreCase(enableDbWrite);
            setRuntimeConfig("sharding.database.write.enabled", String.valueOf(dbWriteEnabled));
            
            if (dbWriteEnabled) {
                System.out.print("是否自动创建分表? (y/n) [y]: ");
                String autoCreate = scanner.nextLine().trim();
                boolean autoCreateEnabled = autoCreate.isEmpty() || "y".equalsIgnoreCase(autoCreate);
                setRuntimeConfig("sharding.auto.create.table", String.valueOf(autoCreateEnabled));
                
                System.out.println("数据库写入: 启用");
                System.out.println("自动创建分表: " + (autoCreateEnabled ? "启用" : "禁用"));
            } else {
                System.out.println("数据库写入: 禁用");
                setRuntimeConfig("sharding.auto.create.table", "false");
            }
            
            // 7. 保存配置
            System.out.print("\n是否保存分表配置到文件? (y/n) [y]: ");
            String saveConfig = scanner.nextLine().trim();
            if (saveConfig.isEmpty() || "y".equalsIgnoreCase(saveConfig)) {
                saveShardingConfigurationToFile();
            }
            
            System.out.println("\n✅ 分表配置完成！");
            showShardingConfiguration();
            
        } catch (Exception e) {
            logger.error("分表配置过程中发生错误", e);
            System.out.println("❌ 分表配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 显示当前分表配置
     */
    public static void showShardingConfiguration() {
        System.out.println("\n=== 当前分表配置 ===");
        
        String enabled = getRuntimeConfig("sharding.enabled");
        System.out.println("分表功能: " + ("true".equals(enabled) ? "启用" : "禁用"));
        
        if ("true".equals(enabled)) {
            String strategy = getRuntimeConfig("sharding.strategy.type");
            String count = getRuntimeConfig("sharding.table.count");
            String dbWrite = getRuntimeConfig("sharding.database.write.enabled");
            String autoCreate = getRuntimeConfig("sharding.auto.create.table");
            
            System.out.println("分表策略: " + (strategy != null ? strategy : "hash"));
            System.out.println("分表数量: " + (count != null ? count : "8"));
            System.out.println("数据库写入: " + ("true".equals(dbWrite) ? "启用" : "禁用"));
            System.out.println("自动创建分表: " + ("true".equals(autoCreate) ? "启用" : "禁用"));
            
            // 显示分表映射配置
            System.out.println("\n跨表字段分表映射:");
            boolean hasMapping = false;
            for (String key : runtimeConfig.keySet()) {
                if (key.startsWith("sharding.mapping.")) {
                    String tableName = key.substring("sharding.mapping.".length());
                    String mapping = runtimeConfig.get(key);
                    System.out.println("  " + tableName + " -> " + mapping);
                    hasMapping = true;
                }
            }
            if (!hasMapping) {
                System.out.println("  (无配置)");
            }
            
            // 显示分表字段配置
            System.out.println("\n分表字段配置:");
            boolean hasField = false;
            for (String key : runtimeConfig.keySet()) {
                if (key.startsWith("sharding.field.")) {
                    String tableName = key.substring("sharding.field.".length());
                    String fieldName = runtimeConfig.get(key);
                    System.out.println("  " + tableName + " -> " + fieldName);
                    hasField = true;
                }
            }
            if (!hasField) {
                System.out.println("  (无配置)");
            }
        }
        System.out.println("========================\n");
    }
    
    /**
     * 保存分表配置到文件
     */
    private static void saveShardingConfigurationToFile() {
        try {
            // 读取现有配置
            Properties props = new Properties();
            try (InputStream is = DynamicConfigManager.class.getClassLoader().getResourceAsStream("application.properties")) {
                if (is != null) {
                    props.load(new InputStreamReader(is, StandardCharsets.UTF_8));
                }
            }
            
            // 更新分表配置
            for (Map.Entry<String, String> entry : runtimeConfig.entrySet()) {
                if (entry.getKey().startsWith("sharding.")) {
                    props.setProperty(entry.getKey(), entry.getValue());
                }
            }
            
            // 写入配置文件
            String configPath = DynamicConfigManager.class.getClassLoader().getResource("application.properties").getPath();
            try (FileOutputStream fos = new FileOutputStream(configPath);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                
                props.store(osw, "Updated sharding configuration - " + new java.util.Date());
                logger.info("分表配置已保存到配置文件");
                System.out.println("✅ 分表配置已保存");
            }
            
        } catch (Exception e) {
            logger.error("保存分表配置失败", e);
            System.out.println("❌ 保存分表配置失败: " + e.getMessage());
        }
    }

    /**
     * 配置分表映射（跨表字段分表）
     */
    private static void configureShardingMappings(Scanner scanner) {
        System.out.println("\n=== 配置跨表字段分表映射 ===");
        System.out.println("示例：通过用户表的id字段为订单表分表");
        System.out.println("格式：目标表名=源表名.字段名");
        System.out.println("如：order_table=user_table.id");
        System.out.println("输入空行完成配置");
        
        int mappingCount = 0;
        while (true) {
            System.out.print("分表映射 " + (mappingCount + 1) + " (目标表名=源表名.字段名): ");
            String mapping = scanner.nextLine().trim();
            
            if (mapping.isEmpty()) {
                break;
            }
            
            if (mapping.contains("=") && mapping.contains(".")) {
                String[] parts = mapping.split("=", 2);
                if (parts.length == 2) {
                    String targetTable = parts[0].trim();
                    String sourceMapping = parts[1].trim();
                    
                    if (Pattern.matches("\\w+\\.\\w+", sourceMapping)) {
                        setRuntimeConfig("sharding.mapping." + targetTable, sourceMapping);
                        System.out.println("✓ 添加映射: " + targetTable + " -> " + sourceMapping);
                        mappingCount++;
                    } else {
                        System.out.println("❌ 格式错误，正确格式: 表名.字段名");
                    }
                } else {
                    System.out.println("❌ 格式错误，正确格式: 目标表名=源表名.字段名");
                }
            } else {
                System.out.println("❌ 格式错误，正确格式: 目标表名=源表名.字段名");
            }
        }
        
        System.out.println("已配置 " + mappingCount + " 个分表映射");
    }

    /**
     * 配置分表字段
     */
    private static void configureShardingFields(Scanner scanner) {
        System.out.println("\n=== 配置分表字段 ===");
        System.out.println("为需要分表的表指定分表字段");
        System.out.println("格式：表名=字段名");
        System.out.println("如：user_table=id");
        System.out.println("输入空行完成配置");
        
        int fieldCount = 0;
        while (true) {
            System.out.print("分表字段 " + (fieldCount + 1) + " (表名=字段名): ");
            String field = scanner.nextLine().trim();
            
            if (field.isEmpty()) {
                break;
            }
            
            if (field.contains("=")) {
                String[] parts = field.split("=", 2);
                if (parts.length == 2) {
                    String tableName = parts[0].trim();
                    String fieldName = parts[1].trim();
                    
                    if (!tableName.isEmpty() && !fieldName.isEmpty()) {
                        setRuntimeConfig("sharding.field." + tableName, fieldName);
                        System.out.println("✓ 添加分表字段: " + tableName + " -> " + fieldName);
                        fieldCount++;
                    } else {
                        System.out.println("❌ 表名和字段名不能为空");
                    }
                } else {
                    System.out.println("❌ 格式错误，正确格式: 表名=字段名");
                }
            } else {
                System.out.println("❌ 格式错误，正确格式: 表名=字段名");
            }
        }
        
        System.out.println("已配置 " + fieldCount + " 个分表字段");
    }

    /**
     * 完整的交互式配置流程
     */
    public static void interactiveConfiguration() {
        System.out.println("=== EasyJava 交互式配置 ===");
        System.out.println("将引导您完成项目配置，按 Ctrl+C 随时退出");
        System.out.println();
        
        try (Scanner scanner = new Scanner(System.in)) {
            // 1. 数据库配置
            System.out.print("是否配置数据库连接？(y/n) [y]: ");
            String configDb = scanner.nextLine().trim();
            if (configDb.isEmpty() || "y".equalsIgnoreCase(configDb)) {
                configureDatabase();
            }
            
            // 2. 包名配置
            System.out.print("\n是否配置包名？(y/n) [y]: ");
            String configPkg = scanner.nextLine().trim();
            if (configPkg.isEmpty() || "y".equalsIgnoreCase(configPkg)) {
                configurePackages();
            }
            
            // 3. 输出路径配置
            System.out.print("\n是否配置输出路径？(y/n) [y]: ");
            String configPath = scanner.nextLine().trim();
            if (configPath.isEmpty() || "y".equalsIgnoreCase(configPath)) {
                configureOutputPaths();
            }
            
            // 4. 分表配置
            System.out.print("\n是否配置分表功能？(y/n) [n]: ");
            String configSharding = scanner.nextLine().trim();
            if ("y".equalsIgnoreCase(configSharding)) {
                configureSharding();
            }
            
            // 5. 其他配置
            System.out.print("\n是否配置其他选项？(y/n) [n]: ");
            String configOther = scanner.nextLine().trim();
            if ("y".equalsIgnoreCase(configOther)) {
                configureOtherOptions();
            }
            
            // 6. 保存配置
            System.out.print("\n是否保存配置到文件？(y/n) [y]: ");
            String saveConfig = scanner.nextLine().trim();
            if (saveConfig.isEmpty() || "y".equalsIgnoreCase(saveConfig)) {
                saveConfigurationToFile();
            }
            
            System.out.println("\n✅ 配置完成！现在可以开始生成代码了。");
            
        } catch (Exception e) {
            logger.error("配置过程中出错: {}", e.getMessage());
            System.out.println("❌ 配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置运行时配置
     */
    public static void setRuntimeConfig(String key, String value) {
        if (value != null) {
            runtimeConfig.put(key, value);
            logger.debug("设置配置: {} = {}", key, value);
        }
    }
    
    /**
     * 获取运行时配置
     */
    public static String getRuntimeConfig(String key) {
        // 优先返回运行时配置
        String runtimeValue = runtimeConfig.get(key);
        if (runtimeValue != null) {
            return runtimeValue;
        }
        
        // 其次返回原始配置
        String originalValue = PropertiesUtils.geString(key);
        if (originalValue != null && !originalValue.isEmpty()) {
            return originalValue;
        }
        
        // 最后返回默认值
        return defaultValues.get(key);
    }
    
    /**
     * 验证配置是否有效
     */
    public static boolean isValidConfig(String key, String value) {
        Pattern validator = configValidators.get(key);
        if (validator == null) {
            return true; // 没有验证规则，认为有效
        }
        return validator.matcher(value).matches();
    }
    
    /**
     * 获取所有运行时配置
     */
    public static Map<String, String> getAllRuntimeConfig() {
        Map<String, String> allConfig = new HashMap<>(defaultValues);
        allConfig.putAll(runtimeConfig);
        return allConfig;
    }
    
    /**
     * 清除运行时配置
     */
    public static void clearRuntimeConfig() {
        runtimeConfig.clear();
        logger.info("已清除运行时配置");
    }
    
    /**
     * 保存配置到文件
     */
    public static void saveConfigurationToFile() {
        try {
            String configPath = System.getProperty("user.dir") + File.separator + "easyjava-config.properties";
            
            Properties props = new Properties();
            
            // 添加所有运行时配置
            for (Map.Entry<String, String> entry : runtimeConfig.entrySet()) {
                props.setProperty(entry.getKey(), entry.getValue());
            }
            
            // 写入文件
            try (FileOutputStream fos = new FileOutputStream(configPath);
                 OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                
                props.store(writer, "EasyJava 自定义配置文件 - 生成时间: " + new Date());
                System.out.println("✅ 配置已保存到: " + configPath);
                
            }
            
        } catch (IOException e) {
            logger.error("保存配置文件失败: {}", e.getMessage());
            System.out.println("❌ 保存配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 从文件加载配置
     */
    public static void loadConfigurationFromFile(String configPath) {
        try {
            File configFile = new File(configPath);
            if (!configFile.exists()) {
                logger.warn("配置文件不存在: {}", configPath);
                return;
            }
            
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile);
                 InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
                
                props.load(reader);
                
                // 加载到运行时配置
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    setRuntimeConfig(entry.getKey().toString(), entry.getValue().toString());
                }
                
                logger.info("从文件加载配置: {}", configPath);
                System.out.println("✅ 配置加载完成，共加载 " + props.size() + " 项配置");
            }
            
        } catch (IOException e) {
            logger.error("加载配置文件失败: {}", e.getMessage());
            System.out.println("❌ 加载配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 显示当前配置
     */
    public static void showCurrentConfiguration() {
        System.out.println("\n=== 当前配置信息 ===");
        
        System.out.println("\n数据库配置:");
        System.out.println("  驱动类: " + getRuntimeConfig("spring.datasource.driver-class-name"));
        System.out.println("  连接URL: " + getRuntimeConfig("spring.datasource.url"));
        System.out.println("  用户名: " + getRuntimeConfig("spring.datasource.username"));
        System.out.println("  密码: " + maskPassword(getRuntimeConfig("spring.datasource.password")));
        
        System.out.println("\n包名配置:");
        System.out.println("  基础包名: " + getRuntimeConfig("package.base"));
        System.out.println("  实体类包: " + getRuntimeConfig("package.po"));
        System.out.println("  查询参数包: " + getRuntimeConfig("package.query"));
        System.out.println("  Service包: " + getRuntimeConfig("package.service"));
        System.out.println("  Controller包: " + getRuntimeConfig("package.controller"));
        System.out.println("  Mapper包: " + getRuntimeConfig("package.mapper"));
        
        System.out.println("\n路径配置:");
        System.out.println("  项目根路径: " + getRuntimeConfig("path.base"));
        System.out.println("  Java源码路径: " + getRuntimeConfig("path.java"));
        System.out.println("  资源文件路径: " + getRuntimeConfig("path.resources"));
        System.out.println("  Mapper XML路径: " + getRuntimeConfig("path.mappers.xml"));
        
        System.out.println("\n其他配置:");
        System.out.println("  作者: " + getRuntimeConfig("auther.comment"));
        System.out.println("  忽略表前缀: " + getRuntimeConfig("ignore.table.prefix"));
        System.out.println("  查询类后缀: " + getRuntimeConfig("suffix.bean.param"));
        System.out.println("  Mapper后缀: " + getRuntimeConfig("suffix.mapper"));
    }
    
    /**
     * 掩码密码显示
     */
    private static String maskPassword(String password) {
        if (password == null || password.length() <= 2) {
            return "***";
        }
        return password.charAt(0) + "***" + password.charAt(password.length() - 1);
    }
    
    /**
     * 验证所有配置的有效性
     */
    public static ValidationResult validateAllConfiguration() {
        ValidationResult result = new ValidationResult();
        
        // 验证数据库配置
        String url = getRuntimeConfig("spring.datasource.url");
        if (!isValidConfig("spring.datasource.url", url)) {
            result.addError("数据库URL格式不正确: " + url);
        }
        
        String username = getRuntimeConfig("spring.datasource.username");
        if (username == null || username.trim().isEmpty()) {
            result.addError("数据库用户名不能为空");
        }
        
        // 验证包名配置
        String basePackage = getRuntimeConfig("package.base");
        if (!isValidConfig("package.base", basePackage)) {
            result.addError("基础包名格式不正确: " + basePackage);
        }
        
        // 验证路径配置
        String basePath = getRuntimeConfig("path.base");
        if (basePath == null || basePath.trim().isEmpty()) {
            result.addError("项目根路径不能为空");
        }
        
        return result;
    }
    
    /**
     * 配置验证结果
     */
    public static class ValidationResult {
        private boolean valid = true;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            this.valid = false;
            this.errors.add(error);
        }
        
        public void addWarning(String warning) {
            this.warnings.add(warning);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
    }
}
