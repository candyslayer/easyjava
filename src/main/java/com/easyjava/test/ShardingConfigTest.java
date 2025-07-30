package com.easyjava.test;

import com.easyjava.utils.ShardingConfigUtils;
import java.util.Set;

/**
 * 分表配置测试
 */
public class ShardingConfigTest {
    
    public static void main(String[] args) {
        System.out.println("=== 分表配置测试 ===");
        
        System.out.println("分表功能启用: " + ShardingConfigUtils.isShardingEnabled());
        System.out.println("自动创建分表: " + ShardingConfigUtils.isAutoCreateEnabled());
        System.out.println("自动检测分表: " + ShardingConfigUtils.isAutoDetectEnabled());
        
        Set<String> shardingTables = ShardingConfigUtils.getShardingTables();
        System.out.println("配置的分表列表: " + shardingTables);
        
        // 测试具体表的配置
        for (String tableName : shardingTables) {
            System.out.println("\n表: " + tableName);
            System.out.println("  是否为分表: " + ShardingConfigUtils.isShardingTable(tableName));
            System.out.println("  分表字段: " + ShardingConfigUtils.getShardingField(tableName));
            System.out.println("  分表策略: " + ShardingConfigUtils.getShardingStrategy(tableName));
        }
        
        // 测试非配置表
        String testTable = "blade_user";
        System.out.println("\n测试表: " + testTable);
        System.out.println("  是否为分表: " + ShardingConfigUtils.isShardingTable(testTable));
    }
}
