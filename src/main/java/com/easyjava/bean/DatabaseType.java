package com.easyjava.bean;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import com.easyjava.utils.ConfigUtils;

public enum DatabaseType {
    MYSQL,
    POSTGRESQL,
    UNKNOWN;

    public static DatabaseType fromConfig() {
        String driver = ConfigUtils.Database.getDriverClassName();
        String url = ConfigUtils.Database.getUrl();
        return resolve(driver, url);
    }

    public static DatabaseType fromConnection(Connection connection) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            return resolve(metaData.getDriverName(), metaData.getURL(), metaData.getDatabaseProductName());
        } catch (Exception e) {
            return fromConfig();
        }
    }

    public static DatabaseType resolve(String... values) {
        if (values == null) {
            return UNKNOWN;
        }
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String lowerValue = value.toLowerCase();
            if (lowerValue.contains("postgresql") || lowerValue.contains("postgres")) {
                return POSTGRESQL;
            }
            if (lowerValue.contains("mysql")) {
                return MYSQL;
            }
        }
        return UNKNOWN;
    }

    public boolean isMySql() {
        return this == MYSQL;
    }

    public boolean isPostgreSql() {
        return this == POSTGRESQL;
    }
}
