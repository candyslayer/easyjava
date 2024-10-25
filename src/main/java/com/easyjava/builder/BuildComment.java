package com.easyjava.builder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;

import com.easyjava.bean.Constants;
import com.easyjava.utils.DateUtils;

//使用这个类常见注释
public class BuildComment {
    public static void CreateClassComment(BufferedWriter bw, String classCommnet) throws IOException {
        bw.write("/**");
        bw.newLine();
        bw.write(" * ");
        bw.newLine();
        bw.write(" * @Description " + classCommnet);
        bw.newLine();
        bw.write(" * ");
        bw.newLine();
        bw.write(" * @auther " + Constants.AUTHER_COMMENT);
        bw.newLine();
        bw.write(" * @date " + DateUtils.Format(new Date(), DateUtils.YYYYMMDDHHMMSS));
        bw.newLine();
        bw.write(" * ");
        bw.newLine();
        bw.write(" **/");

        bw.newLine();
    }
    

    public static void CreateFieldComment(BufferedWriter bw, String fieldCommnet) throws IOException {
        bw.write("\t/**");
        bw.newLine();
        bw.write("\t * " + (fieldCommnet == null ? "" : fieldCommnet));
        bw.newLine();
        bw.write("\t */");
        bw.newLine();
    }
}
