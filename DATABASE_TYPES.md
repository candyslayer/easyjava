# EasyJava 数据库类型支持文档

## 🗃️ 支持的数据库类型映射

EasyJava增强版现在支持更多的SQL数据类型，能够智能映射到合适的Java类型，并支持多种主流数据库。

## 📊 类型映射表

### 整数类型
| SQL类型 | Java类型 | 默认值 | 支持数据库 |
|---------|----------|--------|------------|
| `tinyint` | `Integer` | `0` | MySQL, SQL Server |
| `tinyint unsigned` | `Integer` | `0` | MySQL |
| `smallint` | `Integer` | `0` | MySQL, PostgreSQL, SQL Server |
| `smallint unsigned` | `Integer` | `0` | MySQL |
| `mediumint` | `Integer` | `0` | MySQL |
| `mediumint unsigned` | `Integer` | `0` | MySQL |
| `int` | `Integer` | `0` | MySQL, PostgreSQL, SQL Server |
| `integer` | `Integer` | `0` | PostgreSQL, SQL Server |
| `int unsigned` | `Integer` | `0` | MySQL |
| `serial` | `Integer` | `0` | PostgreSQL |
| `smallserial` | `Integer` | `0` | PostgreSQL |

### 长整数类型
| SQL类型 | Java类型 | 默认值 | 支持数据库 |
|---------|----------|--------|------------|
| `bigint` | `Long` | `0L` | MySQL, PostgreSQL, SQL Server |
| `bigint unsigned` | `Long` | `0L` | MySQL |
| `bigserial` | `Long` | `0L` | PostgreSQL |

### 浮点数类型
| SQL类型 | Java类型 | 默认值 | 支持数据库 |
|---------|----------|--------|------------|
| `float` | `Float` | `0.0F` | MySQL, PostgreSQL, SQL Server |
| `real` | `Float` | `0.0F` | PostgreSQL, SQL Server |

### 双精度类型
| SQL类型 | Java类型 | 默认值 | 支持数据库 |
|---------|----------|--------|------------|
| `double` | `Double` | `0.0` | MySQL, SQL Server |
| `double precision` | `Double` | `0.0` | PostgreSQL |

### 精确数值类型
| SQL类型 | Java类型 | 默认值 | 导入包 | 支持数据库 |
|---------|----------|--------|--------|------------|
| `decimal` | `BigDecimal` | `BigDecimal.ZERO` | `java.math.BigDecimal` | MySQL, PostgreSQL, SQL Server |
| `numeric` | `BigDecimal` | `BigDecimal.ZERO` | `java.math.BigDecimal` | PostgreSQL, SQL Server |
| `money` | `BigDecimal` | `BigDecimal.ZERO` | `java.math.BigDecimal` | SQL Server |
| `smallmoney` | `BigDecimal` | `BigDecimal.ZERO` | `java.math.BigDecimal` | SQL Server |

### 字符串类型
| SQL类型 | Java类型 | 默认值 | 支持数据库 |
|---------|----------|--------|------------|
| `varchar` | `String` | `null` | MySQL, PostgreSQL, SQL Server |
| `varchar2` | `String` | `null` | Oracle |
| `char` | `String` | `null` | MySQL, PostgreSQL, SQL Server |
| `character` | `String` | `null` | PostgreSQL |
| `character varying` | `String` | `null` | PostgreSQL |
| `nvarchar` | `String` | `null` | SQL Server |
| `nchar` | `String` | `null` | SQL Server |
| `text` | `String` | `null` | MySQL, PostgreSQL |
| `tinytext` | `String` | `null` | MySQL |
| `mediumtext` | `String` | `null` | MySQL |
| `longtext` | `String` | `null` | MySQL |
| `ntext` | `String` | `null` | SQL Server |
| `clob` | `String` | `null` | Oracle |
| `nclob` | `String` | `null` | Oracle |
| `longvarchar` | `String` | `null` | SQL Server |

### 日期时间类型
| SQL类型 | Java类型 | 默认值 | 导入包 | 支持数据库 |
|---------|----------|--------|--------|------------|
| `date` | `Date` | `new Date()` | `java.util.Date` | MySQL, PostgreSQL, SQL Server |
| `datetime` | `Date` | `new Date()` | `java.util.Date` | MySQL, SQL Server |
| `timestamp` | `Date` | `new Date()` | `java.util.Date` | MySQL, PostgreSQL |
| `timestamp with time zone` | `Date` | `new Date()` | `java.util.Date` | PostgreSQL |
| `timestamptz` | `Date` | `new Date()` | `java.util.Date` | PostgreSQL |

