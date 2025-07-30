package com.easyjava.manager;

import com.easyjava.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 模板配置管理器 - 管理模板的配置和自定义设置
 * 
 * @author EasyJava
 * @since 2025-07-30
 */
public class TemplateConfigManager {
    
    private static final Logger logger = LoggerFactory.getLogger(TemplateConfigManager.class);
    
    // 模板配置文件名
    private static final String TEMPLATE_CONFIG_FILE = "template-config.properties";
    
    // 模板配置
    private static final Properties templateConfig = new Properties();
    
    // 模板类型映射
    private static final Map<String, TemplateType> templateTypes = new HashMap<>();
    
    static {
        initializeTemplateTypes();
        loadTemplateConfig();
    }
    
    /**
     * 初始化模板类型
     */
    private static void initializeTemplateTypes() {
        templateTypes.put("po", new TemplateType("Po.txt", "实体类模板", true));
        templateTypes.put("query", new TemplateType("Query.txt", "查询参数模板", true));
        templateTypes.put("service", new TemplateType("Service.txt", "Service接口模板", true));
        templateTypes.put("serviceImpl", new TemplateType("ServiceImpl.txt", "Service实现模板", true));
        templateTypes.put("controller", new TemplateType("Controller.txt", "Controller模板", true));
        templateTypes.put("mapper", new TemplateType("Mapper.txt", "Mapper接口模板", true));
        templateTypes.put("mapperXml", new TemplateType("MapperXML.txt", "Mapper XML模板", true));
        templateTypes.put("test", new TemplateType("Test.txt", "测试类模板", false));
    }
    
