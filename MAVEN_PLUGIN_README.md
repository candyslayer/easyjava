# EasyJava Maven Plugin

EasyJava Maven Plugin 是一个强大的代码生成插件，可以从数据库表结构自动生成完整的Java CRUD代码。

## 功能特性

- 🚀 **一键生成**: 从数据库表自动生成完整的CRUD代码
- 📦 **完整架构**: 生成Entity、Query、Mapper、Service、Controller等完整分层代码
- 🔄 **分表支持**: 内置分表功能，支持时间、哈希、范围等多种分表策略  
- 🎯 **高度可配置**: 支持包名、作者、输出路径等多种自定义配置
- 📋 **表过滤**: 支持包含/排除特定表的代码生成
- 🔧 **Maven集成**: 完美集成Maven构建流程

## 快速开始

### 1. 在项目的 pom.xml 中添加插件

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.easyjava</groupId>
            <artifactId>easyjava-maven-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
            <configuration>
                <!-- 数据库连接配置 -->
                <dbUrl>jdbc:mysql://localhost:3306/your_database</dbUrl>
                <dbUsername>your_username</dbUsername>
                <dbPassword>your_password</dbPassword>
                
                <!-- 代码生成配置 -->
                <packageBase>com.yourcompany.yourproject</packageBase>
                <author>Your Name</author>
                <outputPath>${project.basedir}/src/main/java</outputPath>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 2. 执行代码生成

```bash
# 生成代码
mvn easyjava:generate

# 或者指定具体参数
mvn easyjava:generate -Deasyjava.db.url=jdbc:mysql://localhost:3306/test -Deasyjava.db.username=root -Deasyjava.db.password=123456
```

## 配置参数

| 参数名 | 默认值 | 说明 |
|--------|--------|------|
| `dbUrl` | 无 | 数据库连接URL |
| `dbUsername` | 无 | 数据库用户名 |
| `dbPassword` | 无 | 数据库密码 |
| `dbDriver` | `com.mysql.cj.jdbc.Driver` | 数据库驱动类名 |
| `outputPath` | `${project.build.directory}/generated-sources/easyjava` | 代码生成输出目录 |
| `author` | `EasyJava Generator` | 代码注释中的作者名称 |
| `packageBase` | `com.example` | 生成代码的包名前缀 |
| `ignoreTablePrefix` | `true` | 是否忽略表前缀 |
| `tablePrefix` | 无 | 表前缀列表（逗号分隔） |
| `shardingEnabled` | `false` | 是否启用分表功能 |
| `includeTables` | 无 | 需要包含的表名列表（逗号分隔） |
| `excludeTables` | 无 | 需要排除的表名列表（逗号分隔） |

## 使用示例

### 基本使用

```xml
<plugin>
    <groupId>com.easyjava</groupId>
    <artifactId>easyjava-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <configuration>
        <dbUrl>jdbc:mysql://localhost:3306/blog</dbUrl>
        <dbUsername>root</dbUsername>
        <dbPassword>123456</dbPassword>
        <packageBase>com.myblog</packageBase>
        <author>张三</author>
    </configuration>
</plugin>
```

### 高级配置

```xml
<plugin>
    <groupId>com.easyjava</groupId>
    <artifactId>easyjava-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <configuration>
        <!-- 数据库配置 -->
        <dbUrl>jdbc:mysql://localhost:3306/ecommerce</dbUrl>
        <dbUsername>dev_user</dbUsername>
        <dbPassword>dev_password</dbPassword>
        
        <!-- 代码生成配置 -->
        <packageBase>com.ecommerce.system</packageBase>
        <author>开发团队</author>
        <outputPath>${project.basedir}/src/main/java</outputPath>
        
        <!-- 表过滤配置 -->
        <includeTables>user,product,order</includeTables>
        <excludeTables>temp_table,log_table</excludeTables>
        
        <!-- 分表配置 -->
        <shardingEnabled>true</shardingEnabled>
        
        <!-- 表前缀配置 -->
        <ignoreTablePrefix>true</ignoreTablePrefix>
        <tablePrefix>sys_,biz_</tablePrefix>
    </configuration>
</plugin>
```

### 命令行参数

```bash
# 使用命令行参数覆盖配置
mvn easyjava:generate \
    -Deasyjava.db.url=jdbc:mysql://localhost:3306/test \
    -Deasyjava.db.username=root \
    -Deasyjava.db.password=123456 \
    -Deasyjava.package.base=com.test \
    -Deasyjava.author="测试开发者" \
    -Deasyjava.include.tables=user,role
```

## 生成的代码结构

执行插件后，会为每个数据库表生成以下文件：

```
src/main/java/
└── com/yourpackage/
    ├── entity/
    │   └── User.java              # 实体类
    ├── query/
    │   └── UserQuery.java         # 查询参数类
    ├── mapper/
    │   ├── UserMapper.java        # Mapper接口
    │   └── UserMapper.xml         # MyBatis XML映射
    ├── service/
    │   ├── UserService.java       # Service接口
    │   └── impl/
    │       └── UserServiceImpl.java  # Service实现类
    └── controller/
        └── UserController.java    # 控制器
```

## 分表功能

当启用 `shardingEnabled=true` 时，插件会自动检测适合分表的字段，并生成分表相关代码：

- **时间分表**: datetime、timestamp、date类型字段
- **哈希分表**: 字符串、ID类型字段  
- **范围分表**: 数值类型字段

## 与IDE集成

### IntelliJ IDEA

1. 在Maven工具窗口中找到插件
2. 双击 `easyjava:generate` 执行代码生成
3. 或者使用快捷键 `Ctrl+Shift+X` 运行Maven命令

### Eclipse

1. 右键项目 → Run As → Maven build
2. 在Goals中输入 `easyjava:generate`
3. 点击Run执行

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查数据库URL、用户名、密码是否正确
   - 确认数据库服务是否启动
   - 检查网络连接

2. **代码生成失败**
   - 查看Maven输出日志中的错误信息
   - 确认输出目录是否有写权限
   - 检查包名格式是否正确

3. **找不到表**
   - 确认数据库中存在表
   - 检查用户权限是否足够
   - 验证includeTables/excludeTables配置

### 调试模式

```bash
# 启用调试模式查看详细日志
mvn easyjava:generate -X
```

## 版本说明

- **1.0-SNAPSHOT**: 初始版本，支持基本的CRUD代码生成和分表功能

## 贡献

欢迎提交Issue和Pull Request来改进这个插件！

## 许可证

本项目采用 MIT 许可证。
