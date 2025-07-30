package com.easyjava.manager;

import com.easyjava.bean.TableInfo;
import com.easyjava.builder.BuilderTable;
import com.easyjava.builder.TemplateBasedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Scanner;

/**
 * 模板命令行工具
 * 
 * @author EasyJava
 * @since 2025-07-30
 */
public class TemplateCommandTool {
    
    private static final Logger logger = LoggerFactory.getLogger(TemplateCommandTool.class);
    private static final Scanner scanner = new Scanner(System.in);
    
    /**
     * 启动命令行工具
     */
    public static void start() {
        System.out.println("=== EasyJava 模板管理工具 ===");
        System.out.println("支持的命令：");
        System.out.println("1. init        - 初始化自定义模板环境");
        System.out.println("2. list        - 列出所有可用模板");
        System.out.println("3. status      - 显示模板配置状态");
        System.out.println("4. enable      - 启用指定模板类型");
        System.out.println("5. disable     - 禁用指定模板类型");
        System.out.println("6. copy        - 复制默认模板到自定义目录");
        System.out.println("7. validate    - 验证模板语法");
        System.out.println("8. generate    - 生成代码（基于模板）");
        System.out.println("9. config      - 配置管理");
        System.out.println("0. exit        - 退出");
        System.out.println("==============================");
        
        while (true) {
            System.out.print("\n请输入命令: ");
            String command = scanner.nextLine().trim();
            
            try {
                switch (command) {
                    case "1":
                    case "init":
                        initializeTemplateEnvironment();
                        break;
                    case "2":
                    case "list":
                        listTemplates();
                        break;
                    case "3":
                    case "status":
                        showStatus();
                        break;
                    case "4":
                    case "enable":
                        enableTemplate();
                        break;
                    case "5":
                    case "disable":
                        disableTemplate();
                        break;
                    case "6":
                    case "copy":
                        copyTemplate();
                        break;
                    case "7":
                    case "validate":
                        validateTemplate();
                        break;
                    case "8":
                    case "generate":
                        generateCode();
                        break;
                    case "9":
                    case "config":
                        configManagement();
                        break;
                    case "0":
                    case "exit":
                        System.out.println("退出模板管理工具");
                        return;
                    case "help":
                        showHelp();
                        break;
                    default:
                        System.out.println("未知命令: " + command + "，输入 help 查看帮助");
                }
            } catch (Exception e) {
                System.err.println("执行命令失败: " + e.getMessage());
                logger.error("命令执行异常", e);
            }
        }
    }
    
    /**
     * 初始化模板环境
     */
    private static void initializeTemplateEnvironment() {
        System.out.print("是否初始化自定义模板环境？(y/n): ");
        String confirm = scanner.nextLine().trim();
        
        if ("y".equalsIgnoreCase(confirm) || "yes".equalsIgnoreCase(confirm)) {
            TemplateConfigManager.initializeCustomTemplateEnvironment();
            System.out.println("自定义模板环境初始化完成");
        } else {
            System.out.println("操作已取消");
        }
    }
    
    /**
     * 列出模板
     */
    private static void listTemplates() {
        System.out.println("\n=== 可用模板列表 ===");
        List<String> templates = TemplateManager.listAvailableTemplates();
        
        if (templates.isEmpty()) {
            System.out.println("未找到可用模板");
            return;
        }
        
        for (int i = 0; i < templates.size(); i++) {
            System.out.println((i + 1) + ". " + templates.get(i));
        }
        
        System.out.println("\n=== 模板类型配置 ===");
        TemplateConfigManager.getTemplateTypes().forEach((type, config) -> {
            boolean enabled = TemplateConfigManager.isTemplateEnabled(type);
            System.out.println(type + " (" + config.getDescription() + "): " + 
                             (enabled ? "启用" : "禁用"));
        });
    }
    
    /**
     * 显示状态
     */
    private static void showStatus() {
        System.out.println("\n" + TemplateConfigManager.generateConfigReport());
    }
    
