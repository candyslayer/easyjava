package com.easyjava.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maven插件专用配置管理器
 * 完全基于系统属性，不依赖任何配置文件
 */
public class PluginConfigManager {
    
    private static final Map<String, String> configMap = new ConcurrentHashMap<>();
    
    /**
     * 设置配置值
     */
    public static void setConfig(String key, String value) {
        if (value != null) {
            configMap.put(key, value);
        }
    }
    
    /**
     * 获取配置值，优先从内存配置获取，然后从系统属性获取
     */
    public static String getConfig(String key) {
        // 优先从内存配置获取
        String value = configMap.get(key);
        if (value != null) {
            return value;
        }
        
        // 然后从系统属性获取
        return System.getProperty(key);
    }
    
    /**
     * 获取配置值，如果不存在则返回默认值
     */
    public static String getConfig(String key, String defaultValue) {
        String value = getConfig(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 清空所有配置
     */
    public static void clearConfig() {
        configMap.clear();
    }
    
    /**
     * 初始化默认配置（Maven插件专用）
     */
    public static void initDefaultConfig() {
        // 后缀配置
        setConfig("suffix.bean.param", "Query");
        setConfig("suffix.bean.param.fuzzy", "Fuzzy");
        setConfig("suffix.bean.param.time.start", "Start");
        setConfig("suffix.bean.param.time.end", "End");
        setConfig("suffix.mapper", "Mapper");
        
        // 包名配置
        setConfig("package.po", "entity.po");
        setConfig("package.vo", "entity.vo");
        setConfig("package.param", "entity.query");
        setConfig("package.utils", "utils");
        setConfig("package.enums", "enums");
        setConfig("package.mapper", "mapper");
        setConfig("package.service", "service");
        setConfig("package.service.impl", "service.impl");
        setConfig("package.exception", "exception");
        setConfig("package.controller", "controller");
        setConfig("package.exception.strategy", "exception.strategy");
        
        // JSON忽略配置
        setConfig("ignore.bean.tojson.field", "password");
        setConfig("ignore.bean.tojson.expression", "@JsonIgnore");
        setConfig("ignore.bean.tojson.class", "import com.fasterxml.jackson.annotation.JsonIgnore;");
        
        // 日期序列化配置
        setConfig("bean.date.serialization", "@JsonFormat(pattern = \"%s\",timezone = \"GMT+8\")");
        setConfig("bean.date.serialization.class", "import com.fasterxml.jackson.annotation.JsonFormat;");
        
        // 日期反序列化配置
        setConfig("bean.data.deserializatio", "@DateTimeFormat(pattern = \"%s\")");
        setConfig("bean.date.deserializatio.class", "import org.springframework.format.annotation.DateTimeFormat;");
    }
}
