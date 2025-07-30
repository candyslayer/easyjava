# 模板自定义功能文档

## 🎨 功能概述

EasyJava增强版现在支持完整的代码模板自定义功能，允许用户：

- 自定义代码生成模板
- 管理模板配置
- 支持变量替换和条件逻辑
- 批量生成多种类型的代码文件
- 命令行工具管理模板

## 🚀 快速开始

### 1. 启动模板管理工具

```bash
# 编译项目
mvn clean compile

# 启动模板管理工具
java -cp target/classes com.easyjava.AppEnhanced template
```

或者直接运行：

```bash
java -cp target/classes com.easyjava.manager.TemplateCommandTool
```

### 2. 初始化模板环境

首次使用时，需要初始化自定义模板环境：

```
=== EasyJava 模板管理工具 ===
请输入命令: init
是否初始化自定义模板环境？(y/n): y
自定义模板环境初始化完成
```

## 📁 目录结构

初始化后会创建以下目录结构：

```
custom-templates/
├── template-config.properties    # 模板配置文件
├── examples/                     # 示例模板
│   ├── Po.txt.example
│   ├── Query.txt.example
│   ├── Service.txt.example
│   └── ...
├── Po.txt                       # 自定义实体类模板
├── Query.txt                    # 自定义查询参数模板
├── Service.txt                  # 自定义Service接口模板
├── ServiceImpl.txt              # 自定义Service实现模板
├── Controller.txt               # 自定义Controller模板
├── Mapper.txt                   # 自定义Mapper接口模板
├── MapperXML.txt               # 自定义Mapper XML模板
└── Test.txt                    # 自定义测试类模板
```

## 🔧 模板语法

### 变量替换

使用 `${变量名}` 格式进行变量替换：

```java
package ${package.po};

/**
 * ${table.comment}
 *
 * @author ${author}
 * @date ${date}
 */
public class ${table.beanName} implements Serializable {
    // 字段定义...
}
```

### 支持的变量

#### 全局变量
- `${author}` - 作者名称
- `${date}` - 当前日期 (yyyy-MM-dd)
- `${datetime}` - 当前时间 (yyyy-MM-dd HH:mm:ss)
- `${year}` - 当前年份

#### 包路径变量
- `${package.base}` - 基础包路径
- `${package.po}` - 实体类包路径
- `${package.query}` - 查询参数包路径
- `${package.service}` - Service包路径
- `${package.service.impl}` - Service实现包路径
- `${package.controller}` - Controller包路径
- `${package.mapper}` - Mapper包路径

#### 表信息变量
- `${table.tableName}` - 数据库表名
- `${table.beanName}` - 实体类名称
- `${table.beanParamName}` - 实体类参数名称
- `${table.comment}` - 表注释
- `${table.fieldList}` - 字段列表

#### 字段信息变量（在循环中使用）
- `${field.fieldName}` - 数据库字段名
- `${field.propertyName}` - Java属性名
- `${field.javaType}` - Java类型
- `${field.sqlType}` - SQL类型
- `${field.comment}` - 字段注释
- `${field.isAutoIncrement}` - 是否自增

### 嵌套属性访问

支持点号访问嵌套属性：

```java
// 访问表的第一个字段
${table.fieldList.0.propertyName}

// 访问主键信息
${primaryKey.javaType}
```

## 📝 模板示例

### 实体类模板 (Po.txt)

```java
package ${package.po};

import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * ${table.comment}
 *
 * @author ${author}
 * @date ${date}
 */
public class ${table.beanName} implements Serializable {

    private static final long serialVersionUID = 1L;

    // 字段定义
    <#list table.fieldList as field>
    /**
     * ${field.comment}
     */
    <#if field.javaType == "Date">
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    </#if>
    private ${field.javaType} ${field.propertyName};

    </#list>

    // Getter/Setter方法
    <#list table.fieldList as field>
    public ${field.javaType} get${field.propertyName?cap_first}() {
        return ${field.propertyName};
    }

    public void set${field.propertyName?cap_first}(${field.javaType} ${field.propertyName}) {
        this.${field.propertyName} = ${field.propertyName};
    }

    </#list>
}
```

### Service接口模板 (Service.txt)

```java
package ${package.service};

import ${package.po}.${table.beanName};
import ${package.query}.${table.beanName}Query;
import com.easyjava.entity.vo.PaginationResultVO;
import java.util.List;

/**
 * ${table.comment} Service接口
 *
 * @author ${author}
 * @date ${date}
 */
public interface ${table.beanName}Service {

    /**
     * 根据条件查询列表
     */
    List<${table.beanName}> findListByParam(${table.beanName}Query param);

    /**
     * 分页查询
     */
    PaginationResultVO<${table.beanName}> findListByPage(${table.beanName}Query param);

    /**
     * 新增
     */
    Integer add(${table.beanName} bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<${table.beanName}> listBean);

    // 根据主键的CRUD方法...
}
```

## ⚙️ 配置管理

### 模板配置文件 (template-config.properties)

