package com.easyjava.bean;

/**
 * 代码生成选项配置类
 * 用于控制哪些代码需要生成
 * 
 * @author 唐伟
 * @since 2025-07-30
 */
public class GenerateOptions {
    
    /** 是否生成PO实体类 */
    private boolean generatePo = true;
    
    /** 是否生成Mapper接口 */
    private boolean generateMapper = true;
    
    /** 是否生成Mapper XML文件 */
    private boolean generateMapperXml = true;
    
    /** 是否生成Service接口 */
    private boolean generateService = true;
    
    /** 是否生成Service实现类 */
    private boolean generateServiceImpl = true;
    
    /** 是否生成Controller控制器 */
    private boolean generateController = true;
    
    /** 是否生成Query查询类 */
    private boolean generateQuery = true;
    
    /** 是否生成测试类 */
    private boolean generateTests = false;
    
    /** 是否生成Swagger注解 */
    private boolean generateSwagger = false;
    
    /** 是否覆盖已存在的文件 */
    private boolean overwriteExisting = false;
    
    /** 是否增量生成（只生成变更的表） */
    private boolean incrementalGenerate = false;
    
    /** 是否生成基础类（如果不存在的话） */
    private boolean generateBaseClasses = true;
    
    /**
     * 默认构造函数
     */
    public GenerateOptions() {
    }
    
    /**
     * 获取默认的生成选项（生成所有基础代码）
     */
    public static GenerateOptions getDefault() {
        return new GenerateOptions();
    }
    
    /**
     * 获取只生成基础代码的选项（不包括测试和文档）
     */
    public static GenerateOptions getBasicOnly() {
        GenerateOptions options = new GenerateOptions();
        options.setGenerateTests(false);
        options.setGenerateSwagger(false);
        return options;
    }
    
    /**
     * 获取完整生成选项（包括测试和文档）
     */
    public static GenerateOptions getFull() {
        GenerateOptions options = new GenerateOptions();
        options.setGenerateTests(true);
        options.setGenerateSwagger(true);
        return options;
    }
    
    /**
     * 验证生成选项的合理性
     */
    public void validate() throws IllegalArgumentException {
        if (!generatePo && !generateMapper && !generateService && !generateController) {
            throw new IllegalArgumentException("至少需要选择一种代码类型进行生成");
        }
        
        if (generateServiceImpl && !generateService) {
            throw new IllegalArgumentException("生成Service实现类需要同时生成Service接口");
        }
        
        if (generateMapperXml && !generateMapper) {
            throw new IllegalArgumentException("生成Mapper XML需要同时生成Mapper接口");
        }
    }
    
    /**
     * 打印当前配置
     */
    public void printOptions() {
        System.out.println("=== 代码生成选项 ===");
        System.out.println("PO实体类: " + (generatePo ? "✅" : "❌"));
        System.out.println("Mapper接口: " + (generateMapper ? "✅" : "❌"));
        System.out.println("Mapper XML: " + (generateMapperXml ? "✅" : "❌"));
        System.out.println("Service接口: " + (generateService ? "✅" : "❌"));
        System.out.println("Service实现: " + (generateServiceImpl ? "✅" : "❌"));
        System.out.println("Controller: " + (generateController ? "✅" : "❌"));
        System.out.println("Query查询类: " + (generateQuery ? "✅" : "❌"));
        System.out.println("测试类: " + (generateTests ? "✅" : "❌"));
        System.out.println("Swagger注解: " + (generateSwagger ? "✅" : "❌"));
        System.out.println("覆盖已存在文件: " + (overwriteExisting ? "✅" : "❌"));
        System.out.println("增量生成: " + (incrementalGenerate ? "✅" : "❌"));
        System.out.println("==================");
    }
    
    // Getters and Setters
    public boolean isGeneratePo() {
        return generatePo;
    }
    
    public void setGeneratePo(boolean generatePo) {
        this.generatePo = generatePo;
    }
    
    public boolean isGenerateMapper() {
        return generateMapper;
    }
    
    public void setGenerateMapper(boolean generateMapper) {
        this.generateMapper = generateMapper;
    }
    
    public boolean isGenerateMapperXml() {
        return generateMapperXml;
    }
    
    public void setGenerateMapperXml(boolean generateMapperXml) {
        this.generateMapperXml = generateMapperXml;
    }
    
    public boolean isGenerateService() {
        return generateService;
    }
    
    public void setGenerateService(boolean generateService) {
        this.generateService = generateService;
    }
    
    public boolean isGenerateServiceImpl() {
        return generateServiceImpl;
    }
    
    public void setGenerateServiceImpl(boolean generateServiceImpl) {
        this.generateServiceImpl = generateServiceImpl;
    }
    
    public boolean isGenerateController() {
        return generateController;
    }
    
    public void setGenerateController(boolean generateController) {
        this.generateController = generateController;
    }
    
    public boolean isGenerateQuery() {
        return generateQuery;
    }
    
    public void setGenerateQuery(boolean generateQuery) {
        this.generateQuery = generateQuery;
    }
    
    public boolean isGenerateTests() {
        return generateTests;
    }
    
    public void setGenerateTests(boolean generateTests) {
        this.generateTests = generateTests;
    }
    
    public boolean isGenerateSwagger() {
        return generateSwagger;
    }
    
    public void setGenerateSwagger(boolean generateSwagger) {
        this.generateSwagger = generateSwagger;
    }
    
    public boolean isOverwriteExisting() {
        return overwriteExisting;
    }
    
    public void setOverwriteExisting(boolean overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
    }
    
    public boolean isIncrementalGenerate() {
        return incrementalGenerate;
    }
    
    public void setIncrementalGenerate(boolean incrementalGenerate) {
        this.incrementalGenerate = incrementalGenerate;
    }
    
    public boolean isGenerateBaseClasses() {
        return generateBaseClasses;
    }
    
    public void setGenerateBaseClasses(boolean generateBaseClasses) {
        this.generateBaseClasses = generateBaseClasses;
    }
}
