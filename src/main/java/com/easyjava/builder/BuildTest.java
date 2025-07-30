package com.easyjava.builder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.StringUtils;
import com.easyjava.utils.PropertiesUtils;
import com.easyjava.utils.SqlTypeMapper;

/**
 * 测试代码生成器
 * 生成JUnit测试类，包括Service测试和Controller集成测试
 * 
 * @author 唐伟
 * @since 2025-07-30
 */
public class BuildTest {
    
    private static final Logger log = LoggerFactory.getLogger(BuildTest.class);
    
    /**
     * 生成Service测试类
     * 
     * @param tableInfo 表信息
     */
    public static void executeServiceTest(TableInfo tableInfo) {
        
        // 构建测试目录路径
        String testBasePath = PropertiesUtils.geString("path.base") + "/src/test/java";
        String serviceTestPath = testBasePath + "/" + Constants.PACKAGE_SERVICE.replace(".", "/");
        File folder = new File(serviceTestPath);
        
        if (!folder.exists()) {
            folder.mkdirs();
        }
        
        String testFileName = tableInfo.getBeanName() + "ServiceTest.java";
        File testFile = new File(folder, testFileName);
        
        try (OutputStream out = new FileOutputStream(testFile);
             OutputStreamWriter outw = new OutputStreamWriter(out, "utf-8");
             BufferedWriter bw = new BufferedWriter(outw)) {
            
            // 包名
            bw.write("package " + Constants.PACKAGE_SERVICE + ";");
            bw.newLine();
            bw.newLine();
            
            // 导入语句
            writeServiceTestImports(bw, tableInfo);
            
            // 类注释和声明
            BuildComment.CreateClassComment(bw, tableInfo.getComment() + "Service测试类");
            bw.write("@SpringBootTest");
            bw.newLine();
            bw.write("@RunWith(SpringRunner.class)");
            bw.newLine();
            bw.write("@Transactional");
            bw.newLine();
            bw.write("@Rollback");
            bw.newLine();
            bw.write("public class " + tableInfo.getBeanName() + "ServiceTest {");
            bw.newLine();
            bw.newLine();
            
            // 注入Service
            String serviceName = tableInfo.getBeanName() + "Service";
            String serviceBeanName = StringUtils.lowerCaseFirstLetter(serviceName);
            
            bw.write("\t@Autowired");
            bw.newLine();
            bw.write("\tprivate " + serviceName + " " + serviceBeanName + ";");
            bw.newLine();
            bw.newLine();
            
            // 生成测试方法
            writeServiceTestMethods(bw, tableInfo, serviceBeanName);
            
            // 生成测试数据创建方法
            writeTestDataMethods(bw, tableInfo);
            
            bw.write("}");
            bw.flush();
            
            log.info("{}ServiceTest生成成功", tableInfo.getBeanName());
            
        } catch (Exception e) {
            log.error("{}ServiceTest构建失败", tableInfo.getBeanName(), e);
        }
    }
    
    /**
     * 生成Controller测试类
     * 
     * @param tableInfo 表信息
     */
    public static void executeControllerTest(TableInfo tableInfo) {
        
        // 构建测试目录路径
        String testBasePath = PropertiesUtils.geString("path.base") + "/src/test/java";
        String controllerTestPath = testBasePath + "/" + Constants.PACKAGE_CONTROLLER.replace(".", "/");
        File folder = new File(controllerTestPath);
        
        if (!folder.exists()) {
            folder.mkdirs();
        }
        
        String testFileName = tableInfo.getBeanName() + "ControllerTest.java";
        File testFile = new File(folder, testFileName);
        
        try (OutputStream out = new FileOutputStream(testFile);
             OutputStreamWriter outw = new OutputStreamWriter(out, "utf-8");
             BufferedWriter bw = new BufferedWriter(outw)) {
            
            // 包名
            bw.write("package " + Constants.PACKAGE_CONTROLLER + ";");
            bw.newLine();
            bw.newLine();
            
            // 导入语句
            writeControllerTestImports(bw, tableInfo);
            
            // 类注释和声明
            BuildComment.CreateClassComment(bw, tableInfo.getComment() + "Controller测试类");
            bw.write("@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)");
            bw.newLine();
            bw.write("@RunWith(SpringRunner.class)");
            bw.newLine();
            bw.write("@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)");
            bw.newLine();
            bw.write("@Transactional");
            bw.newLine();
            bw.write("@Rollback");
            bw.newLine();
            bw.write("public class " + tableInfo.getBeanName() + "ControllerTest {");
            bw.newLine();
            bw.newLine();
            
            // 注入TestRestTemplate
            bw.write("\t@Autowired");
            bw.newLine();
            bw.write("\tprivate TestRestTemplate restTemplate;");
            bw.newLine();
            bw.newLine();
            
            // 生成Controller测试方法
            writeControllerTestMethods(bw, tableInfo);
            
            bw.write("}");
            bw.flush();
            
            log.info("{}ControllerTest生成成功", tableInfo.getBeanName());
            
        } catch (Exception e) {
            log.error("{}ControllerTest构建失败", tableInfo.getBeanName(), e);
        }
    }
    
