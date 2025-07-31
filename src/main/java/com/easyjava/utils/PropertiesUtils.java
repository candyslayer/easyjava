package com.easyjava.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class PropertiesUtils {
    private static Properties props = new Properties();
    private static Map<String, String> properMap = new ConcurrentHashMap<>();

    static {
        InputStream is = null;
        try {
            is = PropertiesUtils.class.getClassLoader().getResourceAsStream("application.properties");
            props.load(new InputStreamReader(is, "utf-8"));

            Iterator<Object> iterator = props.keySet().iterator();

            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                properMap.put(key, props.getProperty(key));

            }
        } catch (Exception e) {
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

    public static String geString(String key) {
        // 优先从系统属性读取，这样Maven插件设置的参数就会覆盖配置文件中的值
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }
        // 如果系统属性中没有，则从配置文件读取
        return properMap.get(key);
    }

    
}
