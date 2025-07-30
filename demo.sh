#!/bin/bash

# EasyJava 增强版功能演示脚本
# 演示所有新增功能的使用方法

echo "=== EasyJava 增强版功能演示 ==="
echo "版本: v2.0"
echo "日期: $(date '+%Y-%m-%d')"
echo ""

# 设置项目路径
PROJECT_DIR="d:/java/project/easyjava"
JAVA_CLASSPATH="$PROJECT_DIR/target/classes"

echo "项目路径: $PROJECT_DIR"
echo "Java类路径: $JAVA_CLASSPATH"
echo ""

# 功能演示菜单
show_menu() {
    echo "请选择要演示的功能："
    echo "1. 配置验证功能"
    echo "2. 标准代码生成"
    echo "3. 增量生成功能"
    echo "4. 测试代码生成"
    echo "5. SQL类型支持演示"
    echo "6. 模板管理工具"
    echo "7. 模板自定义演示"
    echo "8. 批量功能演示"
    echo "9. 查看项目统计"
    echo "0. 退出"
    echo ""
}

# 配置验证演示
demo_config_validation() {
    echo "=== 配置验证功能演示 ==="
    echo "正在验证项目配置..."
    
    cd "$PROJECT_DIR"
    
    # 编译项目
    echo "编译项目..."
    mvn clean compile -q
    
    if [ $? -eq 0 ]; then
        echo "✅ 项目编译成功"
        
        # 运行配置验证
        echo "运行配置验证..."
        java -cp "$JAVA_CLASSPATH" com.easyjava.utils.ConfigValidator
        
        echo ""
        echo "配置验证功能包括："
        echo "- 数据库连接测试"
        echo "- 路径配置验证"
        echo "- 包名格式检查"
        echo "- 生成配置完整性检查"
    else
        echo "❌ 项目编译失败，请检查环境配置"
    fi
    
    echo ""
    read -p "按Enter键继续..."
}

# 标准代码生成演示
demo_standard_generation() {
    echo "=== 标准代码生成演示 ==="
    echo "演示基础代码生成功能..."
    
    cd "$PROJECT_DIR"
    
    echo "运行标准代码生成..."
    java -cp "$JAVA_CLASSPATH" com.easyjava.AppEnhanced
    
    if [ $? -eq 0 ]; then
        echo "✅ 代码生成完成"
        echo ""
        echo "生成的文件包括："
        echo "- 实体类 (Po)"
        echo "- 查询参数类 (Query)"
        echo "- Mapper接口"
        echo "- Mapper XML"
        echo "- Service接口"
        echo "- Service实现"
        echo "- Controller"
    else
        echo "❌ 代码生成失败"
    fi
    
    echo ""
    read -p "按Enter键继续..."
}

# 增量生成演示
demo_incremental_generation() {
    echo "=== 增量生成功能演示 ==="
    echo "演示智能增量生成..."
    
    cd "$PROJECT_DIR"
    
    echo "第一次运行（生成缓存）..."
    java -cp "$JAVA_CLASSPATH" com.easyjava.AppEnhanced --incremental
    
    echo ""
    echo "第二次运行（检测变更）..."
    java -cp "$JAVA_CLASSPATH" com.easyjava.AppEnhanced --incremental
    
    echo ""
    echo "增量生成功能特点："
    echo "- 只生成变更的表"
    echo "- 自动备份现有文件"
    echo "- MD5校验检测变更"
    echo "- 表结构缓存机制"
    
    echo ""
    read -p "按Enter键继续..."
}

# 测试代码生成演示
demo_test_generation() {
    echo "=== 测试代码生成演示 ==="
    echo "演示自动化测试代码生成..."
    
    cd "$PROJECT_DIR"
    
    echo "生成测试代码..."
    java -cp "$JAVA_CLASSPATH" com.easyjava.AppEnhanced --full
    
    if [ $? -eq 0 ]; then
        echo "✅ 测试代码生成完成"
        echo ""
        echo "生成的测试文件："
        echo "- Service单元测试"
        echo "- Controller集成测试"
        echo "- 自动生成测试数据"
        echo "- Spring Boot Test配置"
        
        # 显示生成的测试文件
        if [ -d "$PROJECT_DIR/src/test" ]; then
            echo ""
            echo "生成的测试文件列表："
            find "$PROJECT_DIR/src/test" -name "*.java" -type f | head -10
        fi
    else
        echo "❌ 测试代码生成失败"
    fi
    
    echo ""
    read -p "按Enter键继续..."
}

