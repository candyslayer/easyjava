package com.easyjava.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.project.MavenProject;

import com.easyjava.bean.TableInfo;
import com.easyjava.builder.*;
import com.easyjava.utils.PluginConfigManager;

import java.util.List;
import java.io.File;

/**
 * EasyJava代码生成器Maven插件
 * 
 * 用于从数据库表结构生成完整的CRUD代码，包括：
 * - Entity/PO类
 * - Query参数类  
 * - Mapper接口和XML
 * - Service接口和实现类
 * - Controller控制器
 * - 支持分表功能
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class CodeGeneratorMojo extends AbstractMojo {

    /**
     * Maven项目对象
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * 数据库连接URL
     */
    @Parameter(property = "easyjava.db.url")
    private String dbUrl;

    /**
     * 数据库用户名
     */
    @Parameter(property = "easyjava.db.username")
    private String dbUsername;

    /**
     * 数据库密码
     */
    @Parameter(property = "easyjava.db.password")
    private String dbPassword;

    /**
     * 数据库驱动类名
     */
    @Parameter(property = "easyjava.db.driver", defaultValue = "com.mysql.cj.jdbc.Driver")
    private String dbDriver;

    /**
     * 生成代码的输出目录
     */
    @Parameter(property = "easyjava.output.path", defaultValue = "${project.build.directory}/generated-sources/easyjava")
    private String outputPath;

    /**
     * 作者名称
     */
    @Parameter(property = "easyjava.author", defaultValue = "EasyJava Generator")
    private String author;

    /**
     * 包名前缀
     */
    @Parameter(property = "easyjava.package.base", defaultValue = "com.example")
    private String packageBase;

    /**
     * 是否忽略表前缀
     */
    @Parameter(property = "easyjava.ignore.table.prefix", defaultValue = "true")
    private boolean ignoreTablePrefix;

    /**
     * 表前缀列表（逗号分隔）
     */
    @Parameter(property = "easyjava.table.prefix")
    private String tablePrefix;

    /**
     * 是否启用分表功能
     */
    @Parameter(property = "easyjava.sharding.enabled", defaultValue = "false")
    private boolean shardingEnabled;

    /**
     * 需要包含的表名列表（逗号分隔），为空则生成所有表
     */
    @Parameter(property = "easyjava.include.tables")
    private String includeTables;

    /**
     * 需要排除的表名列表（逗号分隔）
     */
    @Parameter(property = "easyjava.exclude.tables")
    private String excludeTables;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        
        getLog().info("=== EasyJava代码生成器开始执行 ===");
        
        try {
            // 启用插件模式，完全脱离application.properties文件
            com.easyjava.utils.PropertiesUtils.enablePluginMode();
            
            // 设置插件配置，供PropertiesUtils使用
            setSystemProperties();
            
            // 配置日志
            // LogbackConfig.configureLogback();
            
            // 创建输出目录
            File outputDir = new File(outputPath);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
                getLog().info("创建输出目录: " + outputPath);
            }
            
            // 生成基础文件
            getLog().info("生成基础文件...");
            BuildBase.execute();
            
            // 获取表信息
            getLog().info("分析数据库表结构...");
            List<TableInfo> tableInfos = BuilderTable.GetTables();
            
            if (tableInfos == null || tableInfos.isEmpty()) {
                getLog().warn("未找到任何数据库表，请检查数据库连接配置");
                return;
            }
            
            getLog().info("找到 " + tableInfos.size() + " 个表，开始生成代码...");
            
            // 为每个表生成代码
            for (TableInfo tableInfo : tableInfos) {
                String tableName = tableInfo.getTableName();
                
                // 检查是否需要生成该表
                if (!shouldGenerateTable(tableName)) {
                    getLog().debug("跳过表: " + tableName);
                    continue;
                }
                
                getLog().info("正在生成表 [" + tableName + "] 的代码...");
                
                try {
                    // 生成各种代码文件
                    BuildPo.execute(tableInfo);
                    BuildQuery.execute(tableInfo);
                    BuildMapper.execute(tableInfo);
                    BuildMapperXML.execute(tableInfo);
                    BuildService.execute(tableInfo);
                    BuildServiceImpl.execute(tableInfo);
                    BuildController.execute(tableInfo);
                    
                    getLog().info("表 [" + tableName + "] 代码生成完成");
                    
                } catch (Exception e) {
                    getLog().error("生成表 [" + tableName + "] 代码时出错: " + e.getMessage(), e);
                    throw new MojoExecutionException("代码生成失败", e);
                }
            }
            
            // 将生成的源代码目录添加到项目的源代码路径中
            project.addCompileSourceRoot(outputPath);
            
            getLog().info("=== EasyJava代码生成完成 ===");
            getLog().info("生成的代码位置: " + outputPath);
            getLog().info("共处理了 " + tableInfos.size() + " 个表");
            
        } catch (Exception e) {
            getLog().error("代码生成过程中发生错误: " + e.getMessage(), e);
            throw new MojoExecutionException("EasyJava代码生成失败", e);
        }
    }

    /**
     * 设置插件配置，供现有的PropertiesUtils使用
     */
    private void setSystemProperties() {
        // 清空并初始化默认配置
        PluginConfigManager.clearConfig();
        PluginConfigManager.initDefaultConfig();
        
        // 设置数据库配置
        if (dbUrl != null) {
            PluginConfigManager.setConfig("spring.datasource.url", dbUrl);
        }
        if (dbUsername != null) {
            PluginConfigManager.setConfig("spring.datasource.username", dbUsername);
        }
        if (dbPassword != null) {
            PluginConfigManager.setConfig("spring.datasource.password", dbPassword);
        }
        if (dbDriver != null) {
            PluginConfigManager.setConfig("spring.datasource.driver-class-name", dbDriver);
        }
        
        // 设置其他配置
        if (author != null) {
            PluginConfigManager.setConfig("auther.comment", author);
        }
        if (packageBase != null) {
            PluginConfigManager.setConfig("package.base", packageBase);
        }
        if (outputPath != null) {
            PluginConfigManager.setConfig("path.base", outputPath);
        }
        
        PluginConfigManager.setConfig("ignore.table.prefix", String.valueOf(ignoreTablePrefix));
        
        if (tablePrefix != null) {
            PluginConfigManager.setConfig("table.prefix", tablePrefix);
        }
        
        PluginConfigManager.setConfig("sharding.enabled", String.valueOf(shardingEnabled));
    }

    /**
     * 判断是否需要生成指定表的代码
     */
    private boolean shouldGenerateTable(String tableName) {
        // 如果指定了包含列表，只生成列表中的表
        if (includeTables != null && !includeTables.trim().isEmpty()) {
            String[] includes = includeTables.split(",");
            for (String include : includes) {
                if (tableName.equals(include.trim())) {
                    return true;
                }
            }
            return false;
        }
        
        // 如果指定了排除列表，不生成列表中的表
        if (excludeTables != null && !excludeTables.trim().isEmpty()) {
            String[] excludes = excludeTables.split(",");
            for (String exclude : excludes) {
                if (tableName.equals(exclude.trim())) {
                    return false;
                }
            }
        }
        
        return true;
    }
}
