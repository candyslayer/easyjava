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

public class BuildController {
    private static final Logger log = LoggerFactory.getLogger(BuildController.class);

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_CONTROLLER);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File controllerFile = new File(folder, tableInfo.getBeanName() + "Controller.java");

        try (OutputStream out = new FileOutputStream(
                controllerFile);
                OutputStreamWriter outw = new OutputStreamWriter(out);
                BufferedWriter bw = new BufferedWriter(outw)) {

            bw.write("package " + Constants.PACKAGE_CONTROLLER + ";");
            bw.newLine();
            bw.newLine();

            String serviceName = tableInfo.getBeanName() + "Service";
            String serviceBeanName = StringUtils.lowerCaseFirstLetter(serviceName);
            String query = tableInfo.getBeanName() + Constants.SUFFIX_BEAN_PARAM;
            String beanName = tableInfo.getBeanName();

            bw.write("import org.springframework.web.bind.annotation.RestController;");
            bw.newLine();
            bw.write("import org.springframework.beans.factory.annotation.Autowired;");
            bw.newLine();
            bw.write("import org.springframework.web.bind.annotation.RequestMapping;");
            bw.newLine();
            bw.write("import org.springframework.web.bind.annotation.RequestBody;");
            bw.newLine();
            bw.write("import java.util.List;");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_SERVICE + "." + serviceName + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_VO + ".ResponseVO;");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_PARAM + "." + query + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_PO + "." + beanName + ";");
            bw.newLine();
            
            // 如果启用分表，导入分表相关的工具类
            if (tableInfo.getEnableSharding() != null && tableInfo.getEnableSharding()) {
                bw.write("import " + Constants.PACKAGE_UTILS + ".ShardingUtils;");
                bw.newLine();
            }
            
            bw.newLine();

            BuildComment.CreateClassComment(bw, tableInfo.getComment() + "Controller");
            bw.write("@RestController");
            bw.newLine();
            bw.write("public class " + tableInfo.getBeanName() + "Controller extends AbaseController {");
            bw.newLine();
            bw.newLine();

            bw.write("\t@Autowired");
            bw.newLine();
            bw.write("\t" + serviceName + " " + serviceBeanName + ";");
            bw.newLine();
            bw.newLine();

            // 如果启用分表，添加分表配置
            if (tableInfo.getEnableSharding() != null && tableInfo.getEnableSharding()) {
                BuildComment.CreateFieldComment(bw, "分表配置");
                bw.write("\tprivate static final String SHARDING_FIELD = \"" + tableInfo.getShardingField() + "\";");
                bw.newLine();
                bw.write("\tprivate static final String SHARDING_STRATEGY = \"" + tableInfo.getShardingStrategy() + "\";");
                bw.newLine();
                bw.newLine();

                BuildComment.CreateFieldComment(bw, "获取分表表名");
                bw.write("\tprivate String getShardingTableName(Object shardingValue) {");
                bw.newLine();
                bw.write("\t\treturn ShardingUtils.getTableName(\"" + tableInfo.getTableName() + "\", shardingValue, SHARDING_STRATEGY);");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();
            }

            BuildComment.CreateFieldComment(bw, "根据条件分页查询");
            bw.write("\t@RequestMapping(\"/loadDataList\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO loadDataList(" + query + " query) {");
            bw.newLine();
            if (tableInfo.getEnableSharding() != null && tableInfo.getEnableSharding()) {
                bw.write("\t\t// 分表查询逻辑");
                bw.newLine();
                bw.write("\t\tif (query.get" + StringUtils.uperCaseFirstLetter(tableInfo.getShardingField()) + "() != null) {");
                bw.newLine();
                bw.write("\t\t\t// 单表查询");
                bw.newLine();
                bw.write("\t\t\treturn getSuccessResponseVO(" + serviceBeanName + ".findListByPage(query));");
                bw.newLine();
                bw.write("\t\t} else {");
                bw.newLine();
                bw.write("\t\t\t// 多表查询");
                bw.newLine();
                bw.write("\t\t\treturn getSuccessResponseVO(" + serviceBeanName + ".findListByPageWithSharding(query));");
                bw.newLine();
                bw.write("\t\t}");
            } else {
                bw.write("\t\treturn getSuccessResponseVO(" + serviceBeanName + ".findListByPage(query));");
            }
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "根据条件检查数据是否存在");
            bw.write("\t@RequestMapping(\"/checkExists\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO checkExists(" + query + " query) {");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(" + serviceBeanName + ".checkExists(query));");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "根据条件查询单条记录");
            bw.write("\t@RequestMapping(\"/findOne\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO findOne(" + query + " query) {");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(" + serviceBeanName + ".findOneByParam(query));");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "新增");
            bw.write("\t@RequestMapping(\"/add\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO add(" + beanName + " bean) {");
            bw.newLine();
            if (tableInfo.getEnableSharding() != null && tableInfo.getEnableSharding()) {
                bw.write("\t\t// 分表新增逻辑");
                bw.newLine();
                bw.write("\t\tString tableName = getShardingTableName(bean.get" + 
                        StringUtils.uperCaseFirstLetter(tableInfo.getShardingField()) + "());");
                bw.newLine();
                bw.write("\t\treturn getSuccessResponseVO(" + serviceBeanName + ".addWithSharding(bean, tableName));");
            } else {
                bw.write("\t\treturn getSuccessResponseVO(" + serviceBeanName + ".add(bean));");
            }
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "新增或更新");
            bw.write("\t@RequestMapping(\"/addOrUpdate\")");
            bw.newLine();
            bw.write("\t@GlobalInterceptor(checkParams = true)");
            bw.newLine();
            bw.write("\tpublic ResponseVO addOrUpdate(" + beanName + " bean) {");
            bw.newLine();
            if (tableInfo.getEnableSharding() != null && tableInfo.getEnableSharding()) {
                bw.write("\t\t// 分表新增或更新逻辑");
                bw.newLine();
                bw.write("\t\tString tableName = getShardingTableName(bean.get" + 
                        StringUtils.uperCaseFirstLetter(tableInfo.getShardingField()) + "());");
                bw.newLine();
                bw.write("\t\treturn getSuccessResponseVO(" + serviceBeanName + ".addOrUpdateWithSharding(bean, tableName));");
            } else {
                bw.write("\t\treturn getSuccessResponseVO(" + serviceBeanName + ".addOrUpdate(bean));");
            }
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "批量新增");
            bw.write("\t@RequestMapping(\"/addBatch\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO addBatch(@RequestBody List<" + beanName + "> beanlist) {");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(" + serviceBeanName + ".addBatch(beanlist));");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "批量新增或修改");
            bw.write("\t@RequestMapping(\"/addOrUpdateBatch\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO addOrUpdateBatch(@RequestBody List<" + beanName + "> beanlist) {");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(" + serviceBeanName + ".addOrUpdateBatch(beanlist));");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "根据参数删除");
            bw.write("\t@RequestMapping(\"/deleteByParam\")");
            bw.newLine();
            bw.write("\t@GlobalInterceptor(checkParams = true)");
            bw.newLine();
            bw.write("\tpublic ResponseVO deleteByParam(" + tableInfo.getBeanParamName() + " param) {");
            bw.newLine();
            if (tableInfo.getEnableSharding() != null && tableInfo.getEnableSharding()) {
                bw.write("\t\t// 分表删除逻辑");
                bw.newLine();
                bw.write("\t\tif (param.get" + StringUtils.uperCaseFirstLetter(tableInfo.getShardingField()) + "() != null) {");
                bw.newLine();
                bw.write("\t\t\t// 单表删除");
                bw.newLine();
                bw.write("\t\t\tString tableName = getShardingTableName(param.get" + 
                        StringUtils.uperCaseFirstLetter(tableInfo.getShardingField()) + "());");
                bw.newLine();
                bw.write("\t\t\treturn getSuccessResponseVO(" + serviceBeanName + ".deleteByParamWithSharding(param, tableName));");
                bw.newLine();
                bw.write("\t\t} else {");
                bw.newLine();
                bw.write("\t\t\t// 多表删除");
                bw.newLine();
                bw.write("\t\t\treturn getSuccessResponseVO(" + serviceBeanName + ".deleteByParamWithAllSharding(param));");
                bw.newLine();
                bw.write("\t\t}");
            } else {
                bw.write("\t\treturn getSuccessResponseVO(" + serviceBeanName + ".deleteByParam(param));");
            }
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "根据参数更新");
            bw.write("\t@RequestMapping(\"/updateByParam\")");
            bw.newLine();
            bw.write("\t@GlobalInterceptor(checkParams = true)");
            bw.newLine();
            bw.write("\tpublic ResponseVO updateByParam(" + tableInfo.getBeanName() + " bean, " + tableInfo.getBeanParamName() + " param) {");
            bw.newLine();
            bw.write("\t\treturn GetSuccessResponseVO(" + serviceBeanName + ".updateByParam(bean, param));");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            for (Map.Entry<String, List<FieldInfo>> entry : tableInfo.getKeyIndexMap().entrySet()) {
                List<FieldInfo> keyFieldInfos = entry.getValue();

                int index = 0;
                StringBuffer methodName = new StringBuffer();
                StringBuilder methodParam = new StringBuilder();
                StringBuffer queryParam = new StringBuffer();

                for (FieldInfo fInfo : keyFieldInfos) {
                    index++;
                    methodName.append(StringUtils.uperCaseFirstLetter(fInfo.getPropertyName()));

                    if (index < keyFieldInfos.size()) {
                        methodName.append("And");
                    }

                    methodParam.append(fInfo.getJavaType() + " " + fInfo.getPropertyName());

                    queryParam.append(fInfo.getPropertyName());

                    if (index < keyFieldInfos.size()) {
                        methodParam.append(", ");
                        queryParam.append(",");
                    }
                }

                BuildComment.CreateFieldComment(bw, "根据" + methodName + "查询");
                bw.write("\t@RequestMapping(\"/get" + methodName + "\")");
                bw.newLine();
                bw.write("\tpublic ResponseVO getBy" + methodName + "(" + methodParam + ") {");
                bw.newLine();
                bw.write("\t\treturn GetSuccessResponseVO(" + serviceBeanName + ".getBy" + methodName + "("
                        + queryParam + "));");
                bw.write("\t}");

                bw.newLine();
                bw.newLine();

                BuildComment.CreateFieldComment(bw, "根据" + methodName + "更新");
                bw.write("\t@RequestMapping(\"/update"+methodName+"\")");
                bw.newLine();
                bw.write("\tpublic ResponseVO updateBy" + methodName + "(" + beanName + " bean, "
                        + methodParam + ") {");
                bw.newLine();
                bw.write("\t\treturn GetSuccessResponseVO(" + serviceBeanName + ".updateBy" + methodName + "("
                        + "bean, " + queryParam + "));");
                bw.newLine();
                bw.write("\t}");

                bw.newLine();
                bw.newLine();

                BuildComment.CreateFieldComment(bw, "根据" + methodName + "删除");
                bw.write("\t@RequestMapping(\"/delete" + methodName + "\")");
                bw.newLine();
                bw.write("\tpublic ResponseVO deleteBy" + methodName + "(" + methodParam + ") {");
                bw.newLine();
                bw.write("\t\treturn GetSuccessResponseVO(" + serviceBeanName + ".deleteBy" + methodName + "("
                        + queryParam + "));");
                bw.newLine();
                bw.write("\t}");

                bw.newLine();
                bw.newLine();
            }

            bw.newLine();
            bw.write("}");

        } catch (Exception e) {
            log.info("{}Controller创建失败", tableInfo.getBeanName(), e);
        }
    }
}
