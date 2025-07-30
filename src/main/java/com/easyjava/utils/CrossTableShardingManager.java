package com.easyjava.utils;

import java.util.Scanner;

/**
 * 跨表分表管理工具
 */
public class CrossTableShardingManager {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== 跨表分表管理工具 ===");
        System.out.println("1. 测试跨表分表配置");
        System.out.println("2. 查看分表结果");
        System.out.println("3. 清理缓存");
        System.out.println("4. 查看跨表配置");
        System.out.println("0. 退出");
        System.out.print("请选择操作: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); // 消费换行符
        
        switch (choice) {
            case 1:
                testCrossTableSharding(scanner);
                break;
            case 2:
                viewShardingResult(scanner);
                break;
            case 3:
                cleanCache();
                break;
            case 4:
                viewCrossTableConfig(scanner);
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
     * 测试跨表分表配置
     */
    private static void testCrossTableSharding(Scanner scanner) {
        System.out.print("请输入表名: ");
        String tableName = scanner.nextLine();
        
        System.out.print("请输入参考值: ");
        String referenceValue = scanner.nextLine();
        
        try {
            boolean isCrossTable = CrossTableShardingUtils.isCrossTableSharding(tableName);
            System.out.println("是否跨表分表: " + isCrossTable);
            
            if (isCrossTable) {
                String crossConfig = CrossTableShardingUtils.getCrossTableConfig(tableName);
                System.out.println("跨表配置: " + crossConfig);
                
                String strategy = ShardingConfigUtils.getShardingStrategy(tableName);
                System.out.println("分表策略: " + strategy);
                
                // 注意：这里会实际查询数据库
                Object shardingValue = CrossTableShardingUtils.getCrossTableShardingValue(
                    tableName, referenceValue, crossConfig);
                System.out.println("分表值: " + shardingValue);
                
                String shardedTableName = ShardingUtils.getTableName(tableName, shardingValue, strategy);
                System.out.println("分表后表名: " + shardedTableName);
            } else {
                System.out.println("该表未配置跨表分表");
            }
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 查看分表结果
     */
    private static void viewShardingResult(Scanner scanner) {
        System.out.print("请输入基础表名: ");
        String baseTableName = scanner.nextLine();
        
        System.out.print("请输入分表策略 (hash/range/time): ");
        String strategy = scanner.nextLine();
        
        System.out.print("请输入起始值: ");
        int startValue = scanner.nextInt();
        
        System.out.print("请输入结束值: ");
        int endValue = scanner.nextInt();
        
        System.out.println("\n分表结果:");
        System.out.println("-".repeat(50));
        
        for (int i = startValue; i <= endValue; i++) {
            String shardedTableName = ShardingUtils.getTableName(baseTableName, i, strategy);
            System.out.printf("值: %d -> 表名: %s%n", i, shardedTableName);
        }
    }
    
    /**
     * 清理缓存
     */
    private static void cleanCache() {
        try {
            CrossTableShardingUtils.cleanExpiredCache();
            System.out.println("过期缓存清理完成");
            
            CrossTableShardingUtils.clearCache();
            System.out.println("所有缓存已清空");
        } catch (Exception e) {
            System.err.println("清理缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 查看跨表配置
     */
    private static void viewCrossTableConfig(Scanner scanner) {
        System.out.print("请输入表名 (留空显示所有): ");
        String tableName = scanner.nextLine();
        
        if (tableName.isEmpty()) {
            // 显示所有跨表配置
            System.out.println("所有跨表分表配置:");
            System.out.println("-".repeat(60));
            
            // 这里需要遍历所有配置的表
            var shardingTables = ShardingConfigUtils.getShardingTables();
            for (String table : shardingTables) {
                String crossConfig = CrossTableShardingUtils.getCrossTableConfig(table);
                if (crossConfig != null) {
                    String shardingField = ShardingConfigUtils.getShardingField(table);
                    String strategy = ShardingConfigUtils.getShardingStrategy(table);
                    
                    System.out.println("表名: " + table);
                    System.out.println("  分表字段: " + shardingField);
                    System.out.println("  分表策略: " + strategy);
                    System.out.println("  跨表配置: " + crossConfig);
                    System.out.println();
                }
            }
        } else {
            // 显示指定表的配置
            boolean isCrossTable = CrossTableShardingUtils.isCrossTableSharding(tableName);
            System.out.println("表名: " + tableName);
            System.out.println("是否跨表分表: " + isCrossTable);
            
            if (isCrossTable) {
                String crossConfig = CrossTableShardingUtils.getCrossTableConfig(tableName);
                String shardingField = ShardingConfigUtils.getShardingField(tableName);
                String strategy = ShardingConfigUtils.getShardingStrategy(tableName);
                
                System.out.println("分表字段: " + shardingField);
                System.out.println("分表策略: " + strategy);
                System.out.println("跨表配置: " + crossConfig);
                
                // 解析跨表配置
                if (crossConfig != null) {
                    String[] parts = crossConfig.split("->");
                    if (parts.length == 2) {
                        String[] refParts = parts[0].split("\\.");
                        if (refParts.length == 2) {
                            System.out.println("参考表: " + refParts[0]);
                            System.out.println("参考字段: " + refParts[1]);
                            System.out.println("目标字段: " + parts[1]);
                        }
                    }
                }
            }
        }
    }
}
