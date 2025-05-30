package com.easyjava.bean;

public class FieldInfo {
    // 字段名
    private String FieldName;
    // bean属性名称
    private String propertyName;

    private String sqlType;
    // 字段类型
    private String javaType;

    // 字段备注
    private String comment;
    // 是否自增长
    private Boolean isAutoIncrement;

    /**
     * @return the fieldName
     */
    public String getFieldName() {
        return FieldName;
    }

    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName) {
        FieldName = fieldName;
    }

    /**
     * @return the propertyName
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * @param propertyName the propertyName to set
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * @return the sqlType
     */
    public String getSqlType() {
        return sqlType;
    }

    /**
     * @param sqlType the sqlType to set
     */
    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    /**
     * @return the javaType
     */
    public String getJavaType() {
        return javaType;
    }

    /**
     * @param javaType the javaType to set
     */
    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return the isAutoIncrement
     */
    public Boolean getIsAutoIncrement() {
        return isAutoIncrement;
    }

    /**
     * @param isAutoIncrement the isAutoIncrement to set
     */
    public void setIsAutoIncrement(Boolean isAutoIncrement) {
        this.isAutoIncrement = isAutoIncrement;
    }

    @Override
    public String toString() {
        return "FieldInfo [FieldName=" + FieldName + ", propertyName=" + propertyName + ", sqlType=" + sqlType
                + ", javaType=" + javaType + ", comment=" + comment + ", isAutoIncrement=" + isAutoIncrement + "]";
    }

    /**
     * @param fieldName
     * @param propertyName
     * @param sqlType
     * @param javaType
     * @param comment
     * @param isAutoIncrement
     */
    public FieldInfo(String fieldName, String propertyName, String sqlType, String javaType, String comment,
            Boolean isAutoIncrement) {
        FieldName = fieldName;
        this.propertyName = propertyName;
        this.sqlType = sqlType;
        this.javaType = javaType;
        this.comment = comment;
        this.isAutoIncrement = isAutoIncrement;
    }

    public FieldInfo() {
        
    }
    
}
