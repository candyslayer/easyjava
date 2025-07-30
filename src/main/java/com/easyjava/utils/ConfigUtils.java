package com.easyjava.utils;

import com.easyjava.manager.DynamicConfigManager;

/**
 * 配置工具类 - 统一配置访问接口
 * 优先使用动态配置，回退到原始配置
 * 
 * @author EasyJava
 * @since 2025-07-30
 */
public class ConfigUtils {
    
    /**
     * 获取配置值（优先动态配置）
     * 
     * @param key 配置键
     * @return 配置值
     */
    public static String getString(String key) {
        return DynamicConfigManager.getRuntimeConfig(key);
    }
    
    /**
     * 获取配置值（带默认值）
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static String getString(String key, String defaultValue) {
        String value = getString(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 获取数据库配置
     */
    public static class Database {
        public static String getDriverClassName() {
            return getString("spring.datasource.driver-class-name");
        }
        
        public static String getUrl() {
            return getString("spring.datasource.url");
        }
        
        public static String getUsername() {
            return getString("spring.datasource.username");
        }
        
        public static String getPassword() {
            return getString("spring.datasource.password");
        }
    }
    
    /**
     * 获取包配置
     */
    public static class Package {
        public static String getBase() {
            return getString("package.base");
        }
        
        public static String getPo() {
            return getString("package.po");
        }
        
        public static String getQuery() {
            return getString("package.query");
        }
        
        public static String getService() {
            return getString("package.service");
        }
        
        public static String getServiceImpl() {
            return getString("package.service.impl");
        }
        
        public static String getController() {
            return getString("package.controller");
        }
        
        public static String getMapper() {
            return getString("package.mapper");
        }
        
        /**
         * 获取完整包名
         */
        public static String getFullPackage(String subPackage) {
            String base = getBase();
            if (base == null || base.isEmpty()) {
                return subPackage;
            }
            if (subPackage == null || subPackage.isEmpty()) {
                return base;
            }
            return base + "." + subPackage;
        }
    }
    
    /**
     * 获取路径配置
     */
    public static class Path {
        public static String getBase() {
            return getString("path.base");
        }
        
        public static String getJava() {
            return getString("path.java");
        }
        
        public static String getResources() {
            return getString("path.resources");
        }
        
        public static String getMappersXml() {
            return getString("path.mappers.xml");
        }
        
        public static String getTest() {
            return getString("path.test");
        }
        
        /**
         * 获取包对应的完整路径
         */
        public static String getPackagePath(String packageName) {
            String javaPath = getJava();
            if (javaPath == null) {
                return null;
            }
            if (packageName == null || packageName.isEmpty()) {
                return javaPath;
            }
            return javaPath + java.io.File.separator + packageName.replace(".", java.io.File.separator);
        }
    }
    
    /**
     * 获取其他配置
     */
    public static class Other {
        public static String getAuthor() {
            return getString("auther.comment");
        }
        
        public static boolean isIgnoreTablePrefix() {
            return "true".equalsIgnoreCase(getString("ignore.table.prefix"));
        }
        
        public static String getQuerySuffix() {
            return getString("suffix.bean.param", "Query");
        }
        
        public static String getMapperSuffix() {
            return getString("suffix.mapper", "Mapper");
        }
        
        public static String getFuzzySuffix() {
            return getString("suffix.bean.param.fuzzy", "Fuzzy");
        }
    }
}