    /**
     * 启用模板
     */
    private static void enableTemplate() {
        System.out.println("\n可用的模板类型:");
        showTemplateTypes();
        
        System.out.print("请输入要启用的模板类型: ");
        String templateType = scanner.nextLine().trim();
        
        if (TemplateConfigManager.getTemplateTypes().containsKey(templateType)) {
            TemplateConfigManager.setTemplateEnabled(templateType, true);
            TemplateConfigManager.saveTemplateConfig();
            System.out.println("模板类型 " + templateType + " 已启用");
        } else {
            System.out.println("无效的模板类型: " + templateType);
        }
    }
    
    /**
     * 禁用模板
     */
    private static void disableTemplate() {
        System.out.println("\n可用的模板类型:");
        showTemplateTypes();
        
        System.out.print("请输入要禁用的模板类型: ");
        String templateType = scanner.nextLine().trim();
        
        if (TemplateConfigManager.getTemplateTypes().containsKey(templateType)) {
            TemplateConfigManager.setTemplateEnabled(templateType, false);
            TemplateConfigManager.saveTemplateConfig();
            System.out.println("模板类型 " + templateType + " 已禁用");
        } else {
            System.out.println("无效的模板类型: " + templateType);
        }
    }
    
    /**
     * 复制模板
     */
    private static void copyTemplate() {
        System.out.println("\n可用的模板:");
        List<String> templates = TemplateManager.listAvailableTemplates();
        
        for (int i = 0; i < templates.size(); i++) {
            System.out.println((i + 1) + ". " + templates.get(i));
        }
        
        System.out.print("请输入要复制的模板编号或名称: ");
        String input = scanner.nextLine().trim();
        
        String templateName = null;
        try {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < templates.size()) {
                templateName = templates.get(index);
            }
        } catch (NumberFormatException e) {
            if (templates.contains(input)) {
                templateName = input;
            }
        }
        