    /**
     * 写入Service测试的导入语句
     */
    private static void writeServiceTestImports(BufferedWriter bw, TableInfo tableInfo) throws Exception {
        bw.write("import org.junit.Test;");
        bw.newLine();
        bw.write("import org.junit.runner.RunWith;");
        bw.newLine();
        bw.write("import org.springframework.beans.factory.annotation.Autowired;");
        bw.newLine();
        bw.write("import org.springframework.boot.test.context.SpringBootTest;");
        bw.newLine();
        bw.write("import org.springframework.test.context.junit4.SpringRunner;");
        bw.newLine();
        bw.write("import org.springframework.transaction.annotation.Transactional;");
        bw.newLine();
        bw.write("import org.springframework.test.annotation.Rollback;");
        bw.newLine();
        bw.write("import static org.junit.Assert.*;");
        bw.newLine();
        bw.write("import java.util.List;");
        bw.newLine();
        bw.write("import java.util.ArrayList;");
        bw.newLine();
        bw.newLine();
        
        bw.write("import " + Constants.PACKAGE_PO + "." + tableInfo.getBeanName() + ";");
        bw.newLine();
        bw.write("import " + Constants.PACKAGE_PARAM + "." + tableInfo.getBeanParamName() + ";");
        bw.newLine();
        bw.write("import " + Constants.PACKAGE_VO + ".PaginationResultVO;");
        bw.newLine();
        bw.newLine();
    }
    
    /**
     * 写入Controller测试的导入语句
     */
    private static void writeControllerTestImports(BufferedWriter bw, TableInfo tableInfo) throws Exception {
        bw.write("import org.junit.Test;");
        bw.newLine();
        bw.write("import org.junit.runner.RunWith;");
        bw.newLine();
        bw.write("import org.springframework.beans.factory.annotation.Autowired;");
        bw.newLine();
        bw.write("import org.springframework.boot.test.context.SpringBootTest;");
        bw.newLine();
        bw.write("import org.springframework.boot.test.web.client.TestRestTemplate;");
        bw.newLine();
        bw.write("import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;");
        bw.newLine();
        bw.write("import org.springframework.test.context.junit4.SpringRunner;");
        bw.newLine();
        bw.write("import org.springframework.transaction.annotation.Transactional;");
        bw.newLine();
        bw.write("import org.springframework.test.annotation.Rollback;");
        bw.newLine();
        bw.write("import org.springframework.http.ResponseEntity;");
        bw.newLine();
        bw.write("import org.springframework.http.HttpStatus;");
        bw.newLine();
        bw.write("import static org.junit.Assert.*;");
        bw.newLine();
        bw.newLine();
        
        bw.write("import " + Constants.PACKAGE_PO + "." + tableInfo.getBeanName() + ";");
        bw.newLine();
        bw.write("import " + Constants.PACKAGE_PARAM + "." + tableInfo.getBeanParamName() + ";");
        bw.newLine();
        bw.write("import " + Constants.PACKAGE_VO + ".ResponseVO;");
        bw.newLine();
        bw.newLine();
    }
    
