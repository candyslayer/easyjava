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
            
            // 4. 其他配置
            System.out.print("\n是否配置其他选项？(y/n) [n]: ");
            String configOther = scanner.nextLine().trim();
            if ("y".equalsIgnoreCase(configOther)) {
                configureOtherOptions();
            }
            
            // 5. 保存配置
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
