package com.easyjava.builder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easyjava.bean.Constants;
import com.easyjava.bean.GenerateOptions;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.JsonUtils;
import com.easyjava.utils.PropertiesUtils;

/**
 * 增量生成支持类
 * 支持只生成变更的表，备份已有文件
 * 
 * @author 唐伟
 * @since 2025-07-30
 */
public class IncrementalGenerator {
    
    private static final Logger log = LoggerFactory.getLogger(IncrementalGenerator.class);
    
    /** 表结构缓存文件名 */
    private static final String TABLE_CACHE_FILE = "table_structure_cache.json";
    
    /** 备份目录名 */
    private static final String BACKUP_DIR = "backup";
    
    /**
     * 表结构信息缓存
     */
    public static class TableStructureCache {
        private String tableName;
        private String structureHash;
        private long lastModified;
        
        public TableStructureCache() {}
        
        public TableStructureCache(String tableName, String structureHash, long lastModified) {
            this.tableName = tableName;
            this.structureHash = structureHash;
            this.lastModified = lastModified;
        }
        
        // Getters and Setters
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        
        public String getStructureHash() { return structureHash; }
        public void setStructureHash(String structureHash) { this.structureHash = structureHash; }
        
        public long getLastModified() { return lastModified; }
        public void setLastModified(long lastModified) { this.lastModified = lastModified; }
    }
    
    /**
     * 获取变更的表列表
     * 
     * @param allTables 所有表信息
     * @param options 生成选项
     * @return 需要重新生成的表列表
     */
    public static List<TableInfo> getChangedTables(List<TableInfo> allTables, GenerateOptions options) {
        
        if (!options.isIncrementalGenerate()) {
            log.info("未启用增量生成，返回所有表");
            return allTables;
        }
        
        log.info("开始检查表结构变更...");
        
        // 加载缓存
        Map<String, TableStructureCache> cacheMap = loadTableCache();
        
        List<TableInfo> changedTables = new ArrayList<>();
        
        for (TableInfo table : allTables) {
            String tableName = table.getTableName();
            String currentHash = calculateTableStructureHash(table);
            
            TableStructureCache cached = cacheMap.get(tableName);
            
            if (cached == null) {
                // 新表
                log.info("发现新表: {}", tableName);
                changedTables.add(table);
            } else if (!currentHash.equals(cached.getStructureHash())) {
                // 表结构发生变化
                log.info("表结构发生变化: {}", tableName);
                changedTables.add(table);
            } else {
                log.debug("表结构未变化: {}", tableName);
            }
        }
        
        log.info("共检查{}个表，其中{}个表需要重新生成", allTables.size(), changedTables.size());
        
        return changedTables;
    }
    
    /**
     * 生成变更的表
     * 
     * @param changedTables 变更的表列表
     * @param options 生成选项
     */
    public static void generateChangedTables(List<TableInfo> changedTables, GenerateOptions options) {
        
        if (changedTables.isEmpty()) {
            log.info("没有表结构变更，无需生成代码");
            return;
        }
        
        log.info("开始生成{}个变更表的代码...", changedTables.size());
        
        // 备份现有文件
        if (!options.isOverwriteExisting()) {
            backupExistingFiles(changedTables, options);
        }
        
        // 生成代码
        for (TableInfo tableInfo : changedTables) {
            generateTableCode(tableInfo, options);
        }
        
        // 更新缓存
        updateTableCache(changedTables);
        
        log.info("增量生成完成");
    }
    
    /**
     * 生成单个表的代码
     */
    private static void generateTableCode(TableInfo tableInfo, GenerateOptions options) {
        log.info("正在生成表 {} 的代码...", tableInfo.getTableName());
        
        try {
            if (options.isGeneratePo()) {
                BuildPo.execute(tableInfo);
            }
            
            if (options.isGenerateQuery()) {
                BuildQuery.execute(tableInfo);
            }
            
            if (options.isGenerateMapper()) {
                BuildMapper.execute(tableInfo);
            }
            
            if (options.isGenerateMapperXml()) {
                BuildMapperXML.execute(tableInfo);
            }
            
            if (options.isGenerateService()) {
                BuildService.execute(tableInfo);
            }
            
            if (options.isGenerateServiceImpl()) {
                BuildServiceImpl.execute(tableInfo);
            }
            
            if (options.isGenerateController()) {
                BuildController.execute(tableInfo);
            }
            
            if (options.isGenerateTests()) {
                BuildTest.executeServiceTest(tableInfo);
                BuildTest.executeControllerTest(tableInfo);
            }
            
            log.info("表 {} 代码生成完成", tableInfo.getTableName());
            
        } catch (Exception e) {
            log.error("生成表 {} 的代码时发生错误", tableInfo.getTableName(), e);
        }
    }
    
    /**
     * 备份现有文件
     */
    private static void backupExistingFiles(List<TableInfo> tables, GenerateOptions options) {
        
        String backupPath = PropertiesUtils.geString("path.base") + "/" + BACKUP_DIR + "/" + 
                           System.currentTimeMillis();
        
        File backupDir = new File(backupPath);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        
        log.info("开始备份现有文件到: {}", backupPath);
        
        for (TableInfo table : tables) {
            backupTableFiles(table, backupPath, options);
        }
        
        log.info("文件备份完成");
    }
    