        if (templateName != null) {
            TemplateManager.copyTemplateToCustom(templateName);
            System.out.println("模板 " + templateName + " 已复制到自定义目录");
        } else {
            System.out.println("无效的模板选择: " + input);
        }
    }
    
    /**
     * 验证模板
     */
    private static void validateTemplate() {
        System.out.print("请输入模板文件路径: ");
        String templatePath = scanner.nextLine().trim();
        
        try {
            File templateFile = new File(templatePath);
            if (!templateFile.exists()) {
                System.out.println("模板文件不存在: " + templatePath);
                return;
            }
            
            String content = new String(java.nio.file.Files.readAllBytes(templateFile.toPath()));
            TemplateManager.TemplateValidationResult result = TemplateManager.validateTemplate(content);
            
            if (result.isValid()) {
                System.out.println("模板语法验证通过");
                System.out.println("发现变量: " + result.getVariables());
            } else {
                System.out.println("模板语法验证失败:");
                result.getErrors().forEach(System.out::println);
            }
        } catch (Exception e) {
            System.err.println("验证模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成代码
     */
    private static void generateCode() {
        System.out.print("请输入表名: ");
        String tableName = scanner.nextLine().trim();
        
        if (org.apache.commons.lang3.StringUtils.isEmpty(tableName)) {
            System.out.println("表名不能为空");
            return;
        }
        
        try {
            // 获取表信息
            List<TableInfo> tableInfoList = BuilderTable.GetTables();
            TableInfo targetTable = null;
            
            for (TableInfo tableInfo : tableInfoList) {
                if (tableName.equalsIgnoreCase(tableInfo.getTableName())) {
                    targetTable = tableInfo;
                    break;
                }
            }
            
            if (targetTable == null) {
                System.out.println("未找到表: " + tableName);
                return;
            }
            
            System.out.println("\n生成选项:");
            System.out.println("1. 生成所有启用的模板");
            System.out.println("2. 选择特定模板类型");
            
            System.out.print("请选择: ");
            String choice = scanner.nextLine().trim();
            
            if ("1".equals(choice)) {
                TemplateBasedBuilder.generateAllEnabledTemplates(targetTable);
                System.out.println("代码生成完成");
            } else if ("2".equals(choice)) {
                System.out.println("\n可用的模板类型:");
                showTemplateTypes();
                
                System.out.print("请输入模板类型: ");
                String templateType = scanner.nextLine().trim();
                
                if (TemplateConfigManager.getTemplateTypes().containsKey(templateType)) {
                    String outputPath = System.getProperty("user.dir") + File.separator + "generated";
                    TemplateBasedBuilder.generateWithTemplate(targetTable, templateType, outputPath);
                    System.out.println("代码生成完成，输出目录: " + outputPath);
                } else {
                    System.out.println("无效的模板类型: " + templateType);
                }
            } else {
                System.out.println("无效的选择");
            }
            
        } catch (Exception e) {
            System.err.println("生成代码失败: " + e.getMessage());
        }
    }
    
    /**
     * 配置管理
     */
    private static void configManagement() {
        System.out.println("\n=== 配置管理 ===");
        System.out.println("1. 查看当前配置");
        System.out.println("2. 设置作者");
        System.out.println("3. 设置编码");
        System.out.println("4. 返回主菜单");
        
        System.out.print("请选择: ");
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                showCurrentConfig();
                break;
            case "2":
                setAuthor();
                break;
            case "3":
                setEncoding();
                break;
            case "4":
                return;
            default:
                System.out.println("无效的选择");
        }
    }
    
    /**
     * 显示当前配置
     */
    private static void showCurrentConfig() {
        System.out.println("\n当前配置:");
        System.out.println("作者: " + TemplateConfigManager.getAuthor());
        System.out.println("编码: " + TemplateConfigManager.getEncoding());
    }
    
    /**
     * 设置作者
     */
    private static void setAuthor() {
        System.out.print("请输入作者名称: ");
        String author = scanner.nextLine().trim();
        
        if (!org.apache.commons.lang3.StringUtils.isEmpty(author)) {
            TemplateConfigManager.setAuthor(author);
            TemplateConfigManager.saveTemplateConfig();
            System.out.println("作者设置成功: " + author);
        } else {
            System.out.println("作者名称不能为空");
        }
    }
    
    /**
     * 设置编码
     */
    private static void setEncoding() {
        System.out.print("请输入编码格式 (默认UTF-8): ");
        String encoding = scanner.nextLine().trim();
        
        if (org.apache.commons.lang3.StringUtils.isEmpty(encoding)) {
            encoding = "UTF-8";
        }
        
        TemplateConfigManager.setTemplateConfig("template.encoding", encoding);
        TemplateConfigManager.saveTemplateConfig();
        System.out.println("编码设置成功: " + encoding);
    }
    
    /**
     * 显示模板类型
     */
    private static void showTemplateTypes() {
        TemplateConfigManager.getTemplateTypes().forEach((type, config) -> {
            System.out.println(type + " - " + config.getDescription());
        });
    }
    
    /**
     * 显示帮助
     */
    private static void showHelp() {
        System.out.println("\n=== 帮助信息 ===");
        System.out.println("EasyJava 模板管理工具用于管理代码生成模板");
        System.out.println("支持自定义模板、模板配置、批量生成等功能");
        System.out.println();
        System.out.println("使用流程：");
        System.out.println("1. 使用 init 命令初始化自定义模板环境");
        System.out.println("2. 使用 copy 命令复制默认模板到自定义目录进行修改");
        System.out.println("3. 使用 enable/disable 命令控制模板的启用状态");
        System.out.println("4. 使用 generate 命令生成代码");
        System.out.println();
        System.out.println("模板变量说明：");
        System.out.println("${table.beanName}     - 实体类名称");
        System.out.println("${table.tableName}    - 数据库表名");
        System.out.println("${table.comment}      - 表注释");
        System.out.println("${author}             - 作者名称");
        System.out.println("${date}               - 当前日期");
        System.out.println("${package.po}         - Po包路径");
        System.out.println("更多变量请查看文档...");
    }
}
