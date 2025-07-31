# EasyJava Maven Plugin 📦

![Version](https://img.shields.io/badge/version-1.0--SNAPSHOT-blue)
![Java](https://img.shields.io/badge/Java-8%2B-orange)
![Maven](https://img.shields.io/badge/Maven-3.6%2B-red)
![MySQL](https://img.shields.io/badge/MySQL-5.7%2B-blue)

EasyJava Maven Plugin 是一个强大的 Java 代码生成插件，能够从 MySQL 数据库表结构自动生成完整的企业级 CRUD 代码。专为提高开发效率和代码一致性而设计。

## ✨ 核心特性

- 🚀 **一键生成完整CRUD**: 从数据库表自动生成 Entity、Query、Mapper、Service、Controller 等完整分层架构代码
- � **企业级代码模板**: 内置Spring Boot + MyBatis架构的最佳实践代码模板
- 🔄 **智能类型映射**: 自动将MySQL数据类型映射为Java类型，支持所有常用数据类型
- 🎯 **高度可配置**: 支持包名、作者、输出路径、注解等多维度自定义配置
- 📋 **灵活表过滤**: 支持包含/排除特定表，支持表前缀处理
- 🔧 **Maven完美集成**: 无缝集成Maven构建流程，支持命令行和配置文件两种使用方式
- 🛠️ **分表支持**: 内置分表功能支持（实验性功能）
- 📝 **详细日志**: 提供详细的生成过程日志和调试信息
- 🔒 **安全配置**: 支持敏感信息的系统属性覆盖机制

## 🎯 生成的代码组件

### 核心组件
- **Entity/PO类**: 数据库表对应的实体类，包含完整的字段映射和注解
- **Query参数类**: 查询条件封装类，支持模糊查询和时间范围查询
- **Mapper接口**: MyBatis数据访问层接口，包含基础CRUD操作
- **Mapper XML**: MyBatis映射文件，包含完整的SQL语句
- **Service接口**: 业务逻辑层接口定义
- **Service实现**: 业务逻辑层具体实现
- **Controller类**: RESTful API控制器，包含完整的HTTP接口

### 基础框架组件
- **基础工具类**: 日期处理、分页、响应封装等通用工具
- **异常处理框架**: 统一异常处理和错误响应机制
- **分页组件**: 完整的分页查询支持
- **响应封装**: 统一的API响应格式

## 🚀 快速开始

### 1. 环境要求

- **Java**: 8 或更高版本
- **Maven**: 3.6 或更高版本  
- **MySQL**: 5.7 或更高版本
- **Spring Boot**: 2.0+ （生成的代码基于此版本）

### 2. 安装插件

目前插件处于开发阶段，需要手动安装到本地Maven仓库：

```bash
# 克隆项目
git clone https://github.com/candyslayer/easyjava.git
cd easyjava

# 编译并安装到本地仓库
mvn clean install
```

### 3. 在项目中使用

#### 方式一：pom.xml配置（推荐）

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.easyjava</groupId>
            <artifactId>easyjava-maven-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
            <configuration>
                <!-- 数据库连接配置 -->
                <dbUrl>jdbc:mysql://localhost:3306/your_database?useUnicode=true&amp;characterEncoding=utf8</dbUrl>
                <dbUsername>your_username</dbUsername>
                <dbPassword>your_password</dbPassword>
                
                <!-- 代码生成配置 -->
                <packageBase>com.yourcompany.yourproject</packageBase>
                <author>Your Name</author>
                <outputPath>src/main/java</outputPath>
                
                <!-- 可选配置 -->
                <ignoreTablePrefix>true</ignoreTablePrefix>
                <includeTables>user,order,product</includeTables>
                <!-- <excludeTables>temp_table,log_table</excludeTables> -->
            </configuration>
        </plugin>
    </plugins>
</build>
```

然后执行：
```bash
mvn easyjava:generate
```

#### 方式二：命令行参数（灵活）

```bash
mvn easyjava:generate \
    -Deasyjava.db.url="jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8" \
    -Deasyjava.db.username="root" \
    -Deasyjava.db.password="123456" \
    -Deasyjava.package.base="com.example.demo" \
    -Deasyjava.author="张三" \
    -Deasyjava.output.path="src/main/java"
```

## ⚙️ 配置参数详解

### 必需参数

| 参数名 | 说明 | 示例 |
|--------|------|------|
| `easyjava.db.url` | 数据库连接URL | `jdbc:mysql://localhost:3306/test` |
| `easyjava.db.username` | 数据库用户名 | `root` |
| `easyjava.db.password` | 数据库密码 | `123456` |

### 可选参数

| 参数名 | 默认值 | 说明 | 示例 |
|--------|--------|------|------|
| `easyjava.db.driver` | `com.mysql.cj.jdbc.Driver` | 数据库驱动类名 | `com.mysql.cj.jdbc.Driver` |
| `easyjava.package.base` | `com.example` | 生成代码的包名前缀 | `com.yourcompany.project` |
| `easyjava.author` | `EasyJava Generator` | 代码注释中的作者名称 | `张三` |
| `easyjava.output.path` | `src/main/java` | 代码生成输出目录 | `src/main/java` |
| `easyjava.ignore.table.prefix` | `true` | 是否忽略表前缀 | `true` |
| `easyjava.table.prefix` | 无 | 表前缀列表（逗号分隔） | `sys_,biz_` |
| `easyjava.include.tables` | 无 | 需要包含的表名列表（逗号分隔） | `user,order,product` |
| `easyjava.exclude.tables` | 无 | 需要排除的表名列表（逗号分隔） | `temp_table,log_table` |
| `easyjava.sharding.enabled` | `false` | 是否启用分表功能 | `true` |

## 📁 生成的代码结构
        └── UserController.java    # 控制器
```

## 分表功能

当启用 `shardingEnabled=true` 时，插件会自动检测适合分表的字段，并生成分表相关代码：

- **时间分表**: datetime、timestamp、date类型字段
- **哈希分表**: 字符串、ID类型字段  
- **范围分表**: 数值类型字段

## 编译、安装和使用

这个项目是一个Maven插件，它会被编译成一个JAR包。其他项目可以通过在`pom.xml`中引用这个插件来使用它的功能，而不需要直接依赖JAR包。

### 1. 编译和安装到本地Maven仓库

**重要提示**：您需要在 **本插件的源代码根目录**（即包含 `pom.xml` 文件的 `easyjava-maven-plugin` 项目目录）下执行此命令，而不是在生成的JAR包所在的目录。

在命令行中，进入插件项目的根目录，然后执行以下命令：

```bash
# 清理、编译、测试并将插件安装到本地Maven仓库
mvn clean install
```

这个命令会完成以下操作：
- **clean**: 清理`target`目录。
- **compile**: 编译插件的Java源代码。
- **test**: 运行所有测试用例。
- **package**: 将编译后的代码打包成一个JAR文件，这个JAR就是插件本身。
- **install**: 将打包好的插件JAR文件 **安装** 到您的本地Maven仓库（通常位于您用户目录下的 `.m2/repository` 文件夹中）。

一旦命令成功执行，您的插件就已经在本地准备就绪，可以被任何其他本地的Maven项目引用了。

### 2. 在其他项目中使用插件

要在您的其他项目中使用`easyjava-maven-plugin`，请在该项目的`pom.xml`文件中添加以下配置：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.easyjava</groupId>
            <artifactId>easyjava-maven-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
            <configuration>
                <!-- 在这里配置插件参数 -->
                <dbUrl>jdbc:mysql://localhost:3306/your_database</dbUrl>
                <dbUsername>your_username</dbUsername>
                <dbPassword>your_password</dbPassword>
                <packageBase>com.yourcompany.yourproject</packageBase>
                <!-- 其他参数... -->
            </configuration>
        </plugin>
    </plugins>
</build>
```

**重要提示**：
- 确保`<groupId>`, `<artifactId>`, 和 `<version>`与插件项目的`pom.xml`中的定义完全一致。
- 由于版本是`1.0-SNAPSHOT`，Maven会检查本地仓库中是否有更新。

### 3. 执行代码生成

配置完成后，在您的项目根目录下执行以下命令即可生成代码：

```bash
# 执行代码生成
mvn easyjava:generate
```

Maven会自动从本地仓库找到`easyjava-maven-plugin`插件并执行其`generate`目标。

### 4. 分享插件给他人

如果您想让其他人使用您的插件，有两种主要方法：

#### 方法一：手动安装（适用于快速测试和内部共享）

这是最简单直接的方法，但需要对方手动操作。

1.  **提供插件文件**：
    *   首先，在本插件项目根目录执行 `mvn clean package` 来生成JAR文件。
    *   将生成的JAR文件（位于 `target/easyjava-maven-plugin-1.0-SNAPSHOT.jar`）和本项目的 `pom.xml` 文件发给您的同事或朋友。

2.  **接收方执行安装命令**：
    接收方需要在他们的电脑上打开命令行，然后执行以下命令，将插件手动安装到他们自己的本地Maven仓库。

    ```bash
    mvn install:install-file \
       -Dfile=/path/to/easyjava-maven-plugin-1.0-SNAPSHOT.jar \
       -DpomFile=/path/to/pom.xml
    ```
    **注意**:
    - `-Dfile`: 需要替换为JAR文件的 **绝对路径**。
    - `-DpomFile`: 需要替换为`pom.xml`文件的 **绝对路径**。

    安装成功后，他们就可以像您一样在自己的项目中使用这个插件了。

#### 方法二：部署到远程仓库（推荐的标准做法）

这是企业级和开源项目的标准做法，可以更好地管理版本和依赖。

1.  **配置远程仓库**：
    在插件项目的 `pom.xml` 中，添加 `<distributionManagement>` 部分，指向您的私有Maven仓库（如 Nexus, Artifactory）或公共仓库（如 Maven Central）。

    ```xml
    <distributionManagement>
        <repository>
            <id>your-repo-id</id>
            <name>Your Repository Name</name>
            <url>https://your-repo-url/releases</url>
        </repository>
        <snapshotRepository>
            <id>your-repo-id</id>
            <name>Your Snapshot Repository Name</name>
            <url>https://your-repo-url/snapshots</url>
        </snapshotRepository>
    </distributionManagement>
    ```

2.  **配置认证信息**：
    在您的本地 `~/.m2/settings.xml` 文件中，配置访问远程仓库所需的用户名和密码。

    ```xml
    <settings>
      <servers>
        <server>
          <id>your-repo-id</id>
          <username>your-username</username>
          <password>your-password</password>
        </server>
      </servers>
    </settings>
    ```

3.  **执行部署命令**：
    在插件项目根目录下，执行 `mvn clean deploy`。

    ```bash
    # 清理、打包并部署到远程仓库
    mvn clean deploy
    ```

4.  **其他人使用**：
    一旦部署成功，其他人只需要在他们的 `pom.xml` 中配置好您的远程仓库地址，然后就可以像使用任何中央仓库的插件一样，直接通过 `<plugin>` 标签引用您的插件，无需任何手动安装。

    *如果部署到公共的Maven中央仓库，其他人甚至不需要配置仓库地址。*

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
