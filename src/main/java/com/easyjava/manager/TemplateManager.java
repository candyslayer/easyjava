package com.easyjava.manager;

import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.PropertiesUtils;
import com.easyjava.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板管理器 - 支持自定义模板功能
 * 
 * @author EasyJava
 * @since 2025-07-30
 */
public class TemplateManager {
    
    private static final Logger logger = LoggerFactory.getLogger(TemplateManager.class);
    
    // 模板变量匹配模式
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    // 默认模板目录
    private static final String DEFAULT_TEMPLATE_PATH = "template/";
    
    // 自定义模板目录
    private static String customTemplatePath;
    
    // 模板缓存
    private static final Map<String, String> templateCache = new HashMap<>();
    
    // 全局变量
    private static final Map<String, Object> globalVariables = new HashMap<>();
    
    static {
        // 初始化自定义模板路径
        customTemplatePath = PropertiesUtils.geString("template.custom.path");
        if (org.apache.commons.lang3.StringUtils.isEmpty(customTemplatePath)) {
            customTemplatePath = System.getProperty("user.dir") + File.separator + "custom-templates" + File.separator;
        }
        
        // 初始化全局变量
        initializeGlobalVariables();
    }
    
    /**
     * 初始化全局变量
     */
    private static void initializeGlobalVariables() {
        String author = PropertiesUtils.geString("template.author");
        globalVariables.put("author", org.apache.commons.lang3.StringUtils.isEmpty(author) ? "EasyJava" : author);
        globalVariables.put("date", new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        globalVariables.put("datetime", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        globalVariables.put("year", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        globalVariables.put("package.base", PropertiesUtils.geString("package.base"));
        globalVariables.put("package.po", PropertiesUtils.geString("package.po"));
        globalVariables.put("package.query", PropertiesUtils.geString("package.query"));
        globalVariables.put("package.service", PropertiesUtils.geString("package.service"));
        globalVariables.put("package.service.impl", PropertiesUtils.geString("package.service.impl"));
        globalVariables.put("package.controller", PropertiesUtils.geString("package.controller"));
        globalVariables.put("package.mapper", PropertiesUtils.geString("package.mapper"));
    }
    
    /**
     * 获取模板内容
     * 
     * @param templateName 模板名称
     * @return 模板内容
     */
    public static String getTemplate(String templateName) {
        // 先检查缓存
        if (templateCache.containsKey(templateName)) {
            return templateCache.get(templateName);
        }
        
        String templateContent = null;
        
        // 1. 优先查找自定义模板
        templateContent = loadCustomTemplate(templateName);
        
        // 2. 如果自定义模板不存在，使用默认模板
        if (templateContent == null) {
            templateContent = loadDefaultTemplate(templateName);
        }
        
        // 3. 缓存模板内容
        if (templateContent != null) {
            templateCache.put(templateName, templateContent);
        }
        
        return templateContent;
    }
    
    /**
     * 加载自定义模板
     * 
     * @param templateName 模板名称
     * @return 模板内容
     */
    private static String loadCustomTemplate(String templateName) {
        try {
            Path customPath = Paths.get(customTemplatePath + templateName);
            if (Files.exists(customPath)) {
                logger.info("使用自定义模板: {}", customPath);
                return new String(Files.readAllBytes(customPath), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            logger.warn("读取自定义模板失败: {}, 错误: {}", templateName, e.getMessage());
        }
        return null;
    }
    
    /**
     * 加载默认模板
     * 
     * @param templateName 模板名称
     * @return 模板内容
     */
    private static String loadDefaultTemplate(String templateName) {
        try (InputStream is = TemplateManager.class.getClassLoader()
                .getResourceAsStream(DEFAULT_TEMPLATE_PATH + templateName)) {
            if (is != null) {
                logger.debug("使用默认模板: {}", templateName);
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            logger.error("读取默认模板失败: {}, 错误: {}", templateName, e.getMessage());
        }
        return null;
    }
    
    /**
     * 处理模板变量替换
     * 
     * @param template 模板内容
     * @param variables 变量映射
     * @return 处理后的内容
     */
    public static String processTemplate(String template, Map<String, Object> variables) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(template)) {
            return template;
        }
        
        // 合并全局变量和局部变量
        Map<String, Object> allVariables = new HashMap<>(globalVariables);
        if (variables != null) {
            allVariables.putAll(variables);
        }
        
        String result = template;
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            String placeholder = matcher.group(0);
            
            Object value = getVariableValue(varName, allVariables);
            if (value != null) {
                result = result.replace(placeholder, value.toString());
            } else {
                logger.warn("未找到模板变量: {}", varName);
            }
        }
        
        return result;
    }
    
    /**
     * 获取变量值，支持嵌套属性访问
     * 
     * @param varName 变量名
     * @param variables 变量映射
     * @return 变量值
     */
    private static Object getVariableValue(String varName, Map<String, Object> variables) {
        if (varName.contains(".")) {
            // 处理嵌套属性，如 table.beanName
            String[] parts = varName.split("\\.");
            Object obj = variables.get(parts[0]);
            
            for (int i = 1; i < parts.length && obj != null; i++) {
                try {
                    String methodName = "get" + StringUtils.uperCaseFirstLetter(parts[i]);
                    obj = obj.getClass().getMethod(methodName).invoke(obj);
                } catch (Exception e) {
                    logger.warn("无法获取嵌套属性值: {}, 错误: {}", varName, e.getMessage());
                    return null;
                }
            }
            return obj;
        } else {
            return variables.get(varName);
        }
    }
    
    /**
     * 创建自定义模板目录
     */
    public static void createCustomTemplateDirectory() {
        try {
            Path customDir = Paths.get(customTemplatePath);
            if (!Files.exists(customDir)) {
                Files.createDirectories(customDir);
                logger.info("创建自定义模板目录: {}", customDir);
            }
        } catch (IOException e) {
            logger.error("创建自定义模板目录失败: {}", e.getMessage());
        }
    }
    
    /**
     * 复制默认模板到自定义目录
     * 
     * @param templateName 模板名称
     */
    public static void copyTemplateToCustom(String templateName) {
        try {
            String defaultTemplate = loadDefaultTemplate(templateName);
            if (defaultTemplate != null) {
                Path customPath = Paths.get(customTemplatePath + templateName);
                Files.createDirectories(customPath.getParent());
                Files.write(customPath, defaultTemplate.getBytes(StandardCharsets.UTF_8));
                logger.info("复制模板到自定义目录: {}", customPath);
            }
        } catch (IOException e) {
            logger.error("复制模板失败: {}, 错误: {}", templateName, e.getMessage());
        }
    }
    
    /**
     * 列出所有可用的模板
     * 
     * @return 模板名称列表
     */
    public static List<String> listAvailableTemplates() {
        Set<String> templates = new HashSet<>();
        
        // 添加默认模板
        try (InputStream is = TemplateManager.class.getClassLoader()
                .getResourceAsStream(DEFAULT_TEMPLATE_PATH)) {
            if (is != null) {
                // 这里需要实际实现列出资源目录的逻辑
                // 暂时使用硬编码的模板列表
                String[] defaultTemplates = {
                    "Po.txt", "Query.txt", "Service.txt", "ServiceImpl.txt",
                    "Controller.txt", "Mapper.txt", "MapperXML.txt"
                };
                templates.addAll(Arrays.asList(defaultTemplates));
            }
        } catch (Exception e) {
            logger.warn("获取默认模板列表失败: {}", e.getMessage());
        }
        
        // 添加自定义模板
        try {
            Path customDir = Paths.get(customTemplatePath);
            if (Files.exists(customDir)) {
                Files.list(customDir)
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .forEach(templates::add);
            }
        } catch (IOException e) {
            logger.warn("获取自定义模板列表失败: {}", e.getMessage());
        }
        
        return new ArrayList<>(templates);
    }
    
    /**
     * 清除模板缓存
     */
    public static void clearCache() {
        templateCache.clear();
        logger.info("已清除模板缓存");
    }
    
    /**
     * 验证模板语法
     * 
     * @param templateContent 模板内容
     * @return 验证结果
     */
    public static TemplateValidationResult validateTemplate(String templateContent) {
        TemplateValidationResult result = new TemplateValidationResult();
        
        if (org.apache.commons.lang3.StringUtils.isEmpty(templateContent)) {
            result.addError("模板内容为空");
            return result;
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(templateContent);
        Set<String> variables = new HashSet<>();
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            variables.add(varName);
            
            // 检查变量名是否合法
            if (!isValidVariableName(varName)) {
                result.addError("无效的变量名: " + varName);
            }
        }
        
        result.setVariables(variables);
        return result;
    }
    
    /**
     * 检查变量名是否合法
     * 
     * @param varName 变量名
     * @return 是否合法
     */
    private static boolean isValidVariableName(String varName) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(varName)) {
            return false;
        }
        
        // 允许字母、数字、点号、下划线
        return varName.matches("[a-zA-Z][a-zA-Z0-9._]*");
    }
    
    /**
     * 设置全局变量
     * 
     * @param key 变量名
     * @param value 变量值
     */
    public static void setGlobalVariable(String key, Object value) {
        globalVariables.put(key, value);
    }
    
    /**
     * 获取全局变量
     * 
     * @param key 变量名
     * @return 变量值
     */
    public static Object getGlobalVariable(String key) {
        return globalVariables.get(key);
    }
    
    /**
     * 创建表级别的变量映射
     * 
     * @param tableInfo 表信息
     * @return 变量映射
     */
    public static Map<String, Object> createTableVariables(TableInfo tableInfo) {
        Map<String, Object> variables = new HashMap<>();
        
        if (tableInfo != null) {
            variables.put("table", tableInfo);
            variables.put("tableName", tableInfo.getTableName());
            variables.put("beanName", tableInfo.getBeanName());
            variables.put("beanParamName", tableInfo.getBeanParamName());
            variables.put("comment", tableInfo.getComment());
            variables.put("fieldList", tableInfo.getFieldList());
            variables.put("keyFieldList", tableInfo.getKeyIndexMap().values());
            
            // 添加便捷访问的字段信息
            if (tableInfo.getFieldList() != null && !tableInfo.getFieldList().isEmpty()) {
                variables.put("firstField", tableInfo.getFieldList().get(0));
                
                // 查找主键字段
                FieldInfo primaryKey = tableInfo.getFieldList().stream()
                    .filter(field -> field.getIsAutoIncrement() != null && field.getIsAutoIncrement())
                    .findFirst().orElse(null);
                if (primaryKey != null) {
                    variables.put("primaryKey", primaryKey);
                }
            }
        }
        
        return variables;
    }
    
    /**
     * 模板验证结果
     */
    public static class TemplateValidationResult {
        private boolean valid = true;
        private List<String> errors = new ArrayList<>();
        private Set<String> variables = new HashSet<>();
        
        public void addError(String error) {
            this.valid = false;
            this.errors.add(error);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public Set<String> getVariables() {
            return variables;
        }
        
        public void setVariables(Set<String> variables) {
            this.variables = variables;
        }
    }
}
