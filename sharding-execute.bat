@echo off
REM EasyJava 分表直接执行脚本
REM 直接读取配置文件执行分表，无需交互

echo ============================================
echo       EasyJava 分表直接执行脚本
echo ============================================
echo 直接读取配置文件执行分表功能，无需交互配置
echo.

REM 检查是否有Java环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请先安装Java
    pause
    exit /b 1
)

REM 检查项目是否已编译
if not exist "target\classes" (
    echo 正在编译项目...
    mvn clean compile
    if %errorlevel% neq 0 (
        echo 错误: 项目编译失败
        pause
        exit /b 1
    )
)

REM 根据参数执行不同命令
if "%1"=="" goto execute
if "%1"=="execute" goto execute
if "%1"=="run" goto execute
if "%1"=="validate" goto validate
if "%1"=="show" goto show
if "%1"=="test" goto test
if "%1"=="help" goto help
goto help

:execute
echo 正在执行分表创建...
echo 读取配置文件: src/main/resources/application.properties
echo.
java -cp target/classes com.easyjava.sharding.ShardingExecutor execute
goto end

:validate
echo 正在验证分表配置...
java -cp target/classes com.easyjava.sharding.ShardingExecutor validate
goto end

:show
echo 显示当前分表配置...
java -cp target/classes com.easyjava.sharding.ShardingExecutor show
goto end

:test
echo 测试分表索引计算...
java -cp target/classes com.easyjava.sharding.ShardingExecutor test
goto end

:help
echo.
echo 用法: sharding-execute.bat [命令]
echo.
echo 可用命令:
echo   execute   - 执行分表创建 (默认)
echo   run       - 执行分表创建 (同 execute)
echo   validate  - 验证分表配置
echo   show      - 显示当前配置
echo   test      - 测试分表索引计算
echo   help      - 显示此帮助信息
echo.
echo 示例:
echo   sharding-execute.bat
echo   sharding-execute.bat execute
echo   sharding-execute.bat validate
echo.
echo 说明:
echo   此脚本直接读取 application.properties 中的分表配置
echo   无需交互，适合在自动化部署或CI/CD中使用
echo.
echo 配置示例:
echo   sharding.enabled=true
echo   sharding.strategy.type=hash
echo   sharding.table.count=8
echo   sharding.field.users=id
echo.
goto end

:end
echo.
echo 操作完成，按任意键退出...
pause >nul
