package com.easyjava.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * 显示EasyJava插件的帮助信息
 */
@Mojo(name = "help")
public class HelpMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        
        getLog().info("=== EasyJava Maven Plugin 帮助信息 ===");
        getLog().info("");
        getLog().info("这是一个强大的Java代码生成插件，可以从数据库表结构自动生成完整的CRUD代码。");
        getLog().info("");
        getLog().info("主要功能：");
        getLog().info("  • 自动生成Entity、Query、Mapper、Service、Controller等完整分层代码");
        getLog().info("  • 支持分表功能（时间分表、哈希分表、范围分表）");
        getLog().info("  • 支持多种数据库类型映射");
        getLog().info("  • 高度可配置的代码生成选项");
        getLog().info("");
        getLog().info("可用目标（Goals）：");
        getLog().info("  easyjava:generate  - 生成CRUD代码");
        getLog().info("  easyjava:help      - 显示此帮助信息");
        getLog().info("");
        getLog().info("基本用法：");
        getLog().info("  mvn easyjava:generate");
        getLog().info("");
        getLog().info("带参数的用法：");
        getLog().info("  mvn easyjava:generate \\");
        getLog().info("    -Deasyjava.db.url=jdbc:mysql://localhost:3306/yourdb \\");
        getLog().info("    -Deasyjava.db.username=root \\");
        getLog().info("    -Deasyjava.db.password=123456 \\");
        getLog().info("    -Deasyjava.package.base=com.yourcompany.yourproject");
        getLog().info("");
        getLog().info("主要配置参数：");
        getLog().info("  dbUrl              - 数据库连接URL（必需）");
        getLog().info("  dbUsername         - 数据库用户名（必需）");
        getLog().info("  dbPassword         - 数据库密码（必需）");
        getLog().info("  packageBase        - 生成代码的包名前缀");
        getLog().info("  author             - 代码注释中的作者名称");
        getLog().info("  outputPath         - 代码生成输出目录");
        getLog().info("  shardingEnabled    - 是否启用分表功能");
        getLog().info("  includeTables      - 需要包含的表名列表（逗号分隔）");
        getLog().info("  excludeTables      - 需要排除的表名列表（逗号分隔）");
        getLog().info("");
        getLog().info("详细文档请参考：MAVEN_PLUGIN_README.md");
        getLog().info("");
        getLog().info("=== 帮助信息结束 ===");
    }
}