```properties
# 基本配置
template.author=EasyJava
template.encoding=UTF-8
template.dateFormat=yyyy-MM-dd
template.datetimeFormat=yyyy-MM-dd HH:mm:ss

# 模板启用配置
template.po.enabled=true
template.query.enabled=true
template.service.enabled=true
template.serviceImpl.enabled=true
template.controller.enabled=true
template.mapper.enabled=true
template.mapperXml.enabled=true
template.test.enabled=false
```

### 在application.properties中配置

```properties
# 自定义模板路径
template.custom.path=D:/templates/custom/

# 模板作者
template.author=Your Name
```

## 🎯 命令行工具使用

### 基本命令

```bash
# 查看所有命令
请输入命令: help

# 列出所有模板
请输入命令: list

# 查看配置状态
请输入命令: status

# 启用/禁用模板类型
请输入命令: enable
请输入要启用的模板类型: po

请输入命令: disable
请输入要禁用的模板类型: test
```

### 模板管理

```bash
# 复制默认模板到自定义目录进行修改
请输入命令: copy
请输入要复制的模板编号或名称: Po.txt

# 验证模板语法
请输入命令: validate
请输入模板文件路径: custom-templates/Po.txt
```

### 代码生成

```bash
# 生成代码
请输入命令: generate
请输入表名: user
生成选项:
1. 生成所有启用的模板
2. 选择特定模板类型
请选择: 1
代码生成完成
```

### 配置管理

```bash
# 配置管理
请输入命令: config
=== 配置管理 ===
1. 查看当前配置
2. 设置作者
3. 设置编码
4. 返回主菜单
请选择: 2
请输入作者名称: John Doe
作者设置成功: John Doe
```

## 🔍 高级特性

### 1. 条件生成

可以在模板中使用条件逻辑：

```java
<#list table.fieldList as field>
    <#if field.javaType == "Date">
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    </#if>
    private ${field.javaType} ${field.propertyName};
</#list>
```

### 2. 智能导入包

系统会自动收集需要导入的包：

```java
import java.io.Serializable;
<#list imports as import>
import ${import};
</#list>
```

### 3. 测试数据生成

支持智能生成测试数据：

```java
private ${table.beanName} createTestEntity() {
    ${table.beanName} entity = new ${table.beanName}();
    <#list table.fieldList as field>
    entity.set${field.propertyName?cap_first}(${testData[field.propertyName]});
    </#list>
    return entity;
}
```

### 4. 模板验证

提供模板语法验证功能：

```java
TemplateValidationResult result = TemplateManager.validateTemplate(templateContent);
if (result.isValid()) {
    System.out.println("模板语法正确");
    System.out.println("发现变量: " + result.getVariables());
} else {
    result.getErrors().forEach(System.out::println);
}
```

## 🔧 编程接口

### 直接使用API

```java
// 获取模板内容
String template = TemplateManager.getTemplate("Po.txt");

// 准备变量
Map<String, Object> variables = TemplateManager.createTableVariables(tableInfo);

// 处理模板
String result = TemplateManager.processTemplate(template, variables);

// 使用模板生成器
TemplateBasedBuilder.generateWithTemplate(tableInfo, "po", outputPath);

// 批量生成
TemplateBasedBuilder.generateAllEnabledTemplates(tableInfo);
```

### 配置管理API

```java
// 检查模板是否启用
boolean enabled = TemplateConfigManager.isTemplateEnabled("po");

// 设置模板状态
TemplateConfigManager.setTemplateEnabled("po", true);

// 获取配置
String author = TemplateConfigManager.getAuthor();

// 保存配置
TemplateConfigManager.saveTemplateConfig();
```

## 📚 最佳实践

### 1. 模板组织

- 将相关模板放在同一目录
- 使用有意义的文件名
- 添加模板注释说明用途

### 2. 变量命名

- 使用清晰的变量名
- 遵循一致的命名约定
- 避免过长的嵌套访问

### 3. 模板复用

- 创建通用的模板片段
- 使用include机制复用代码
- 保持模板简洁易读

### 4. 版本控制

- 将自定义模板纳入版本控制
- 定期备份模板文件
- 记录模板变更历史

## 🐛 故障排除

### 常见问题

1. **模板文件找不到**
   - 检查文件路径是否正确
   - 确认文件扩展名是否匹配

2. **变量替换失败**
   - 验证变量名拼写
   - 检查变量是否在作用域内

3. **生成的代码有语法错误**
   - 使用模板验证功能检查语法
   - 检查条件逻辑是否正确

4. **配置不生效**
   - 重启应用程序
   - 检查配置文件格式

### 日志排查

启用详细日志：

```properties
logging.level.com.easyjava.manager=DEBUG
```

## 🔮 未来计划

1. **可视化模板编辑器** - 提供图形界面编辑模板
2. **模板市场** - 分享和下载社区模板
3. **更多模板引擎支持** - 支持Velocity、Thymeleaf等
4. **实时预览** - 编辑时实时预览生成效果
5. **模板版本管理** - 支持模板版本控制和回滚

---

**文档版本**: v1.0  
**更新时间**: 2025-07-30  
**适用版本**: EasyJava 增强版
