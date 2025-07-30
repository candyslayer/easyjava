package com.easyjava;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easyjava.bean.GenerateOptions;
import com.easyjava.bean.TableInfo;
import com.easyjava.builder.BuildBase;
import com.easyjava.builder.BuildController;
import com.easyjava.builder.BuildMapper;
import com.easyjava.builder.BuildMapperXML;
import com.easyjava.builder.BuildPo;
import com.easyjava.builder.BuildQuery;
import com.easyjava.builder.BuildService;
import com.easyjava.builder.BuildServiceImpl;
import com.easyjava.builder.BuildTest;
import com.easyjava.builder.BuilderTable;
import com.easyjava.builder.IncrementalGenerator;
import com.easyjava.manager.DynamicConfigManager;
import com.easyjava.manager.TemplateCommandTool;
import com.easyjava.utils.ConfigValidator;
import com.easyjava.utils.ConfigUtils;
import com.easyjava.utils.PropertiesUtils;

/**
 * EasyJava代码生成器主类
 * 支持配置验证、增量生成、测试代码生成等高级功能
 * 
 * @author 唐伟
 * @since 2025-07-30
 */
public class AppEnhanced {
    
    private static final Logger log = LoggerFactory.getLogger(AppEnhanced.class);
    
    public static void main(String[] args) {
        
        // 检查是否启动模板管理工具
        if (args.length > 0 && "template".equalsIgnoreCase(args[0])) {
            TemplateCommandTool.start();
            return;
        }
        
        // 检查是否启动交互式配置
        if (args.length > 0 && "config".equalsIgnoreCase(args[0])) {
            DynamicConfigManager.interactiveConfiguration();
            return;
        }
        
        // 检查是否从文件加载配置
        if (args.length > 1 && "--config".equalsIgnoreCase(args[0])) {
            DynamicConfigManager.loadConfigurationFromFile(args[1]);
        }
        
        log.info("=== EasyJava增强版代码生成器启动 ===");
        log.info("应用名称: {}", ConfigUtils.getString("spring.application.name", "EasyJava"));
        
        // 显示当前配置信息
        if (args.length > 0 && "--show-config".equalsIgnoreCase(args[0])) {
            DynamicConfigManager.showCurrentConfiguration();
            return;
        }
        
        // 配置日志
        LogbackConfig.configureLogback();
        
        // 1. 配置验证
        log.info("步骤1: 验证配置...");
        ConfigValidator.ValidationResult validationResult = ConfigValidator.validateAllConfig();
        
        if (!validationResult.isValid()) {
            log.error("配置验证失败，程序终止");
            return;
        }
        
        // 2. 创建生成选项
        GenerateOptions options = createGenerateOptions(args);
        options.printOptions();
        
        try {
            // 验证生成选项
            options.validate();
        } catch (IllegalArgumentException e) {
            log.error("生成选项配置错误: {}", e.getMessage());
            return;
        }
        
        // 3. 生成基础类（如果需要）
        if (options.isGenerateBaseClasses()) {
            log.info("步骤2: 生成基础类...");
            BuildBase.execute();
        }
        
        // 4. 获取表信息
        log.info("步骤3: 获取数据库表信息...");
        List<TableInfo> allTables = BuilderTable.GetTables();
        log.info("共发现 {} 个表", allTables.size());
        
        if (allTables.isEmpty()) {
            log.warn("没有发现任何表，程序结束");
            return;
        }
        
        // 5. 增量生成处理
        List<TableInfo> tablesToGenerate;
        if (options.isIncrementalGenerate()) {
            log.info("步骤4: 增量生成检查...");
            tablesToGenerate = IncrementalGenerator.getChangedTables(allTables, options);
        } else {
            log.info("步骤4: 完整生成模式...");
            tablesToGenerate = allTables;
        }
        
        if (tablesToGenerate.isEmpty()) {
            log.info("没有需要生成的表，程序结束");
            return;
        }
        
        // 6. 生成代码
        log.info("步骤5: 开始生成代码...");
        
        if (options.isIncrementalGenerate()) {
            IncrementalGenerator.generateChangedTables(tablesToGenerate, options);
        } else {
            generateAllTables(tablesToGenerate, options);
        }
        
        log.info("=== 代码生成完成 ===");
        printGenerationSummary(tablesToGenerate, options);
    }
    
