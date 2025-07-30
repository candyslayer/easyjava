# EasyJava 使用示例

本文档提供EasyJava增强版的详细使用示例和最佳实践。

## 📋 目录

1. [快速开始示例](#快速开始示例)
2. [配置验证示例](#配置验证示例)
3. [增量生成示例](#增量生成示例)
4. [测试代码示例](#测试代码示例)
5. [生成选项示例](#生成选项示例)
6. [常见问题解决](#常见问题解决)

## 🚀 快速开始示例

### 1. 基础配置

创建测试数据库和表：

```sql
-- 创建数据库
CREATE DATABASE easyjava_demo DEFAULT CHARSET utf8mb4;

USE easyjava_demo;

-- 创建用户表
CREATE TABLE `sys_user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 1:启用 0:禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 创建角色表
CREATE TABLE `sys_role` (
  `role_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 1:启用 0:禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';
```

### 2. 配置文件设置

编辑 `src/main/resources/application.properties`：

```properties
# 应用名称
spring.application.name="EasyJava Demo 代码生成开始----------------->"

# 数据库配置
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/easyjava_demo
spring.datasource.username=root
spring.datasource.password=123456

# 生成包配置
package.base=com.easyjava.demo
package.po=entity.po
package.vo=entity.vo
package.param=entity.query
package.utils=utils
package.enums=enums
package.mapper=mapper
package.service=service
package.service.impl=service.impl
package.controller=controller
package.exception=exception
package.exception.strategy=exception.strategy

# 生成路径配置
path.base=D:/projects/easyjava-demo

# 作者信息
auther.comment=张三

# 后缀配置
suffix.bean.param=Query
suffix.bean.param.fuzzy=Fuzzy
suffix.bean.param.time.start=Start
suffix.bean.param.time.end=End
suffix.mapper=Mapper

# 表前缀处理
ignore.table.prefix=true

# JSON序列化配置
ignore.bean.tojson.field=password
ignore.bean.tojson.expression=@JsonIgnore
ignore.bean.tojson.class=import com.fasterxml.jackson.annotation.JsonIgnore;

# 日期序列化配置
bean.date.serialization=@JsonFormat(pattern = "%s",timezone = "GMT+8")
bean.date.serialization.class=import com.fasterxml.jackson.annotation.JsonFormat;
bean.data.deserializatio=@DateTimeFormat(pattern = "%s")
bean.date.deserializatio.class=import org.springframework.format.annotation.DateTimeFormat;
```

### 3. 运行生成器

```bash
# 基础生成
mvn compile exec:java -Dexec.mainClass="com.easyjava.AppEnhanced"

# 生成完整代码（包含测试）
mvn compile exec:java -Dexec.mainClass="com.easyjava.AppEnhanced" -Dexec.args="--full"
```

### 4. 生成结果

生成的项目结构：

```
D:/projects/easyjava-demo/
└── src/main/java/com/easyjava/demo/
    ├── entity/
    │   ├── po/
    │   │   ├── SysUser.java
    │   │   └── SysRole.java
    │   ├── query/
    │   │   ├── SysUserQuery.java
    │   │   └── SysRoleQuery.java
    │   └── vo/
    │       ├── ResponseVO.java
    │       └── PaginationResultVO.java
    ├── mapper/
    │   ├── BaseMapper.java
    │   ├── SysUserMapper.java
    │   └── SysRoleMapper.java
    ├── service/
    │   ├── SysUserService.java
    │   └── SysRoleService.java
    ├── service/impl/
    │   ├── SysUserServiceImpl.java
    │   └── SysRoleServiceImpl.java
    ├── controller/
    │   ├── AbaseController.java
    │   ├── AGlobalExceptionHandlerController.java
    │   ├── SysUserController.java
    │   └── SysRoleController.java
    └── ... (其他支撑类)
```

## 🔍 配置验证示例

### 配置验证成功示例

```
2025-07-30 10:30:15 INFO  ConfigValidator - 开始验证配置...
2025-07-30 10:30:15 INFO  ConfigValidator - ✅ 数据库连接验证成功
2025-07-30 10:30:15 INFO  ConfigValidator - ✅ 路径配置验证完成
2025-07-30 10:30:15 INFO  ConfigValidator - ✅ 包名配置验证完成
2025-07-30 10:30:15 INFO  ConfigValidator - ✅ 生成配置验证完成
2025-07-30 10:30:15 INFO  ConfigValidator - ✅ 配置验证通过
```

### 配置验证失败示例

```
2025-07-30 10:30:15 ERROR ConfigValidator - 配置验证错误: 数据库连接失败: Access denied for user 'root'@'localhost'
2025-07-30 10:30:15 ERROR ConfigValidator - 配置验证错误: 基础路径无写入权限: D:/readonly/path
2025-07-30 10:30:15 WARN  ConfigValidator - 配置验证警告: 作者信息未配置 (auther.comment)
2025-07-30 10:30:15 ERROR ConfigValidator - ❌ 配置验证失败
2025-07-30 10:30:15 ERROR ConfigValidator -   - 数据库连接失败: Access denied for user 'root'@'localhost'
2025-07-30 10:30:15 ERROR ConfigValidator -   - 基础路径无写入权限: D:/readonly/path
2025-07-30 10:30:15 WARN  ConfigValidator - ⚠️ 配置警告:
2025-07-30 10:30:15 WARN  ConfigValidator -   - 作者信息未配置 (auther.comment)
```

## ⚡ 增量生成示例

### 首次运行

```bash
mvn compile exec:java -Dexec.mainClass="com.easyjava.AppEnhanced" -Dexec.args="--incremental"
```

输出：
```
2025-07-30 10:35:00 INFO  IncrementalGenerator - 开始检查表结构变更...
2025-07-30 10:35:00 INFO  IncrementalGenerator - 发现新表: sys_user
2025-07-30 10:35:00 INFO  IncrementalGenerator - 发现新表: sys_role
2025-07-30 10:35:00 INFO  IncrementalGenerator - 共检查2个表，其中2个表需要重新生成
2025-07-30 10:35:00 INFO  IncrementalGenerator - 开始生成2个变更表的代码...
2025-07-30 10:35:05 INFO  IncrementalGenerator - 更新表结构缓存: D:/projects/easyjava-demo/table_structure_cache.json
2025-07-30 10:35:05 INFO  IncrementalGenerator - 增量生成完成
```

### 修改表结构后再次运行

修改用户表，添加新字段：
```sql
ALTER TABLE sys_user ADD COLUMN `department` varchar(100) DEFAULT NULL COMMENT '部门';
```

再次运行：
```bash
mvn compile exec:java -Dexec.mainClass="com.easyjava.AppEnhanced" -Dexec.args="--incremental"
```

输出：
```
2025-07-30 10:40:00 INFO  IncrementalGenerator - 开始检查表结构变更...
2025-07-30 10:40:00 INFO  IncrementalGenerator - 表结构发生变化: sys_user
2025-07-30 10:40:00 DEBUG IncrementalGenerator - 表结构未变化: sys_role
2025-07-30 10:40:00 INFO  IncrementalGenerator - 共检查2个表，其中1个表需要重新生成
2025-07-30 10:40:00 INFO  IncrementalGenerator - 开始备份现有文件到: D:/projects/easyjava-demo/backup/1722312000123
2025-07-30 10:40:00 DEBUG IncrementalGenerator - 备份文件: D:/projects/easyjava-demo/src/main/java/com/easyjava/demo/entity/po/SysUser.java -> D:/projects/easyjava-demo/backup/1722312000123/po/SysUser.java
2025-07-30 10:40:00 INFO  IncrementalGenerator - 文件备份完成
2025-07-30 10:40:00 INFO  IncrementalGenerator - 正在生成表 sys_user 的代码...
2025-07-30 10:40:01 INFO  IncrementalGenerator - 表 sys_user 代码生成完成
2025-07-30 10:40:01 INFO  IncrementalGenerator - 增量生成完成
```

## 🧪 测试代码示例

### 生成的Service测试

```java
package com.easyjava.demo.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;

import com.easyjava.demo.entity.po.SysUser;
import com.easyjava.demo.entity.query.SysUserQuery;
import com.easyjava.demo.entity.vo.PaginationResultVO;

/**
 * 系统用户表Service测试类
 * 
 * @author 张三
 * @since 2025-07-30
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
@Rollback
public class SysUserServiceTest {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 测试新增功能
     */
    @Test
    public void testAdd() {
        SysUser entity = createTestEntity();
        Integer result = sysUserService.Add(entity);
        assertNotNull("添加结果不能为空", result);
        assertTrue("添加失败", result > 0);
    }

    /**
     * 测试分页查询功能
     */
    @Test
    public void testFindListByPage() {
        SysUserQuery param = new SysUserQuery();
        PaginationResultVO<SysUser> result = sysUserService.FindListByPage(param);
        assertNotNull("查询结果不能为空", result);
        assertNotNull("查询列表不能为空", result.getList());
    }

    /**
     * 测试批量新增功能
     */
    @Test
    public void testAddBatch() {
        List<SysUser> entityList = createTestEntityList();
        Integer result = sysUserService.AddBatch(entityList);
        assertNotNull("批量添加结果不能为空", result);
        assertTrue("批量添加失败", result > 0);
    }

    /**
     * 创建测试实体
     */
    private SysUser createTestEntity() {
        SysUser entity = new SysUser();
        entity.setUsername("test_username");
        entity.setPassword("test_password");
        entity.setEmail("test_email");
        entity.setPhone("test_phone");
        entity.setStatus(1);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        return entity;
    }

    /**
     * 创建测试实体列表
     */
    private List<SysUser> createTestEntityList() {
        List<SysUser> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.add(createTestEntity());
        }
        return list;
    }
}
```

### 生成的Controller测试

```java
package com.easyjava.demo.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import static org.junit.Assert.*;

import com.easyjava.demo.entity.po.SysUser;
import com.easyjava.demo.entity.query.SysUserQuery;
import com.easyjava.demo.entity.vo.ResponseVO;

/**
 * 系统用户表Controller测试类
 * 
 * @author 张三
 * @since 2025-07-30
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
public class SysUserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * 测试分页查询接口
     */
    @Test
    public void testLoadDataList() {
        String url = "/sysUser/loadDataList";
        ResponseEntity<ResponseVO> response = restTemplate.getForEntity(url, ResponseVO.class);
        assertEquals("HTTP状态码应为200", HttpStatus.OK, response.getStatusCode());
        assertNotNull("响应体不能为空", response.getBody());
    }

    /**
     * 测试新增接口
     */
    @Test
    public void testAdd() {
        String url = "/sysUser/add";
        SysUser entity = new SysUser();
        // TODO: 设置测试数据
        ResponseEntity<ResponseVO> response = restTemplate.postForEntity(url, entity, ResponseVO.class);
        assertEquals("HTTP状态码应为200", HttpStatus.OK, response.getStatusCode());
        assertNotNull("响应体不能为空", response.getBody());
    }
}
```

## ⚙️ 生成选项示例

### 完整生成示例

```bash
# 生成所有代码，包括测试和Swagger注解
mvn compile exec:java -Dexec.mainClass="com.easyjava.AppEnhanced" -Dexec.args="--full --with-swagger"
```

输出：
```
=== 代码生成选项 ===
PO实体类: ✅
Mapper接口: ✅
Mapper XML: ✅
Service接口: ✅
Service实现: ✅
Controller: ✅
Query查询类: ✅
测试类: ✅
Swagger注解: ✅
覆盖已存在文件: ❌
增量生成: ❌
==================
```

### 基础生成示例

```bash
# 只生成基础代码，不包含测试
mvn compile exec:java -Dexec.mainClass="com.easyjava.AppEnhanced" -Dexec.args="--basic"
```

输出：
```
=== 代码生成选项 ===
PO实体类: ✅
Mapper接口: ✅
Mapper XML: ✅
Service接口: ✅
Service实现: ✅
Controller: ✅
Query查询类: ✅
测试类: ❌
Swagger注解: ❌
覆盖已存在文件: ❌
增量生成: ❌
==================
```

### 增量生成示例

```bash
# 增量生成模式，备份现有文件
mvn compile exec:java -Dexec.mainClass="com.easyjava.AppEnhanced" -Dexec.args="--incremental --with-tests"
```

## ❗ 常见问题解决

### 1. 数据库连接失败

**问题**：
```
配置验证错误: 数据库连接失败: Communications link failure
```

**解决方案**：
1. 检查数据库服务是否启动
2. 验证连接参数（主机、端口、数据库名）
3. 确认用户名和密码正确
4. 检查防火墙设置

### 2. 路径权限问题

**问题**：
```
配置验证错误: 基础路径无写入权限: D:/readonly/path
```

**解决方案**：
1. 更改输出路径到有写入权限的目录
2. 以管理员身份运行程序
3. 修改目录权限

### 3. 包名格式错误

**问题**：
```
配置验证错误: 基础包名格式不正确: com.123invalid
```

**解决方案**：
1. 确保包名以字母开头
2. 包名只能包含字母、数字和下划线
3. 包名分段使用点号分隔

### 4. 生成的文件编码问题

**问题**：生成的文件中文乱码

**解决方案**：
1. 确保IDE使用UTF-8编码
2. 检查系统环境变量JAVA_TOOL_OPTIONS
3. 在运行时添加参数：`-Dfile.encoding=UTF-8`

### 5. Maven编译失败

**问题**：
```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin
```

**解决方案**：
1. 检查Java版本是否为21+
2. 确认Maven版本为3.6+
3. 清理Maven缓存：`mvn clean`
4. 检查依赖版本兼容性

### 6. 表结构获取失败

**问题**：
```
共发现 0 个表
```

**解决方案**：
1. 确认数据库中确实存在表
2. 检查用户是否有表的查询权限
3. 验证数据库名称是否正确
4. 检查表前缀配置是否正确

## 📚 进阶使用

### 自定义生成选项

创建自定义的生成配置：

```java
public class CustomApp {
    public static void main(String[] args) {
        // 创建自定义生成选项
        GenerateOptions options = new GenerateOptions();
        options.setGenerateTests(true);
        options.setIncrementalGenerate(true);
        options.setOverwriteExisting(false);
        
        // 验证配置
        ConfigValidator.ValidationResult result = ConfigValidator.validateAllConfig();
        if (!result.isValid()) {
            return;
        }
        
        // 获取表信息并生成
        List<TableInfo> tables = BuilderTable.GetTables();
        IncrementalGenerator.generateChangedTables(tables, options);
    }
}
```

### 批量处理多个数据库

```java
public class MultipleDatabaseGenerator {
    public static void main(String[] args) {
        String[] databases = {"db1", "db2", "db3"};
        
        for (String db : databases) {
            // 动态修改配置
            System.setProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/" + db);
            System.setProperty("path.base", "D:/projects/" + db);
            
            // 生成代码
            AppEnhanced.main(new String[]{"--incremental"});
        }
    }
}
```

## 🎯 最佳实践

### 1. 项目结构建议

```
your-project/
├── easyjava-generator/     # 代码生成器项目
├── your-app-common/        # 公共模块
├── your-app-entity/        # 实体模块（生成的PO、VO等）
├── your-app-mapper/        # 数据访问层（生成的Mapper）
├── your-app-service/       # 业务逻辑层（生成的Service）
├── your-app-web/          # Web层（生成的Controller）
└── your-app-test/         # 测试模块（生成的测试类）
```

### 2. 配置管理建议

- 使用不同的配置文件管理不同环境
- 将敏感信息（如数据库密码）放在环境变量中
- 使用版本控制管理配置文件模板

### 3. 增量生成最佳实践

- 定期备份重要的自定义代码
- 在版本控制中标记生成的代码
- 使用分支管理代码生成的版本

---

**更多示例和文档，请参考项目主README文件。**