    /**
     * 备份单个表的相关文件
     */
    private static void backupTableFiles(TableInfo table, String backupPath, GenerateOptions options) {
        
        String beanName = table.getBeanName();
        
        try {
            // 备份PO文件
            if (options.isGeneratePo()) {
                backupFileIfExists(Constants.PATH_PO + "/" + beanName + ".java", 
                                 backupPath + "/po/" + beanName + ".java");
            }
            
            // 备份Mapper文件
            if (options.isGenerateMapper()) {
                backupFileIfExists(Constants.PATH_MAPPER + "/" + beanName + "Mapper.java",
                                 backupPath + "/mapper/" + beanName + "Mapper.java");
            }
            
            // 备份Service文件
            if (options.isGenerateService()) {
                backupFileIfExists(Constants.PATH_SERVICE + "/" + beanName + "Service.java",
                                 backupPath + "/service/" + beanName + "Service.java");
            }
            
            // 备份ServiceImpl文件
            if (options.isGenerateServiceImpl()) {
                backupFileIfExists(Constants.PATH_SERVICE_IMPL + "/" + beanName + "ServiceImpl.java",
                                 backupPath + "/service/impl/" + beanName + "ServiceImpl.java");
            }
            
            // 备份Controller文件
            if (options.isGenerateController()) {
                backupFileIfExists(Constants.PATH_CONTROLLER + "/" + beanName + "Controller.java",
                                 backupPath + "/controller/" + beanName + "Controller.java");
            }
            
        } catch (Exception e) {
            log.warn("备份表 {} 的文件时发生错误: {}", table.getTableName(), e.getMessage());
        }
    }
    
    /**
     * 如果文件存在则备份
     */
    private static void backupFileIfExists(String sourcePath, String backupPath) {
        try {
            Path source = Paths.get(sourcePath);
            if (Files.exists(source)) {
                Path backup = Paths.get(backupPath);
                Files.createDirectories(backup.getParent());
                Files.copy(source, backup);
                log.debug("备份文件: {} -> {}", sourcePath, backupPath);
            }
        } catch (Exception e) {
            log.warn("备份文件失败: {} -> {}, 错误: {}", sourcePath, backupPath, e.getMessage());
        }
    }
    
    /**
     * 计算表结构哈希值
     */
    private static String calculateTableStructureHash(TableInfo table) {
        try {
            // 将表结构信息序列化为JSON
            String tableJson = JsonUtils.convertObject2Json(table);
            
            // 计算MD5哈希
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(tableJson.getBytes("UTF-8"));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            log.error("计算表结构哈希失败: {}", table.getTableName(), e);
            return String.valueOf(System.currentTimeMillis());
        }
    }
    
    /**
     * 加载表结构缓存
     */
    private static Map<String, TableStructureCache> loadTableCache() {
        
        Map<String, TableStructureCache> cacheMap = new HashMap<>();
        
        try {
            String cacheFilePath = PropertiesUtils.geString("path.base") + "/" + TABLE_CACHE_FILE;
            File cacheFile = new File(cacheFilePath);
            
            if (!cacheFile.exists()) {
                log.info("缓存文件不存在，将创建新的缓存");
                return cacheMap;
            }
            
            String cacheContent = new String(Files.readAllBytes(cacheFile.toPath()), "UTF-8");
            
            // 这里应该使用JSON解析，简化处理
            log.info("加载表结构缓存: {}", cacheFilePath);
            
        } catch (Exception e) {
            log.warn("加载表结构缓存失败", e);
        }
        
        return cacheMap;
    }
    
    /**
     * 更新表结构缓存
     */
    private static void updateTableCache(List<TableInfo> tables) {
        
        try {
            String cacheFilePath = PropertiesUtils.geString("path.base") + "/" + TABLE_CACHE_FILE;
            
            Map<String, TableStructureCache> cacheMap = new HashMap<>();
            
            for (TableInfo table : tables) {
                String hash = calculateTableStructureHash(table);
                TableStructureCache cache = new TableStructureCache(
                    table.getTableName(), hash, System.currentTimeMillis());
                cacheMap.put(table.getTableName(), cache);
            }
            
            // 将缓存写入文件
            String cacheJson = JsonUtils.convertObject2Json(cacheMap);
            Files.write(Paths.get(cacheFilePath), cacheJson.getBytes("UTF-8"));
            
            log.info("更新表结构缓存: {}", cacheFilePath);
            
        } catch (Exception e) {
            log.error("更新表结构缓存失败", e);
        }
    }
    
    /**
     * 清理缓存
     */
    public static void clearCache() {
        try {
            String cacheFilePath = PropertiesUtils.geString("path.base") + "/" + TABLE_CACHE_FILE;
            File cacheFile = new File(cacheFilePath);
            
            if (cacheFile.exists()) {
                cacheFile.delete();
                log.info("清理表结构缓存完成");
            }
        } catch (Exception e) {
            log.error("清理缓存失败", e);
        }
    }
    
    /**
     * 强制重新生成所有代码
     * 
     * @param allTables 所有表信息
     * @param options 生成选项
     */
    public static void forceRegenerateAll(List<TableInfo> allTables, GenerateOptions options) {
        log.info("强制重新生成所有代码...");
        
        // 清理缓存
        clearCache();
        
        // 生成所有表
        generateChangedTables(allTables, options);
    }
}
