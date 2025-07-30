@echo off
REM EasyJava 分表功能快速启动脚本
REM 使用方法: sharding-start.bat [命令]

echo ============================================
echo      EasyJava 分表功能快速启动脚本
echo ============================================
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
if "%1"=="" goto auto
if "%1"=="auto" goto auto
if "%1"=="config" goto config
if "%1"=="check" goto check
if "%1"=="show" goto show
if "%1"=="test" goto test
if "%1"=="help" goto help
goto help

:auto
echo 启动自动分表功能...
java -cp target/classes com.easyjava.manager.ShardingBootstrap auto
goto end

:config
echo 启动分表配置...
java -cp target/classes com.easyjava.manager.ShardingManager config
goto end

:check
echo 检查分表配置...
java -cp target/classes com.easyjava.manager.ShardingBootstrap check
goto end

:show
echo 显示分表配置...
java -cp target/classes com.easyjava.manager.ShardingBootstrap show
goto end

:test
echo 测试分表功能...
java -cp target/classes com.easyjava.manager.ShardingBootstrap test
goto end

:help
echo.
echo 用法: sharding-start.bat [命令]
echo.
echo 可用命令:
echo   auto     - 自动创建分表 (默认)
echo   config   - 交互式配置分表
echo   check    - 检查分表配置
echo   show     - 显示当前配置
echo   test     - 测试分表功能
echo   help     - 显示此帮助信息
echo.
echo 示例:
echo   sharding-start.bat auto
echo   sharding-start.bat config
echo   sharding-start.bat check
echo.
goto end

:end
echo.
echo 操作完成，按任意键退出...
pause >nul
