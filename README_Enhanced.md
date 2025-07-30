# EasyJava 增强版代码生成器

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)

EasyJava是一个强大的Java代码生成器，能够从MySQL数据库表结构自动生成完整的Spring Boot项目代码，包括实体类、Mapper、Service、Controller等各层代码。

## ✨ 新增功能（v2.0）

### 🎨 模板自定义功能
- ✅ 自定义代码生成模板
- ✅ 模板变量替换系统
- ✅ 命令行模板管理工具
- ✅ 模板配置管理
- ✅ 模板语法验证

### 🔍 配置验证功能
- ✅ 数据库连接验证
- ✅ 路径配置验证  
- ✅ 包名配置验证
- ✅ 生成配置验证
- ✅ 详细的错误和警告提示

### 🧪 测试代码生成
- ✅ Service层单元测试
- ✅ Controller层集成测试
- ✅ 自动生成测试数据
- ✅ 支持Spring Boot Test

### ⚡ 增量生成支持
- ✅ 检测表结构变更
- ✅ 只生成变更的表
- ✅ 自动备份现有文件
- ✅ 表结构缓存机制

## 🚀 功能特性

### 核心功能
- **PO实体类生成** - 支持字段注解、序列化配置
- **Mapper接口生成** - 基于MyBatis的数据访问层
- **Mapper XML生成** - 完整的SQL映射文件
- **Service接口生成** - 业务逻辑层接口
- **Service实现生成** - 业务逻辑层实现
- **Controller生成** - REST API控制器
- **Query查询类生成** - 查询参数封装

### 高级功能
- **模板自定义** - 自定义代码生成模板，支持变量替换
- **模板管理** - 命令行工具管理模板配置和生成
- **分页支持** - 内置分页查询功能
- **异常处理** - 完整的异常处理框架
- **响应封装** - 统一的API响应格式
- **日期处理** - 自动的日期序列化配置
- **批量操作** - 批量新增、更新、删除

## 📦 项目结构

```
easyjava/
├── src/main/java/com/easyjava/
│   ├── App.java                    # 原版主类
│   ├── AppEnhanced.java           # 增强版主类 ⭐
│   ├── LogbackConfig.java         # 日志配置
│   ├── bean/
│   │   ├── Constants.java         # 常量配置
│   │   ├── FieldInfo.java         # 字段信息
│   │   ├── TableInfo.java         # 表信息
│   │   └── GenerateOptions.java   # 生成选项 ⭐
│   ├── builder/
│   │   ├── BuildBase.java         # 基础类生成
│   │   ├── BuildPo.java           # PO生成
│   │   ├── BuildMapper.java       # Mapper生成
│   │   ├── BuildMapperXML.java    # XML生成
│   │   ├── BuildService.java      # Service生成
│   │   ├── BuildServiceImpl.java  # ServiceImpl生成
│   │   ├── BuildController.java   # Controller生成
│   │   ├── BuildQuery.java        # Query生成
│   │   ├── BuildTest.java         # 测试代码生成 ⭐
│   │   ├── BuildComment.java      # 注释生成
│   │   ├── BuilderTable.java      # 表信息构建
│   │   ├── IncrementalGenerator.java # 增量生成 ⭐
│   │   └── TemplateBasedBuilder.java # 模板生成器 ⭐
│   ├── manager/                   # 模板管理 ⭐
│   │   ├── TemplateManager.java   # 模板管理器
│   │   ├── TemplateConfigManager.java # 模板配置管理
│   │   └── TemplateCommandTool.java # 命令行工具
│   └── utils/
│       ├── ConfigValidator.java   # 配置验证 ⭐
│       ├── PropertiesUtils.java   # 配置工具
│       ├── StringUtils.java       # 字符串工具
│       ├── JsonUtils.java         # JSON工具
│       └── DateUtils.java         # 日期工具
├── src/main/resources/
│   ├── application.properties     # 主配置文件
│   └── template/                  # 代码模板
└── pom.xml                       # Maven配置
```

## ⚙️ 快速开始

### 1. 环境要求

- Java 21+
- Maven 3.6+
- MySQL 8.0+

### 2. 配置数据库

编辑 `src/main/resources/application.properties`：

```properties
# 数据库配置
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/your_database
spring.datasource.username=root
spring.datasource.password=your_password

# 生成配置
package.base=com.yourcompany.yourproject
path.base=D:/your/project/path

# 作者信息
auther.comment=你的名字
```

### 3. 运行生成器

#### 基础使用（原版）
```bash
mvn compile exec:java -Dexec.mainClass="com.easyjava.App"
```

#### 增强版使用
```bash
# 默认生成
mvn compile exec:java -Dexec.mainClass="com.easyjava.AppEnhanced"

# 完整生成（包括测试）
mvn compile exec:java -Dexec.mainClass="com.easyjava.AppEnhanced" -Dexec.args="--full"

# 增量生成
mvn compile exec:java -Dexec.mainClass="com.easyjava.AppEnhanced" -Dexec.args="--incremental"

# 强制重新生成
mvn compile exec:java -Dexec.mainClass="com.easyjava.AppEnhanced" -Dexec.args="--force"

# 启动模板管理工具
mvn compile exec:java -Dexec.mainClass="com.easyjava.AppEnhanced" -Dexec.args="template"
```

### 4. 模板管理（新功能）

#### 初始化模板环境
```bash
# 启动模板管理工具
java -cp target/classes com.easyjava.AppEnhanced template

# 在工具中执行
请输入命令: init
是否初始化自定义模板环境？(y/n): y
```

#### 管理模板
```bash
# 列出所有模板
请输入命令: list

# 查看配置状态
请输入命令: status

# 复制默认模板进行自定义
请输入命令: copy
请输入要复制的模板编号或名称: Po.txt

# 生成代码
请输入命令: generate
请输入表名: user
```