    /**
     * 写入Service测试方法
     */
    private static void writeServiceTestMethods(BufferedWriter bw, TableInfo tableInfo, String serviceBeanName) throws Exception {
        String beanName = tableInfo.getBeanName();
        String paramName = tableInfo.getBeanParamName();
        
        // 测试新增方法
        BuildComment.CreateFieldComment(bw, "测试新增功能");
        bw.write("\t@Test");
        bw.newLine();
        bw.write("\tpublic void testAdd() {");
        bw.newLine();
        bw.write("\t\t" + beanName + " entity = createTestEntity();");
        bw.newLine();
        bw.write("\t\tInteger result = " + serviceBeanName + ".Add(entity);");
        bw.newLine();
        bw.write("\t\tassertNotNull(\"添加结果不能为空\", result);");
        bw.newLine();
        bw.write("\t\tassertTrue(\"添加失败\", result > 0);");
        bw.newLine();
        bw.write("\t}");
        bw.newLine();
        bw.newLine();
        
        // 测试查询方法
        BuildComment.CreateFieldComment(bw, "测试分页查询功能");
        bw.write("\t@Test");
        bw.newLine();
        bw.write("\tpublic void testFindListByPage() {");
        bw.newLine();
        bw.write("\t\t" + paramName + " param = new " + paramName + "();");
        bw.newLine();
        bw.write("\t\tPaginationResultVO<" + beanName + "> result = " + serviceBeanName + ".FindListByPage(param);");
        bw.newLine();
        bw.write("\t\tassertNotNull(\"查询结果不能为空\", result);");
        bw.newLine();
        bw.write("\t\tassertNotNull(\"查询列表不能为空\", result.getList());");
        bw.newLine();
        bw.write("\t}");
        bw.newLine();
        bw.newLine();
        
        // 测试批量新增方法
        BuildComment.CreateFieldComment(bw, "测试批量新增功能");
        bw.write("\t@Test");
        bw.newLine();
        bw.write("\tpublic void testAddBatch() {");
        bw.newLine();
        bw.write("\t\tList<" + beanName + "> entityList = createTestEntityList();");
        bw.newLine();
        bw.write("\t\tInteger result = " + serviceBeanName + ".AddBatch(entityList);");
        bw.newLine();
        bw.write("\t\tassertNotNull(\"批量添加结果不能为空\", result);");
        bw.newLine();
        bw.write("\t\tassertTrue(\"批量添加失败\", result > 0);");
        bw.newLine();
        bw.write("\t}");
        bw.newLine();
        bw.newLine();
    }
    
    /**
     * 写入Controller测试方法
     */
    private static void writeControllerTestMethods(BufferedWriter bw, TableInfo tableInfo) throws Exception {
        String beanName = tableInfo.getBeanName();
        String controllerPath = "/" + StringUtils.lowerCaseFirstLetter(beanName);
        
        // 测试分页查询接口
        BuildComment.CreateFieldComment(bw, "测试分页查询接口");
        bw.write("\t@Test");
        bw.newLine();
        bw.write("\tpublic void testLoadDataList() {");
        bw.newLine();
        bw.write("\t\tString url = \"" + controllerPath + "/loadDataList\";");
        bw.newLine();
        bw.write("\t\tResponseEntity<ResponseVO> response = restTemplate.getForEntity(url, ResponseVO.class);");
        bw.newLine();
        bw.write("\t\tassertEquals(\"HTTP状态码应为200\", HttpStatus.OK, response.getStatusCode());");
        bw.newLine();
        bw.write("\t\tassertNotNull(\"响应体不能为空\", response.getBody());");
        bw.newLine();
        bw.write("\t}");
        bw.newLine();
        bw.newLine();
        
        // 测试新增接口
        BuildComment.CreateFieldComment(bw, "测试新增接口");
        bw.write("\t@Test");
        bw.newLine();
        bw.write("\tpublic void testAdd() {");
        bw.newLine();
        bw.write("\t\tString url = \"" + controllerPath + "/add\";");
        bw.newLine();
        bw.write("\t\t" + beanName + " entity = new " + beanName + "();");
        bw.newLine();
        bw.write("\t\t// TODO: 设置测试数据");
        bw.newLine();
        bw.write("\t\tResponseEntity<ResponseVO> response = restTemplate.postForEntity(url, entity, ResponseVO.class);");
        bw.newLine();
        bw.write("\t\tassertEquals(\"HTTP状态码应为200\", HttpStatus.OK, response.getStatusCode());");
        bw.newLine();
        bw.write("\t\tassertNotNull(\"响应体不能为空\", response.getBody());");
        bw.newLine();
        bw.write("\t}");
        bw.newLine();
        bw.newLine();
    }
    