    /**
     * 创建生成选项
     */
    private static GenerateOptions createGenerateOptions(String[] args) {
        
        GenerateOptions options = GenerateOptions.getDefault();
        
        // 解析命令行参数
        for (String arg : args) {
            switch (arg) {
                case "--full":
                    options = GenerateOptions.getFull();
                    break;
                case "--basic":
                    options = GenerateOptions.getBasicOnly();
                    break;
                case "--incremental":
                    options.setIncrementalGenerate(true);
                    break;
                case "--overwrite":
                    options.setOverwriteExisting(true);
                    break;
                case "--no-tests":
                    options.setGenerateTests(false);
                    break;
                case "--with-tests":
                    options.setGenerateTests(true);
                    break;
                case "--with-swagger":
                    options.setGenerateSwagger(true);
                    break;
                case "--force":
                    options.setIncrementalGenerate(false);
                    options.setOverwriteExisting(true);
                    break;
                case "--help":
                    printUsage();
                    System.exit(0);
                    break;
            }
        }
        
        return options;
    }
    
    /**
     * 生成所有表的代码
     */
    private static void generateAllTables(List<TableInfo> tables, GenerateOptions options) {
        
        int totalTables = tables.size();
        int currentTable = 0;
        
        for (TableInfo tableInfo : tables) {
            currentTable++;
            log.info("正在处理表 {}/{}: {}", currentTable, totalTables, tableInfo.getTableName());
            
            try {
                generateSingleTable(tableInfo, options);
                log.info("表 {} 处理完成", tableInfo.getTableName());
            } catch (Exception e) {
                log.error("处理表 {} 时发生错误", tableInfo.getTableName(), e);
            }
        }
    }
    
    /**
     * 生成单个表的代码
     */
    private static void generateSingleTable(TableInfo tableInfo, GenerateOptions options) {
        
        if (options.isGeneratePo()) {
            BuildPo.execute(tableInfo);
        }
        
        if (options.isGenerateQuery()) {
            BuildQuery.execute(tableInfo);
        }
        
        if (options.isGenerateMapper()) {
            BuildMapper.execute(tableInfo);
        }
        
        if (options.isGenerateMapperXml()) {
            BuildMapperXML.execute(tableInfo);
        }
        
        if (options.isGenerateService()) {
            BuildService.execute(tableInfo);
        }
        
        if (options.isGenerateServiceImpl()) {
            BuildServiceImpl.execute(tableInfo);
        }
        
        if (options.isGenerateController()) {
            BuildController.execute(tableInfo);
        }
        
        if (options.isGenerateTests()) {
            BuildTest.executeServiceTest(tableInfo);
            BuildTest.executeControllerTest(tableInfo);
        }
    }
    
    /**
     * 打印生成摘要
     */
    private static void printGenerationSummary(List<TableInfo> tables, GenerateOptions options) {
        log.info("");
        log.info("=== 生成摘要 ===");
        log.info("处理表数量: {}", tables.size());
        
        for (TableInfo table : tables) {
            log.info("  - {} ({})", table.getTableName(), table.getComment());
        }
        
        log.info("");
        log.info("生成的文件类型:");
        if (options.isGeneratePo()) log.info("  ✅ PO实体类");
        if (options.isGenerateQuery()) log.info("  ✅ Query查询类");
        if (options.isGenerateMapper()) log.info("  ✅ Mapper接口");
        if (options.isGenerateMapperXml()) log.info("  ✅ Mapper XML");
        if (options.isGenerateService()) log.info("  ✅ Service接口");
        if (options.isGenerateServiceImpl()) log.info("  ✅ Service实现");
        if (options.isGenerateController()) log.info("  ✅ Controller");
        if (options.isGenerateTests()) log.info("  ✅ 测试类");
        
        log.info("");
        log.info("代码输出目录: {}", PropertiesUtils.geString("path.base"));
        log.info("===============");
    }
    
    /**
     * 打印使用说明
     */
    private static void printUsage() {
        System.out.println("EasyJava增强版代码生成器");
        System.out.println("");
        System.out.println("使用方法:");
        System.out.println("  java -jar easyjava.jar [选项]");
        System.out.println("");
        System.out.println("选项:");
        System.out.println("  --full          生成完整代码（包括测试和文档）");
        System.out.println("  --basic         只生成基础代码");
        System.out.println("  --incremental   启用增量生成");
        System.out.println("  --overwrite     覆盖已存在的文件");
        System.out.println("  --no-tests      不生成测试代码");
        System.out.println("  --with-tests    生成测试代码");
        System.out.println("  --with-swagger  生成Swagger注解");
        System.out.println("  --force         强制重新生成所有文件");
        System.out.println("  --help          显示此帮助信息");
        System.out.println("");
        System.out.println("示例:");
        System.out.println("  java -jar easyjava.jar --full --incremental");
        System.out.println("  java -jar easyjava.jar --basic --overwrite");
    }
}
