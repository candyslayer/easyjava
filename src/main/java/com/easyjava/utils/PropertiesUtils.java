package com.easyjava.utils;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class PropertiesUtils {
    private static Properties props = new Properties();
    private static Map<String, String> properMap = new ConcurrentHashMap<>();
    private static final String CONFIG_FILE = "application.properties";

    static {
        loadPropertiesFromFile();
    }

    /**
     * 从配置文件加载属性
     */
    private static void loadPropertiesFromFile() {
        InputStream is = null;
        try {
            is = PropertiesUtils.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (is != null) {
                props.load(new InputStreamReader(is, "utf-8"));

                Iterator<Object> iterator = props.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    properMap.put(key, props.getProperty(key));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取字符串属性值
     */
    public static String geString(String key) {
        return properMap.get(key);
    }
    
    /**
     * 获取字符串属性值（修正方法名）
     */
    public static String getString(String key) {
        return properMap.get(key);
    }
    
    /**
     * 获取字符串属性值，带默认值
     */
    public static String getString(String key, String defaultValue) {
        String value = properMap.get(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 获取整数属性值
     */
    public static int getInt(String key) {
        String value = properMap.get(key);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                // 忽略格式错误，返回默认值
            }
        }
        return 0;
    }
    
    /**
     * 获取整数属性值，带默认值
     */
    public static int getInt(String key, int defaultValue) {
        String value = properMap.get(key);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                // 忽略格式错误，返回默认值
            }
        }
        return defaultValue;
    }
    
    /**
     * 获取布尔属性值
     */
    public static boolean getBoolean(String key) {
        String value = properMap.get(key);
        if (value != null && !value.trim().isEmpty()) {
            return "true".equalsIgnoreCase(value.trim()) || "yes".equalsIgnoreCase(value.trim()) || "1".equals(value.trim());
        }
        return false;
    }
    
    /**
     * 获取布尔属性值，带默认值
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properMap.get(key);
        if (value != null && !value.trim().isEmpty()) {
            return "true".equalsIgnoreCase(value.trim()) || "yes".equalsIgnoreCase(value.trim()) || "1".equals(value.trim());
        }
        return defaultValue;
    }
    
    /**
     * 加载Properties对象
     */
    public static Properties loadProperties() {
        Properties newProps = new Properties();
        InputStream is = null;
        try {
            is = PropertiesUtils.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (is != null) {
                newProps.load(new InputStreamReader(is, "utf-8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return newProps;
    }
    
    /**
     * 保存Properties到文件
     */
    public static void saveProperties(Properties properties) {
        try {
            // 获取配置文件路径
            String configPath = PropertiesUtils.class.getClassLoader().getResource(CONFIG_FILE).getPath();
            
            // 使用FileOutputStream写入文件
            try (FileOutputStream fos = new FileOutputStream(configPath);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8")) {
                
                properties.store(osw, "Updated by DynamicShardingManager - " + new java.util.Date());
                
                // 重新加载到内存
                properMap.clear();
                properties.forEach((key, value) -> properMap.put(key.toString(), value.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 重新加载配置文件
     */
    public static void reload() {
        properMap.clear();
        props.clear();
        loadPropertiesFromFile();
    }
}
