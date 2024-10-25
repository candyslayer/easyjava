package com.easyjava.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JsonUtils {
    public static String convertObject2Json(Object object) {
        if (object == null) {
            return null;
        }
        return JSON.toJSONString(object, SerializerFeature.DisableCircularReferenceDetect);
    }
}
