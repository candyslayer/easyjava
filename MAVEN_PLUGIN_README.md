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