    /**
     * 加载模板配置
     */
    private static void loadTemplateConfig() {
        try {
            String configPath = getCustomTemplatePath() + TEMPLATE_CONFIG_FILE;
            File configFile = new File(configPath);
            
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile);
                     InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
                    templateConfig.load(reader);
                    logger.info("加载模板配置文件: {}", configPath);
                }
            } else {
                // 创建默认配置
                createDefaultTemplateConfig();
            }
        } catch (IOException e) {
            logger.error("加载模板配置失败: {}", e.getMessage());
        }
    }
    
    /**
     * 创建默认模板配置
     */
    private static void createDefaultTemplateConfig() {
        try {
            // 设置默认配置
            templateConfig.setProperty("template.author", "EasyJava");
            templateConfig.setProperty("template.encoding", "UTF-8");
            templateConfig.setProperty("template.dateFormat", "yyyy-MM-dd");
            templateConfig.setProperty("template.datetimeFormat", "yyyy-MM-dd HH:mm:ss");
            
            // 设置各个模板的启用状态
            for (Map.Entry<String, TemplateType> entry : templateTypes.entrySet()) {
                String key = "template." + entry.getKey() + ".enabled";
                templateConfig.setProperty(key, String.valueOf(entry.getValue().isEnabled()));
            }
            
            // 保存配置文件
            saveTemplateConfig();
            logger.info("创建默认模板配置");
        } catch (Exception e) {
            logger.error("创建默认模板配置失败: {}", e.getMessage());
        }
    }
    
    /**
     * 保存模板配置
     */
    public static void saveTemplateConfig() {
        try {
            String configPath = getCustomTemplatePath() + TEMPLATE_CONFIG_FILE;
            File configFile = new File(configPath);
            
            // 确保目录存在
            Files.createDirectories(configFile.getParentFile().toPath());
            
            try (FileOutputStream fos = new FileOutputStream(configFile);
                 OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                
                templateConfig.store(writer, "模板配置文件 - Template Configuration");
                logger.info("保存模板配置文件: {}", configPath);
            }
        } catch (IOException e) {
            logger.error("保存模板配置失败: {}", e.getMessage());
        }
    }
    
    /**
     * 获取自定义模板路径
     */
    private static String getCustomTemplatePath() {
        String path = PropertiesUtils.geString("template.custom.path");
        if (org.apache.commons.lang3.StringUtils.isEmpty(path)) {
            path = System.getProperty("user.dir") + File.separator + "custom-templates" + File.separator;
        }
        return path;
    }
    
    /**
     * 获取模板配置值
     */
    public static String getTemplateConfig(String key) {
        return templateConfig.getProperty(key);
    }
    
    /**
     * 获取模板配置值（带默认值）
     */
    public static String getTemplateConfig(String key, String defaultValue) {
        return templateConfig.getProperty(key, defaultValue);
    }
    
    /**
     * 设置模板配置值
     */
    public static void setTemplateConfig(String key, String value) {
        templateConfig.setProperty(key, value);
    }
    
    /**
     * 获取作者配置
     */
    public static String getAuthor() {
        return getTemplateConfig("template.author", "EasyJava");
    }
    
    /**
     * 设置作者配置
     */
    public static void setAuthor(String author) {
        setTemplateConfig("template.author", author);
    }
    
    /**
     * 获取编码配置
     */
    public static String getEncoding() {
        return getTemplateConfig("template.encoding", "UTF-8");
    }
    
    /**
     * 检查模板类型是否启用
     */
    public static boolean isTemplateEnabled(String templateType) {
        String key = "template." + templateType + ".enabled";
        return Boolean.parseBoolean(getTemplateConfig(key, "true"));
    }
    
    /**
     * 设置模板类型启用状态
     */
    public static void setTemplateEnabled(String templateType, boolean enabled) {
        String key = "template." + templateType + ".enabled";
        setTemplateConfig(key, String.valueOf(enabled));
    }
    
    /**
     * 获取所有模板类型
     */
    public static Map<String, TemplateType> getTemplateTypes() {
        return new HashMap<>(templateTypes);
    }
    
    /**
     * 获取启用的模板类型
     */
    public static List<String> getEnabledTemplateTypes() {
        List<String> enabledTypes = new ArrayList<>();
        for (String type : templateTypes.keySet()) {
            if (isTemplateEnabled(type)) {
                enabledTypes.add(type);
            }
        }
        return enabledTypes;
    }
    
    /**
     * 初始化自定义模板环境
     */
    public static void initializeCustomTemplateEnvironment() {
        try {
            String customPath = getCustomTemplatePath();
            Path customDir = Paths.get(customPath);
            
            // 创建自定义模板目录
            if (!Files.exists(customDir)) {
                Files.createDirectories(customDir);
                logger.info("创建自定义模板目录: {}", customDir);
            }
            
            // 创建配置文件
            if (!Files.exists(Paths.get(customPath + TEMPLATE_CONFIG_FILE))) {
                createDefaultTemplateConfig();
            }
            
            // 复制示例模板
            copyExampleTemplates();
            
            logger.info("自定义模板环境初始化完成");
        } catch (Exception e) {
            logger.error("初始化自定义模板环境失败: {}", e.getMessage());
        }
    }
    
    /**
     * 复制示例模板
     */
    private static void copyExampleTemplates() {
        String customPath = getCustomTemplatePath();
        String examplePath = customPath + "examples" + File.separator;
        
        try {
            Files.createDirectories(Paths.get(examplePath));
            
            for (TemplateType templateType : templateTypes.values()) {
                String templateName = templateType.getFileName();
                Path exampleFile = Paths.get(examplePath + templateName + ".example");
                
                if (!Files.exists(exampleFile)) {
                    String content = TemplateManager.getTemplate(templateName);
                    if (content != null) {
                        Files.write(exampleFile, content.getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
            
            logger.info("示例模板复制完成: {}", examplePath);
        } catch (IOException e) {
            logger.warn("复制示例模板失败: {}", e.getMessage());
        }
    }
    
    /**
     * 生成模板配置报告
     */
    public static String generateConfigReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== 模板配置报告 ===\n");
        report.append("生成时间: ").append(new Date()).append("\n\n");
        
        report.append("基本配置:\n");
        report.append("  作者: ").append(getAuthor()).append("\n");
        report.append("  编码: ").append(getEncoding()).append("\n");
        report.append("  自定义模板路径: ").append(getCustomTemplatePath()).append("\n\n");
        
        report.append("模板状态:\n");
        for (Map.Entry<String, TemplateType> entry : templateTypes.entrySet()) {
            String type = entry.getKey();
            TemplateType templateType = entry.getValue();
            boolean enabled = isTemplateEnabled(type);
            
            report.append("  ").append(templateType.getDescription())
                  .append(" (").append(templateType.getFileName()).append("): ")
                  .append(enabled ? "启用" : "禁用").append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * 模板类型定义
     */
    public static class TemplateType {
        private String fileName;
        private String description;
        private boolean enabled;
        
        public TemplateType(String fileName, String description, boolean enabled) {
            this.fileName = fileName;
            this.description = description;
            this.enabled = enabled;
        }
        
        public String getFileName() {
            return fileName;
        }
        
        public String getDescription() {
            return description;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
