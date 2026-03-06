package com.easyjava.builder;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easyjava.bean.Constants;
import com.easyjava.bean.DatabaseType;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.ConfigUtils;
import com.easyjava.utils.SqlTypeMapper;
import com.easyjava.utils.StringUtils;

public class BuilderTable {

    private final static Logger log = LoggerFactory.getLogger(BuilderTable.class);

    public static List<TableInfo> GetTables() {
        List<TableInfo> tableInfos = new ArrayList<>();

        try (Connection conn = createConnection()) {
            DatabaseType databaseType = DatabaseType.fromConnection(conn);
            String schemaName = resolveSchema(conn, databaseType);

            for (TableMeta tableMeta : loadTableMetas(conn, databaseType, schemaName)) {
                TableInfo tableInfo = buildTableInfo(tableMeta.tableName, tableMeta.comment);
                tableInfo.setFieldList(ReadFieldInfo(conn, databaseType, schemaName, tableInfo));
                GetKeyIndexInfo(conn, databaseType, schemaName, tableInfo);
                tableInfos.add(tableInfo);
            }
        } catch (Exception e) {
            log.error("获取表异常", e);
        }

        return tableInfos;
    }

    private static List<FieldInfo> ReadFieldInfo(Connection conn, DatabaseType databaseType, String schemaName,
            TableInfo tableInfo) {
        List<FieldInfo> fieldInfos = new ArrayList<>();
        List<FieldInfo> fieldExtendList = new ArrayList<>();

        try (PreparedStatement ps = createFieldStatement(conn, databaseType, schemaName, tableInfo.getTableName());
                ResultSet fieldResult = ps.executeQuery()) {
            while (fieldResult.next()) {
                String field = fieldResult.getString("field");
                String type = fieldResult.getString("type");
                String extra = fieldResult.getString("extra");
                String comment = fieldResult.getString("comment");

                String propertyName = ProcessField(field, false);
                String baseType = SqlTypeMapper.getBaseType(type);

                FieldInfo fieldInfo = new FieldInfo();
                fieldInfo.setFieldName(field);
                fieldInfo.setPropertyName(propertyName);
                fieldInfo.setSqlType(baseType);
                fieldInfo.setComment(comment);
                fieldInfo.setIsAutoIncrement("auto_increment".equalsIgnoreCase(extra));
                fieldInfo.setJavaType(ProcessJavaType(baseType));

                if (SqlTypeMapper.isDateTimeType(baseType)) {
                    if (SqlTypeMapper.isDateTimeWithTimeType(baseType)) {
                        tableInfo.setHaveDateTime(true);
                    }
                    if (SqlTypeMapper.isDateOnlyType(baseType)) {
                        tableInfo.setHaveDate(true);
                    }
                }

                if ("BigDecimal".equals(SqlTypeMapper.getJavaType(baseType))) {
                    tableInfo.setHaveBigDecimal(true);
                }

                if (SqlTypeMapper.isStringType(baseType)) {
                    fieldExtendList.add(new FieldInfo(field, propertyName + Constants.SUFFIX_BEAN_PARAM_FUZZY,
                            baseType, ProcessJavaType(baseType), comment,
                            "auto_increment".equalsIgnoreCase(extra)));
                } else if (SqlTypeMapper.isDateTimeType(baseType)) {
                    fieldExtendList.add(new FieldInfo(field, propertyName + Constants.SUFFIX_BEAN_PARAM_TIME_START,
                            baseType, "String", comment,
                            "auto_increment".equalsIgnoreCase(extra)));

                    fieldExtendList.add(new FieldInfo(field, propertyName + Constants.SUFFIX_BEAN_PARAM_TIME_END,
                            baseType, "String", comment,
                            "auto_increment".equalsIgnoreCase(extra)));
                }

                fieldInfos.add(fieldInfo);
            }

            tableInfo.setFieldListExtend(fieldExtendList);
        } catch (Exception e) {
            log.error("获取表字段异常", e);
        }

        return fieldInfos;
    }

