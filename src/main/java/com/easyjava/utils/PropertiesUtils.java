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
        return properMap.get(key);
    }

    
}
