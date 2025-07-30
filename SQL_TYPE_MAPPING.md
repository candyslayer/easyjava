# SQL 类型到 Java 类型映射表

## 支持的 SQL 类型及其 Java 映射

### 1. 整数类型 → Integer
- `int`
- `tinyint`
- `smallint`
- `mediumint`
- `int unsigned`
- `tinyint unsigned`
- `smallint unsigned`
- `mediumint unsigned`

### 2. 长整数类型 → Long
- `bigint`
- `bigint unsigned`

### 3. 浮点数类型 → BigDecimal
- `decimal`
- `double`
- `float`
- `numeric`

### 4. 日期时间类型 → Date
- `datetime`
- `timestamp`
- `date`

### 5. 时间类型 → String
- `time`

### 6. 字符串类型 → String
- `varchar`
- `char`
- `text`
- `mediumtext`
- `longtext`
- `tinytext`

### 7. 二进制类型 → byte[]
- `blob`
- `mediumblob`
- `longblob`
- `tinyblob`
- `binary`
- `varbinary`

### 8. 布尔类型 → Boolean
- `boolean`
- `bool`
- `bit`

### 9. JSON 类型 → String
- `json`

### 10. 枚举类型 → String
- `enum`

### 11. 集合类型 → String
- `set`

### 12. 几何类型 → String
- `geometry`
- `point`
- `linestring`
- `polygon`
- `multipoint`
- `multilinestring`
- `multipolygon`
- `geometrycollection`

## 扩展查询字段生成规则

### 支持模糊查询的类型
- 字符串类型（varchar、char、text等）
- JSON 类型
- 枚举类型
- 集合类型

这些类型会生成带 `Fuzzy` 后缀的扩展查询字段，用于模糊查询。

### 支持时间范围查询的类型
- 日期时间类型（datetime、timestamp、date）

这些类型会生成带 `Start` 和 `End` 后缀的扩展查询字段，用于时间范围查询。

### 不生成扩展查询字段的类型
- 二进制类型（blob、binary等）
- 几何类型
- 布尔类型

这些类型通常不适合模糊查询或范围查询，因此不生成扩展查询字段。

## 分表支持

### 时间分表策略
- datetime、timestamp、date、time 类型字段
- 优先选择包含 `create`、`time`、`date` 关键字的字段

### 哈希分表策略
- 字符串类型字段
- JSON、枚举、集合类型字段
- ID 类型的整数字段

### 范围分表策略
- 非 ID 类型的整数字段

## 使用示例

```sql
-- 支持的表结构示例
CREATE TABLE example_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,           -- Long 类型
    name VARCHAR(100) NOT NULL,                     -- String 类型，支持模糊查询
    age INT,                                        -- Integer 类型
    salary DECIMAL(10,2),                           -- BigDecimal 类型
    is_active BOOLEAN DEFAULT TRUE,                 -- Boolean 类型
    created_at DATETIME,                            -- Date 类型，支持时间范围查询
    profile_data JSON,                              -- String 类型，支持模糊查询
    status ENUM('active', 'inactive'),              -- String 类型，支持模糊查询
    tags SET('tag1', 'tag2', 'tag3'),              -- String 类型，支持模糊查询
    avatar MEDIUMBLOB,                              -- byte[] 类型，不支持扩展查询
    location POINT                                  -- String 类型，不支持扩展查询
);
```

生成的查询参数类将包含：
- `name` + `nameFuzzy`（模糊查询）
- `createdAt` + `createdAtStart` + `createdAtEnd`（时间范围查询）
- `profileData` + `profileDataFuzzy`（JSON 模糊查询）
- `status` + `statusFuzzy`（枚举模糊查询）
- `tags` + `tagsFuzzy`（集合模糊查询）

其他字段只生成基本的精确查询条件。
