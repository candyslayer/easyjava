package com.easyjava.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * 显示EasyJava插件的详细信息和使用说明
 */
@Mojo(name = "info")
public class InfoMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        
        printSeparator();
        getLog().info("                   EasyJava Maven Plugin v1.0-SNAPSHOT");
        getLog().info("           强大的Java代码生成器 - 从数据库到完整CRUD代码");
        printSeparator();
        
        getLog().info("");
        getLog().info("🚀 主要功能：");
        getLog().info("   ✓ 自动生成Entity/PO实体类（支持注解配置）");
        getLog().info("   ✓ 自动生成Query查询参数类（支持模糊查询、时间范围）");
        getLog().info("   ✓ 自动生成Mapper接口和XML映射文件");
        getLog().info("   ✓ 自动生成Service接口和实现类");
        getLog().info("   ✓ 自动生成Controller控制器（RESTful API）");
        getLog().info("   ✓ 自动生成基础工具类和异常处理");
        getLog().info("   ✓ 支持MySQL数据库");
        getLog().info("   ✓ 支持分表功能");
        getLog().info("");
        
        getLog().info("📋 可用命令：");
        getLog().info("   mvn easyjava:generate    生成完整CRUD代码");
        getLog().info("   mvn easyjava:init        创建配置文件模板");
        getLog().info("   mvn easyjava:info        显示此详细信息");
        getLog().info("   mvn easyjava:help        显示基本帮助信息");
        getLog().info("");
        
        getLog().info("⚡ 快速开始：");
        getLog().info("   1. 在你的Maven项目根目录运行：");
        getLog().info("      mvn easyjava:generate \\");
        getLog().info("        -Deasyjava.db.url=\"jdbc:mysql://localhost:3306/yourdb\" \\");
        getLog().info("        -Deasyjava.db.username=\"root\" \\");
        getLog().info("        -Deasyjava.db.password=\"password\" \\");
        getLog().info("        -Deasyjava.package.base=\"com.yourcompany.project\"");
        getLog().info("");
        getLog().info("   2. 代码将生成到：src/main/java/com/yourcompany/project/");
        getLog().info("");
        
        getLog().info("⚙️  配置参数：");
        printConfigParameter("easyjava.db.url", "数据库连接URL", "jdbc:mysql://localhost:3306/test", true);
        printConfigParameter("easyjava.db.username", "数据库用户名", "root", true);
        printConfigParameter("easyjava.db.password", "数据库密码", "123456", true);
        printConfigParameter("easyjava.package.base", "包名前缀", "com.example", false);
        printConfigParameter("easyjava.author", "作者名称", "Your Name", false);
        printConfigParameter("easyjava.output.path", "输出目录", "src/main/java", false);
        printConfigParameter("easyjava.ignore.table.prefix", "忽略表前缀", "true", false);
        printConfigParameter("easyjava.include.tables", "包含的表（逗号分隔）", "user,order", false);
        printConfigParameter("easyjava.exclude.tables", "排除的表（逗号分隔）", "temp_table", false);
        printConfigParameter("easyjava.sharding.enabled", "启用分表", "false", false);
        getLog().info("");
        
        getLog().info("📁 生成的代码结构：");
        getLog().info("   src/main/java/");
        getLog().info("   └── com/yourcompany/project/");
        getLog().info("       ├── entity/po/           # 实体类");
        getLog().info("       ├── entity/query/        # 查询参数类");
        getLog().info("       ├── mapper/              # Mapper接口");
        getLog().info("       ├── service/             # Service接口");
        getLog().info("       ├── service/impl/        # Service实现");
        getLog().info("       ├── controller/          # Controller类");
        getLog().info("       ├── exception/           # 异常类");
        getLog().info("       └── utils/               # 工具类");
        getLog().info("   src/main/resources/");
        getLog().info("   └── mapper/                  # MyBatis XML文件");
        getLog().info("");
        
        getLog().info("📖 示例：");
        getLog().info("   # 生成指定表的代码");
        getLog().info("   mvn easyjava:generate -Deasyjava.include.tables=\"user,order\"");
        getLog().info("");
        getLog().info("   # 生成到自定义目录");
        getLog().info("   mvn easyjava:generate -Deasyjava.output.path=\"src/main/java\"");
        getLog().info("");
        getLog().info("   # 启用分表功能");
        getLog().info("   mvn easyjava:generate -Deasyjava.sharding.enabled=true");
        getLog().info("");
        
        getLog().info("💡 提示：");
        getLog().info("   • 确保数据库连接正常且有读取权限");
        getLog().info("   • 建议先在测试项目中试用");
        getLog().info("   • 生成前会自动备份现有文件");
        getLog().info("   • 支持增量生成，不会覆盖手动修改的文件");
        getLog().info("");
        
        getLog().info("🔗 更多信息：");
        getLog().info("   GitHub: https://github.com/candyslayer/easyjava");
        getLog().info("   文档: 请查看项目根目录的 MAVEN_PLUGIN_README.md");
        getLog().info("");
        
        printSeparator();
    }
    
    private void printSeparator() {
        getLog().info("================================================================");
    }
    
    private void printConfigParameter(String param, String description, String example, boolean required) {
        String requiredText = required ? " (必需)" : " (可选)";
        getLog().info(String.format("   %-30s %s%s", param, description + requiredText, ""));
        getLog().info(String.format("   %-30s 示例: %s", "", example));
    }
}
