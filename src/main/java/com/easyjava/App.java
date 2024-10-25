package com.easyjava;

import java.util.List;

import com.easyjava.bean.TableInfo;
import com.easyjava.builder.BuildBase;
import com.easyjava.builder.BuildController;
import com.easyjava.builder.BuildMapper;
import com.easyjava.builder.BuildMapperXML;
import com.easyjava.builder.BuildPo;
import com.easyjava.builder.BuildQuery;
import com.easyjava.builder.BuildService;
import com.easyjava.builder.BuildServiceImpl;
import com.easyjava.builder.BuilderTable;
import com.easyjava.utils.PropertiesUtils;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {

        System.out.println(PropertiesUtils.geString("spring.application.name"));

        //系统没改utf-8的化就用这个gbk格式的配置，这里我没有解决日志输出两次的问题
        //是由于日志的可加性引起的,我设置了一个Appender,由于root上也有一个Appender，由于可加性，我所需要打印的日志会被两个Appender处理导致打印两次。这个问题已经解决
        LogbackConfig.configureLogback();
        
        BuildBase.execute();

        List<TableInfo> tableInfos = BuilderTable.GetTables();

        for (TableInfo tableInfo : tableInfos) {
            BuildPo.execute(tableInfo);
            BuildQuery.execute(tableInfo);
            BuildMapper.execute(tableInfo);
            BuildMapperXML.execute(tableInfo);
            BuildService.execute(tableInfo);
            BuildServiceImpl.execute(tableInfo);
            BuildController.execute(tableInfo);
        }
    }
}
