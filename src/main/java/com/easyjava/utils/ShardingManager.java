package com.easyjava.utils;

import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easyjava.bean.TableInfo;
import com.easyjava.builder.BuilderTable;

/**
 * 分表管理器 - 可独立运行来管理分表
 */
public class ShardingManager {
    
    private static final Logger log = LoggerFactory.getLogger(ShardingManager.class);
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== 分表管理器 ===");
        System.out.println("1. 自动创建所有分表");
        System.out.println("2. 创建指定表的分表");
        System.out.println("3. 为时间分表创建未来表");
        System.out.println("4. 清理过期时间分表");
        System.out.println("5. 查看分表配置");
        System.out.println("0. 退出");
        System.out.print("请选择操作: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); // 消费换行符
        
        switch (choice) {
            case 1:
                createAllShardingTables();
                break;
            case 2:
                createSpecificShardingTable(scanner);
                break;
            case 3:
                createFutureTimeTables(scanner);
                break;
            case 4:
                cleanupExpiredTables(scanner);
                break;
            case 5:
                showShardingConfig();
                break;
            case 0:
                System.out.println("退出程序");
                break;
            default:
                System.out.println("无效选择");
        }
        
        scanner.close();
    }
    
    /**
     * 自动创建所有分表
     */
    private static void createAllShardingTables() {
        try {
            System.out.println("正在获取表信息...");
            List<TableInfo> tableInfos = BuilderTable.GetTables();
            
            System.out.println("开始创建分表...");
            ShardingTableCreator.createAllShardingTables(tableInfos);
            
            System.out.println("分表创建完成！");
        } catch (Exception e) {
            System.err.println("创建分表失败: " + e.getMessage());
            log.error("创建分表失败", e);
        }
    }
    
    /**
     * 创建指定表的分表
     */
    private static void createSpecificShardingTable(Scanner scanner) {
        System.out.print("请输入表名: ");
        String tableName = scanner.nextLine();
        
        System.out.print("请输入分表策略 (hash/range/time): ");
        String strategy = scanner.nextLine();
        
        try {
            ShardingUtils.autoCreateShardingTables(tableName, strategy);
            System.out.println("表 " + tableName + " 的分表创建完成！");
        } catch (Exception e) {
            System.err.println("创建分表失败: " + e.getMessage());
            log.error("创建分表失败", e);
        }
    }
    
    /**
     * 为时间分表创建未来表
     */
    private static void createFutureTimeTables(Scanner scanner) {
        System.out.print("请输入表名: ");
        String tableName = scanner.nextLine();
        
        System.out.print("请输入创建未来多少个月的表: ");
        int months = scanner.nextInt();
        scanner.nextLine();
        
        try {
            TableInfo tableInfo = new TableInfo();
            tableInfo.setTableName(tableName);
            tableInfo.setEnableSharding(true);
            tableInfo.setShardingStrategy("time");
            
            ShardingTableCreator.createFutureTimeTables(tableInfo, months);
            System.out.println("未来 " + months + " 个月的时间分表创建完成！");
        } catch (Exception e) {
            System.err.println("创建未来分表失败: " + e.getMessage());
            log.error("创建未来分表失败", e);
        }
    }
    
    /**
     * 清理过期时间分表
     */
    private static void cleanupExpiredTables(Scanner scanner) {
        System.out.print("请输入表名: ");
        String tableName = scanner.nextLine();
        
        System.out.print("请输入保留多少个月的历史表: ");
        int monthsToKeep = scanner.nextInt();
        scanner.nextLine();
        
        System.out.print("确认要删除过期分表吗? (yes/no): ");
        String confirm = scanner.nextLine();
        
        if ("yes".equalsIgnoreCase(confirm)) {
            try {
                TableInfo tableInfo = new TableInfo();
                tableInfo.setTableName(tableName);
                tableInfo.setEnableSharding(true);
                tableInfo.setShardingStrategy("time");
                
                ShardingTableCreator.cleanupExpiredTimeTables(tableInfo, monthsToKeep);
                System.out.println("过期分表清理完成！");
            } catch (Exception e) {
                System.err.println("清理过期分表失败: " + e.getMessage());
                log.error("清理过期分表失败", e);
            }
        } else {
            System.out.println("取消清理操作");
        }
    }
    
    /**
     * 显示分表配置
     */
    private static void showShardingConfig() {
        System.out.println("\n=== 分表配置信息 ===");
        System.out.println("分表功能启用: " + ShardingConfigUtils.isShardingEnabled());
        System.out.println("自动创建分表: " + ShardingConfigUtils.isAutoCreateEnabled());
        System.out.println("配置的分表: " + ShardingConfigUtils.getShardingTables());
        
        for (String tableName : ShardingConfigUtils.getShardingTables()) {
            System.out.println("\n表: " + tableName);
            System.out.println("  分表字段: " + ShardingConfigUtils.getShardingField(tableName));
            System.out.println("  分表策略: " + ShardingConfigUtils.getShardingStrategy(tableName));
        }
        System.out.println();
    }
}