## 🎯 使用指南

### 命令行参数

| 参数 | 说明 |
|------|------|
| `--full` | 生成完整代码（包括测试和文档） |
| `--basic` | 只生成基础代码 |
| `--incremental` | 启用增量生成 |
| `--overwrite` | 覆盖已存在的文件 |
| `--no-tests` | 不生成测试代码 |
| `--with-tests` | 生成测试代码 |
| `--with-swagger` | 生成Swagger注解 |
| `--force` | 强制重新生成所有文件 |
| `--help` | 显示帮助信息 |

### 配置选项详解

#### 数据库配置
```properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/easymeeting
spring.datasource.username=root
spring.datasource.password=password
```

#### 包名配置
```properties
package.base=com.easymeeting
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
```

#### 路径配置
```properties
path.base=D:/java/project/generator
```

#### 生成配置
```properties
# 后缀配置
suffix.bean.param=Query
suffix.bean.param.fuzzy=Fuzzy
suffix.bean.param.time.start=Start
suffix.bean.param.time.end=End
suffix.mapper=Mapper

# 忽略表前缀
ignore.table.prefix=true

# JSON序列化配置
ignore.bean.tojson.field=password
ignore.bean.tojson.expression=@JsonIgnore
ignore.bean.tojson.class=import com.fasterxml.jackson.annotation.JsonIgnore;

# 日期配置
bean.date.serialization=@JsonFormat(pattern = "%s",timezone = "GMT+8")
bean.date.serialization.class=import com.fasterxml.jackson.annotation.JsonFormat;
bean.data.deserializatio=@DateTimeFormat(pattern = "%s")
bean.date.deserializatio.class=import org.springframework.format.annotation.DateTimeFormat;
```

## 🧪 测试功能

生成器会自动生成以下测试代码：

### Service测试
```java
@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
@Rollback
public class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    public void testAdd() {
        User entity = createTestEntity();
        Integer result = userService.Add(entity);
        assertNotNull("添加结果不能为空", result);
        assertTrue("添加失败", result > 0);
    }
    
    // 更多测试方法...
}
```

### Controller测试
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
public class UserControllerTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testLoadDataList() {
        String url = "/user/loadDataList";
        ResponseEntity<ResponseVO> response = restTemplate.getForEntity(url, ResponseVO.class);
        assertEquals("HTTP状态码应为200", HttpStatus.OK, response.getStatusCode());
    }
    
    // 更多测试方法...
}
```

## ⚡ 增量生成

增量生成功能可以检测表结构变更，只生成发生变化的表：

### 工作原理
1. **表结构哈希** - 计算每个表结构的MD5哈希值
2. **缓存比较** - 与上次生成时的哈希值进行比较
3. **变更检测** - 识别新增、修改的表
4. **自动备份** - 备份现有文件到备份目录
5. **增量生成** - 只生成变更的表

### 使用示例
```bash
# 首次运行（生成所有表）
mvn compile exec:java -Dexec.mainClass="com.easyjava.AppEnhanced" -Dexec.args="--incremental"

# 修改数据库表结构后再次运行（只生成变更的表）
mvn compile exec:java -Dexec.mainClass="com.easyjava.AppEnhanced" -Dexec.args="--incremental"
```

## 🔍 配置验证

启动时自动验证配置的有效性：

### 验证内容
- ✅ **数据库连接** - 验证连接参数和连通性
- ✅ **路径配置** - 检查输出路径的可写性
- ✅ **包名配置** - 验证包名格式的正确性
- ✅ **生成配置** - 检查必要的配置项

### 验证结果
```
=== 配置验证结果 ===
✅ 数据库连接验证成功
✅ 路径配置验证完成
✅ 包名配置验证完成
✅ 生成配置验证完成
⚠️ 配置警告:
  - 作者信息未配置 (auther.comment)
==================
```

## 🎨 生成的代码结构

### 实体类示例
```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {
    
    private Integer userId;
    
    @JsonIgnore
    private String password;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    // getters and setters...
}
```

### Service接口示例
```java
public interface UserService {
    
    List<User> FindListParam(UserQuery param);
    
    Integer FindCountByParam(UserQuery param);
    
    PaginationResultVO<User> FindListByPage(UserQuery param);
    
    Integer Add(User bean);
    
    Integer AddBatch(List<User> listbean);
    
    Integer AddOrUpdateBatch(List<User> listbean);
    
    User GetByUserId(Integer userId);
    
    Integer UpdateByUserId(User bean, Integer userId);
    
    Integer DeleteByUserId(Integer userId);
}
```

### Controller示例
```java
@RestController
public class UserController extends AbaseController {
    
    @Autowired
    UserService userService;
    
    @RequestMapping("/loadDataList")
    public ResponseVO LoadDataList(UserQuery query) {
        return GetSuccessResponseVO(userService.FindListByPage(query));
    }
    
    @RequestMapping("/add")
    public ResponseVO Add(User bean) {
        userService.Add(bean);
        return GetSuccessResponseVO(null);
    }
    
    // 更多方法...
}
```

## 📝 开发计划

### 已完成 ✅
- [x] 配置验证功能
- [x] 测试代码生成
- [x] 增量生成支持
- [x] 生成选项配置
- [x] 命令行参数支持

### 计划中 🚧
- [ ] Swagger文档生成
- [ ] 更多数据库支持（PostgreSQL、Oracle）
- [ ] 代码模板自定义
- [ ] GUI界面
- [ ] Maven插件化
- [ ] 代码质量检查

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

### 开发环境设置
1. Fork本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

**⭐ 如果这个项目对你有帮助，请给它一个Star！**
