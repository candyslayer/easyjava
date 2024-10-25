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

public class BuildService {
    private final static Logger log = LoggerFactory.getLogger(BuildService.class);

    public static void execute(TableInfo tableInfo) {

        File folder = new File(Constants.PATH_SERVICE);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File serviceFile = new File(folder, tableInfo.getBeanName() + "Service.java");

        try (OutputStream out = new FileOutputStream(serviceFile);
                OutputStreamWriter outw = new OutputStreamWriter(out);
                BufferedWriter bw = new BufferedWriter(outw)) {

            bw.write("package " + Constants.PACKAGE_SERVICE + ";");
            bw.newLine();
            bw.newLine();

            // if (tableInfo.getHaveDate() || tableInfo.getHaveDateTime()) {
            // bw.write("import java.util.Date;");
            // bw.newLine();

            // bw.write(Constants.BEAN_DATE_SERIALIZATION_CLASS);
            // bw.newLine();

            // bw.write(Constants.BEAN_DATE_DESERIALIZATIO_CLASS);
            // bw.newLine();

            // bw.write("import " + Constants.PACKAGE_ENUMS + "." + "DateTimePatternEnum" +
            // ";");
            // bw.newLine();

            // bw.write("import " + Constants.PACKAGE_UTILS + "." + "DateUtil" + ";");
            // bw.newLine();
            // }

            bw.write("import java.util.List;");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_PO + "." + tableInfo.getBeanName() + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_PARAM + "." + tableInfo.getBeanParamName() + ";");
            bw.newLine();
            bw.write("import "+Constants.PACKAGE_VO+".PaginationResultVO;");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateClassComment(bw, tableInfo.getComment() + "Service");
            bw.write("public interface " + tableInfo.getBeanName() + "Service {");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "根据条件查询列表");
            bw.write(
                    "\tList<" + tableInfo.getBeanName() + "> FindListParam(" + tableInfo.getBeanParamName()
                            + " param);");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "根据条件查询数量");
            bw.write("\tInteger FindCountByParam(" + tableInfo.getBeanParamName() + " param);");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "分页查询");
            bw.write("\tPaginationResultVO<" + tableInfo.getBeanName() + "> FindListByPage("
                    + tableInfo.getBeanParamName() + " param);");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "新增");
            bw.write("\tInteger Add(" + tableInfo.getBeanName() + " bean);");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "批量新增");
            bw.write("\tInteger AddBatch(List<" + tableInfo.getBeanName() + "> listbean);");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateFieldComment(bw, "批量新增或修改");
            bw.write("\tInteger AddOrUpdateBatch(List<" + tableInfo.getBeanName() + "> listbean);");
            bw.newLine();
            bw.newLine();

            for (Map.Entry<String, List<FieldInfo>> entry : tableInfo.getKeyIndexMap().entrySet()) {
                List<FieldInfo> keyFieldInfos = entry.getValue();

                int index = 0;
                StringBuffer methodName = new StringBuffer();
                StringBuilder methodParam = new StringBuilder();

                for (FieldInfo fInfo : keyFieldInfos) {
                    index++;
                    methodName.append(StringUtils.uperCaseFirstLetter(fInfo.getPropertyName()));

                    if (index < keyFieldInfos.size()) {
                        methodName.append("And");
                    }

                    methodParam.append(fInfo.getJavaType() + " " + fInfo.getPropertyName());

                    if (index < keyFieldInfos.size()) {
                        methodParam.append(", ");
                    }
                }

                BuildComment.CreateFieldComment(bw, "根据" + methodName + "查询");
                bw.write("\t"+tableInfo.getBeanName()+" GetBy" + methodName + "(" + methodParam + ");");
                bw.newLine();
                bw.newLine();

                BuildComment.CreateFieldComment(bw, "根据" + methodName + "更新");
                bw.write("\tInteger UpdateBy" + methodName + "("+tableInfo.getBeanName()+" bean, " + methodParam + ");");
                bw.newLine();
                bw.newLine();

                BuildComment.CreateFieldComment(bw, "根据" + methodName + "删除");
                bw.write("\tInteger DeleteBy" + methodName + "(" + methodParam + ");");
                bw.newLine();
                bw.newLine();
            }

            bw.write("}");
            bw.flush();
        } catch (Exception e) {
            log.info("{}Service构建失败", tableInfo.getBeanName(), e.getMessage());
        }
    }
}
