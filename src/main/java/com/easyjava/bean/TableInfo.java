package com.easyjava.bean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TableInfo {
    // 表名称
    private String tableName;
    // bean名称
    private String beanName;
    // 参数名称
    private String beanParamName;
    // 表注释
    private String comment;
    // 字段信息
    private List<FieldInfo> fieldList;
    // 扩展字段信息
    private List<FieldInfo> fieldListExtend;
    // 唯一索引合集
    private Map<String, List<FieldInfo>> keyIndexMap = new LinkedHashMap<>();
    // 是否有date类型
    private Boolean haveDate;
    // 是否有时间类型
    private Boolean haveDateTime;

    // 是否有bigdecimal类型
    private Boolean haveBigDecimal;

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * @return the beanName
     */
    public String getBeanName() {
        return beanName;
    }

    /**
     * @param beanName the beanName to set
     */
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /**
     * @return the beanParamName
     */
    public String getBeanParamName() {
        return beanParamName;
    }

    /**
     * @param beanParamName the beanParamName to set
     */
    public void setBeanParamName(String beanParamName) {
        this.beanParamName = beanParamName;
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
     * @return the fieldList
     */
    public List<FieldInfo> getFieldList() {
        return fieldList;
    }

    /**
     * @param fieldList the fieldList to set
     */
    public void setFieldList(List<FieldInfo> fieldList) {
        this.fieldList = fieldList;
    }

    /**
     * @return the fieldListExtend
     */
    public List<FieldInfo> getFieldListExtend() {
        return fieldListExtend;
    }

    /**
     * @param fieldListExtend the fieldListExtend to set
     */
    public void setFieldListExtend(List<FieldInfo> fieldListExtend) {
        this.fieldListExtend = fieldListExtend;
    }

    /**
     * @return the keyIndexMap
     */
    public Map<String, List<FieldInfo>> getKeyIndexMap() {
        return keyIndexMap;
    }

    /**
     * @param keyIndexMap the keyIndexMap to set
     */
    public void setKeyIndexMap(Map<String, List<FieldInfo>> keyIndexMap) {
        this.keyIndexMap = keyIndexMap;
    }

    /**
     * @return the haveDate
     */
    public Boolean getHaveDate() {
        return haveDate;
    }

    /**
     * @param haveDate the haveDate to set
     */
    public void setHaveDate(Boolean haveDate) {
        this.haveDate = haveDate;
    }

    /**
     * @return the haveDateTime
     */
    public Boolean getHaveDateTime() {
        return haveDateTime;
    }

    /**
     * @param haveDateTime the haveDateTime to set
     */
    public void setHaveDateTime(Boolean haveDateTime) {
        this.haveDateTime = haveDateTime;
    }

    /**
     * @return the haveBigDecimal
     */
    public Boolean getHaveBigDecimal() {
        return haveBigDecimal;
    }

    /**
     * @param haveBigDecimal the haveBigDecimal to set
     */
    public void setHaveBigDecimal(Boolean haveBigDecimal) {
        this.haveBigDecimal = haveBigDecimal;
    }

    @Override
    public String toString() {
        return "TableInfo [tableName=" + tableName + ", beanName=" + beanName + ", beanParamName=" + beanParamName
                + ", comment=" + comment + ", fieldList=" + fieldList + ", fieldListExtend=" + fieldListExtend
                + ", keyIndexMap=" + keyIndexMap + ", haveDate=" + haveDate + ", haveDateTime=" + haveDateTime
                + ", haveBigDecimal=" + haveBigDecimal + "]";
    }

}
