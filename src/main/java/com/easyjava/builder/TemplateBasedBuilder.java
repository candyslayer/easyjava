package com.easyjava.builder;

import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.manager.TemplateManager;
import com.easyjava.manager.TemplateConfigManager;
import com.easyjava.utils.PropertiesUtils;
import com.easyjava.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * 基于模板的代码生成器
 * 
 * @author EasyJava
 * @since 2025-07-30
 */
public class TemplateBasedBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(TemplateBasedBuilder.class);
    
    /**
     * 使用模板生成代码
     * 
     * @param tableInfo 表信息
     * @param templateType 模板类型
     * @param outputPath 输出路径
     */
    public static void generateWithTemplate(TableInfo tableInfo, String templateType, String outputPath) {
        try {
            // 检查模板是否启用
            if (!TemplateConfigManager.isTemplateEnabled(templateType)) {
                logger.info("模板类型 {} 已禁用，跳过生成", templateType);
                return;
            }
            
            // 获取模板类型配置
            TemplateConfigManager.TemplateType templateTypeConfig = 
                TemplateConfigManager.getTemplateTypes().get(templateType);
            
            if (templateTypeConfig == null) {
                logger.error("未知的模板类型: {}", templateType);
                return;
            }
            
            // 获取模板内容
            String templateContent = TemplateManager.getTemplate(templateTypeConfig.getFileName());
            if (templateContent == null) {
                logger.error("无法获取模板内容: {}", templateTypeConfig.getFileName());
                return;
            }
            
            // 准备变量
            Map<String, Object> variables = prepareVariables(tableInfo, templateType);
            
            // 处理模板
            String generatedCode = TemplateManager.processTemplate(templateContent, variables);
            
            // 确定输出文件名
            String fileName = generateFileName(tableInfo, templateType);
            
            // 写入文件
            writeToFile(generatedCode, outputPath, fileName);
            
            logger.info("成功生成 {} 文件: {}", templateTypeConfig.getDescription(), fileName);
            
        } catch (Exception e) {
            logger.error("生成模板代码失败: templateType={}, error={}", templateType, e.getMessage(), e);
        }
    }
    
    /**
     * 准备模板变量
     * 
     * @param tableInfo 表信息
     * @param templateType 模板类型
     * @return 变量映射
     */
    private static Map<String, Object> prepareVariables(TableInfo tableInfo, String templateType) {
        Map<String, Object> variables = TemplateManager.createTableVariables(tableInfo);
        
        // 添加导入包列表
        Set<String> imports = collectImports(tableInfo, templateType);
        variables.put("imports", new ArrayList<>(imports));
        
        // 添加模板特定变量
        switch (templateType) {
            case "po":
                addPoVariables(variables, tableInfo);
                break;
            case "query":
                addQueryVariables(variables, tableInfo);
                break;
            case "service":
                addServiceVariables(variables, tableInfo);
                break;
            case "serviceImpl":
                addServiceImplVariables(variables, tableInfo);
                break;
            case "controller":
                addControllerVariables(variables, tableInfo);
                break;
            case "mapper":
                addMapperVariables(variables, tableInfo);
                break;
            case "mapperXml":
                addMapperXmlVariables(variables, tableInfo);
                break;
            case "test":
                addTestVariables(variables, tableInfo);
                break;
        }
        
        return variables;
    }
    
    /**
     * 收集需要导入的包
     */
    private static Set<String> collectImports(TableInfo tableInfo, String templateType) {
        Set<String> imports = new HashSet<>();
        
        for (FieldInfo field : tableInfo.getFieldList()) {
            String javaType = field.getJavaType();
            
            // 添加字段类型的导入
            if ("Date".equals(javaType)) {
                imports.add("java.util.Date");
            } else if ("BigDecimal".equals(javaType)) {
                imports.add("java.math.BigDecimal");
            } else if ("Time".equals(javaType)) {
                imports.add("java.sql.Time");
            } else if ("Timestamp".equals(javaType)) {
                imports.add("java.sql.Timestamp");
            }
        }
        
        // 根据模板类型添加特定导入
        switch (templateType) {
            case "po":
                imports.add("java.io.Serializable");
                imports.add("com.fasterxml.jackson.annotation.JsonFormat");
                break;
            case "query":
                imports.add("com.easyjava.entity.query.BaseParam");
                break;
            case "service":
                imports.add("java.util.List");
                imports.add("com.easyjava.entity.vo.PaginationResultVO");
                break;
            case "serviceImpl":
                imports.add("org.springframework.stereotype.Service");
                imports.add("javax.annotation.Resource");
                break;
            case "controller":
                imports.add("org.springframework.web.bind.annotation.*");
                imports.add("org.springframework.validation.annotation.Validated");
                break;
            case "mapper":
                imports.add("org.apache.ibatis.annotations.Param");
                break;
        }
        
        return imports;
    }
    
    /**
     * 添加Po模板变量
     */
    private static void addPoVariables(Map<String, Object> variables, TableInfo tableInfo) {
        // 检查是否有日期字段
        boolean hasDateField = tableInfo.getFieldList().stream()
            .anyMatch(field -> "Date".equals(field.getJavaType()));
        variables.put("hasDateField", hasDateField);
    }
    
    /**
     * 添加Query模板变量
     */
    private static void addQueryVariables(Map<String, Object> variables, TableInfo tableInfo) {
        // 统计字符串字段数量（用于模糊查询）
        long stringFieldCount = tableInfo.getFieldList().stream()
            .filter(field -> "String".equals(field.getJavaType()))
            .count();
        variables.put("stringFieldCount", stringFieldCount);
    }
    
    /**
     * 添加Service模板变量
     */
    private static void addServiceVariables(Map<String, Object> variables, TableInfo tableInfo) {
        // 添加主键信息
        List<FieldInfo> primaryKeys = new ArrayList<>();
        for (FieldInfo field : tableInfo.getFieldList()) {
            if (field.getIsAutoIncrement() != null && field.getIsAutoIncrement()) {
                primaryKeys.add(field);
            }
        }
        variables.put("primaryKeys", primaryKeys);
    }
    
    /**
     * 添加ServiceImpl模板变量
     */
    private static void addServiceImplVariables(Map<String, Object> variables, TableInfo tableInfo) {
        addServiceVariables(variables, tableInfo);
    }
    
    /**
     * 添加Controller模板变量
     */
    private static void addControllerVariables(Map<String, Object> variables, TableInfo tableInfo) {
        // 添加URL前缀
        String urlPrefix = "/" + StringUtils.lowerCaseFirstLetter(tableInfo.getBeanName());
        variables.put("urlPrefix", urlPrefix);
    }
    
    /**
     * 添加Mapper模板变量
     */
    private static void addMapperVariables(Map<String, Object> variables, TableInfo tableInfo) {
        // 添加表名
        variables.put("tableName", tableInfo.getTableName());
    }
    
    /**
     * 添加MapperXml模板变量
     */
    private static void addMapperXmlVariables(Map<String, Object> variables, TableInfo tableInfo) {
        addMapperVariables(variables, tableInfo);
        
        // 添加SQL相关变量
        variables.put("insertColumns", buildInsertColumns(tableInfo));
        variables.put("insertValues", buildInsertValues(tableInfo));
        variables.put("updateSet", buildUpdateSet(tableInfo));
    }
    
    /**
     * 添加Test模板变量
     */
    private static void addTestVariables(Map<String, Object> variables, TableInfo tableInfo) {
        // 添加测试数据
        Map<String, Object> testData = new HashMap<>();
        for (FieldInfo field : tableInfo.getFieldList()) {
            testData.put(field.getPropertyName(), generateTestValue(field));
        }
        variables.put("testData", testData);
    }
    
    /**
     * 构建插入列名
     */
    private static String buildInsertColumns(TableInfo tableInfo) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tableInfo.getFieldList().size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(tableInfo.getFieldList().get(i).getFieldName());
        }
        return sb.toString();
    }
    
    /**
     * 构建插入值
     */
    private static String buildInsertValues(TableInfo tableInfo) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tableInfo.getFieldList().size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("#{").append(tableInfo.getFieldList().get(i).getPropertyName()).append("}");
        }
        return sb.toString();
    }
    
    /**
     * 构建更新Set子句
     */
    private static String buildUpdateSet(TableInfo tableInfo) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tableInfo.getFieldList().size(); i++) {
            if (i > 0) sb.append(", ");
            FieldInfo field = tableInfo.getFieldList().get(i);
            sb.append(field.getFieldName()).append(" = #{").append(field.getPropertyName()).append("}");
        }
        return sb.toString();
    }
    
    /**
     * 生成测试值
     */
    private static Object generateTestValue(FieldInfo field) {
        String javaType = field.getJavaType();
        String fieldName = field.getPropertyName().toLowerCase();
        
        // 根据字段名生成特定值
        if (fieldName.contains("email")) {
            return "test@example.com";
        } else if (fieldName.contains("phone") || fieldName.contains("mobile")) {
            return "13800138000";
        } else if (fieldName.contains("url")) {
            return "http://example.com";
        }
        
        // 根据类型生成默认值
        switch (javaType) {
            case "String":
                return "test_" + fieldName;
            case "Integer":
                return 1;
            case "Long":
                return 1L;
            case "Float":
                return 1.0F;
            case "Double":
                return 1.0;
            case "BigDecimal":
                return "new BigDecimal(\"1.00\")";
            case "Boolean":
                return true;
            case "Date":
                return "new Date()";
            default:
                return "null";
        }
    }
    
    /**
     * 生成文件名
     */
    private static String generateFileName(TableInfo tableInfo, String templateType) {
        String baseName = tableInfo.getBeanName();
        
        switch (templateType) {
            case "po":
                return baseName + ".java";
            case "query":
                return baseName + "Query.java";
            case "service":
                return baseName + "Service.java";
            case "serviceImpl":
                return baseName + "ServiceImpl.java";
            case "controller":
                return baseName + "Controller.java";
            case "mapper":
                return baseName + "Mapper.java";
            case "mapperXml":
                return baseName + "Mapper.xml";
            case "test":
                return baseName + "Test.java";
            default:
                return baseName + "_" + templateType + ".java";
        }
    }
    
    /**
     * 写入文件
     */
    private static void writeToFile(String content, String outputPath, String fileName) throws IOException {
        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        File outputFile = new File(outputDir, fileName);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, false))) {
            writer.write(content);
            writer.flush();
        }
    }
    
    /**
     * 批量生成所有启用的模板
     */
    public static void generateAllEnabledTemplates(TableInfo tableInfo) {
        List<String> enabledTypes = TemplateConfigManager.getEnabledTemplateTypes();
        
        for (String templateType : enabledTypes) {
            String outputPath = getOutputPath(templateType);
            generateWithTemplate(tableInfo, templateType, outputPath);
        }
    }
    
    /**
     * 获取输出路径
     */
    private static String getOutputPath(String templateType) {
        String basePath = PropertiesUtils.geString("path.java");
        
        switch (templateType) {
            case "po":
                return basePath + PropertiesUtils.geString("package.po").replace(".", "/");
            case "query":
                return basePath + PropertiesUtils.geString("package.query").replace(".", "/");
            case "service":
                return basePath + PropertiesUtils.geString("package.service").replace(".", "/");
            case "serviceImpl":
                return basePath + PropertiesUtils.geString("package.service.impl").replace(".", "/");
            case "controller":
                return basePath + PropertiesUtils.geString("package.controller").replace(".", "/");
            case "mapper":
                return basePath + PropertiesUtils.geString("package.mapper").replace(".", "/");
            case "mapperXml":
                return PropertiesUtils.geString("path.mappers.xml");
            case "test":
                return PropertiesUtils.geString("path.java").replace("main", "test") + 
                       PropertiesUtils.geString("package.base").replace(".", "/") + "/test";
            default:
                return basePath;
        }
    }
}