    private static void GetKeyIndexInfo(Connection conn, DatabaseType databaseType, String schemaName,
            TableInfo tableInfo) {
        tableInfo.getKeyIndexMap().clear();

        try (PreparedStatement ps = createIndexStatement(conn, databaseType, schemaName, tableInfo.getTableName());
                ResultSet fieldResult = ps.executeQuery()) {
            Map<String, FieldInfo> tempMap = new HashMap<>();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                tempMap.put(fieldInfo.getFieldName(), fieldInfo);
            }

            Map<String, List<FieldInfoWithOrder>> indexFieldOrderMap = new LinkedHashMap<>();

            while (fieldResult.next()) {
                String keyName = fieldResult.getString("key_name");
                String columnName = fieldResult.getString("column_name");
                int seqInIndex = fieldResult.getInt("seq_in_index");

                FieldInfo field = tempMap.get(columnName);
                if (field == null) {
                    continue;
                }

                if (SqlTypeMapper.isDateTimeType(field.getSqlType())) {
                    continue;
                }

                List<FieldInfoWithOrder> fieldList = indexFieldOrderMap.computeIfAbsent(keyName,
                        k -> new ArrayList<>());
                fieldList.add(new FieldInfoWithOrder(field, seqInIndex));
            }

            for (Map.Entry<String, List<FieldInfoWithOrder>> entry : indexFieldOrderMap.entrySet()) {
                List<FieldInfoWithOrder> list = entry.getValue();
                list.sort((a, b) -> Integer.compare(a.seq, b.seq));
                List<FieldInfo> sortedFields = new ArrayList<>();
                for (FieldInfoWithOrder fieldInfoWithOrder : list) {
                    sortedFields.add(fieldInfoWithOrder.fieldInfo);
                }
                tableInfo.getKeyIndexMap().put(entry.getKey(), sortedFields);
            }

        } catch (Exception e) {
            log.error("获取索引异常", e);
        }
    }

    private static Connection createConnection() throws Exception {
        String driverName = ConfigUtils.Database.getDriverClassName();
        String url = ConfigUtils.Database.getUrl();
        String user = ConfigUtils.Database.getUsername();
        String password = ConfigUtils.Database.getPassword();
        Class.forName(driverName);
        return DriverManager.getConnection(url, user, password);
    }

    private static TableInfo buildTableInfo(String tableName, String comment) {
        TableInfo tableInfo = new TableInfo();

        String beanSourceName = tableName;
        if (Constants.IGNORE_TABLE_PREFIX && beanSourceName.contains("_")) {
            beanSourceName = beanSourceName.substring(beanSourceName.indexOf("_") + 1);
        }
        String beanName = ProcessField(beanSourceName, true);

        tableInfo.setTableName(tableName);
        tableInfo.setComment(comment);
        tableInfo.setBeanName(beanName);
        tableInfo.setBeanParamName(beanName + ProcessField(Constants.SUFFIX_BEAN_PARAM, true));
        tableInfo.setHaveDate(false);
        tableInfo.setHaveDateTime(false);
        tableInfo.setHaveBigDecimal(false);
        return tableInfo;
    }

    private static List<TableMeta> loadTableMetas(Connection conn, DatabaseType databaseType, String schemaName)
            throws Exception {
        List<TableMeta> tables = new ArrayList<>();
        String sql;
        if (databaseType.isPostgreSql()) {
            sql = "select c.relname as name, coalesce(obj_description(c.oid, 'pg_class'), '') as comment "
                    + "from pg_class c "
                    + "join pg_namespace n on n.oid = c.relnamespace "
                    + "where c.relkind = 'r' and n.nspname = ? order by c.relname";
        } else {
            sql = "show table status";
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (databaseType.isPostgreSql()) {
                ps.setString(1, schemaName);
            }
            try (ResultSet tableResult = ps.executeQuery()) {
                while (tableResult.next()) {
                    tables.add(new TableMeta(tableResult.getString("name"), tableResult.getString("comment")));
                }
            }
        }

        return tables;
    }

    private static PreparedStatement createFieldStatement(Connection conn, DatabaseType databaseType, String schemaName,
            String tableName) throws Exception {
        if (databaseType.isPostgreSql()) {
            String sql = "select a.attname as field, format_type(a.atttypid, a.atttypmod) as type, "
                    + "case when a.attidentity in ('a','d') then 'auto_increment' "
                    + "when pg_get_expr(ad.adbin, ad.adrelid) like 'nextval(%' then 'auto_increment' else '' end as extra, "
                    + "coalesce(col_description(a.attrelid, a.attnum), '') as comment "
                    + "from pg_attribute a "
                    + "join pg_class c on a.attrelid = c.oid "
                    + "join pg_namespace n on c.relnamespace = n.oid "
                    + "left join pg_attrdef ad on a.attrelid = ad.adrelid and a.attnum = ad.adnum "
                    + "where c.relname = ? and n.nspname = ? and a.attnum > 0 and not a.attisdropped "
                    + "order by a.attnum";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tableName);
            ps.setString(2, schemaName);
            return ps;
        }

        return conn.prepareStatement(String.format("show full fields from %s", tableName));
    }

    private static PreparedStatement createIndexStatement(Connection conn, DatabaseType databaseType, String schemaName,
            String tableName) throws Exception {
        if (databaseType.isPostgreSql()) {
            String sql = "select case when i.indisprimary then 'PRIMARY' else ci.relname end as key_name, "
                    + "a.attname as column_name, (ord.ordinality)::int as seq_in_index "
                    + "from pg_index i "
                    + "join pg_class ct on ct.oid = i.indrelid "
                    + "join pg_namespace n on n.oid = ct.relnamespace "
                    + "join pg_class ci on ci.oid = i.indexrelid "
                    + "join unnest(i.indkey) with ordinality as ord(attnum, ordinality) on true "
                    + "join pg_attribute a on a.attrelid = ct.oid and a.attnum = ord.attnum "
                    + "where ct.relname = ? and n.nspname = ? and (i.indisprimary or i.indisunique) "
                    + "order by key_name, seq_in_index";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tableName);
            ps.setString(2, schemaName);
            return ps;
        }

        return conn.prepareStatement(String.format("show index from %s where Non_unique = 0", tableName));
    }

    private static String resolveSchema(Connection conn, DatabaseType databaseType) {
        try {
            if (databaseType.isPostgreSql()) {
                try (PreparedStatement ps = conn.prepareStatement("select current_schema()");
                        ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                }
                return "public";
            }

            String schema = conn.getCatalog();
            if (schema != null && !schema.isEmpty()) {
                return schema;
            }

            DatabaseMetaData metaData = conn.getMetaData();
            return metaData.getUserName();
        } catch (Exception e) {
            log.warn("解析schema失败，使用默认schema", e);
            return databaseType.isPostgreSql() ? "public" : null;
        }
    }

    private static class TableMeta {
        private final String tableName;
        private final String comment;

        private TableMeta(String tableName, String comment) {
            this.tableName = tableName;
            this.comment = comment;
        }
    }

    private static class FieldInfoWithOrder {
        FieldInfo fieldInfo;
        int seq;

        FieldInfoWithOrder(FieldInfo fieldInfo, int seq) {
            this.fieldInfo = fieldInfo;
            this.seq = seq;
        }
    }

    private static String ProcessField(String field, Boolean upCaseFirstLetter) {
        StringBuffer sb = new StringBuffer();
        String[] fields = field.split("_");
        sb.append(upCaseFirstLetter ? StringUtils.uperCaseFirstLetter(fields[0]) : fields[0]);

        for (int i = 1, len = fields.length; i < len; i++) {
            sb.append(StringUtils.uperCaseFirstLetter(fields[i]));
        }

        return sb.toString();
    }

    private static String ProcessJavaType(String type) {
        try {
            return SqlTypeMapper.getJavaType(type);
        } catch (Exception e) {
            log.error("类型转换失败，SQL类型: {}, 错误: {}", type, e.getMessage());

            if (SqlTypeMapper.isNumericType(type)) {
                return SqlTypeMapper.getJavaType(type);
            } else if (SqlTypeMapper.isDateTimeType(type)) {
                return "Date";
            } else if (SqlTypeMapper.isStringType(type)) {
                return "String";
            } else {
                log.warn("未识别的SQL类型: {}，使用String作为默认类型", type);
                return "String";
            }
        }
    }
}
