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

            BuildComment.CreateFieldComment(bw, "根据条件分页查询");
            bw.write("\t@RequestMapping(\"/loadDataList\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO LoadDataList(" + query + " query) {");
            bw.newLine();
            bw.write("\t\treturn GetSuccessResponseVO(" + serviceBeanName + ".FindListByPage(query));");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "新增");
            bw.write("\t@RequestMapping(\"/add\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO Add(" + beanName + " bean) {");
            bw.newLine();
            bw.write("\t\t" + serviceBeanName + ".Add(bean);");
            bw.newLine();
            bw.write("\t\treturn GetSuccessResponseVO(null);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "批量新增");
            bw.write("\t@RequestMapping(\"/addBatch\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO AddBatch(@RequestBody List<" + beanName + "> beanlist) {");
            bw.newLine();
            bw.write("\t\t" + serviceBeanName + ".AddBatch(beanlist);");
            bw.newLine();
            bw.write("\t\treturn GetSuccessResponseVO(null);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "批量新增或修改");
            bw.write("\t@RequestMapping(\"/addOrUpdateBatch\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO AddOrUpdateBatchBatch(@RequestBody List<" + beanName + "> beanlist) {");
            bw.newLine();
            bw.write("\t\t" + serviceBeanName + ".AddOrUpdateBatch(beanlist);");
            bw.newLine();
            bw.write("\t\treturn GetSuccessResponseVO(null);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "根据参数更新记录");
            bw.write("\t@RequestMapping(\"/updateByParam\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO UpdateByParam(@RequestBody " + beanName + " bean, " + tableInfo.getBeanParamName() + " param) {");
            bw.newLine();
            bw.write("\t\treturn GetSuccessResponseVO(" + serviceBeanName + ".UpdateByParam(bean, param));");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "根据参数删除记录");
            bw.write("\t@RequestMapping(\"/deleteByParam\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO DeleteByParam(" + tableInfo.getBeanParamName() + " param) {");
            bw.newLine();
            bw.write("\t\treturn GetSuccessResponseVO(" + serviceBeanName + ".DeleteByParam(param));");
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
                bw.write("\tpublic ResponseVO GetBy" + methodName + "(" + methodParam + ") {");
                bw.newLine();
                bw.write("\t\treturn GetSuccessResponseVO(" + serviceBeanName + ".GetBy" + methodName + "("
                        + queryParam + "));");
                bw.write("\t}");

                bw.newLine();
                bw.newLine();

                BuildComment.CreateFieldComment(bw, "根据" + methodName + "更新");
                bw.write("\t@RequestMapping(\"/update"+methodName+"\")");
                bw.newLine();
                bw.write("\tpublic ResponseVO UpdateBy" + methodName + "(" + beanName + " bean, "
                        + methodParam + ") {");
                bw.newLine();
                bw.write("\t\treturn GetSuccessResponseVO(" + serviceBeanName + ".UpdateBy" + methodName + "("
                        + "bean, " + queryParam + "));");
                bw.newLine();
                bw.write("\t}");

                bw.newLine();
                bw.newLine();

                BuildComment.CreateFieldComment(bw, "根据" + methodName + "删除");
                bw.write("\t@RequestMapping(\"/delete" + methodName + "\")");
                bw.newLine();
                bw.write("\tpublic ResponseVO DeleteBy" + methodName + "(" + methodParam + ") {");
                bw.newLine();
                bw.write("\t\treturn GetSuccessResponseVO(" + serviceBeanName + ".DeleteBy" + methodName + "("
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
