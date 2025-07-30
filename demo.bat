@echo off
chcp 65001 >nul
title EasyJava 增强版功能演示

:: EasyJava 增强版功能演示脚本 (Windows版)
:: 演示所有新增功能的使用方法

echo === EasyJava 增强版功能演示 ===
echo 版本: v2.0
echo 日期: %date%
echo.

:: 设置项目路径
set PROJECT_DIR=d:\java\project\easyjava
set JAVA_CLASSPATH=%PROJECT_DIR%\target\classes

echo 项目路径: %PROJECT_DIR%
echo Java类路径: %JAVA_CLASSPATH%
echo.

:main_menu
cls
echo === EasyJava 增强版功能演示 ===
echo.
echo 请选择要演示的功能：
echo 1. 配置验证功能
echo 2. 标准代码生成
echo 3. 增量生成功能
echo 4. 测试代码生成
echo 5. SQL类型支持演示
echo 6. 模板管理工具
echo 7. 模板自定义演示
echo 8. 批量功能演示
echo 9. 查看项目统计
echo 0. 退出
echo.

set /p choice=请选择 (0-9): 

if "%choice%"=="1" goto demo_config_validation
if "%choice%"=="2" goto demo_standard_generation
if "%choice%"=="3" goto demo_incremental_generation
if "%choice%"=="4" goto demo_test_generation
if "%choice%"=="5" goto demo_sql_types
if "%choice%"=="6" goto demo_template_management
if "%choice%"=="7" goto demo_template_customization
if "%choice%"=="8" goto demo_batch_operations
if "%choice%"=="9" goto show_project_stats
if "%choice%"=="0" goto exit_program

echo 无效选择，请重新输入
timeout /t 2 >nul
goto main_menu

:demo_config_validation
cls
echo === 配置验证功能演示 ===
echo 正在验证项目配置...

cd /d "%PROJECT_DIR%"

echo 编译项目...
call mvn clean compile -q

if %errorlevel% equ 0 (
    echo ✅ 项目编译成功
    
    echo 运行配置验证...
    java -cp "%JAVA_CLASSPATH%" com.easyjava.utils.ConfigValidator
    
    echo.
    echo 配置验证功能包括：
    echo - 数据库连接测试
    echo - 路径配置验证
    echo - 包名格式检查
    echo - 生成配置完整性检查
) else (
    echo ❌ 项目编译失败，请检查环境配置
)

echo.
pause
goto main_menu

:demo_standard_generation
cls
echo === 标准代码生成演示 ===
echo 演示基础代码生成功能...

cd /d "%PROJECT_DIR%"

echo 运行标准代码生成...
java -cp "%JAVA_CLASSPATH%" com.easyjava.AppEnhanced

if %errorlevel% equ 0 (
    echo ✅ 代码生成完成
    echo.
    echo 生成的文件包括：
    echo - 实体类 (Po)
    echo - 查询参数类 (Query)
    echo - Mapper接口
    echo - Mapper XML
    echo - Service接口
    echo - Service实现
    echo - Controller
) else (
    echo ❌ 代码生成失败
)

echo.
pause
goto main_menu

:demo_incremental_generation
cls
echo === 增量生成功能演示 ===
echo 演示智能增量生成...

cd /d "%PROJECT_DIR%"

echo 第一次运行（生成缓存）...
java -cp "%JAVA_CLASSPATH%" com.easyjava.AppEnhanced --incremental

echo.
echo 第二次运行（检测变更）...
java -cp "%JAVA_CLASSPATH%" com.easyjava.AppEnhanced --incremental

echo.
echo 增量生成功能特点：
echo - 只生成变更的表
echo - 自动备份现有文件
echo - MD5校验检测变更
echo - 表结构缓存机制

echo.
pause
goto main_menu

:demo_test_generation
cls
echo === 测试代码生成演示 ===
echo 演示自动化测试代码生成...

cd /d "%PROJECT_DIR%"

echo 生成测试代码...
java -cp "%JAVA_CLASSPATH%" com.easyjava.AppEnhanced --full

if %errorlevel% equ 0 (
    echo ✅ 测试代码生成完成
    echo.
    echo 生成的测试文件：
    echo - Service单元测试
    echo - Controller集成测试
    echo - 自动生成测试数据
    echo - Spring Boot Test配置
    
    echo.
    echo 生成的测试文件列表：
    if exist "src\test" (
        dir /s /b "src\test\*.java" | findstr /v /c:".class" | more
    )
) else (
    echo ❌ 测试代码生成失败
)

echo.
pause
goto main_menu

:demo_sql_types
cls
echo === SQL类型支持演示 ===
echo 展示支持的SQL数据类型...
echo.

echo 支持的数据库类型：
echo ✅ MySQL 8.0+
echo ✅ PostgreSQL 12+
echo ✅ SQL Server 2017+
echo ✅ Oracle 19c+
echo.

echo 支持的SQL类型（部分）：
echo 整数类型: tinyint, smallint, int, bigint, serial
echo 浮点类型: float, double, real, decimal, numeric
echo 字符串: varchar, char, text, clob, nvarchar
echo 日期时间: date, datetime, timestamp, time
echo 布尔类型: boolean, bool, bit
echo 二进制: blob, binary, bytea, raw
echo 特殊类型: json, jsonb, uuid, geometry
echo.