# SQL类型支持演示
demo_sql_types() {
    echo "=== SQL类型支持演示 ==="
    echo "展示支持的SQL数据类型..."
    
    echo ""
    echo "支持的数据库类型："
    echo "✅ MySQL 8.0+"
    echo "✅ PostgreSQL 12+"
    echo "✅ SQL Server 2017+"
    echo "✅ Oracle 19c+"
    echo ""
    
    echo "支持的SQL类型（部分）："
    echo "整数类型: tinyint, smallint, int, bigint, serial"
    echo "浮点类型: float, double, real, decimal, numeric"
    echo "字符串: varchar, char, text, clob, nvarchar"
    echo "日期时间: date, datetime, timestamp, time"
    echo "布尔类型: boolean, bool, bit"
    echo "二进制: blob, binary, bytea, raw"
    echo "特殊类型: json, jsonb, uuid, geometry"
    echo ""
    
    echo "类型映射示例："
    echo "varchar(255)  -> String"
    echo "int           -> Integer"
    echo "bigint        -> Long"
    echo "decimal(10,2) -> BigDecimal"
    echo "datetime      -> Date"
    echo "json          -> String"
    echo ""
    
    read -p "按Enter键继续..."
}

# 模板管理工具演示
demo_template_management() {
    echo "=== 模板管理工具演示 ==="
    echo "启动模板管理命令行工具..."
    
    cd "$PROJECT_DIR"
    
    echo "可用的模板管理命令："
    echo "1. init        - 初始化自定义模板环境"
    echo "2. list        - 列出所有可用模板"
    echo "3. status      - 显示模板配置状态"
    echo "4. enable      - 启用指定模板类型"
    echo "5. disable     - 禁用指定模板类型"
    echo "6. copy        - 复制默认模板到自定义目录"
    echo "7. validate    - 验证模板语法"
    echo "8. generate    - 生成代码（基于模板）"
    echo "9. config      - 配置管理"
    echo ""
    
    echo "启动模板管理工具..."
    echo "（提示：在工具中输入 'help' 查看详细帮助）"
    echo ""
    
    # 启动模板管理工具
    java -cp "$JAVA_CLASSPATH" com.easyjava.AppEnhanced template
    
    echo ""
    read -p "按Enter键继续..."
}

# 模板自定义演示
demo_template_customization() {
    echo "=== 模板自定义演示 ==="
    echo "展示模板自定义功能..."
    
    # 创建示例自定义模板目录
    CUSTOM_TEMPLATE_DIR="$PROJECT_DIR/custom-templates"
    
    if [ ! -d "$CUSTOM_TEMPLATE_DIR" ]; then
        mkdir -p "$CUSTOM_TEMPLATE_DIR"
        echo "创建自定义模板目录: $CUSTOM_TEMPLATE_DIR"
    fi
    
    echo ""
    echo "模板变量系统："
    echo "全局变量: \${author}, \${date}, \${year}"
    echo "包路径: \${package.po}, \${package.service}"
    echo "表信息: \${table.beanName}, \${table.tableName}"
    echo "字段信息: \${field.javaType}, \${field.propertyName}"
    echo ""
    
    echo "示例模板片段："
    cat << 'EOF'
package ${package.po};

/**
 * ${table.comment}
 *
 * @author ${author}
 * @date ${date}
 */
public class ${table.beanName} implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 字段定义...
}
EOF
    
    echo ""
    echo "模板功能特点："
    echo "- 变量替换系统"
    echo "- 条件逻辑支持"
    echo "- 嵌套属性访问"
    echo "- 智能导入包管理"
    echo "- 模板语法验证"
    
    echo ""
    read -p "按Enter键继续..."
}

# 批量功能演示
demo_batch_operations() {
    echo "=== 批量功能演示 ==="
    echo "展示批量操作功能..."
    
    cd "$PROJECT_DIR"
    
    echo "批量功能包括："
    echo "✅ 批量生成所有表的代码"
    echo "✅ 批量启用/禁用模板类型"
    echo "✅ 批量复制默认模板"
    echo "✅ 批量验证模板语法"
    echo ""
    
    echo "执行批量生成演示..."
    
    # 模拟批量操作
    echo "正在批量生成代码..."
    java -cp "$JAVA_CLASSPATH" com.easyjava.AppEnhanced --full --force
    
    if [ $? -eq 0 ]; then
        echo "✅ 批量生成完成"
        
        # 统计生成的文件
        echo ""
        echo "生成文件统计："
        if [ -d "$PROJECT_DIR/src/main/java" ]; then
            JAVA_COUNT=$(find "$PROJECT_DIR/src/main/java" -name "*.java" -type f | wc -l)
            echo "Java文件: $JAVA_COUNT 个"
        fi
        
        if [ -d "$PROJECT_DIR/src/main/resources" ]; then
            XML_COUNT=$(find "$PROJECT_DIR/src/main/resources" -name "*.xml" -type f | wc -l)
            echo "XML文件: $XML_COUNT 个"
        fi
        
        if [ -d "$PROJECT_DIR/src/test" ]; then
            TEST_COUNT=$(find "$PROJECT_DIR/src/test" -name "*.java" -type f | wc -l)
            echo "测试文件: $TEST_COUNT 个"
        fi
    else
        echo "❌ 批量生成失败"
    fi
    
    echo ""
    read -p "按Enter键继续..."
}