### 时间类型
| SQL类型 | Java类型 | 默认值 | 导入包 | 支持数据库 |
|---------|----------|--------|--------|------------|
| `time` | `Time` | `new Time(System.currentTimeMillis())` | `java.sql.Time` | MySQL, PostgreSQL, SQL Server |
| `time with time zone` | `Time` | `new Time(System.currentTimeMillis())` | `java.sql.Time` | PostgreSQL |
| `timetz` | `Time` | `new Time(System.currentTimeMillis())` | `java.sql.Time` | PostgreSQL |

### 布尔类型
| SQL类型 | Java类型 | 默认值 | 支持数据库 |
|---------|----------|--------|------------|
| `boolean` | `Boolean` | `false` | PostgreSQL |
| `bool` | `Boolean` | `false` | PostgreSQL |
| `bit` | `Boolean` | `false` | MySQL, SQL Server |

### 二进制数据类型
| SQL类型 | Java类型 | 默认值 | 支持数据库 |
|---------|----------|--------|------------|
| `binary` | `byte[]` | `null` | SQL Server |
| `varbinary` | `byte[]` | `null` | SQL Server |
| `blob` | `byte[]` | `null` | MySQL |
| `tinyblob` | `byte[]` | `null` | MySQL |
| `mediumblob` | `byte[]` | `null` | MySQL |
| `longblob` | `byte[]` | `null` | MySQL |
| `bytea` | `byte[]` | `null` | PostgreSQL |
| `raw` | `byte[]` | `null` | Oracle |
| `long raw` | `byte[]` | `null` | Oracle |
| `image` | `byte[]` | `null` | SQL Server |

### 特殊类型
| SQL类型 | Java类型 | 默认值 | 支持数据库 | 说明 |
|---------|----------|--------|------------|------|
| `json` | `String` | `null` | MySQL, PostgreSQL | JSON数据 |
| `jsonb` | `String` | `null` | PostgreSQL | 二进制JSON |
| `uuid` | `String` | `null` | PostgreSQL | UUID类型 |
| `enum` | `String` | `null` | MySQL | 枚举类型 |
| `geometry` | `String` | `null` | MySQL, PostgreSQL | 几何数据 |
| `geography` | `String` | `null` | PostgreSQL | 地理数据 |
| `point` | `String` | `null` | MySQL, PostgreSQL | 点坐标 |
| `linestring` | `String` | `null` | MySQL, PostgreSQL | 线段 |
| `polygon` | `String` | `null` | MySQL, PostgreSQL | 多边形 |
| `array` | `String` | `null` | PostgreSQL | 数组类型 |

## 🚀 新增功能特性

### 1. 智能类型识别
- **精确匹配**：优先使用精确的类型映射
- **模糊匹配**：对于未知类型，使用模糊匹配算法
- **默认回退**：无法识别的类型默认映射为String

### 2. 类型检查工具
```java
// 检查是否为数值类型
boolean isNumeric = SqlTypeMapper.isNumericType("decimal(10,2)");

// 检查是否为日期时间类型
boolean isDateTime = SqlTypeMapper.isDateTimeType("timestamp");

// 检查是否为字符串类型
boolean isString = SqlTypeMapper.isStringType("varchar(255)");

// 检查是否为布尔类型
boolean isBoolean = SqlTypeMapper.isBooleanType("boolean");
```

### 3. 增强的测试数据生成
根据字段名和类型智能生成测试数据：
- **邮箱字段**：`test@example.com`
- **手机字段**：`13800138000`
- **URL字段**：`http://example.com`
- **JSON字段**：`{"key":"value"}`
- **UUID字段**：`123e4567-e89b-12d3-a456-426614174000`

### 4. 类型安全检查
```java
// 检查类型是否被支持
boolean isSupported = SqlTypeMapper.isSupportedType("varchar(255)");

// 获取所有支持的类型
String[] supportedTypes = SqlTypeMapper.getSupportedSqlTypes();
```

## 🗄️ 数据库兼容性

### MySQL 8.0+
- ✅ 完全支持所有MySQL数据类型
- ✅ 支持unsigned修饰符
- ✅ 支持JSON类型
- ✅ 支持几何类型（需要安装对应扩展）

### PostgreSQL 12+
- ✅ 完全支持PostgreSQL数据类型
- ✅ 支持时区相关类型
- ✅ 支持JSON/JSONB类型
- ✅ 支持UUID类型
- ✅ 支持数组类型
- ✅ 支持几何类型