    /**
     * 写入测试数据创建方法
     */
    private static void writeTestDataMethods(BufferedWriter bw, TableInfo tableInfo) throws Exception {
        String beanName = tableInfo.getBeanName();
        
        // 创建单个测试实体方法
        BuildComment.CreateFieldComment(bw, "创建测试实体");
        bw.write("\tprivate " + beanName + " createTestEntity() {");
        bw.newLine();
        bw.write("\t\t" + beanName + " entity = new " + beanName + "();");
        bw.newLine();
        
        // 为非自增字段设置测试数据
        for (FieldInfo field : tableInfo.getFieldList()) {
            if (field.getIsAutoIncrement() != null && field.getIsAutoIncrement()) {
                continue; // 跳过自增字段
            }
            
            String setMethod = "set" + StringUtils.uperCaseFirstLetter(field.getPropertyName());
            String testValue = getTestValue(field);
            
            bw.write("\t\tentity." + setMethod + "(" + testValue + ");");
            bw.newLine();
        }
        
        bw.write("\t\treturn entity;");
        bw.newLine();
        bw.write("\t}");
        bw.newLine();
        bw.newLine();
        
        // 创建测试实体列表方法
        BuildComment.CreateFieldComment(bw, "创建测试实体列表");
        bw.write("\tprivate List<" + beanName + "> createTestEntityList() {");
        bw.newLine();
        bw.write("\t\tList<" + beanName + "> list = new ArrayList<>();");
        bw.newLine();
        bw.write("\t\tfor (int i = 0; i < 3; i++) {");
        bw.newLine();
        bw.write("\t\t\tlist.add(createTestEntity());");
        bw.newLine();
        bw.write("\t\t}");
        bw.newLine();
        bw.write("\t\treturn list;");
        bw.newLine();
        bw.write("\t}");
        bw.newLine();
        bw.newLine();
    }
    
    /**
     * 根据字段类型生成测试值
     * 支持更多的SQL数据类型
     */
    private static String getTestValue(FieldInfo field) {
        String javaType = field.getJavaType();
        String fieldName = field.getPropertyName();
        String sqlType = field.getSqlType();
        
        // 使用SqlTypeMapper获取默认值
        try {
            String defaultValue = SqlTypeMapper.getDefaultValue(sqlType);
            if (!"null".equals(defaultValue)) {
                return defaultValue;
            }
        } catch (Exception e) {
            // 如果获取默认值失败，使用传统的类型判断
        }
        
        // 根据Java类型生成测试值
        switch (javaType) {
            case "String":
                return "\"test_" + fieldName + "\"";
            case "Integer":
                return "1";
            case "Long":
                return "1L";
            case "Double":
                return "1.0";
            case "Float":
                return "1.0F";
            case "Boolean":
                return "true";
            case "Date":
                return "new Date()";
            case "BigDecimal":
                return "new BigDecimal(\"1.00\")";
            case "byte[]":
                return "\"test_bytes\".getBytes()";
            case "Time":
                return "new Time(System.currentTimeMillis())";
            default:
                // 对于其他类型，尝试根据字段名生成合理的测试值
                if (fieldName.toLowerCase().contains("email")) {
                    return "\"test@example.com\"";
                } else if (fieldName.toLowerCase().contains("phone")) {
                    return "\"13800138000\"";
                } else if (fieldName.toLowerCase().contains("url")) {
                    return "\"http://example.com\"";
                } else if (fieldName.toLowerCase().contains("json")) {
                    return "\"{\\\"key\\\":\\\"value\\\"}\"";
                } else if (fieldName.toLowerCase().contains("uuid")) {
                    return "\"123e4567-e89b-12d3-a456-426614174000\"";
                } else {
                    return "null";
                }
        }
    }
}
