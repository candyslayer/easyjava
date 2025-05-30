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

public class BuildMapper {
    private static final Logger log = LoggerFactory.getLogger(BuildMapper.class);

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_MAPPER);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File mapperFile = new File(folder, tableInfo.getBeanName() + Constants.SUFFIX_MAPPER + ".java");

        try (OutputStream out = new FileOutputStream(mapperFile);
                OutputStreamWriter outw = new OutputStreamWriter(out);
                BufferedWriter bw = new BufferedWriter(outw)) {

            bw.write("package " + Constants.PACKAGE_MAPPER + ";");
            bw.newLine();
            bw.newLine();

            bw.write("import org.apache.ibatis.annotations.Mapper;");
            bw.newLine();
            bw.write("import org.apache.ibatis.annotations.Param;");
            bw.newLine();
            bw.newLine();

            BuildComment.CreateClassComment(bw, tableInfo.getComment() + "Mapper");
            bw.write("@Mapper");
            bw.newLine();
            bw.write("public interface " + tableInfo.getBeanName() + Constants.SUFFIX_MAPPER
                    + "<T, P>" + " extends BaseMapper<T, P> {");
            bw.newLine();

            Map<String, List<FieldInfo>> keyIndexInfo = tableInfo.getKeyIndexMap();

            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexInfo.entrySet()) {
                List<FieldInfo> keyFieldInfos = entry.getValue();

                Integer index = 0;
                StringBuilder methodName = new StringBuilder();
                StringBuilder methodParam = new StringBuilder();

                for (FieldInfo fieldInfo : keyFieldInfos) {

                    methodName.append(StringUtils.uperCaseFirstLetter(fieldInfo.getPropertyName()));
                    index++;
                    if (index < keyFieldInfos.size()) {
                        methodName.append("And");
                    }

                    methodParam.append("@Param(\"" + fieldInfo.getPropertyName() + "\"" + ") " + fieldInfo.getJavaType()
                            + " " + fieldInfo.getPropertyName());

                    if (index < keyFieldInfos.size()) {
                        methodParam.append(", ");
                    }
                }
                BuildComment.CreateFieldComment(bw, "根据" + methodName + "查询");
                bw.write("\tT SelectBy" + methodName + "(" + methodParam + ");");
                bw.newLine();
                bw.newLine();

                BuildComment.CreateFieldComment(bw, "根据" + methodName + "更新");
                bw.write("\tInteger UpdateBy" + methodName + "(" +"@Param(\"bean\") T t, "+ methodParam + ");");
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
            log.error("{}的mapper构建失败", tableInfo.getBeanName(), e);
        }
    }
}
