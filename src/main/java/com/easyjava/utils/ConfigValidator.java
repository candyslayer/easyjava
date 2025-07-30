package com.easyjava.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easyjava.bean.Constants;

/**
 * 配置验证工具类
 * 用于验证代码生成器的各项配置是否正确
 * 
 * @author 唐伟
 * @since 2025-07-30
 */
public class ConfigValidator {
    
    private static final Logger log = LoggerFactory.getLogger(ConfigValidator.class);
    
    /**
     * 验证配置结果
     */
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;
        
        public ValidationResult() {
            this.valid = true;
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
        }
        
        public void addError(String error) {
            this.errors.add(error);
            this.valid = false;
            log.error("配置验证错误: {}", error);
        }
        
        public void addWarning(String warning) {
            this.warnings.add(warning);
            log.warn("配置验证警告: {}", warning);
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        
        public void printResult() {
            if (valid) {
                log.info("✅ 配置验证通过");
            } else {
                log.error("❌ 配置验证失败");
                errors.forEach(error -> log.error("  - {}", error));
            }
            
            if (!warnings.isEmpty()) {
                log.warn("⚠️ 配置警告:");
                warnings.forEach(warning -> log.warn("  - {}", warning));
            }
        }
    }
    
    /**
     * 验证所有配置
     * 
     * @return 验证结果
     */
    public static ValidationResult validateAllConfig() {
        log.info("开始验证配置...");
        
        ValidationResult result = new ValidationResult();
        
        // 验证数据库连接
        validateDatabaseConnection(result);
        
        // 验证路径配置
        validatePathConfig(result);
        
        // 验证包名配置
        validatePackageConfig(result);
        
        // 验证生成配置
        validateGenerateConfig(result);
        
        result.printResult();
        return result;
    }
    
    /**
     * 验证数据库连接
     */
    private static void validateDatabaseConnection(ValidationResult result) {
        try {
            String url = PropertiesUtils.geString("spring.datasource.url");
            String username = PropertiesUtils.geString("spring.datasource.username");
            String password = PropertiesUtils.geString("spring.datasource.password");
            String driverClassName = PropertiesUtils.geString("spring.datasource.driver-class-name");
            
            // 检查配置是否为空
            if (StringUtils.isEmpty(url)) {
                result.addError("数据库URL未配置 (spring.datasource.url)");
                return;
            }
            
            if (StringUtils.isEmpty(username)) {
                result.addError("数据库用户名未配置 (spring.datasource.username)");
                return;
            }
            
            if (StringUtils.isEmpty(driverClassName)) {
                result.addError("数据库驱动未配置 (spring.datasource.driver-class-name)");
                return;
            }
            
            // 尝试连接数据库
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                if (conn != null && !conn.isClosed()) {
                    log.info("✅ 数据库连接验证成功");
                } else {
                    result.addError("数据库连接失败：连接为空或已关闭");
                }
            }
            
        } catch (SQLException e) {
            result.addError("数据库连接失败: " + e.getMessage());
        } catch (Exception e) {
            result.addError("数据库配置验证异常: " + e.getMessage());
        }
    }
    
    /**
     * 验证路径配置
     */
    private static void validatePathConfig(ValidationResult result) {
        String pathBase = PropertiesUtils.geString("path.base");
        
        if (StringUtils.isEmpty(pathBase)) {
            result.addError("基础路径未配置 (path.base)");
            return;
        }
        
        File baseDir = new File(pathBase);
        if (!baseDir.exists()) {
            result.addWarning("基础路径不存在，将自动创建: " + pathBase);
            if (!baseDir.mkdirs()) {
                result.addError("无法创建基础路径: " + pathBase);
            }
        }
        
        if (!baseDir.canWrite()) {
            result.addError("基础路径无写入权限: " + pathBase);
        }
        
        log.info("✅ 路径配置验证完成");
    }
    
    /**
     * 验证包名配置
     */
    private static void validatePackageConfig(ValidationResult result) {
        String packageBase = PropertiesUtils.geString("package.base");
        
        if (StringUtils.isEmpty(packageBase)) {
            result.addError("基础包名未配置 (package.base)");
            return;
        }
        
        // 验证包名格式
        if (!isValidPackageName(packageBase)) {
            result.addError("基础包名格式不正确: " + packageBase);
        }
        
        // 验证子包配置
        String[] subPackages = {
            "package.po", "package.vo", "package.param", "package.utils",
            "package.enums", "package.mapper", "package.service", 
            "package.service.impl", "package.controller", "package.exception"
        };
        
        for (String subPkg : subPackages) {
            String value = PropertiesUtils.geString(subPkg);
            if (StringUtils.isEmpty(value)) {
                result.addWarning("子包配置为空: " + subPkg);
            }
        }
        
        log.info("✅ 包名配置验证完成");
    }
    
    /**
     * 验证生成配置
     */
    private static void validateGenerateConfig(ValidationResult result) {
        // 验证作者配置
        String author = PropertiesUtils.geString("auther.comment");
        if (StringUtils.isEmpty(author)) {
            result.addWarning("作者信息未配置 (auther.comment)");
        }
        
        // 验证后缀配置
        String[] suffixConfigs = {
            "suffix.bean.param", "suffix.bean.param.fuzzy",
            "suffix.bean.param.time.start", "suffix.bean.param.time.end",
            "suffix.mapper"
        };
        
        for (String suffix : suffixConfigs) {
            String value = PropertiesUtils.geString(suffix);
            if (StringUtils.isEmpty(value)) {
                result.addWarning("后缀配置为空: " + suffix);
            }
        }
        
        log.info("✅ 生成配置验证完成");
    }
    
    /**
     * 验证包名格式是否正确
     */
    private static boolean isValidPackageName(String packageName) {
        if (StringUtils.isEmpty(packageName)) {
            return false;
        }
        
        String[] parts = packageName.split("\\.");
        for (String part : parts) {
            if (StringUtils.isEmpty(part) || !part.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 快速验证关键配置
     * 
     * @return 是否通过基本验证
     */
    public static boolean quickValidate() {
        try {
            // 检查关键配置
            String url = PropertiesUtils.geString("spring.datasource.url");
            String packageBase = PropertiesUtils.geString("package.base");
            String pathBase = PropertiesUtils.geString("path.base");
            
            return !StringUtils.isEmpty(url) && 
                   !StringUtils.isEmpty(packageBase) && 
                   !StringUtils.isEmpty(pathBase);
        } catch (Exception e) {
            log.error("快速验证失败", e);
            return false;
        }
    }
}
