package com.easyjava.test;

import com.easyjava.utils.CrossTableShardingUtils;
import com.easyjava.utils.ShardingUtils;
import com.easyjava.utils.ShardingConfigUtils;

/**
 * 跨表分表功能测试
 */
public class CrossTableShardingTest {
    
    public static void main(String[] args) {
        System.out.println("=== 跨表分表功能测试 ===\n");
        
        // 测试配置读取
        testConfigReading();
        
        // 测试跨表分表逻辑
        testCrossTableSharding();
        
        // 测试缓存功能
        testCaching();
    }
    
    /**
     * 测试配置读取
     */
    private static void testConfigReading() {
        System.out.println("1. 配置读取测试:");
        
        String tableName = "user_message";
        boolean isCrossTable = CrossTableShardingUtils.isCrossTableSharding(tableName);
        String crossConfig = CrossTableShardingUtils.getCrossTableConfig(tableName);
        
        System.out.println("表名: " + tableName);
        System.out.println("是否跨表分表: " + isCrossTable);
        System.out.println("跨表配置: " + crossConfig);
        
        if (isCrossTable) {
            String shardingField = ShardingConfigUtils.getShardingField(tableName);
            String shardingStrategy = ShardingConfigUtils.getShardingStrategy(tableName);
            System.out.println("分表字段: " + shardingField);
            System.out.println("分表策略: " + shardingStrategy);
        }
        
        System.out.println();
    }
    
    /**
     * 测试跨表分表逻辑（模拟）
     */
    private static void testCrossTableSharding() {
        System.out.println("2. 跨表分表逻辑测试 (模拟):");
        
        String baseTableName = "user_message";
        String crossConfig = "blade_user.id->user_id";
        
        // 模拟不同的用户ID
        Long[] userIds = {1001L, 1002L, 1003L, 1004L, 1005L};
        
        System.out.println("基础表名: " + baseTableName);
        System.out.println("跨表配置: " + crossConfig);
        System.out.println("分表策略: hash");
        System.out.println();
        
        for (Long userId : userIds) {
            // 这里模拟跨表查询的结果，实际情况下会从数据库查询
            Object shardingValue = simulateShardingValue(userId);
            String shardedTableName = ShardingUtils.getTableName(baseTableName, shardingValue, "hash");
            
            System.out.printf("用户ID: %d -> 分表值: %s -> 分表名: %s%n", 
                userId, shardingValue, shardedTableName);
        }
        
        System.out.println();
    }
    
    /**
     * 测试缓存功能
     */
    private static void testCaching() {
        System.out.println("3. 缓存功能测试:");
        
        System.out.println("清空缓存...");
        CrossTableShardingUtils.clearCache();
        
        System.out.println("模拟缓存操作...");
        // 这里只是演示缓存API的使用
        System.out.println("缓存清理完成");
        
        System.out.println();
    }
    
    /**
     * 模拟分表值获取（实际环境中会从数据库查询）
     */
    private static Object simulateShardingValue(Long userId) {
        // 这里模拟根据用户ID查询到的user_id值
        // 实际情况下，这个值会通过CrossTableShardingUtils.getCrossTableShardingValue()从数据库查询
        return userId;
    }
    
    /**
     * 演示跨表分表的具体场景
     */
    public static void demonstrateUseCases() {
        System.out.println("=== 跨表分表使用场景演示 ===");
        
        System.out.println("\n场景1: 用户消息表按用户ID分表");
        System.out.println("配置: user_message.sharding.cross.table=blade_user.id->user_id");
        System.out.println("说明: 根据blade_user表的id字段值来分user_message表");
        System.out.println("效果: 同一用户的所有消息都存储在同一个分表中");
        
        System.out.println("\n场景2: 订单详情表按订单的用户ID分表");
        System.out.println("配置: order_detail.sharding.cross.table=order.user_id->user_id");
        System.out.println("说明: 根据order表的user_id字段值来分order_detail表");
        System.out.println("效果: 同一用户的所有订单详情都存储在同一个分表中");
        
        System.out.println("\n场景3: 用户积分记录按用户ID分表");
        System.out.println("配置: user_points.sharding.cross.table=blade_user.id->user_id");
        System.out.println("说明: 根据blade_user表的id字段值来分user_points表");
        System.out.println("效果: 同一用户的积分记录都存储在同一个分表中");
    }
}
