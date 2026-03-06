package com.easyjava;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.easyjava.codegen.MergeOutcome;
import com.easyjava.codegen.MergeStatus;
import com.easyjava.codegen.SafeGenerationEngine;
import com.easyjava.utils.ConfigValidator;
import com.easyjava.utils.PropertiesUtils;

/**
 * EasyJava代码生成器主类（原版）
 * 
 * 使用增强版功能，请运行 AppEnhanced.java
 */
public class App 
{
    private static final Logger log = LoggerFactory.getLogger(App.class);
    
    public static void main( String[] args )
    {
        log.info("=== EasyJava代码生成器启动 ===");
        log.info("应用名称: {}", PropertiesUtils.geString("spring.application.name"));
        
        // 快速配置验证
        if (!ConfigValidator.quickValidate()) {
            log.error("配置验证失败，建议使用 AppEnhanced 进行详细验证");
            log.info("运行命令: mvn compile exec:java -Dexec.mainClass=\"com.easyjava.AppEnhanced\"");
            return;
        }

        //系统没改utf-8的化就用这个gbk格式的配置，这里我没有解决日志输出两次的问题
        //是由于日志的可加性引起的,我设置了一个Appender,由于root上也有一个Appender，由于可加性，我所需要打印的日志会被两个Appender处理导致打印两次。这个问题已经解决
        LogbackConfig.configureLogback();
        SafeGenerationEngine.getInstance().prepareRun();
        
        BuildBase.execute();

        List<TableInfo> tableInfos = BuilderTable.GetTables();
        log.info("共发现 {} 个表", tableInfos.size());

        for (TableInfo tableInfo : tableInfos) {
            log.info("正在处理表: {}", tableInfo.getTableName());
            BuildPo.execute(tableInfo);
            BuildQuery.execute(tableInfo);
            BuildMapper.execute(tableInfo);
            BuildMapperXML.execute(tableInfo);
            BuildService.execute(tableInfo);
            BuildServiceImpl.execute(tableInfo);
            BuildController.execute(tableInfo);
        }
        
        printSafeGenerationSummary();
        log.info("=== 代码生成完成 ===");
        log.info("提示: 使用 AppEnhanced 获得增量生成、测试代码生成等高级功能");
    }

    private static void printSafeGenerationSummary() {
        int created = 0;
        int updated = 0;
        int keptLocal = 0;
        int autoMerged = 0;
        int conflicts = 0;

        for (MergeOutcome outcome : SafeGenerationEngine.getInstance().getOutcomes()) {
            MergeStatus status = outcome.getStatus();
            if (status == MergeStatus.CREATED) {
                created++;
            } else if (status == MergeStatus.UPDATED) {
                updated++;
            } else if (status == MergeStatus.KEPT_LOCAL) {
                keptLocal++;
            } else if (status == MergeStatus.AUTO_MERGED) {
                autoMerged++;
            } else if (status == MergeStatus.CONFLICT) {
                conflicts++;
            }
        }

        log.info("安全再生成摘要: created={}, updated={}, keptLocal={}, autoMerged={}, conflicts={}",
                created, updated, keptLocal, autoMerged, conflicts);
    }
}
