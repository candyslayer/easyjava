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

public class BuildServiceImpl {
        private final static Logger log = LoggerFactory.getLogger(BuildPo.class);

        public static void execute(TableInfo tableInfo) {
                File folder = new File(Constants.PATH_SERVICE_IMPL);

                if (!folder.exists()) {
                        folder.mkdirs();
                }

                File serviceImpFile = new File(folder, tableInfo.getBeanName() + "ServiceImpl.java");

                try (OutputStream out = new FileOutputStream(
                                serviceImpFile);
                                OutputStreamWriter outw = new OutputStreamWriter(out);
                                BufferedWriter bw = new BufferedWriter(outw)) {

                        bw.write("package " + Constants.PACKAGE_SERVICE_IMPL + ";");
                        bw.newLine();
                        bw.newLine();

                        bw.write("import java.util.List;");
                        bw.newLine();
                        bw.write("import org.springframework.stereotype.Service;");
                        bw.newLine();
                        bw.write("import org.springframework.beans.factory.annotation.Autowired;");
                        bw.newLine();
                        bw.write("import " + Constants.PACKAGE_PO + "." + tableInfo.getBeanName() + ";");
                        bw.newLine();
                        bw.write("import " + Constants.PACKAGE_PARAM + "." + tableInfo.getBeanParamName() + ";");
                        bw.newLine();
                        bw.write("import " + Constants.PACKAGE_PARAM + "." + "SimplePage;");
                        bw.newLine();
                        bw.write("import " + Constants.PACKAGE_ENUMS + "." + "PageSize;");
                        bw.newLine();
                        bw.write("import " + Constants.PACKAGE_MAPPER + "." + tableInfo.getBeanName()
                                        + Constants.SUFFIX_MAPPER
                                        + ";");
                        bw.newLine();
                        bw.write("import " + Constants.PACKAGE_VO + ".PaginationResultVO;");
                        bw.newLine();

                        bw.write("import " + Constants.PACKAGE_SERVICE + "." + tableInfo.getBeanName() + "Service;");
                        bw.newLine();
                        bw.newLine();

                        //将第一个字母小写，这样方便resource注入
                        bw.write("@Service(\"" + StringUtils.lowerCaseFirstLetter(
                                        tableInfo.getBeanName()) + "Service\")");
                        bw.newLine();
                        bw.write("public class " + tableInfo.getBeanName() + "ServiceImpl implements "
                                        + tableInfo.getBeanName()
                                        + "Service{");
                        bw.newLine();
                        bw.newLine();

                        String mapperName = tableInfo.getBeanName() + Constants.SUFFIX_MAPPER;
                        String mapperBeanName = StringUtils.lowerCaseFirstLetter(tableInfo.getBeanName())
                                        + Constants.SUFFIX_MAPPER;

                        bw.write("\t@Autowired");
                        bw.newLine();
                        bw.write("\tprivate " + mapperName + "<" + tableInfo.getBeanName() + ", "
                                        + tableInfo.getBeanParamName()
                                        + "> " + mapperBeanName + ";");
                        bw.newLine();
                        bw.newLine();

                        BuildComment.CreateFieldComment(bw, "根据条件查询列表");
                        bw.write("\t@Override");
                        bw.newLine();
                        bw.write(
                                        "\tpublic List<" + tableInfo.getBeanName() + "> findListParam("
                                                        + tableInfo.getBeanParamName()
                                                        + " param) {");
                        bw.newLine();
                        bw.write("\t\treturn this." + mapperBeanName + ".selectList(param);");
                        bw.newLine();
                        bw.write("\t}");

                        bw.newLine();
                        bw.newLine();

                        BuildComment.CreateFieldComment(bw, "根据条件查询单条记录");
                        bw.write("\t@Override");
                        bw.newLine();
                        bw.write("\tpublic " + tableInfo.getBeanName() + " findOneByParam(" + tableInfo.getBeanParamName() + " param) {");
                        bw.newLine();
                        bw.write("\t\tList<" + tableInfo.getBeanName() + "> list = this.findListParam(param);");
                        bw.newLine();
                        bw.write("\t\treturn list.isEmpty() ? null : list.get(0);");
                        bw.newLine();
                        bw.write("\t}");

                        bw.newLine();
                        bw.newLine();

                        BuildComment.CreateFieldComment(bw, "根据条件查询数量");
                        bw.write("\t@Override");
                        bw.newLine();
                        bw.write("\tpublic Integer findCountByParam(" + tableInfo.getBeanParamName() + " param) {");
                        bw.newLine();
                        bw.write("\t\treturn this." + mapperBeanName + ".selectCount(param);");
                        bw.newLine();
                        bw.write("\t}");

                        bw.newLine();
                        bw.newLine();

                        BuildComment.CreateFieldComment(bw, "根据条件检查数据是否存在");
                        bw.write("\t@Override");
                        bw.newLine();
                        bw.write("\tpublic Boolean checkExists(" + tableInfo.getBeanParamName() + " param) {");
                        bw.newLine();
                        bw.write("\t\treturn this.findCountByParam(param) > 0;");
                        bw.newLine();
                        bw.write("\t}");

                        bw.newLine();
                        bw.newLine();

                        BuildComment.CreateFieldComment(bw, "分页查询");
                        bw.write("\t@Override");
                        bw.newLine();
                        bw.write("\tpublic PaginationResultVO<" + tableInfo.getBeanName() + "> findListByPage("
                                        + tableInfo.getBeanParamName() + " param) {");
                        bw.newLine();
                        bw.write("\t\tint count = this.findCountByParam(param);");
                        bw.newLine();
                        bw.write(
                                        "\t\tint pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();");
                        bw.newLine();
                        bw.write("\t\tSimplePage page = new SimplePage(param.getPageNo(), count, pageSize);");
                        bw.newLine();
                        bw.write("\t\tparam.setSimplePage(page);");
                        bw.newLine();
                        bw.write("\t\tList<" + tableInfo.getBeanName() + "> list = this.findListParam(param);");
                        bw.newLine();
                        bw.write("\t\tPaginationResultVO<" + tableInfo.getBeanName()
                                        + "> result = new PaginationResultVO<>(count, page.getPageNo(), page.getPageSize (),\r\n"
                                        + //
                                        "\t\t\t\tpage.getPageTotal(), list);");
                        bw.newLine();
                        bw.write("\t\treturn result;");
                        bw.newLine();
                        bw.write("\t}");

                        bw.newLine();
                        bw.newLine();

                        BuildComment.CreateFieldComment(bw, "新增");
                        bw.write("\t@Override");
                        bw.newLine();
                        bw.write("\tpublic Integer add(" + tableInfo.getBeanName() + " bean) {");
                        bw.newLine();
                        bw.write("\t\treturn this." + mapperBeanName + ".insert(bean);");
                        bw.newLine();
                        bw.write("\t}");

                        bw.newLine();
                        bw.newLine();

                        BuildComment.CreateFieldComment(bw, "新增或更新");
                        bw.write("\t@Override");
                        bw.newLine();
                        bw.write("\tpublic Integer addOrUpdate(" + tableInfo.getBeanName() + " bean) {");
                        bw.newLine();
                        bw.write("\t\treturn this." + mapperBeanName + ".insertOrUpdate(bean);");
                        bw.newLine();
                        bw.write("\t}");

                        bw.newLine();
                        bw.newLine();

                        BuildComment.CreateFieldComment(bw, "批量新增");
                        bw.write("\t@Override");
                        bw.newLine();
                        bw.write("\tpublic Integer addBatch(List<" + tableInfo.getBeanName() + "> listbean) {");
                        bw.newLine();
                        bw.write("\t\tif (listbean == null || listbean.isEmpty()){\r\n" + //
                                        "\t\t\treturn 0;\r\n" + //
                                        "\t\t}");
                        bw.newLine();
                        bw.write("\t\treturn this." + mapperBeanName + ".insertBatch(listbean);");
                        bw.newLine();
                        bw.write("\t}");

                        bw.newLine();
                        bw.newLine();

                        BuildComment.CreateFieldComment(bw, "批量新增或修改");
                        bw.write("\t@Override");
                        bw.newLine();
                        bw.write("\tpublic Integer addOrUpdateBatch(List<" + tableInfo.getBeanName() + "> listbean) {");
                        bw.newLine();
                        bw.write("\t\tif (listbean == null || listbean.isEmpty()){\r\n" + //
                                        "\t\t\treturn 0;\r\n" + //
                                        "\t\t}");
                        bw.newLine();
                        bw.write("\t\treturn this." + mapperBeanName + ".insertOrUpdateBatch(listbean);");
                        bw.newLine();
                        bw.write("\t}");

                        bw.newLine();
                        bw.newLine();

                        BuildComment.CreateFieldComment(bw, "根据参数删除");
                        bw.write("\t@Override");
                        bw.newLine();
                        bw.write("\tpublic Integer deleteByParam(" + tableInfo.getBeanParamName() + " param) {");
                        bw.newLine();
                        bw.write("\t\treturn this." + mapperBeanName + ".deleteByParam(param);");
                        bw.newLine();
                        bw.write("\t}");

                        bw.newLine();
                        bw.newLine();

                        BuildComment.CreateFieldComment(bw, "根据参数更新");
                        bw.write("\t@Override");
                        bw.newLine();
                        bw.write("\tpublic Integer updateByParam(" + tableInfo.getBeanName() + " bean, " + tableInfo.getBeanParamName() + " param) {");
                        bw.newLine();
                        bw.write("\t\treturn this." + mapperBeanName + ".updateByParam(bean, param);");
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
                                bw.write("\t@Override");
                                bw.newLine();
                                bw.write("\tpublic " + tableInfo.getBeanName() + " getBy" + methodName + "("
                                                + methodParam + ") {");
                                bw.newLine();
                                bw.write("\t\treturn this." + mapperBeanName + ".selectBy" + methodName + "("
                                                + queryParam + ");");
                                bw.write("\t}");

                                bw.newLine();
                                bw.newLine();

                                BuildComment.CreateFieldComment(bw, "根据" + methodName + "更新");
                                bw.write("\t@Override");
                                bw.newLine();
                                bw.write("\tpublic Integer updateBy" + methodName + "(" + tableInfo.getBeanName()
                                                + " bean, "
                                                + methodParam + ") {");
                                bw.newLine();
                                bw.write("\t\treturn this." + mapperBeanName + ".updateBy" + methodName + "("
                                                + "bean, " + queryParam + ");");
                                bw.newLine();
                                bw.write("\t}");

                                bw.newLine();
                                bw.newLine();

                                BuildComment.CreateFieldComment(bw, "根据" + methodName + "删除");
                                bw.write("\t@Override");
                                bw.newLine();
                                bw.write("\tpublic Integer deleteBy" + methodName + "(" + methodParam + ") {");
                                bw.newLine();
                                bw.write("\t\treturn this." + mapperBeanName + ".deleteBy" + methodName + "("
                                                + queryParam + ");");
                                bw.newLine();
                                bw.write("\t}");

                                bw.newLine();
                                bw.newLine();
                        }

                        bw.write("}");

                } catch (Exception e) {
                        log.info("{}ServiceImpl构建失败", tableInfo.getBeanName(), e.getMessage());
                }
        }

}