### SQL Server 2017+
- ✅ 支持主要的SQL Server数据类型
- ✅ 支持Unicode类型（nvarchar, nchar等）
- ✅ 支持money类型
- ✅ 支持image类型

### Oracle 19c+
- ✅ 支持基本的Oracle数据类型
- ✅ 支持CLOB/BLOB类型
- ✅ 支持RAW类型
- ✅ 支持VARCHAR2类型

## 📝 使用示例

### 创建支持多种类型的表
```sql
-- MySQL示例
CREATE TABLE `user_profile` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `age` tinyint unsigned DEFAULT NULL,
  `balance` decimal(10,2) DEFAULT '0.00',
  `is_active` boolean DEFAULT true,
  `avatar` longblob,
  `preferences` json,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
);

-- PostgreSQL示例
CREATE TABLE user_profile (
  id bigserial PRIMARY KEY,
  username varchar(50) NOT NULL,
  email varchar(100),
  age smallint,
  balance numeric(10,2) DEFAULT 0.00,
  is_active boolean DEFAULT true,
  avatar bytea,
  preferences jsonb,
  location geometry(POINT),
  tags text[],
  user_uuid uuid DEFAULT gen_random_uuid(),
  created_at timestamptz DEFAULT now(),
  updated_at timestamp
);
```

### 生成的Java实体类示例
```java
public class UserProfile implements Serializable {
    
    private Long id;
    private String username;
    private String email;
    private Integer age;
    private BigDecimal balance;
    private Boolean isActive;
    private byte[] avatar;
    private String preferences;  // JSON类型映射为String
    private String location;     // 几何类型映射为String
    private String tags;         // 数组类型映射为String
    private String userUuid;     // UUID类型映射为String
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
    
    // getters and setters...
}
```

### 生成的测试代码示例
```java
private UserProfile createTestEntity() {
    UserProfile entity = new UserProfile();
    entity.setUsername("test_username");
    entity.setEmail("test@example.com");        // 智能识别邮箱字段
    entity.setAge(1);
    entity.setBalance(new BigDecimal("1.00"));  // BigDecimal类型
    entity.setIsActive(true);                   // Boolean类型
    entity.setAvatar("test_bytes".getBytes());  // 二进制类型
    entity.setPreferences("{\"key\":\"value\"}"); // JSON类型
    entity.setLocation("test_location");
    entity.setTags("test_tags");
    entity.setUserUuid("123e4567-e89b-12d3-a456-426614174000"); // UUID格式
    entity.setCreatedAt(new Date());
    entity.setUpdatedAt(new Date());
    return entity;
}
```

## 🔧 配置建议

### 1. 数据库连接配置
```properties
# MySQL配置
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/your_db?useUnicode=true&characterEncoding=utf8mb4

# PostgreSQL配置
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db

# SQL Server配置
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=your_db

# Oracle配置
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:your_db
```

### 2. 类型映射自定义
如果需要自定义类型映射，可以继承SqlTypeMapper类：

```java
public class CustomSqlTypeMapper extends SqlTypeMapper {
    
    public static JavaTypeInfo getCustomJavaTypeInfo(String sqlType) {
        // 自定义映射逻辑
        if ("your_custom_type".equals(sqlType.toLowerCase())) {
            return new JavaTypeInfo("YourCustomType", 
                                  "com.yourcompany.YourCustomType", 
                                  "new YourCustomType()", 
                                  true);
        }
        
        return SqlTypeMapper.getJavaTypeInfo(sqlType);
    }
}
```

## ⚠️ 注意事项

1. **类型精度**：对于decimal/numeric类型，精度信息会在SQL类型中保留，但Java映射统一为BigDecimal
2. **时区处理**：时间类型统一映射为java.util.Date，时区信息需要在应用层处理
3. **JSON类型**：JSON类型映射为String，需要在业务层进行JSON序列化/反序列化
4. **二进制类型**：大二进制对象建议存储文件路径而不是直接存储二进制数据
5. **数组类型**：PostgreSQL数组类型映射为String，建议使用JSON格式存储复杂数据结构

## 🚀 后续计划

1. **更多数据库支持**：计划支持更多数据库（如SQLite、H2等）
2. **自定义类型映射**：支持用户自定义类型映射规则
3. **类型验证**：增加运行时类型验证功能
4. **性能优化**：优化类型映射的性能，支持缓存机制

---

**文档版本**：v2.0  
**更新时间**：2025-07-30  
**适用版本**：EasyJava 增强版