echo 类型映射示例：
echo varchar(255)  -^> String
echo int           -^> Integer
echo bigint        -^> Long
echo decimal(10,2) -^> BigDecimal
echo datetime      -^> Date
echo json          -^> String
echo.

pause
goto main_menu

:demo_template_management
cls
echo === 模板管理工具演示 ===
echo 启动模板管理命令行工具...

cd /d "%PROJECT_DIR%"

echo 可用的模板管理命令：
echo 1. init        - 初始化自定义模板环境
echo 2. list        - 列出所有可用模板
echo 3. status      - 显示模板配置状态
echo 4. enable      - 启用指定模板类型
echo 5. disable     - 禁用指定模板类型
echo 6. copy        - 复制默认模板到自定义目录
echo 7. validate    - 验证模板语法
echo 8. generate    - 生成代码（基于模板）
echo 9. config      - 配置管理
echo.

echo 启动模板管理工具...
echo （提示：在工具中输入 'help' 查看详细帮助）
echo.

:: 启动模板管理工具
java -cp "%JAVA_CLASSPATH%" com.easyjava.AppEnhanced template

echo.
pause
goto main_menu

:demo_template_customization
cls
echo === 模板自定义演示 ===
echo 展示模板自定义功能...

:: 创建示例自定义模板目录
set CUSTOM_TEMPLATE_DIR=%PROJECT_DIR%\custom-templates

if not exist "%CUSTOM_TEMPLATE_DIR%" (
    mkdir "%CUSTOM_TEMPLATE_DIR%"
    echo 创建自定义模板目录: %CUSTOM_TEMPLATE_DIR%
)

echo.
echo 模板变量系统：
echo 全局变量: ${author}, ${date}, ${year}
echo 包路径: ${package.po}, ${package.service}
echo 表信息: ${table.beanName}, ${table.tableName}
echo 字段信息: ${field.javaType}, ${field.propertyName}
echo.

echo 示例模板片段：
echo package ${package.po};
echo.
echo /**
echo  * ${table.comment}
echo  *
echo  * @author ${author}
echo  * @date ${date}
echo  */
echo public class ${table.beanName} implements Serializable {
echo     
echo     private static final long serialVersionUID = 1L;
echo     
echo     // 字段定义...
echo }
echo.

echo 模板功能特点：
echo - 变量替换系统
echo - 条件逻辑支持
echo - 嵌套属性访问
echo - 智能导入包管理
echo - 模板语法验证

echo.
pause
goto main_menu

:demo_batch_operations
cls
echo === 批量功能演示 ===
echo 展示批量操作功能...

cd /d "%PROJECT_DIR%"

echo 批量功能包括：
echo ✅ 批量生成所有表的代码
echo ✅ 批量启用/禁用模板类型
echo ✅ 批量复制默认模板
echo ✅ 批量验证模板语法
echo.

echo 执行批量生成演示...

:: 模拟批量操作
echo 正在批量生成代码...
java -cp "%JAVA_CLASSPATH%" com.easyjava.AppEnhanced --full --force

if %errorlevel% equ 0 (
    echo ✅ 批量生成完成
    
    :: 统计生成的文件
    echo.
    echo 生成文件统计：
    if exist "src\main\java" (
        for /f %%i in ('dir /s /b "src\main\java\*.java" ^| find /c /v ""') do echo Java文件: %%i 个
    )
    
    if exist "src\main\resources" (
        for /f %%i in ('dir /s /b "src\main\resources\*.xml" ^| find /c /v ""') do echo XML文件: %%i 个
    )
    
    if exist "src\test" (
        for /f %%i in ('dir /s /b "src\test\*.java" ^| find /c /v ""') do echo 测试文件: %%i 个
    )
) else (
    echo ❌ 批量生成失败
)

echo.
pause
goto main_menu

:show_project_stats
cls
echo === 项目统计信息 ===

cd /d "%PROJECT_DIR%"

echo 项目信息：
echo 名称: EasyJava 增强版
echo 版本: v2.0
echo 作者: 唐伟
echo 构建工具: Maven
echo.

if exist "pom.xml" (
    echo Maven配置：
    findstr "maven.compiler.source" pom.xml
    findstr "project.build.sourceEncoding" pom.xml
    echo.
)

echo 代码统计：
if exist "src\main\java" (
    for /f %%i in ('dir /s /b "src\main\java\*.java" ^| find /c /v ""') do echo Java文件: %%i 个
    
    :: 计算代码行数（简化版）
    echo 正在统计代码行数...
    powershell -command "(Get-ChildItem -Path 'src\main\java' -Filter '*.java' -Recurse | Get-Content | Measure-Object -Line).Lines" 2>nul && echo 代码行数已统计
)

if exist "src\main\resources" (
    for /f %%i in ('dir /s /b "src\main\resources\*.*" ^| find /c /v ""') do echo 资源文件: %%i 个
)

echo.
echo 新增功能模块：
echo ✅ 配置验证 (ConfigValidator)
echo ✅ 增量生成 (IncrementalGenerator)
echo ✅ 测试代码生成 (BuildTest)
echo ✅ SQL类型映射 (SqlTypeMapper)
echo ✅ 模板管理 (TemplateManager)
echo ✅ 模板配置 (TemplateConfigManager)
echo ✅ 命令行工具 (TemplateCommandTool)
echo.

pause
goto main_menu

:exit_program
echo 退出演示程序
echo 感谢使用 EasyJava 增强版！
timeout /t 3 >nul
exit /b 0
