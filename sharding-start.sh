#!/bin/bash
# EasyJava 分表功能快速启动脚本
# 使用方法: ./sharding-start.sh [命令]

echo "============================================"
echo "     EasyJava 分表功能快速启动脚本"
echo "============================================"
echo

# 检查是否有Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请先安装Java"
    exit 1
fi

# 检查项目是否已编译
if [ ! -d "target/classes" ]; then
    echo "正在编译项目..."
    mvn clean compile
    if [ $? -ne 0 ]; then
        echo "错误: 项目编译失败"
        exit 1
    fi
fi

# 根据参数执行不同命令
case "${1:-auto}" in
    "auto")
        echo "启动自动分表功能..."
        java -cp target/classes com.easyjava.manager.ShardingBootstrap auto
        ;;
    "config")
        echo "启动分表配置..."
        java -cp target/classes com.easyjava.manager.ShardingManager config
        ;;
    "check")
        echo "检查分表配置..."
        java -cp target/classes com.easyjava.manager.ShardingBootstrap check
        ;;
    "show")
        echo "显示分表配置..."
        java -cp target/classes com.easyjava.manager.ShardingBootstrap show
        ;;
    "test")
        echo "测试分表功能..."
        java -cp target/classes com.easyjava.manager.ShardingBootstrap test
        ;;
    "help"|*)
        echo
        echo "用法: ./sharding-start.sh [命令]"
        echo
        echo "可用命令:"
        echo "  auto     - 自动创建分表 (默认)"
        echo "  config   - 交互式配置分表"
        echo "  check    - 检查分表配置"
        echo "  show     - 显示当前配置"
        echo "  test     - 测试分表功能"
        echo "  help     - 显示此帮助信息"
        echo
        echo "示例:"
        echo "  ./sharding-start.sh auto"
        echo "  ./sharding-start.sh config"
        echo "  ./sharding-start.sh check"
        echo
        ;;
esac

echo
echo "操作完成"
