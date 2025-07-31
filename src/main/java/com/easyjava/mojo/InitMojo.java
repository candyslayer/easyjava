package com.easyjava.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 初始化EasyJava插件配置
 * 在项目中创建基础的配置文件和示例配置
 */
@Mojo(name = "init")
public class InitMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        
        getLog().info("=== 初始化EasyJava插件配置 ===");
        
        try {
            // 创建resources目录
            File resourcesDir = new File(project.getBasedir(), "src/main/resources");
            if (!resourcesDir.exists()) {
                resourcesDir.mkdirs();
                getLog().info("创建resources目录: " + resourcesDir.getPath());
            }
            
            // 创建application.properties配置文件
            createApplicationProperties(resourcesDir);
            
            // 创建分表配置文件
            createShardingConfig(resourcesDir);
            
            // 在项目根目录创建easyjava配置示例
            createEasyJavaConfig();
            
            getLog().info("=== 配置初始化完成 ===");
            getLog().info("请根据需要修改生成的配置文件：");
            getLog().info("  • src/main/resources/application.properties - 数据库连接配置");
            getLog().info("  • src/main/resources/sharding-config.properties - 分表配置");
            getLog().info("  • easyjava-config.xml - Maven插件配置示例");
            
        } catch (Exception e) {
            throw new MojoExecutionException("初始化配置失败", e);
        }
    }

    private void createApplicationProperties(File resourcesDir) throws IOException {
        File appPropsFile = new File(resourcesDir, "application.properties");
        if (appPropsFile.exists()) {
            getLog().info("application.properties 已存在，跳过创建");
            return;
        }
        
        try (FileWriter writer = new FileWriter(appPropsFile)) {
            writer.write("# EasyJava 数据库连接配置\n");
            writer.write("spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver\n");
            writer.write("spring.datasource.url=jdbc:mysql://127.0.0.1:3306/your_database\n");
            writer.write("spring.datasource.username=root\n");
            writer.write("spring.datasource.password=123456\n");
            writer.write("\n");
            writer.write("# 代码生成配置\n");
            writer.write("author=EasyJava Generator\n");
            writer.write("package.base=com.example\n");
            writer.write("path.base=src/main/java\n");
            writer.write("ignore.table.prefix=true\n");
            writer.write("table.prefix=sys_,t_\n");
        }
        
        getLog().info("创建 application.properties");
    }

    private void createShardingConfig(File resourcesDir) throws IOException {
        File shardingFile = new File(resourcesDir, "sharding-config.properties");
        if (shardingFile.exists()) {
            getLog().info("sharding-config.properties 已存在，跳过创建");
            return;
        }
        
        try (FileWriter writer = new FileWriter(shardingFile)) {
            writer.write("# EasyJava 分表配置\n");
            writer.write("# 是否启用分表功能 (true=启用, false=禁用)\n");
            writer.write("sharding.enabled=false\n");
            writer.write("\n");
            writer.write("# 是否启用自动检测分表 (true=启用, false=禁用)\n");
            writer.write("sharding.auto.detect=false\n");
            writer.write("\n");
            writer.write("# 是否自动创建分表 (true=启用, false=禁用)\n");
            writer.write("sharding.auto.create=true\n");
            writer.write("\n");
            writer.write("# 启用分表的表名列表（逗号分隔）\n");
            writer.write("sharding.tables=\n");
            writer.write("\n");
            writer.write("# 分表配置示例\n");
            writer.write("# user_message.sharding.field=createTime\n");
            writer.write("# user_message.sharding.strategy=time\n");
            writer.write("# user_log.sharding.field=userId\n");
            writer.write("# user_log.sharding.strategy=hash\n");
        }
        
        getLog().info("创建 sharding-config.properties");
    }

    private void createEasyJavaConfig() throws IOException {
        File configFile = new File(project.getBasedir(), "easyjava-config.xml");
        if (configFile.exists()) {
            getLog().info("easyjava-config.xml 已存在，跳过创建");
            return;
        }
        
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<!-- EasyJava Maven Plugin 配置示例 -->\n");
            writer.write("<!-- 将以下配置添加到您的 pom.xml 的 <build><plugins> 部分 -->\n");
            writer.write("\n");
            writer.write("<plugin>\n");
            writer.write("    <groupId>com.easyjava</groupId>\n");
            writer.write("    <artifactId>easyjava-maven-plugin</artifactId>\n");
            writer.write("    <version>1.0-SNAPSHOT</version>\n");
            writer.write("    <configuration>\n");
            writer.write("        <!-- 数据库连接配置 -->\n");
            writer.write("        <dbUrl>jdbc:mysql://localhost:3306/your_database</dbUrl>\n");
            writer.write("        <dbUsername>root</dbUsername>\n");
            writer.write("        <dbPassword>123456</dbPassword>\n");
            writer.write("        <dbDriver>com.mysql.cj.jdbc.Driver</dbDriver>\n");
            writer.write("        \n");
            writer.write("        <!-- 代码生成配置 -->\n");
            writer.write("        <packageBase>com.yourcompany.yourproject</packageBase>\n");
            writer.write("        <author>Your Name</author>\n");
            writer.write("        <outputPath>${project.basedir}/src/main/java</outputPath>\n");
            writer.write("        \n");
            writer.write("        <!-- 表过滤配置（可选） -->\n");
            writer.write("        <!-- <includeTables>user,role,permission</includeTables> -->\n");
            writer.write("        <!-- <excludeTables>temp_table,log_table</excludeTables> -->\n");
            writer.write("        \n");
            writer.write("        <!-- 分表配置 -->\n");
            writer.write("        <shardingEnabled>false</shardingEnabled>\n");
            writer.write("        \n");
            writer.write("        <!-- 表前缀配置 -->\n");
            writer.write("        <ignoreTablePrefix>true</ignoreTablePrefix>\n");
            writer.write("        <tablePrefix>t_,sys_</tablePrefix>\n");
            writer.write("    </configuration>\n");
            writer.write("</plugin>\n");
        }
        
        getLog().info("创建 easyjava-config.xml (配置示例)");
    }
}