# 项目统计
show_project_stats() {
    echo "=== 项目统计信息 ==="
    
    cd "$PROJECT_DIR"
    
    echo "项目信息："
    echo "名称: EasyJava 增强版"
    echo "版本: v2.0"
    echo "作者: 唐伟"
    echo "构建工具: Maven"
    echo ""
    
    if [ -f "pom.xml" ]; then
        echo "Maven配置："
        echo "Java版本: $(grep -o '<maven.compiler.source>[^<]*' pom.xml | cut -d'>' -f2)"
        echo "编码: $(grep -o '<project.build.sourceEncoding>[^<]*' pom.xml | cut -d'>' -f2)"
        echo ""
    fi
    
    echo "代码统计："
    if [ -d "src/main/java" ]; then
        JAVA_FILES=$(find src/main/java -name "*.java" -type f | wc -l)
        JAVA_LINES=$(find src/main/java -name "*.java" -exec wc -l {} + | tail -1 | awk '{print $1}')
        echo "Java文件: $JAVA_FILES 个"
        echo "代码行数: $JAVA_LINES 行"
    fi
    
    if [ -d "src/main/resources" ]; then
        RESOURCE_FILES=$(find src/main/resources -type f | wc -l)
        echo "资源文件: $RESOURCE_FILES 个"
    fi
    
    echo ""
    echo "新增功能模块："
    echo "✅ 配置验证 (ConfigValidator)"
    echo "✅ 增量生成 (IncrementalGenerator)"
    echo "✅ 测试代码生成 (BuildTest)"
    echo "✅ SQL类型映射 (SqlTypeMapper)"
    echo "✅ 模板管理 (TemplateManager)"
    echo "✅ 模板配置 (TemplateConfigManager)"
    echo "✅ 命令行工具 (TemplateCommandTool)"
    echo ""
    
    read -p "按Enter键继续..."
}

# 主循环
main() {
    while true; do
        clear
        echo "=== EasyJava 增强版功能演示 ==="
        echo ""
        show_menu
        
        read -p "请选择 (0-9): " choice
        
        case $choice in
            1)
                clear
                demo_config_validation
                ;;
            2)
                clear
                demo_standard_generation
                ;;
            3)
                clear
                demo_incremental_generation
                ;;
            4)
                clear
                demo_test_generation
                ;;
            5)
                clear
                demo_sql_types
                ;;
            6)
                clear
                demo_template_management
                ;;
            7)
                clear
                demo_template_customization
                ;;
            8)
                clear
                demo_batch_operations
                ;;
            9)
                clear
                show_project_stats
                ;;
            0)
                echo "退出演示程序"
                exit 0
                ;;
            *)
                echo "无效选择，请重新输入"
                sleep 2
                ;;
        esac
    done
}

# 检查环境
check_environment() {
    echo "检查运行环境..."
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        echo "❌ 未找到Java环境，请安装Java 21+"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | grep -o '"[0-9]\+' | cut -d'"' -f2)
    echo "✅ Java版本: $JAVA_VERSION"
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        echo "❌ 未找到Maven，请安装Maven 3.6+"
        exit 1
    fi
    
    MVN_VERSION=$(mvn -version | grep "Apache Maven" | cut -d' ' -f3)
    echo "✅ Maven版本: $MVN_VERSION"
    
    # 检查项目目录
    if [ ! -d "$PROJECT_DIR" ]; then
        echo "❌ 项目目录不存在: $PROJECT_DIR"
        echo "请修改脚本中的PROJECT_DIR变量"
        exit 1
    fi
    
    echo "✅ 项目目录: $PROJECT_DIR"
    echo ""
}

# 启动演示
echo "EasyJava 增强版功能演示"
echo "========================="
check_environment
echo "环境检查完成，启动演示程序..."
echo ""
sleep 2

main
