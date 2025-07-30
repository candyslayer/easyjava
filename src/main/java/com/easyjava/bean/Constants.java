package com.easyjava.bean;

import com.easyjava.utils.PropertiesUtils;

public class Constants {

    public static String AUTHER_COMMENT;

    public static Boolean IGNORE_TABLE_PREFIX;

    public static String SUFFIX_BEAN_PARAM;
    public static String SUFFIX_BEAN_PARAM_FUZZY;
    public static String SUFFIX_BEAN_PARAM_TIME_START;
    public static String SUFFIX_BEAN_PARAM_TIME_END;
    public static String SUFFIX_MAPPER;

    private static String PATH_BASE;
    public static String PATH_PO;
    public static String PATH_VO;
    public static String PATH_UTILS;
    public static String PATH_ENUMS;
    public static String PATH_PARAM;
    public static String PATH_MAPPER;
    public static String PATH_MAPPER_XML;
    public static String PATH_SERVICE;
    public static String PATH_SERVICE_IMPL;
    public static String PATH_EXCEPTION;
    public static String PATH_CONTROLLER;
    public static String PATH_EXCEPTION_STRATEGY;

    private static String PATH_JAVA = "java";
    public static String PATH_RESOURCES = "resources";

    private static String PACKAGE_BASE;
    public static String PACKAGE_PO;
    public static String PACKAGE_VO;
    public static String PACKAGE_PARAM;
    public static String PACKAGE_UTILS;
    public static String PACKAGE_ENUMS;
    public static String PACKAGE_MAPPER;
    public static String PACKAGE_SERVICE;
    public static String PACKAGE_SERVICE_IMPL;
    public static String PACKAGE_EXCEPTION;
    public static String PACKAGE_EXCEPTION_STRATEGY;
    public static String PACKAGE_CONTROLLER;

    public static String IGNORE_BEAN_TOJSON_FIELD;
    public static String IGNORE_BEAN_TOJSON_EXPRESSION;
    public static String IGNORE_BEAN_TOJSON_CLASS;

    public static String BEAN_DATE_SERIALIZATION;
    public static String BEAN_DATE_SERIALIZATION_CLASS;

    public static String BEAN_DATA_DESERIALIZATIO;
    public static String BEAN_DATE_DESERIALIZATIO_CLASS;

    static {

        // 生成作者名
        AUTHER_COMMENT = PropertiesUtils.geString("auther.comment");

        // 构建表信息
        IGNORE_TABLE_PREFIX = Boolean.valueOf(PropertiesUtils.geString(""));

        // 后缀信息
        SUFFIX_BEAN_PARAM = PropertiesUtils.geString("suffix.bean.param");
        SUFFIX_BEAN_PARAM_FUZZY = PropertiesUtils.geString("suffix.bean.param.fuzzy");
        SUFFIX_BEAN_PARAM_TIME_START = PropertiesUtils.geString("suffix.bean.param.time.start");
        SUFFIX_BEAN_PARAM_TIME_END = PropertiesUtils.geString("suffix.bean.param.time.end");
        SUFFIX_MAPPER = PropertiesUtils.geString("suffix.mapper");

        // 不序列化
        IGNORE_BEAN_TOJSON_FIELD = PropertiesUtils.geString("ignore.bean.tojson.field");
        IGNORE_BEAN_TOJSON_EXPRESSION = PropertiesUtils.geString("ignore.bean.tojson.expression");
        IGNORE_BEAN_TOJSON_CLASS = PropertiesUtils.geString("ignore.bean.tojson.class");

        // 日期序列化
        BEAN_DATE_SERIALIZATION = PropertiesUtils.geString("bean.date.serialization");
        BEAN_DATE_SERIALIZATION_CLASS = PropertiesUtils.geString("bean.date.serialization.class");

        // 日期反序列化
        BEAN_DATA_DESERIALIZATIO = PropertiesUtils.geString("bean.data.deserializatio");
        BEAN_DATE_DESERIALIZATIO_CLASS = PropertiesUtils.geString("bean.date.deserializatio.class");

        // 生成类的包位置
        PACKAGE_BASE = PropertiesUtils.geString("package.base");
        // 生成所需添加包名
        PACKAGE_PO = PACKAGE_BASE + "." + PropertiesUtils.geString("package.po");
        PACKAGE_VO = PACKAGE_BASE + "." + PropertiesUtils.geString("package.vo");
        PACKAGE_PARAM = PACKAGE_BASE + "." + PropertiesUtils.geString("package.param");
        PACKAGE_UTILS = PACKAGE_BASE + "." + PropertiesUtils.geString("package.utils");
        PACKAGE_ENUMS = PACKAGE_BASE + "." + PropertiesUtils.geString("package.enums");
        PACKAGE_MAPPER = PACKAGE_BASE + "." + PropertiesUtils.geString("package.mapper");
        PACKAGE_SERVICE = PACKAGE_BASE + "." + PropertiesUtils.geString("package.service");
        PACKAGE_SERVICE_IMPL = PACKAGE_BASE + "." + PropertiesUtils.geString("package.service.impl");
        PACKAGE_CONTROLLER = PACKAGE_BASE + "." + PropertiesUtils.geString("package.controller");
        PACKAGE_EXCEPTION = PACKAGE_BASE + "." + PropertiesUtils.geString("package.exception");
        PACKAGE_EXCEPTION_STRATEGY = PACKAGE_BASE + "." + PropertiesUtils.geString("package.exception.strategy");

        // 生成基本路径
        PATH_BASE = (PropertiesUtils.geString("path.base") + "/" + PATH_JAVA + "/" + PACKAGE_BASE).replace(".", "/");
        // 生成类路径
        PATH_PO = (PATH_BASE + "/" + PropertiesUtils.geString("package.po")).replace(".", "/");
        PATH_VO = (PATH_BASE + "/" + PropertiesUtils.geString("package.vo")).replace(".", "/");
        PATH_UTILS = (PATH_BASE + "/" + PropertiesUtils.geString("package.utils")).replace(".", "/");
        PATH_ENUMS = (PATH_BASE + "/" + PropertiesUtils.geString("package.enums")).replace(".", "/");
        PATH_PARAM = (PATH_BASE + "/" + PropertiesUtils.geString("package.param")).replace(".", "/");
        PATH_MAPPER = (PATH_BASE + "/" + PropertiesUtils.geString("package.mapper")).replace(".", "/");
        PATH_SERVICE = (PATH_BASE + "/" + PropertiesUtils.geString("package.service")).replace(".", "/");
        PATH_SERVICE_IMPL = (PATH_BASE + "/" + PropertiesUtils.geString("package.service.impl")).replace(".",
                "/");
        PATH_CONTROLLER = (PATH_BASE + "/" + PropertiesUtils.geString("package.controller")).replace(".", "/");
        PATH_EXCEPTION = (PATH_BASE + "/" + PropertiesUtils.geString("package.exception")).replace(".", "/");
        // 异常策略路径
        PATH_EXCEPTION_STRATEGY = (PATH_BASE + "/"
                + PropertiesUtils.geString("package.exception.strategy").replace(".", "/"));

        PATH_MAPPER_XML = (PropertiesUtils.geString("path.base") + "/" + PATH_RESOURCES + "/" + PACKAGE_MAPPER)
                .replace(".", "/");
    }

