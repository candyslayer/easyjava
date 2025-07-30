# 跨表分表功能使用指南

## 功能概述

跨表分表功能允许你根据其他表的字段值来对当前表进行分表。这在实际业务中非常有用，比如：
- 根据用户ID来分用户消息表
- 根据订单的用户ID来分订单详情表
- 根据用户ID来分用户积分记录表

## 配置方式

### 基本配置格式
```properties
# 启用跨表分表的表
sharding.tables=target_table

# 基本分表配置
target_table.sharding.field=reference_field
target_table.sharding.strategy=hash

# 跨表分表配置
target_table.sharding.cross.table=reference_table.reference_field->target_field
```

### 配置说明
- `reference_table`: 参考表名
- `reference_field`: 参考表中的字段名
- `target_field`: 目标表中对应的字段名

## 使用示例

### 示例1：用户消息表按用户ID分表

**业务场景**：用户消息表需要按照用户ID进行分表，确保同一用户的消息都在同一个分表中。

**表结构**：
```sql
-- 用户表
CREATE TABLE blade_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50),
    ...
);

-- 消息表
CREATE TABLE user_message (
    id BIGINT PRIMARY KEY,
    user_id BIGINT,  -- 关联用户ID
    message TEXT,
    create_time DATETIME,
    ...
);
```

**配置**：
```properties
sharding.tables=user_message

user_message.sharding.field=userId
user_message.sharding.strategy=hash
user_message.sharding.cross.table=blade_user.id->user_id
```

**工作原理**：
1. 当插入/查询消息时，系统获取消息的 `user_id` 字段值
2. 根据跨表配置，查询 `blade_user` 表中 `id = user_id` 的记录
3. 使用查询到的用户ID值进行哈希分表
4. 确保同一用户的所有消息都在同一个分表中

### 示例2：订单详情表按订单用户ID分表

**业务场景**：订单详情表需要按照订单所属的用户ID进行分表。

**表结构**：
```sql
-- 订单表
CREATE TABLE order_info (
    id BIGINT PRIMARY KEY,
    user_id BIGINT,
    order_no VARCHAR(50),
    ...
);

-- 订单详情表
CREATE TABLE order_detail (
    id BIGINT PRIMARY KEY,
    order_id BIGINT,  -- 关联订单ID
    product_id BIGINT,
    quantity INT,
    ...
);
```

**配置**：
```properties
sharding.tables=order_detail

order_detail.sharding.field=orderId
order_detail.sharding.strategy=hash
order_detail.sharding.cross.table=order_info.user_id->order_id
```

## 性能优化

### 缓存机制
系统内置了5分钟的缓存机制，避免频繁查询数据库：
```java
// 清理过期缓存
CrossTableShardingUtils.cleanExpiredCache();

// 清空所有缓存
CrossTableShardingUtils.clearCache();
```

### 最佳实践
1. **合理设计索引**：确保参考表的参考字段有索引
2. **缓存策略**：对于频繁访问的数据，可以考虑在应用层增加缓存
3. **批量操作**：对于批量插入/更新，尽量按分表进行分组处理

## API 使用

### 获取跨表分表的表名
```java
// 检查是否是跨表分表
boolean isCrossTable = CrossTableShardingUtils.isCrossTableSharding("user_message");

// 获取跨表配置
String crossConfig = CrossTableShardingUtils.getCrossTableConfig("user_message");

// 获取分表后的表名
String tableName = ShardingUtils.getTableName("user_message", userId, "hash");
```

### 直接使用跨表分表工具
```java
// 获取跨表分表值
Object shardingValue = CrossTableShardingUtils.getCrossTableShardingValue(
    "user_message", 
    userId, 
    "blade_user.id->user_id"
);

// 获取分表名
String shardedTableName = CrossTableShardingUtils.getCrossTableShardingTableName(
    "user_message", 
    userId, 
    "hash", 
    "blade_user.id->user_id"
);
```

## 注意事项

1. **数据一致性**：确保参考表的数据完整性，避免找不到对应记录
2. **性能影响**：跨表查询会增加一定的性能开销，建议合理使用缓存
3. **事务处理**：在事务中操作跨表分表时，需要注意事务的边界
4. **数据迁移**：如果需要修改跨表分表配置，需要考虑现有数据的迁移

## 故障排查

### 常见问题
1. **找不到参考记录**：检查参考表中是否存在对应的记录
2. **配置格式错误**：确保配置格式为 `table.field->field`
3. **字段类型不匹配**：确保参考字段和目标字段类型兼容

### 日志调试
系统会输出详细的日志信息，可以通过日志来排查问题：
```
2025-07-30 20:30:15 [INFO] 跨表分表查询: table=blade_user, field=id, value=1001
2025-07-30 20:30:15 [INFO] 跨表分表结果: shardingValue=1001, tableName=user_message_01
```