    // sql类型映射 - 扩展版本支持更多数据类型
    
    // 日期时间类型
    public final static String[] SQL_DATE_TIME_TYPES = new String[] { 
        "datetime", "timestamp", "timestamp with time zone", "timestamptz" 
    };

    // 日期类型
    public final static String[] SQL_DATE_TYPE = new String[] { "date" };
    
    // 时间类型
    public final static String[] SQL_TIME_TYPE = new String[] { 
        "time", "time with time zone", "timetz" 
    };

    // 布尔类型
    public final static String[] SQL_BOOLEAN_TYPE = new String[] { 
        "boolean", "bool", "bit" 
    };

    // 小整数类型
    public final static String[] SQL_INTEGER_TYPE = new String[] { 
        "int", "integer", "tinyint", "smallint", "mediumint", 
        "int unsigned", "tinyint unsigned", "smallint unsigned", "mediumint unsigned",
        "serial", "smallserial"
    };

    // 长整数类型
    public final static String[] SQL_LONG_TYPE = new String[] { 
        "bigint", "bigint unsigned", "bigserial"
    };

    // 浮点数类型
    public final static String[] SQL_FLOAT_TYPE = new String[] { 
        "float", "real"
    };

    // 双精度类型
    public final static String[] SQL_DOUBLE_TYPE = new String[] { 
        "double", "double precision"
    };

    // 精确数值类型
    public final static String[] SQL_DECIMAL_TYPE = new String[] { 
        "decimal", "numeric", "money", "smallmoney"
    };

    // 字符串类型
    public final static String[] SQL_STRING_TYPE = new String[] { 
        "varchar", "char", "text", "mediumtext", "longtext", "tinytext",
        "character", "character varying", "varchar2", "nvarchar", "nchar", "ntext",
        "clob", "nclob", "longvarchar"
    };

    // 二进制数据类型
    public final static String[] SQL_BINARY_TYPE = new String[] { 
        "binary", "varbinary", "blob", "tinyblob", "mediumblob", "longblob",
        "bytea", "raw", "long raw", "image"
    };

    // JSON类型
    public final static String[] SQL_JSON_TYPE = new String[] { 
        "json", "jsonb"
    };

    // UUID类型
    public final static String[] SQL_UUID_TYPE = new String[] { 
        "uuid"
    };

    // 枚举类型
    public final static String[] SQL_ENUM_TYPE = new String[] { 
        "enum"
    };

    // 几何类型（PostGIS等）
    public final static String[] SQL_GEOMETRY_TYPE = new String[] { 
        "geometry", "geography", "point", "linestring", "polygon"
    };

    // 数组类型（PostgreSQL）
    public final static String[] SQL_ARRAY_TYPE = new String[] { 
        "array"
    };

    public static void main(String[] args) {

        System.out.println(PACKAGE_BASE);

        System.out.println(PACKAGE_SERVICE);
        System.out.println(PATH_SERVICE);

        System.out.println(PACKAGE_SERVICE_IMPL);
        System.out.println(PATH_SERVICE_IMPL);

        System.out.println(PACKAGE_VO);
        System.out.println(PATH_VO);

        System.out.println(PACKAGE_CONTROLLER);
        System.out.println(PATH_CONTROLLER);

        System.out.println(PACKAGE_EXCEPTION);
        System.out.println(PATH_EXCEPTION);
    }
}
